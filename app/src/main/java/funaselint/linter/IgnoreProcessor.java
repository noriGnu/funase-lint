package funaselint.linter;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class IgnoreProcessor {
    private static final String IGNORE_FILE_NAME = ".funaseignore";
    private static final String GLOB_PREFIX = "glob:";

    private Path topLevelDirectory;
    private Map<Path, Set<String>> ignoredPatternsCache = new HashMap<>();
    private Map<Path, Set<String>> allowedPatternsCache = new HashMap<>();
    private Map<Path, Boolean> ignoreFileExistenceCache = new ConcurrentHashMap<>();
    private Map<String, PathMatcher> pathMatcherCache = new ConcurrentHashMap<>();

    public IgnoreProcessor(Path topLevelDirectory) {
        this.topLevelDirectory = topLevelDirectory;
    }

    public Set<Path> findFilesToLint() {
        try {
            return Files.walk(topLevelDirectory)
                    .filter(this::isPptxFile)
                    .filter(this::isFileLintable)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return Collections.emptySet();
        }
    }

    private boolean isPptxFile(Path path) {
        return path.toString().toLowerCase().endsWith(".pptx");
    }

    private boolean isFileLintable(Path path) {
        Path ignoreFile = findNearestConfigurationFile(path);
        if (ignoreFile != null && !ignoredPatternsCache.containsKey(ignoreFile)) {
            processConfigurationFile(ignoreFile);
        }
        Path relativePath = topLevelDirectory.relativize(path);

        return matchesAllowedPatterns(ignoreFile, relativePath) || !matchesIgnoredPatterns(ignoreFile, relativePath);
    }

    private Path findNearestConfigurationFile(Path path) {
        Path current = path.getParent();
        while (current != null && !current.equals(topLevelDirectory.getParent())) {
            Path ignoreFile = current.resolve(IGNORE_FILE_NAME);
            Boolean exists = ignoreFileExistenceCache.computeIfAbsent(ignoreFile, Files::exists);
            if (exists) {
                return ignoreFile;
            }
            current = current.getParent();
        }
        return null;
    }

    private void processConfigurationFile(Path ignoreFile) {
        Set<String> patterns;
        try {
            patterns = Files.lines(ignoreFile)
                    .filter(line -> !line.startsWith("#") && !line.trim().isEmpty())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        Set<String> allowedPatterns = new HashSet<>();
        Set<String> ignoredPatterns = new HashSet<>();
        for (String pattern : patterns) {
            if (pattern.startsWith("!")) {
                allowedPatterns.add(pattern.substring(1));
            } else {
                ignoredPatterns.add(pattern);
            }
        }

        ignoredPatternsCache.put(ignoreFile, ignoredPatterns);
        allowedPatternsCache.put(ignoreFile, allowedPatterns);
    }

    private boolean matchesAllowedPatterns(Path ignoreFile, Path relativePath) {
        return matchesPatterns(allowedPatternsCache, ignoreFile, relativePath);
    }

    private boolean matchesIgnoredPatterns(Path ignoreFile, Path relativePath) {
        return matchesPatterns(ignoredPatternsCache, ignoreFile, relativePath);
    }

    private boolean matchesPatterns(Map<Path, Set<String>> patternsCache, Path ignoreFile, Path relativePath) {
        return patternsCache.getOrDefault(ignoreFile, Collections.emptySet()).stream().anyMatch(pattern -> {
            String globPattern;
            if (pattern.endsWith("/")) {
                pattern = pattern.substring(0, pattern.length() - 1);
                globPattern = GLOB_PREFIX +  pattern + "/**";
            } else {
                globPattern = GLOB_PREFIX + pattern;
            }
            PathMatcher matcher = getPathMatcher(globPattern);
            return matcher.matches(relativePath);
        });
    }

    private PathMatcher getPathMatcher(String globPattern) {
        return pathMatcherCache.computeIfAbsent(globPattern,
                pattern -> FileSystems.getDefault().getPathMatcher(pattern));
    }
}
