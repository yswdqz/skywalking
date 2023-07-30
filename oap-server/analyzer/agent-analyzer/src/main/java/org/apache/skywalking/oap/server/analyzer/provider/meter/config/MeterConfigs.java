/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.apache.skywalking.oap.server.analyzer.provider.meter.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.oap.meter.analyzer.prometheus.rule.Rule;
import org.apache.skywalking.oap.server.core.UnexpectedException;
import org.apache.skywalking.oap.server.library.module.ModuleStartException;
import org.apache.skywalking.oap.server.library.util.CollectionUtils;
import org.apache.skywalking.oap.server.library.util.NativeImageUtils;
import org.apache.skywalking.oap.server.library.util.ResourceUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Meter config loader.
 */
@Slf4j
public class MeterConfigs {

    /**
     * Load all configs from path
     */
    public static List<MeterConfig> loadConfig(String path, List<String> fileNames) throws ModuleStartException {

        if (CollectionUtils.isEmpty(fileNames)) {
            return Collections.emptyList();
        }

        if (NativeImageUtils.isNativeImage()) {
            return loadConfigNative(path, fileNames);
        }

        File[] configs;
        try {
            configs = ResourceUtils.getPathFiles(path);
        } catch (FileNotFoundException e) {
            throw new ModuleStartException("Load meter configs failed", e);
        }

        return Arrays.stream(configs)
                .map(f -> {
                    String fileName = f.getName();
                    int dotIndex = fileName.lastIndexOf('.');
                    fileName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
                    if (!fileNames.contains(fileName)) {
                        return null;
                    }
                    try (Reader r = new FileReader(f)) {
                        return new Yaml().<MeterConfig>loadAs(r, MeterConfig.class);
                    } catch (IOException e) {
                        log.warn("Reading file {} failed", f, e);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public static List<MeterConfig> loadConfigNative(String path, List<String> fileNames) throws ModuleStartException {

        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        List<MeterConfig> list = new ArrayList<>();
        try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("resource:/"), Map.of(), classLoader)) {
            final Path root = fileSystem.getPath(path);
            try (Stream<Path> configs = Files.walk(root)) {
                list = configs.map(f -> {
                    String fileName = f.getFileName().toString();
                    int dotIndex = fileName.lastIndexOf('.');
                    fileName = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
                    if (!fileNames.contains(fileName)) {
                        return null;
                    }
                    if (!Files.isRegularFile(f)) {
                        return null;
                    }
                    Set<StandardOpenOption> permissions = Collections.singleton(StandardOpenOption.READ);
                    String content = null;
                    try (SeekableByteChannel channel = fileSystem.provider().newFileChannel(f, permissions)) {
                        ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
                        channel.read(byteBuffer);
                        content = new String(byteBuffer.array());
                        return new Yaml().<MeterConfig>loadAs(content, MeterConfig.class);
                    } catch (IOException e) {
                        System.out.println("出错了");
                        throw new UnexpectedException("Load rule file failed", e);
                    }
                }).filter(Objects::nonNull).collect(Collectors.toList());

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return list;
    }
}
