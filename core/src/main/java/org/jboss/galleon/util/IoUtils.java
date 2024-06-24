/*
 * Copyright 2016-2022 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.galleon.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.jboss.galleon.Errors;

/**
 *
 * @author Alexey Loubyansky
 */
public class IoUtils {

    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    private static char[] charBuffer;

    private static final Path TMP_DIR = Paths.get(PropertyUtils.getSystemProperty("java.io.tmpdir"));

    private static void failedToMkDir(final Path dir) {
        throw new IllegalStateException(Errors.mkdirs(dir));
    }

    public static Path createTmpDir(String name) {
        final Path dir = TMP_DIR.resolve(name);
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            failedToMkDir(dir);
        }
        return dir;
    }

    public static Path createRandomTmpDir() {
        return createTmpDir(UUID.randomUUID().toString());
    }

    public static Path createRandomDir(Path parentDir) {
        final Path dir = parentDir.resolve(UUID.randomUUID().toString());
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            failedToMkDir(dir);
        }
        return dir;
    }

    public static void emptyDir(Path p) {
        if (p == null || !Files.exists(p)) {
            return;
        }
        try {
            Files.walkFileTree(p, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    try {
                        Files.delete(file);
                    } catch (IOException ex) {
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                    if (e != null) {
                        // directory iteration failed
                        throw e;
                    }
                    if (dir != p) {
                        try {
                            Files.delete(dir);
                        } catch (IOException ex) {
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
        }
    }

    public static void recursiveDelete(Path root) {
        if (root == null || !Files.exists(root)) {
            return;
        }
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                    try {
                        Files.delete(file);
                    } catch (IOException ex) {
                    }
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException e)
                    throws IOException {
                    if (e != null) {
                        // directory iteration failed
                        throw e;
                    }
                    try {
                        Files.delete(dir);
                    } catch (IOException ex) {
                    }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
        }
    }

    public static void copy(Path source, Path target) throws IOException {
        copy(source, target, false);
    }

    public static void copy(Path source, Path target, boolean skipExistingFiles) throws IOException {
        if (Files.isDirectory(source)) {
            try {
                Files.createDirectories(target);
            } catch (FileAlreadyExistsException e) {
                // FileAlreadyExistsException happen if the target path already exists and it's not directly (symbolic link or regular file)
                // this exception can be ignored if it's symbolic link and if real path of the symbolic link is directory.
                if (!Files.isDirectory(target)) {
                    throw new IOException("Unable to copy file(s) from source path [" + source + "] to target path [" + target + "] because the target path is not directory (regular file or symbolic link where real path is not directory).", e);
                }
            }
        } else {
            try {
                Files.createDirectories(target.getParent());
            } catch (FileAlreadyExistsException e) {
                // FileAlreadyExistsException happen if the target path already exists and it's not directly (symbolic link or regular file)
                // this exception can be ignored if it's symbolic link and if real path of the symbolic link is directory.
                if (!Files.isDirectory(target.getParent())) {
                    throw new IOException("Unable to copy file(s) from source path [" + source + "] to target path [" + target + "] because parent directory of the target path is not directory (regular file or symbolic link where real path is not directory).", e);
                }
            }
        }
        Files.walkFileTree(source, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE,
                new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                        throws IOException {
                        final Path targetDir = target.resolve(source.relativize(dir).toString());
                        try {
                            Files.copy(dir, targetDir);
                        } catch (FileAlreadyExistsException e) {
                             if (!Files.isDirectory(targetDir)) {
                                 throw e;
                             }
                        } catch (AccessDeniedException e) {
                            if (!skipExistingFiles || !Files.exists(targetDir)) {
                                throw e;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                        throws IOException {
                        final Path targetFile = target.resolve(source.relativize(file).toString());
                        try {
                            Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                        } catch (AccessDeniedException e) {
                            if (!skipExistingFiles || !Files.exists(targetFile)) {
                                throw e;
                            }
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
    }

    public static String readFile(Path file) throws IOException {
        if(charBuffer == null) {
            charBuffer = new char[DEFAULT_BUFFER_SIZE];
        }
        int n = 0;
        final StringWriter output = new StringWriter();
        try (BufferedReader input = Files.newBufferedReader(file)) {
            while ((n = input.read(charBuffer)) != -1) {
                output.write(charBuffer, 0, n);
            }
        }
        return output.getBuffer().toString();
    }

    public static void writeFile(Path file, String content) throws IOException {
        Files.write(file, content.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
    }

    public static Map<Path, String> listContents(Path root, PathFilter filter) throws IOException {
        if (root == null || !Files.exists(root)) {
            return Collections.emptyMap();
        }
        if(Files.isRegularFile(root)) {
            return Collections.singletonMap(root.relativize(root), HashUtils.hashFile(root));
        }
        Map<Path, String> contents = new HashMap<>();
        Files.walkFileTree(root, new FileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (filter.accept(root.relativize(dir))) {
                    String[] files = dir.toFile().list();
                    if (files == null || files.length == 0) {
                        contents.put(root.relativize(dir), HashUtils.hash(root.relativize(dir).toString()));
                    }
                    return FileVisitResult.CONTINUE;
                }
                return FileVisitResult.SKIP_SUBTREE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if(filter.accept(root.relativize(file))) {
                    contents.put(root.relativize(file), HashUtils.hashFile(file));
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });
        return contents;
    }
}
