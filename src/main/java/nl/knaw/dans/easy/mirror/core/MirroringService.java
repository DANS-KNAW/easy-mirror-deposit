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
import nl.knaw.dans.easy.mirror.core.config.Inbox;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class MirroringService implements Managed {
    private static final Logger log = LoggerFactory.getLogger(MirroringService.class);
    private final int pollingInterval;
    private final List<Inbox> inboxes;
    private final MirrorTask mirrorTask;

    private class EventHandler extends FileAlterationListenerAdaptor {
        private final Inbox inbox;

        public EventHandler(Inbox inbox) {
            this.inbox = inbox;
        }

        @Override
        public void onStart(FileAlterationObserver observer) {
            log.trace("onStart called");
            processAllFromInbox(inbox);
        }

        @Override
        public void onFileCreate(File file) {
            log.trace("onFileCreate: {}", file);
            executeMirrorTask(file.toPath());
        }
    }

    public MirroringService(int pollingInterval, List<Inbox> inboxes,
        Path failedBox, Path mirrorStore) {
        this.pollingInterval = pollingInterval;
        this.inboxes = inboxes;
        this.mirrorTask =new MirrorTask(failedBox, new MirrorStore(mirrorStore));
    }

    @Override
    public void start() {
        log.info("Starting Mirroring Service");

        log.debug("Creating monitor");
        FileAlterationMonitor monitor = new FileAlterationMonitor(pollingInterval);
        for (Inbox inbox : inboxes) {
            log.info("Starting monitoring {}", inbox.getPath());
            FileAlterationObserver observer = new FileAlterationObserver(inbox.getPath().toFile(), new DveFileFilter(inbox.getPath()));
            observer.addListener(new EventHandler(inbox));
            log.debug("Added listener for {}", inbox.getPath());
            monitor.addObserver(observer);
            log.debug("Added observer for {}", inbox.getPath());
        }

        try {
            log.debug("Starting monitor");
            monitor.start();
            log.debug("Monitor started");
        }
        catch (Exception e) {
            throw new IllegalStateException("Could not start monitoring", e);
        }
    }

    private void processAllFromInbox(Inbox inbox) {
        try {
            DveFileFilter fileFilter = new DveFileFilter(inbox.getPath());
            try (Stream<Path> files = Files.list(inbox.getPath())) {
                files.filter(f -> fileFilter.accept(f.toFile()))
                    .forEach(this::executeMirrorTask);
            }
        }
        catch (IOException e) {
            throw new IllegalStateException("Could not read DVEs from inbox", e);
        }
    }

    private void executeMirrorTask(Path dve) {
        log.info("Executing " + dve.getFileName());
        try {
            Optional<Path> optXmlFile = getAssociatedXmlFile(dve);
            if (optXmlFile.isPresent()) {
                log.debug("Removing associated XML file {}", optXmlFile.get());
                Files.deleteIfExists(optXmlFile.get());
            }
            else {
                log.warn("Associated XML file was not found");
            }
            mirrorTask.move(dve);
        }
        catch (IOException e) {
            log.error("Could not move DVE to mirror store", e);
        }
    }

    private Optional<Path> getAssociatedXmlFile(Path path) {
        try {
            ExportedDatasetVersionName dveName = new ExportedDatasetVersionName(path.getFileName().toString());
            String xml = dveName.getSpaceName() + "-datacite.v" + dveName.getMajorVersion() + "." + dveName.getMinorVersion() + ".xml";
            return Optional.of(path.getParent().resolve(Paths.get(xml)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
