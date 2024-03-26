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
import org.apache.velocity.app.Velocity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MirrorTaskTest {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    private final Path inbox = Paths.get("target/test/MirrorTaskTest/inbox");
    private final Path depositOutbox = Paths.get("target/test/MirrorTaskTest/depositOutbox");
    private final Path workDir = Paths.get("target/test/MirrorTaskTest/workingDirectory");
    private final Path failedBox = Paths.get("target/test/MirrorTaskTest/failedBox");
    private final Path mirrorStoreDir = Paths.get("target/test/MirrorTaskTest/mirrorStore");
    private final MirrorStore mirrorStore = new MirrorStore(mirrorStoreDir);
    private final Path dveRootDir = Paths.get("src/test/resources/dves/");

    private final Pattern migratedDatasetDoiPattern = Pattern.compile("^10\\.17026/DANS.*$");

    // TODO: can we ensure that this ObjectMapper has the same behavior as the one from the DropWizard environment?
    private final TransferItemMetadataReader transferItemMetadataReader = new TransferItemMetadataReaderImpl(new ObjectMapper(), new FileServiceImpl());

    @BeforeEach
    public void setUp() throws Exception {
        Velocity.init("src/test/resources/velocity.properties");
        FileUtils.deleteDirectory(inbox.toFile());
        FileUtils.deleteDirectory(workDir.toFile());
        FileUtils.deleteDirectory(depositOutbox.toFile());
        FileUtils.deleteDirectory(failedBox.toFile());
        FileUtils.deleteDirectory(mirrorStoreDir.toFile());
        Files.createDirectories(workDir);
        Files.createDirectories(inbox);
        Files.createDirectories(depositOutbox);
        Files.createDirectories(failedBox);
        Files.createDirectories(mirrorStoreDir);
    }

    private MirrorTask createTask(Path dve) throws Exception {
        Path dveInInbox = inbox.resolve(dve.getFileName());
        Files.copy(dveRootDir.resolve(dve), dveInInbox);
        Date oldDate = dateFormat.parse("2000-01-01");
        return new MirrorTask(transferItemMetadataReader, dveInInbox, oldDate, workDir, depositOutbox, failedBox, migratedDatasetDoiPattern, mirrorStore);
    }

    @Test
    public void dve_with_invalid_name_goes_to_failedBox() throws Exception {
        Path dve = Paths.get("invalid-names/not-a-dve.zip");
        createTask(dve).run();
        assertTrue(Files.exists(failedBox.resolve(dve.getFileName())));
        assertFalse(mirrorStore.contains(dve));
        assertEquals(0, Files.list(depositOutbox).count());
    }

    @Test
    public void dve_V1_1_goes_only_to_mirror_store() throws Exception {
        Path dve = Paths.get("valid/doi-10-5072-fk2-xcfq1bv1.1.zip");
        createTask(dve).run();
        assertEquals(0, Files.list(depositOutbox).count());
        assertTrue(mirrorStore.contains(dve));
    }

    @Test
    public void dve_V1_goes_to_mirror_store_and_produces_deposit() throws Exception {
        Path dve = Paths.get("valid/doi-10-5072-fk2-xcfq1bv1.0.zip");
        createTask(dve).run();
        assertEquals(1, Files.list(depositOutbox).count());
        Path depositDir = Files.list(depositOutbox).collect(Collectors.toList()).get(0);
        assertTrue(Files.exists(depositDir.resolve("deposit.properties")));

        assertTrue(mirrorStore.contains(dve));
    }

}
