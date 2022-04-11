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

import io.dropwizard.lifecycle.Managed;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class MirroringService implements Managed {
    private static final Logger log = LoggerFactory.getLogger(MirroringService.class);
    private final ExecutorService executorService;
    private final TransferItemMetadataReader transferItemMetadataReader;
    private final int pollingInterval;
    private final Path inbox;
    private final Path depositOutbox;
    private final Path failedBox;
    private final Path workDirectory;
    private final MirrorStore mirrorStore;
    private final Path velocityProperties;

    private boolean initialized = false;
    private boolean tasksCreatedInitialization = false;

    private class EventHandler extends FileAlterationListenerAdaptor {
        @Override
        public void onStart(FileAlterationObserver observer) {
            log.trace("onStart called");
            if (!initialized) {
                initialized = true;
                processAllFromInbox();
            }
        }

        @Override
        public void onFileCreate(File file) {
            log.trace("onFileCreate: {}", file);
            if (tasksCreatedInitialization) {
                tasksCreatedInitialization = false;
                return; // file already added to queue by onStart
            }
            scheduleDatasetVersionExport(file.toPath());
        }
    }

    public MirroringService(ExecutorService executorService, TransferItemMetadataReader transferItemMetadataReader, Path velocityProperties, int pollingInterval, Path inbox, Path workDirectory,
        Path depositOutbox, Path failedBox, Path mirrorStore) {
        this.executorService = executorService;
        this.transferItemMetadataReader = transferItemMetadataReader;
        this.velocityProperties = velocityProperties;
        this.pollingInterval = pollingInterval;
        this.inbox = inbox;
        this.workDirectory = workDirectory;
        this.depositOutbox = depositOutbox;
        this.failedBox = failedBox;
        this.mirrorStore = new MirrorStore(mirrorStore);
    }

    @Override
    public void start() throws Exception {
        log.info("Starting Mirroring Service");
        Velocity.init(velocityProperties.toString());
        log.debug("Initialized Velocity");
        FileAlterationObserver observer = new FileAlterationObserver(inbox.toFile(), new DveFileFilter(inbox));
        observer.addListener(new EventHandler());
        log.debug("Added listener");
        FileAlterationMonitor monitor = new FileAlterationMonitor(pollingInterval);
        monitor.addObserver(observer);
        log.debug("Added observer");
        try {
            log.debug("Starting monitor");
            monitor.start();
            log.debug("Monitor started");
        }
        catch (Exception e) {
            throw new IllegalStateException(String.format("Could not start monitoring %s", inbox), e);
        }
    }

    private void processAllFromInbox() {
        try {
            DveFileFilter fileFilter = new DveFileFilter(inbox);
            Files.list(inbox)
                .filter(f -> fileFilter.accept(f.toFile()))
                .forEach(dve -> {
                    scheduleDatasetVersionExport(dve);
                    tasksCreatedInitialization = true;
                });
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read DVEs from inbox", e);
        }
    }

    private void scheduleDatasetVersionExport(Path dve) {
        log.info("Scheduling " + dve.getFileName());
        try {
            Path movedDve = Files.move(dve, workDirectory.resolve(dve.getFileName()));
            Optional<Path> optXmlFile = transferItemMetadataReader.getAssociatedXmlFile(dve);
            if (optXmlFile.isPresent()) {
                log.debug("Removing associated XML file {}", optXmlFile.get());
                Files.deleteIfExists(optXmlFile.get());
            } else {
                log.warn("Associated XML file was not found");
            }
            executorService.execute(new MirrorTask(transferItemMetadataReader, movedDve, workDirectory, depositOutbox, failedBox, mirrorStore));
        }
        catch (IOException e) {
            log.error("Could not move DVE to work directory", e);
        }
    }

    public void stop() {
        executorService.shutdown();
    }
}
