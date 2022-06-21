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
import gov.loc.repository.bagit.domain.Bag;
import gov.loc.repository.bagit.hash.StandardSupportedAlgorithms;
import gov.loc.repository.bagit.writer.BagWriter;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.joda.time.DateTimeZone.UTC;

public class MirrorTask implements Runnable {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final Logger log = LoggerFactory.getLogger(MirrorTask.class);

    private final TransferItemMetadataReader transferItemMetadataReader;
    private final Path datasetVersionExportZip;
    private final Date ignoreMigratedDatasetUpdatesPublishedBefore;
    private final Path workDirectory;
    private final Path depositOutbox;
    private final Path failedBox;
    private final MirrorStore mirrorStore;

    private FilenameAttributes filenameAttributes;
    private FileContentAttributes fileContentAttributes;
    private FilesystemAttributes filesystemAttributes;

    public MirrorTask(TransferItemMetadataReader transferItemMetadataReader, Path datasetVersionExportZip, Date ignoreMigratedDatasetUpdatesPublishedBefore, Path workDirectory,
        Path depositOutbox, Path failedBox,
        MirrorStore mirrorStore) {
        this.transferItemMetadataReader = transferItemMetadataReader;
        this.datasetVersionExportZip = datasetVersionExportZip;
        this.ignoreMigratedDatasetUpdatesPublishedBefore = ignoreMigratedDatasetUpdatesPublishedBefore;
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

            if (isMigratedDataset(filenameAttributes.getDatasetPid())) {
                log.info("Dataset was migrated from EASY");
                if (filenameAttributes.getVersionMajor() == 1 && filenameAttributes.getVersionMinor() == 0) {
                    log.warn("Migrated dataset v1.0. Must be a migration back-log item. Not processing (deleting DVE)");
                    Files.delete(datasetVersionExportZip);
                    return;
                }
                else {
                    DatasetMetadata md = createDatasetMetadata();

                    if (getTimestampFromString(md.getModified()).before(ignoreMigratedDatasetUpdatesPublishedBefore)) {
                        log.warn("Migrated dataset > v1.0 but published on {}, so before the cut-off date of {} and probably a migration back-log item. Not processing (deleting DVE)",
                            md.getModified(),
                            ignoreMigratedDatasetUpdatesPublishedBefore);
                        Files.delete(datasetVersionExportZip);
                        return;
                    }
                }
            }

            if (filenameAttributes.getVersionMajor() == 1 && filenameAttributes.getVersionMinor() == 0) {
                createMetadataOnlyDeposit();
            }
            else {
                log.info("DVE version > 1.0; SKIPPING landing page deposit creation; " + datasetVersionExportZip.getFileName());
            }

            if (mirrorStore.contains(datasetVersionExportZip)) {
                log.warn("DVE already stored: {}. Deleting DVE", datasetVersionExportZip.getFileName());
                Files.delete(datasetVersionExportZip);
            }
            else
                try {
                    mirrorStore.store(datasetVersionExportZip);
                }
                catch (IOException e) {
                    throw new IllegalStateException("Could not move DVE to EASY mirror store", e);
                }
            log.info("SUCCESS. Done processing {}", datasetVersionExportZip.getFileName());
        }
        catch (Exception e) {
            // Java 8 still uses printStackTrace to output exceptions, so better to log all fatal exceptions  ourselves.
            // Not including Errors here, because Errors include things like OOM, which will crash the whole service anyway.
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
        log.debug("Minted deposit ID: {}", uuid);
        try {
            Path deposit = Files.createDirectory(workDirectory.resolve(uuid));
            log.debug("Created working directory at {}", deposit);
            PropertiesConfiguration props = createDepositProperties(uuid);
            props.save(deposit.resolve("deposit.properties").toFile());
            createMetadataOnlyBag(deposit);
            log.debug("Created metadata-only bag");
            Files.move(deposit, depositOutbox.resolve(uuid));
            log.debug("Moved deposit to {}", depositOutbox.resolve(uuid));
        }
        catch (IOException | ConfigurationException e) {
            throw new IllegalStateException(String.format("Could not create working directory for deposit %s", uuid), e);
        }
    }

    private Date getTimestampFromString(String s) {
        try {
            return dateFormat.parse(s);
        }
        catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private boolean isMigratedDataset(String doi) {
        log.trace("isMigratedDataset({})", doi);
        return doi.startsWith("10.17026/DANS");
    }

    private PropertiesConfiguration createDepositProperties(String uuid) {
        PropertiesConfiguration props = new PropertiesConfiguration();
        props.setProperty("creation.timestamp", DateTime.now(UTC));
        props.setProperty("state.label", "SUBMITTED");
        props.setProperty("state.description", "Deposit is submitted and ready for processing");
        props.setProperty("depositor.userId", "easymirror");
        props.setProperty("curation.required", "no");
        props.setProperty("curation.performed", "no");
        props.setProperty("identifier.dans-doi.registered", "no");
        // easy-ingest-flow accepts only update or create. The MirrorCopy workflow however mixes in FlowStepNoDataciteAction will set it to 'none' and not
        // try to update DataCit
        props.setProperty("identifier.dans-doi.action", "create");
        props.setProperty("bag-store.bag-name", "bag");
        props.setProperty("deposit.origin", "DataStation");
        props.setProperty("identifier.doi", filenameAttributes.getDatasetPid());
        props.setProperty("bag-store.bag-id", uuid);
        props.setProperty("identifier.urn", fileContentAttributes.getNbn());
        return props;
    }

    private void createMetadataOnlyBag(Path depositFolder) throws IOException {
        Path bagFolder = depositFolder.resolve("bag");
        Files.createDirectory(bagFolder);
        try {
            DatasetMetadata md = createDatasetMetadata();

            // Create an empty bag first
            Bag bag = BagCreator.bagInPlace(bagFolder, Collections.singletonList(StandardSupportedAlgorithms.SHA1), false);
            bag.getMetadata()
                .add("Created", filesystemAttributes.getCreationTime().atOffset(ZoneOffset.of("+1")).format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX")));

            // Add no files and minimal metadata
            Path metadataDir = Files.createDirectory(bagFolder.resolve("metadata"));
            createDatasetXml(metadataDir, md);
            createEmpyFilesXml(metadataDir);

            // Save the bag
            BagWriter.write(bag, bagFolder);
        }
        catch (NoSuchAlgorithmException e) {
            // This should not be possible
            throw new IllegalStateException("Could not create manifest with algorithm that should be standard supported", e);
        }
    }

    private void createDatasetXml(Path metadataDir, DatasetMetadata md) throws IOException {
        try {
            VelocityContext context = new VelocityContext();
            context.put("metadata", md);
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

    private DatasetMetadata createDatasetMetadata() throws IOException {
        DatasetMetadataReader reader = new DatasetMetadataReader(datasetVersionExportZip);
        DatasetMetadata md = reader.extractDatasetMetadata();
        md.setDoi(filenameAttributes.getDatasetPid());
        return md;
    }

    private void createEmpyFilesXml(Path metadataDir) throws IOException {
        FileUtils.writeStringToFile(metadataDir.resolve("files.xml").toFile(), "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n<files />", StandardCharsets.UTF_8);
    }
}
