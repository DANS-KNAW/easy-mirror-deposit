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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MirrorTaskTest {
    private final Path inbox = Paths.get("target/test/MirrorTaskTest/inbox");
    private final Path outBox = Paths.get("target/test/MirrorTaskTest/outbox");
    private final Path failedBox = Paths.get("target/test/MirrorTaskTest/failedBox");
    private final Path mirrorStore = Paths.get("target/test/MirrorTaskTest/mirrorStore");
    private final Path dveRootDir = Paths.get("src/test/resources/dves/");
    // TODO: can we ensure that this ObjectMapper has the same behavior as the one from the DropWizard environment?
    private final TransferItemMetadataReader transferItemMetadataReader = new TransferItemMetadataReaderImpl(new ObjectMapper(), new FileServiceImpl());

    @BeforeEach
    public void setUp() throws Exception {
        FileUtils.deleteDirectory(inbox.toFile());
        FileUtils.deleteDirectory(outBox.toFile());
        FileUtils.deleteDirectory(failedBox.toFile());
        FileUtils.deleteDirectory(mirrorStore.toFile());
        Files.createDirectories(inbox);
        Files.createDirectories(outBox);
        Files.createDirectories(failedBox);
        Files.createDirectories(mirrorStore);
    }

    private MirrorTask createTask(Path dve) throws Exception {
        Path dveInInbox = inbox.resolve(dve.getFileName());
        Files.copy(dveRootDir.resolve(dve), dveInInbox);
        return new MirrorTask(transferItemMetadataReader, dveInInbox, outBox, failedBox, mirrorStore);
    }

    @Test
    public void dve_with_invalid_name_goes_to_failedBox() throws Exception {
        Path dve = Paths.get("invalid-names/not-a-dve.zip");
        createTask(dve).run();
        assertTrue(Files.exists(failedBox.resolve(dve.getFileName())));
        assertFalse(Files.exists(mirrorStore.resolve(dve.getFileName())));
        assertEquals(0, Files.list(outBox).count());
    }

    @Test
    public void dve_V2_goes_only_to_mirror_store() {
    }

    @Test
    public void dve_V1_goes_to_mirror_store_and_produces_deposit() {
    }

}
