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

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class DatasetMetadataReader {
    private final Path dve;

    public DatasetMetadataReader(Path dve) {
        this.dve = dve;
    }

    public DatasetMetadata extractDatasetMetadata() throws IOException {
        return new DatasetMetadata(extractJsonLd());
    }

    private String extractJsonLd() throws IOException {
        ZipFile dveZip = new ZipFile(dve.toString());
        List<ZipEntry> jsonLdEntries = Collections.list(dveZip.entries()).stream().filter(e -> e.getName().matches("^[^/]+/metadata/oai-ore.jsonld")).collect(Collectors.toList());
        if (jsonLdEntries.size() == 1) {
            return readStringFromZipEntry(dveZip, jsonLdEntries.get(0));
        }
        else {
            throw new IllegalArgumentException(String.format("Found %d files for */metadata/oai-ore.jsonld. Expected exactly 1 file", jsonLdEntries.size()));
        }
    }

    private String readStringFromZipEntry(ZipFile file, ZipEntry entry) throws IOException {
        StringWriter stringWriter = new StringWriter();
        IOUtils.copy(file.getInputStream(entry), stringWriter, "UTF-8");
        return stringWriter.toString();
    }
}
