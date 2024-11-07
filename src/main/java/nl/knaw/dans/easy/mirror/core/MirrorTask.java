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
import java.text.SimpleDateFormat;

public class MirrorTask implements Runnable {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private static final Logger log = LoggerFactory.getLogger(MirrorTask.class);

    private final Path datasetVersionExportZip;
    private final Path failedBox;
    private final MirrorStore mirrorStore;

    public MirrorTask(Path datasetVersionExportZip, Path failedBox, MirrorStore mirrorStore) {
        this.datasetVersionExportZip = datasetVersionExportZip;
        this.failedBox = failedBox;
        this.mirrorStore = mirrorStore;
    }

    @Override
    public void run() {
        log.info("Processing {}", datasetVersionExportZip.getFileName());

        try {
            // check for valid DVE, class also used in MirroringService.getAssociatedXmlFile
            new ExportedDatasetVersionName(datasetVersionExportZip.getFileName().toString());

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
}
