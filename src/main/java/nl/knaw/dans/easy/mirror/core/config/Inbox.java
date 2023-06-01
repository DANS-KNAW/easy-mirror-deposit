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
package nl.knaw.dans.easy.mirror.core.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Inbox {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    @NotNull
    @Valid
    private Path path;

    @NotNull
    @Valid
    private Date ignoreMigratedDatasetUpdatesPublishedBefore;

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }

    public Date getIgnoreMigratedDatasetUpdatesPublishedBefore() {
        return ignoreMigratedDatasetUpdatesPublishedBefore;
    }

    public void setIgnoreMigratedDatasetUpdatesPublishedBefore(String ignoreMigratedDatasetUpdatesPublishedBefore) {
        try {
            this.ignoreMigratedDatasetUpdatesPublishedBefore = dateFormat.parse(ignoreMigratedDatasetUpdatesPublishedBefore);
        }
        catch (ParseException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
