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
import java.util.Arrays;
import java.util.Collections;

public class DatasetMetadataTest {

    @Test
    public void should_extract_all_fields_from_valid_jsonld_example() throws Exception {
        String jsonLd = FileUtils.readFileToString(Paths.get("src/test/resources/jsonld/example1.json").toFile(), "UTF-8");
        DatasetMetadata md = new DatasetMetadata(jsonLd);
        Assertions.assertEquals("Test export &amp; special chars", md.getTitle());
        Assertions.assertEquals("Test", md.getDescription());
        Assertions.assertEquals("2022-04-02 17:02:50.63", md.getModified());
        Assertions.assertEquals("2022-04-02", md.getPublished());
        Assertions.assertEquals(Collections.singletonList("Admin, Dataverse"), md.getCreators());
        Assertions.assertEquals(Collections.singletonList("D14430"), md.getAudiences());
        Assertions.assertEquals("2022-04-02", md.getAvailable());
        Assertions.assertEquals("NO_ACCESS", md.getAccessRights());
        Assertions.assertEquals(Collections.singletonList("DANS"), md.getRightsHolders());
    }

    @Test
    public void should_handle_multi_value_fields() throws Exception {
        String jsonLd = FileUtils.readFileToString(Paths.get("src/test/resources/jsonld/example-multi-value.json").toFile(), "UTF-8");
        DatasetMetadata md = new DatasetMetadata(jsonLd);
        Assertions.assertEquals("Test multi-value", md.getTitle());
        Assertions.assertEquals("Descr 1\n\nDescr 2", md.getDescription());
        Assertions.assertEquals(Arrays.asList("Admin, Dataverse", "Author 2"), md.getCreators());
        Assertions.assertEquals(Arrays.asList("D30100", "D16700", "D38000"), md.getAudiences());
        Assertions.assertEquals("2022-04-13", md.getAvailable());
        Assertions.assertEquals("NO_ACCESS", md.getAccessRights());
        Assertions.assertEquals(Arrays.asList("DANS", "BAILE"), md.getRightsHolders());
    }

    @Test
    public void should_handle_audience_as_string() throws Exception {
        String jsonLd = FileUtils.readFileToString(Paths.get("src/test/resources/jsonld/example-audience-as-string-single.json").toFile(), "UTF-8");
        DatasetMetadata md = new DatasetMetadata(jsonLd);
        Assertions.assertEquals(Collections.singletonList("D30100"), md.getAudiences());
    }

    @Test
    public void should_handle_audiences_as_strings_multi() throws Exception {
        String jsonLd = FileUtils.readFileToString(Paths.get("src/test/resources/jsonld/example-audience-as-string-multi.json").toFile(), "UTF-8");
        DatasetMetadata md = new DatasetMetadata(jsonLd);
        Assertions.assertEquals(Arrays.asList("D30100", "D16700", "D38000"), md.getAudiences());
    }
}
