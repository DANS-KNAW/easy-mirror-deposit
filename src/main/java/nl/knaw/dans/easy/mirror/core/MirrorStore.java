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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MirrorStore {
    private final Path baseDir;

    public MirrorStore(Path baseDir) {
        this.baseDir = baseDir;
    }

    public void store(Path dveZip) throws IOException {
        Path container = calculateContainer(dveZip);
        Files.createDirectories(container);
        Files.move(dveZip, container.resolve(dveZip.getFileName()));
    }

    public boolean contains(Path dveZip) {
        return Files.exists(calculateContainer(dveZip).resolve(dveZip.getFileName()));
    }

    private Path calculateContainer(Path dveZip) {
        String[] parts = dveZip.getFileName().toString().split("-");
        String top = parts[parts.length - 1].substring(0, 2);
        String bottom = parts[parts.length - 1].substring(2, 4);
        return baseDir.resolve(Paths.get(top, bottom));
    }
}
