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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MirrorTaskTest {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Path inbox = Paths.get("target/test/MirrorTaskTest/inbox");
    private final Path failedBox = Paths.get("target/test/MirrorTaskTest/failedBox");
    private final Path mirrorStoreDir = Paths.get("target/test/MirrorTaskTest/mirrorStore");
    private final MirrorStore mirrorStore = new MirrorStore(mirrorStoreDir);
    private final Path dveRootDir = Paths.get("src/test/resources/dves/");

    @BeforeEach
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(inbox.toFile());
        FileUtils.deleteDirectory(failedBox.toFile());
        FileUtils.deleteDirectory(mirrorStoreDir.toFile());
        Files.createDirectories(inbox);
        Files.createDirectories(failedBox);
        Files.createDirectories(mirrorStoreDir);
    }

    private MirrorTask createTask(Path dve) throws Exception {
        Path dveInInbox = inbox.resolve(dve.getFileName());
        Files.copy(dveRootDir.resolve(dve), dveInInbox);
        return new MirrorTask(dveInInbox, failedBox, mirrorStore);
    }

    @Test
    public void dve_with_invalid_name_goes_to_failedBox() throws Exception {
        Path dve = Paths.get("invalid-names/not-a-dve.zip");
        createTask(dve).run();
        assertTrue(Files.exists(failedBox.resolve(dve.getFileName())));
        assertFalse(mirrorStore.contains(dve));
    }

    @Test
    public void dve_V1_1_goes_only_to_mirror_store() throws Exception {
        Path dve = Paths.get("valid/doi-10-5072-fk2-xcfq1bv1.1.zip");
        createTask(dve).run();
        assertTrue(mirrorStore.contains(dve));
    }

    @Test
    public void dve_V1_goes_to_mirror_store_and_produces_deposit() throws Exception {
        Path dve = Paths.get("valid/doi-10-5072-fk2-xcfq1bv1.0.zip");
        createTask(dve).run();
        assertTrue(mirrorStore.contains(dve));
    }
}
