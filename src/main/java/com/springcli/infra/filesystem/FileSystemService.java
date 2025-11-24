package com.springcli.infra.filesystem;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Enumeration;

@Slf4j
@Service
public class FileSystemService {

    public void extractZip(Path zipPath, Path targetDir) throws IOException {
        log.info("Extracting {} to {}", zipPath, targetDir);

        Files.createDirectories(targetDir);

        try (ZipFile zipFile = ZipFile.builder().setPath(zipPath).get()) {
            Enumeration<ZipArchiveEntry> entries = zipFile.getEntries();

            while (entries.hasMoreElements()) {
                ZipArchiveEntry entry = entries.nextElement();
                Path entryPath = targetDir.resolve(entry.getName());

                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    try (InputStream in = zipFile.getInputStream(entry)) {
                        Files.copy(in, entryPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        }

        log.info("Extraction completed");
    }

    public void createDirectories(Path... paths) throws IOException {
        for (Path path : paths) {
            Files.createDirectories(path);
        }
    }

    public void writeFile(Path path, String content) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public String readFile(Path path) throws IOException {
        return Files.readString(path);
    }

    public void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }

        Files.walkFileTree(directory, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }
}
