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

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileServiceImpl implements FileService {
    private static final Logger log = LoggerFactory.getLogger(FileServiceImpl.class);

    @Override
    public Path moveFile(Path current, Path newPath) throws IOException {
        Objects.requireNonNull(current, "current path cannot be null");
        Objects.requireNonNull(newPath, "newPath cannot be null");
        log.trace("moving file from {} to {}", current, newPath);
        return Files.move(current, newPath);
    }

    @Override
    public Path moveFileAtomically(Path filePath, Path newPath) throws IOException {
        Objects.requireNonNull(filePath, "filePath cannot be null");
        Objects.requireNonNull(newPath, "newPath cannot be null");

        Path tempTarget = Paths.get(newPath + ".part");

        // there could be leftovers from a previous attempt, remove them
        Files.deleteIfExists(tempTarget);

        moveFile(filePath, tempTarget);
        return moveFile(tempTarget, newPath);
    }

    @Override
    public void ensureDirectoryExists(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        Files.createDirectories(path);
    }

    @Override
    public boolean exists(Path path) {
        return Files.exists(path);
    }

    @Override
    public boolean canRead(Path path) {
        return Files.isReadable(path);
    }

    @Override
    public boolean canWrite(Path path) {
        return Files.isWritable(path);
    }

    @Override
    public FileStore getFileStore(Path path) throws IOException {
        return Files.getFileStore(path);
    }

    void writeExceptionToFile(Path errorReportName, Exception exception) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(errorReportName.toFile());
        exception.printStackTrace(writer);
        writer.close();
    }

    @Override
    public boolean deleteFile(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        log.trace("Deleting file {}", path);
        return Files.deleteIfExists(path);
    }

    @Override
    public void deleteDirectory(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        log.trace("Deleting directory '{}'", path);
        FileUtils.deleteDirectory(path.toFile());
    }

    @Override
    public Object getFilesystemAttribute(Path path, String property) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(property, "property cannot be null");
        log.trace("Getting attribute {} for path '{}'", property, path);
        return Files.getAttribute(path, property);
    }

    @Override
    public String calculateChecksum(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        log.trace("Calculating checksum for '{}'", path);
        return new DigestUtils("SHA-256").digestAsHex(Files.readAllBytes(path));
    }

    @Override
    public long getFileSize(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        log.trace("Getting file size for path '{}'", path);
        return Files.size(path);
    }

    @Override
    public long getPathSize(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        return Files.walk(path).filter(Files::isRegularFile).map(p -> {
            try {
                long size = getFileSize(p);
                log.trace("File size for file '{}' is {} bytes", p, size);
                return size;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).reduce(0L, Long::sum);
    }

    @Override
    public ZipFile openZipFile(Path path) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        log.trace("Opening zip file '{}'", path);
        return new ZipFile(path.toFile());
    }

    @Override
    public InputStream openFileFromZip(ZipFile zipFile, Path path) throws IOException {
        Objects.requireNonNull(zipFile, "zipFile cannot be null");
        Objects.requireNonNull(path, "path cannot be null");

        ZipEntry entryPath = Objects.requireNonNull(zipFile.stream()
                .filter(e -> e.getName().endsWith(path.toString()))
                .findFirst()
                .orElse(null)
            , String.format("no entries found for path '%s' in zip file %s", path, zipFile)
        );

        log.trace("Requested entry for path '{}', found match on '{}'", path, entryPath);

        return zipFile.getInputStream(entryPath);
    }

}
