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

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;

public class DatasetMetadataTest {

    @Test
    public void should_extract_all_fields_from_valid_jsonld_example() throws Exception {
        String jsonLd = FileUtils.readFileToString(Paths.get("src/test/resources/jsonld/example1.json").toFile(), "UTF-8");
        DatasetMetadata md = new DatasetMetadata(jsonLd);
        Assertions.assertEquals("Test export", md.getTitle());
        Assertions.assertEquals("Test", md.getDescription());
        Assertions.assertEquals("Admin, Dataverse", md.getCreator());
        Assertions.assertEquals("D14430", md.getAudience());
        Assertions.assertEquals("2100-01-01", md.getAvailable());
        Assertions.assertEquals("NO_ACCESS", md.getAccessRights());
    }

    @Test
    public void should_handle_multi_value_fields() throws Exception {

    }


}
