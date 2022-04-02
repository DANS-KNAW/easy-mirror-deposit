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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class MirrorTask implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(MirrorTask.class);

    private final TransferItemMetadataReader transferItemMetadataReader;
    private final Path datasetVersionExportZip;
    private final Path depositOutbox;
    private final Path failedBox;
    private final Path mirrorStore;

    public MirrorTask(TransferItemMetadataReader transferItemMetadataReader, Path datasetVersionExportZip, Path depositOutbox, Path failedBox, Path mirrorStore) {
        this.transferItemMetadataReader = transferItemMetadataReader;
        this.datasetVersionExportZip = datasetVersionExportZip;
        this.depositOutbox = depositOutbox;
        this.failedBox = failedBox;
        this.mirrorStore = mirrorStore;
    }

    @Override
    public void run() {
        log.info("Processing " + datasetVersionExportZip.getFileName());

        try {
            FilenameAttributes filenameAttributes = transferItemMetadataReader.getFilenameAttributes(datasetVersionExportZip);
            FileContentAttributes fileContentAttributes = transferItemMetadataReader.getFileContentAttributes(datasetVersionExportZip);
            FilesystemAttributes filesystemAttributes = transferItemMetadataReader.getFilesystemAttributes(datasetVersionExportZip);

            if (filenameAttributes.getVersionMajor() == 1 && filenameAttributes.getVersionMinor() == 0) {



            }

            try {
                Files.move(datasetVersionExportZip, mirrorStore.resolve(datasetVersionExportZip.getFileName()));
            } catch (IOException e) {
                throw new IllegalStateException("Could not move DVE to EASY mirror store", e);
            }


        /* if (V1.0) {
                Create minimal deposit
                Move minimal deposit to outbox
            }
         */

        }
        catch (InvalidTransferItemException e) {
            try {
                Files.move(datasetVersionExportZip, failedBox.resolve(datasetVersionExportZip.getFileName()));
            }
            catch (IOException ioe) {
                throw new IllegalStateException("Cannot move invalid DVE to failedBox");
            }

        }
    }
}
