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

package org.apache.skywalking.oap.meter.analyzer.prometheus.rule;

import java.io.*;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.*;

import java.util.*;
import java.util.stream.Collectors;

import java.util.stream.Stream;

import org.apache.skywalking.oap.server.core.UnexpectedException;
import org.apache.skywalking.oap.server.library.util.NativeImageUtils;
import org.apache.skywalking.oap.server.library.util.ResourceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * Rules is factory to instance {@link Rule} from a local file.
 */
public class Rules {
    private static final Logger LOG = LoggerFactory.getLogger(Rule.class);

    public static List<Rule> loadRules(final String path) throws IOException {
        return loadRules(path, Collections.emptyList());
    }

    public static List<Rule> loadRules(final String path, List<String> enabledRules) throws IOException {
        List<Rule> rules;
        Map<String, Boolean> formedEnabledRules = enabledRules
                .stream()
                .map(rule -> {
                    rule = rule.trim();
                    if (rule.startsWith("/")) {
                        rule = rule.substring(1);
                    }
                    if (!rule.endsWith(".yaml") && !rule.endsWith(".yml")) {
                        return rule + "{.yaml,.yml}";
                    }
                    return rule;
                })
                .collect(Collectors.toMap(rule -> rule, $ -> false));
        if (NativeImageUtils.isNativeImage()) {
            return loadRulesNative(formedEnabledRules,path);
        }
        final Path root = ResourceUtils.getPath(path);

        try (Stream<Path> stream = Files.walk(root)) {
            rules = stream
                    .filter(it -> formedEnabledRules.keySet().stream()
                            .anyMatch(rule -> {
                                boolean matches = FileSystems.getDefault().getPathMatcher("glob:" + rule)
                                        .matches(root.relativize(it));
                                if (matches) {
                                    formedEnabledRules.put(rule, true);
                                }
                                return matches;
                            }))
                    .map(pathPointer -> {
                        // Use relativized file path without suffix as the rule name.
                        String relativizePath = root.relativize(pathPointer).toString();
                        String ruleName = relativizePath.substring(0, relativizePath.lastIndexOf("."));
                        return getRulesFromFile(ruleName, pathPointer);
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList()) ;
        }

        if (formedEnabledRules.containsValue(false)) {
            List<String> rulesNotFound = formedEnabledRules.keySet().stream()
                    .filter(rule -> !formedEnabledRules.get(rule))
                    .collect(Collectors.toList());
            throw new UnexpectedException("Some configuration files of enabled rules are not found, enabled rules: " + rulesNotFound);
        }
        return rules;
    }

    private static Rule getRulesFromFile(String ruleName, Path path) {
        File file = path.toFile();
        if (!file.isFile() || file.isHidden()) {
            return null;
        }
        try (Reader r = new FileReader(file)) {
            Rule rule = new Yaml().loadAs(r, Rule.class);
            if (rule == null) {
                return null;
            }
            rule.setName(ruleName);
            return rule;
        } catch (IOException e) {
            throw new UnexpectedException("Load rule file" + file.getName() + " failed", e);
        }
    }
    public static List<Rule> loadRulesNative(Map<String, Boolean> formedEnabledRules, String path) {
        List<Rule> rules = null;
        ClassLoader classLoader = ResourceUtils.class.getClassLoader();
        try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("resource:/"), Map.of(), classLoader)) {
            Path p = fileSystem.getPath(path);
            try (Stream<Path> stream = Files.walk(p)) {
                rules = stream
                        .filter(it -> formedEnabledRules.keySet().stream()
                                .anyMatch(rule -> {
                                    boolean matches = FileSystems.getDefault().getPathMatcher("glob:" + rule)
                                            .matches(p.relativize(it));
                                    if (matches) {
                                        formedEnabledRules.put(rule, true);
                                    }
                                    return matches;
                                }))
                        .map(pathPointer -> {
                            // Use relativized file path without suffix as the rule name.
                            String relativizePath = p.relativize(pathPointer).toString();
                            String ruleName = relativizePath.substring(0, relativizePath.lastIndexOf("."));

                            return getRulesFromFileNative(ruleName, pathPointer, fileSystem);
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()) ;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return rules;
    }
    private static Rule getRulesFromFileNative(String ruleName, Path path, FileSystem fileSystem) {
        if (!Files.isRegularFile(path)) {
            return null;
        }
        Set<StandardOpenOption> permissions = Collections.singleton(StandardOpenOption.READ);
        String content = null;
        try (SeekableByteChannel channel = fileSystem.provider().newFileChannel(path, permissions)) {
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            channel.read(byteBuffer);
            content = new String(byteBuffer.array());
            Rule rule = new Yaml().loadAs(content, Rule.class);
            if (rule == null) {
                return null;
            }
            rule.setName(ruleName);
            return rule;
        } catch (IOException e) {
            System.out.println("出错了");
            throw new UnexpectedException("Load rule file failed", e);
        }
    }
}
