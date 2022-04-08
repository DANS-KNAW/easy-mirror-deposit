/*
 * Copyright (C) 2022 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.knaw.dans.easy.mirror.core;

import gov.loc.repository.bagit.creator.BagCreator;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.UUID;

import static org.joda.time.DateTimeZone.UTC;

public class MirrorTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MirrorTask.class);

    private final TransferItemMetadataReader transferItemMetadataReader;
    private final Path datasetVersionExportZip;
    private final Path workDirectory;
    private final Path depositOutbox;
    private final Path failedBox;
    private final Path mirrorStore;

    private FilenameAttributes filenameAttributes;
    private FileContentAttributes fileContentAttributes;
    private FilesystemAttributes filesystemAttributes;

    public MirrorTask(TransferItemMetadataReader transferItemMetadataReader, Path datasetVersionExportZip, Path workDirectory, Path depositOutbox, Path failedBox,
        Path mirrorStore) {
        this.transferItemMetadataReader = transferItemMetadataReader;
        this.datasetVersionExportZip = datasetVersionExportZip;
        this.workDirectory = workDirectory;
        this.depositOutbox = depositOutbox;
        this.failedBox = failedBox;
        this.mirrorStore = mirrorStore;
    }

    @Override
    public void run() {
        log.info("Processing {}", datasetVersionExportZip.getFileName());

        try {
            filenameAttributes = transferItemMetadataReader.getFilenameAttributes(datasetVersionExportZip);
            fileContentAttributes = transferItemMetadataReader.getFileContentAttributes(datasetVersionExportZip);
            filesystemAttributes = transferItemMetadataReader.getFilesystemAttributes(datasetVersionExportZip);

            if (filenameAttributes.getVersionMajor() == 1 && filenameAttributes.getVersionMinor() == 0) {
                createMetadataOnlyDeposit();
            }

            try {
                Files.move(datasetVersionExportZip, mirrorStore.resolve(datasetVersionExportZip.getFileName()));
            }
            catch (IOException e) {
                throw new IllegalStateException("Could not move DVE to EASY mirror store", e);
            }
            log.info("SUCCESS. Done processing {}", datasetVersionExportZip.getFileName());
        }
        catch (InvalidTransferItemException e) {
            try {
                log.error("FAIL. Could not process DVE {}, moving to failedBox.", datasetVersionExportZip.getFileName(), e);
                Files.move(datasetVersionExportZip, failedBox.resolve(datasetVersionExportZip.getFileName()));
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Cannot move invalid DVE to failedBox", ioe);
            }

        }
    }

    private void createMetadataOnlyDeposit() {
        String uuid = UUID.randomUUID().toString();
        try {
            Path deposit = Files.createDirectory(workDirectory.resolve(uuid));
            PropertiesConfiguration props = createDepositProperties(uuid);
            props.save(deposit.resolve("deposit.properties").toFile());
            createMetadataOnlyBag(deposit);
            Files.move(deposit, depositOutbox.resolve(uuid));
        }
        catch (IOException | ConfigurationException e) {
            throw new IllegalStateException(String.format("Could not create working directory for deposit %s", uuid), e);
        }
    }

    private PropertiesConfiguration createDepositProperties(String uuid) {
        PropertiesConfiguration props = new PropertiesConfiguration();
        props.setProperty("creation.timestamp", DateTime.now(UTC));
        props.setProperty("state.label", "SUBMITTED");
        props.setProperty("state.description", "Deposit is submitted and ready for processing");
        props.setProperty("depositor.userId", "easymirror");
        props.setProperty("curation.required", "no");
        props.setProperty("curation.performed", "no");
        props.setProperty("identifier.dans-doi.registered", "no"); // TODO: correct?
        props.setProperty("identifier.dans-doi.action", "create"); // TODO: probably no action. We want the DOI to be copied but not sent to DataCite by easy-ingest-flow
        props.setProperty("bag-store.bag-name", "bag");
        props.setProperty("deposit.origin", "API"); // TODO: new type of origin?
        props.setProperty("identifier.doi", filenameAttributes.getDatasetPid()); // TODO: remove "doi:" ?
        props.setProperty("bag-store.bag-id", uuid);
        props.setProperty("identifier.urn", fileContentAttributes.getNbn());
        return props;
    }

    private void createMetadataOnlyBag(Path depositFolder) throws IOException {
        Path bagFolder = depositFolder.resolve("bag");
        Files.createDirectory(bagFolder);
        try {
            // Create an empty bag first
            BagCreator.bagInPlace(bagFolder, Collections.singletonList(StandardSupportedAlgorithms.SHA1), false);

            // Add no files and minimal metadata
            Path metadataDir = Files.createDirectory(bagFolder.resolve("metadata"));
            createDatasetXml(metadataDir);
            createEmpyFilesXml(metadataDir);

            // Update the tagmanifest

        }
        catch (NoSuchAlgorithmException e) {
            // This should not be possible
            throw new IllegalStateException("Could not create manifest with algorithm that should be standard supported", e);
        }
    }

    private void createDatasetXml(Path metadataDir) throws IOException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("metadata", createDatasetMetadata());
            Template template = Velocity.getTemplate("dataset.xml.tmpl", "UTF-8");
            StringWriter content = new StringWriter();
            template.merge(context, content);
            FileUtils.writeStringToFile(metadataDir.resolve("dataset.xml").toFile(), content.toString(), StandardCharsets.UTF_8);
        }
        catch (ResourceNotFoundException e) {
            throw new IllegalStateException("Template for dataset.xml could not be loaded", e);
        }
        catch (ParseErrorException e) {
            throw new IllegalStateException("Template for dataset.xml contains syntax errors", e);
        }

    }

    private DatasetMetadata createDatasetMetadata() {
        DatasetMetadata md = new DatasetMetadata();
        md.setTitle("Title");
        md.setDescription("Description");
        md.setAudience("D37000");
        md.setCreator("Creator");
        md.setAccessRights("NO_ACCESS");
        return md;
    }

    private void createEmpyFilesXml(Path metadataDir) throws IOException {
        FileUtils.writeStringToFile(metadataDir.resolve("files.xml").toFile(), "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<files />", StandardCharsets.UTF_8);
    }
}
