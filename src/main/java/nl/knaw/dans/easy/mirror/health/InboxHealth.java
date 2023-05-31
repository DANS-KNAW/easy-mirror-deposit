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
package nl.knaw.dans.easy.mirror.health;

import com.codahale.metrics.health.HealthCheck;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InboxHealth extends HealthCheck {
    private final Path inboxDir;

    public InboxHealth(Path inboxDir) {
        this.inboxDir = inboxDir;
    }

    @Override
    protected Result check() throws Exception {
        try (Stream<Path> inboxFiles = Files.list(inboxDir)) {
            String inboxFileNames = inboxFiles.map(Path::getFileName).map(Path::toString).collect(Collectors.joining(", "));
            if (inboxFileNames.isEmpty()) {
                return Result.unhealthy("Inbox is empty");
            }
        }
        catch (IOException e) {
            return Result.unhealthy("Could not read inbox", e);
        }
        return Result.healthy();
    }
}
