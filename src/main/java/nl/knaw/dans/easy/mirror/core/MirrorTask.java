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

import java.nio.file.Path;

public class MirrorTask implements Runnable {
    private final Path datasetVersionExportZip;
    private final Path outbox;
    private final Path mirrorStore;

    public MirrorTask(Path datasetVersionExportZip, Path outbox, Path mirrorStore) {
        this.datasetVersionExportZip = datasetVersionExportZip;
        this.outbox = outbox;
        this.mirrorStore = mirrorStore;
    }

    @Override
    public void run() {
        // Validate (name, is it a ZIP, ... ?)

        /* if (V1.0) {
                Create minimal deposit
                Move minimal deposit to outbox
            }
         */

        // Move DVE to mirrorStore
    }
}
