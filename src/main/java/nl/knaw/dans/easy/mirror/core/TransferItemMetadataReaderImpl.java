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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.ZipFile;

public class TransferItemMetadataReaderImpl implements TransferItemMetadataReader {
    private final ObjectMapper objectMapper;
    private final FileService fileService;

    public TransferItemMetadataReaderImpl(ObjectMapper objectMapper, FileService fileService) {
        this.objectMapper = objectMapper;
        this.fileService = fileService;
    }

    @Override
    public FilenameAttributes getFilenameAttributes(Path path) throws InvalidTransferItemException {
        Path filename = path.getFileName();

        FilenameAttributes result = new FilenameAttributes();
        result.setDveFilePath(path.toString());

        try {
            ExportedDatasetVersionName dveName = new ExportedDatasetVersionName(filename.toString());
            String datasetPid = dveName.getSpaceName().substring(4).toUpperCase().replaceFirst("-", ".").replaceAll("-", "/");
            result.setDatasetPid(datasetPid);
            result.setVersionMajor(dveName.getMajorVersion());
            result.setVersionMinor(dveName.getMinorVersion());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidTransferItemException(String.format("filename %s does not match expected pattern", filename));
        }

        return result;
    }

    @Override
    public FilesystemAttributes getFilesystemAttributes(Path path) throws InvalidTransferItemException {
        FilesystemAttributes result = new FilesystemAttributes();

        try {
            Object creationTime = fileService.getFilesystemAttribute(path, "creationTime");

            if (creationTime != null) {
                result.setCreationTime(LocalDateTime.ofInstant(((FileTime) creationTime).toInstant(), ZoneId.systemDefault()));
                result.setBagSize(fileService.getFileSize(path));
            }
        }
        catch (IOException e) {
            throw new InvalidTransferItemException(String.format("unable to read filesystem attributes for file %s", path.toString()), e);
        }

        return result;
    }

    @Override
    public FileContentAttributes getFileContentAttributes(Path path) throws InvalidTransferItemException {
        FileContentAttributes result = new FileContentAttributes();

        try {
            ZipFile datasetVersionExport = fileService.openZipFile(path);

            InputStream metadataContent = fileService.openFileFromZip(datasetVersionExport, Paths.get("metadata/oai-ore.jsonld"));
            InputStream pidMappingContent = fileService.openFileFromZip(datasetVersionExport, Paths.get("metadata/pid-mapping.txt"));

            byte[] oaiOre = IOUtils.toByteArray(metadataContent);
            byte[] pidMapping = IOUtils.toByteArray(pidMappingContent);

            JsonNode jsonNode = Objects.requireNonNull(objectMapper.readTree(oaiOre), "jsonld metadata can't be null: " + path);
            JsonNode describesNode = Objects.requireNonNull(jsonNode.get("ore:describes"), "ore:describes node can't be null");

            String nbn = getStringFromNode(describesNode, "dansDataVaultMetadata:dansNbn");
            String dvPidVersion = getStringFromNode(describesNode, "dansDataVaultMetadata:dansDataversePidVersion");
            String bagId = getStringFromNode(describesNode, "dansDataVaultMetadata:dansBagId");
            String otherId = getOptionalStringFromNode(describesNode, "dansDataVaultMetadata:dansOtherId");
            String otherIdVersion = getOptionalStringFromNode(describesNode, "dansDataVaultMetadata:dansOtherIdVersion");
            String swordClient = getOptionalStringFromNode(describesNode, "dansDataVaultMetadata:dansSwordClient");
            String swordToken = getOptionalStringFromNode(describesNode, "dansDataVaultMetadata:dansSwordToken");

            result.setPidMapping(pidMapping);
            result.setOaiOre(oaiOre);
            result.setNbn(nbn);
            result.setDatasetVersion(dvPidVersion);
            result.setBagId(bagId);
            result.setOtherId(otherId);
            result.setOtherIdVersion(otherIdVersion);
            result.setSwordToken(swordToken);
            result.setSwordClient(swordClient);
        }
        catch (IOException e) {
            throw new InvalidTransferItemException(String.format("unable to read zip file contents for file '%s'", path), e);
        }
        catch (NullPointerException e) {
            throw new InvalidTransferItemException(String.format("unable to extract metadata from file '%s'", path), e);
        }

        return result;
    }

    private String getStringFromNode(JsonNode node, String path) {
        return Objects.requireNonNull(node.get(path), String.format("path '%s' not found in JSON node", path)).asText();
    }

    private String getOptionalStringFromNode(JsonNode node, String path) {
        return Optional.ofNullable(node.get(path))
            .map(JsonNode::asText)
            .orElse(null);
    }

    @Override
    public Optional<Path> getAssociatedXmlFile(Path path) {
        try {
            ExportedDatasetVersionName dveName = new ExportedDatasetVersionName(path.getFileName().toString());
            String xml = dveName.getSpaceName() + "-datacite.v" + dveName.getMajorVersion() + "." + dveName.getMinorVersion() + ".xml";
            return Optional.of(path.getParent().resolve(Paths.get(xml)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
