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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class MirrorStoreTest {
    private final Path testDir = Paths.get("target/test/MirrorStoreTest/");
    private final Path storeDir = testDir.resolve("store");
    private final MirrorStore mirrorStore = new MirrorStore(storeDir);

    @BeforeEach
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(storeDir.toFile());
        Files.createDirectories(storeDir);
        FileUtils.copyDirectory(Paths.get("src/test/resources/dves").toFile(), testDir.toFile());
    }

    @Test
    public void stores_bag_in_path_derived_from_first_four_chars_in_suffix() throws Exception {
        mirrorStore.store(testDir.resolve("valid/doi-10-5072-fk2-xcfq1bv1.0.zip"));
        Assertions.assertTrue(Files.exists(storeDir.resolve("xc/fq/doi-10-5072-fk2-xcfq1bv1.0.zip")));
    }

    @Test
    public void returns_exists_true_if_bag_already_stored() throws Exception {
        Path container = Files.createDirectories(storeDir.resolve("xc/fq"));
        Files.copy(testDir.resolve("valid/doi-10-5072-fk2-xcfq1bv1.0.zip"), container.resolve("doi-10-5072-fk2-xcfq1bv1.0.zip"));
        Assertions.assertTrue(mirrorStore.contains(testDir.resolve("valid/doi-10-5072-fk2-xcfq1bv1.0.zip")));
    }


}
