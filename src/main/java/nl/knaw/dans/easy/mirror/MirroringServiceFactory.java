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
package nl.knaw.dans.easy.mirror;

import nl.knaw.dans.easy.mirror.core.MirroringService;
import nl.knaw.dans.easy.mirror.core.TransferItemMetadataReader;
import nl.knaw.dans.easy.mirror.core.config.Inbox;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public class MirroringServiceFactory {

    @NotNull
    @Valid
    private List<Inbox> inboxes;

    private int pollingInterval;

    @NotNull
    @Valid
    private Path workDir;

    @NotNull
    @Valid
    private Path depositOutbox;

    @NotNull
    @Valid
    private Path failedBox;

    @NotNull
    @Valid
    private Path easyMirrorStore;

    @NotNull
    @Valid
    private Path velocityProperties;


    @NotNull
    @Valid
    private Pattern migratedDatasetDoiPattern;

    public MirroringService build(ExecutorService executorService, TransferItemMetadataReader transferItemMetadataReader) {
        return new MirroringService(executorService, transferItemMetadataReader, velocityProperties, pollingInterval, inboxes, workDir,
            depositOutbox, failedBox, migratedDatasetDoiPattern, easyMirrorStore);
    }

    public List<Inbox> getInboxes() {
        return inboxes;
    }

    public void setInboxes(List<Inbox> inboxes) {
        this.inboxes = inboxes;
    }

    public int getPollingInterval() {
        return pollingInterval;
    }

    public void setPollingInterval(int pollingInterval) {
        this.pollingInterval = pollingInterval;
    }

    public Path getWorkDir() {
        return workDir;
    }

    public void setWorkDir(Path workDir) {
        this.workDir = workDir;
    }

    public Path getDepositOutbox() {
        return depositOutbox;
    }

    public void setDepositOutbox(Path depositOutbox) {
        this.depositOutbox = depositOutbox;
    }

    public Path getFailedBox() {
        return failedBox;
    }

    public void setFailedBox(Path failedBox) {
        this.failedBox = failedBox;
    }

    public Path getEasyMirrorStore() {
        return easyMirrorStore;
    }

    public void setEasyMirrorStore(Path easyMirrorStore) {
        this.easyMirrorStore = easyMirrorStore;
    }

    public Path getVelocityProperties() {
        return velocityProperties;
    }

    public void setVelocityProperties(Path velocityProperties) {
        this.velocityProperties = velocityProperties;
    }

    public Pattern getMigratedDatasetDoiPattern() {
        return migratedDatasetDoiPattern;
    }

    public void setMigratedDatasetDoiPattern(String migratedDatasetDoiPattern) {
        this.migratedDatasetDoiPattern = Pattern.compile(migratedDatasetDoiPattern);
    }


}
