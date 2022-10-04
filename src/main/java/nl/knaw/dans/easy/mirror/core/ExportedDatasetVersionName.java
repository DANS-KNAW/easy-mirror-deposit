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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* DUPLICATED FROM dans-dataverse-client-lib, so that the latter can stay at target level 11 */

/**
 * Utility class to parse the names of dataset versions exported to RDA bags and metadata files. This is useful if you use `LocalSubmitToArchiveCommand`
 * [to export the bags to a local disk](https://guides.dataverse.org/en/latest/installation/config.html#local-path-configuration).
 */
public class ExportedDatasetVersionName {
    /*
     Reverse-engineered from edu.harvard.iq.dataverse.engine.command.impl.LocalSubmitToArchiveCommand, the pattern that the local filename should adhere to is:

     SPACENAME + ('-' + SCHEMA + '.') + 'v' + MAJOR + '.' + MINOR + EXTENSION
     */
    private static final String SPACENAME_PATTERN = "(?<spacename>[A-Za-z0-9-]+?)";
    private static final String SCHEMA_PATTERN = "(-(?<schema>datacite)\\.)?";
    private static final String DATASET_VERSION_PATTERN = "v(?<major>[0-9]+).(?<minor>[0-9]+)";
    private static final String EXTENSION_PATTERN = "(?<extension>.zip|.xml)";
    private static final Pattern PATTERN = Pattern.compile(SPACENAME_PATTERN + SCHEMA_PATTERN + DATASET_VERSION_PATTERN + EXTENSION_PATTERN);

    private final String spaceName;
    private final String schema;
    private final int majorVersion;
    private final int minorVersion;
    private final String extension;

    /**
     * Creates a new object to parse the name of one of the exported files. It works on both the exported ZIP and XML files. Only pass in the base name, not the complete path.
     *
     * @param name the name to parse
     */
    public ExportedDatasetVersionName(String name) {
        Matcher matcher = PATTERN.matcher(name);
        if (!matcher.matches())
            throw new IllegalArgumentException(String.format("Name does not conform to dataset version export naming pattern: %s", name));
        spaceName = matcher.group("spacename");
        schema = matcher.group("schema");
        majorVersion = Integer.parseInt(matcher.group("major"));
        minorVersion = Integer.parseInt(matcher.group("minor"));
        extension = matcher.group("extension");
    }

    /**
     * The part of the filename derived from the global ID (doi, handle)
     *
     * @return the spacename
     */
    public String getSpaceName() {
        return spaceName;
    }

    public String getSchema() {
        return schema;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public String getExtension() {
        return extension;
    }
}
