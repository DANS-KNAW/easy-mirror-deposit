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

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import nl.knaw.dans.lib.util.ExecutorServiceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;

public class EasyMirrorDepositConfiguration extends Configuration {

    @NotNull
    @Valid
    private ExecutorServiceFactory taskQueue;

    @NotNull
    @Valid
    private Path inbox;

    @NotNull
    @Valid
    private Path workDir;

    @NotNull
    @Valid
    private Path depositOutbox;

    @NotNull
    @Valid
    private Path easyMirrorStore;

    public ExecutorServiceFactory getTaskQueue() {
        return taskQueue;
    }

    public void setTaskQueue(ExecutorServiceFactory taskQueue) {
        this.taskQueue = taskQueue;
    }

    public Path getInbox() {
        return inbox;
    }

    public void setInbox(Path inbox) {
        this.inbox = inbox;
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

    public Path getEasyMirrorStore() {
        return easyMirrorStore;
    }

    public void setEasyMirrorStore(Path easyMirrorStore) {
        this.easyMirrorStore = easyMirrorStore;
    }
}
