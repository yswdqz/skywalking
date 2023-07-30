//package org.apache.skywalking.oap.server.starter;
//
//
//import com.oracle.svm.core.annotate.Substitute;
//import com.oracle.svm.core.annotate.TargetClass;
//import org.apache.skywalking.oap.meter.analyzer.prometheus.rule.Rule;
//import org.apache.skywalking.oap.meter.analyzer.prometheus.rule.Rules;
//import org.apache.skywalking.oap.server.core.UnexpectedException;
//import org.apache.skywalking.oap.server.library.util.ResourceUtils;
//import org.yaml.snakeyaml.Yaml;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Reader;
//import java.net.URI;
//import java.nio.file.FileSystem;
//import java.nio.file.FileSystems;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.stream.Collectors;
//import java.util.stream.Stream;
//
//
//@TargetClass(Rules.class)
//final class Target_org_apache_skywalking_oap_meter_analyzer_prometheus_rule_Rules {
//
//    @Substitute
//    public static List<Rule> loadRules(final String path, List<String> enabledRules) throws IOException {
//
//        final Path root = ResourceUtils.getPath(path);
//        ClassLoader classLoader = ClassLoader.class.getClassLoader();
//        List<Rule> rules;
//        Map<String, Boolean> formedEnabledRules = enabledRules
//                .stream()
//                .map(rule -> {
//                    rule = rule.trim();
//                    if (rule.startsWith("/")) {
//                        rule = rule.substring(1);
//                    }
//                    if (!rule.endsWith(".yaml") && !rule.endsWith(".yml")) {
//                        return rule + "{.yaml,.yml}";
//                    }
//                    return rule;
//                })
//                .collect(Collectors.toMap(rule -> rule, $ -> false));
//
//        try (FileSystem fileSystem = FileSystems.newFileSystem(URI.create("resource:/"), Map.of(), classLoader)) {
//            Path p = fileSystem.getPath(path);
//            try (Stream<Path> stream = Files.walk(p)) {
//                rules = stream
//                        .filter(it -> formedEnabledRules.keySet().stream()
//                                .anyMatch(rule -> {
//                                    boolean matches = FileSystems.getDefault().getPathMatcher("glob:" + rule)
//                                            .matches(root.relativize(it));
//                                    if (matches) {
//                                        formedEnabledRules.put(rule, true);
//                                    }
//                                    return matches;
//                                }))
//                        .map(pathPointer -> {
//                            // Use relativized file path without suffix as the rule name.
//                            String relativizePath = root.relativize(pathPointer).toString();
//                            String ruleName = relativizePath.substring(0, relativizePath.lastIndexOf("."));
//
//                            return getRulesFromFile(ruleName, pathPointer);
//                        })
//                        .filter(Objects::nonNull)
//                        .collect(Collectors.toList()) ;
//            }
//        }
//
//
//        if (formedEnabledRules.containsValue(false)) {
//            List<String> rulesNotFound = formedEnabledRules.keySet().stream()
//                    .filter(rule -> !formedEnabledRules.get(rule))
//                    .collect(Collectors.toList());
//            throw new UnexpectedException("Some configuration files of enabled rules are not found, enabled rules: " + rulesNotFound);
//        }
//        return rules;
//    }
//
//
//    private static Rule getRulesFromFile(String ruleName, Path path) {
//
//    }
//}
//final class SWSubtitutions {
//}
