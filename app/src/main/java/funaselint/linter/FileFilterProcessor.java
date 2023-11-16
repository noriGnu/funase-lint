package funaselint.linter;

import java.io.File;
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

public class FileFilterProcessor {
    private static final String IGNORE_FILE_NAME = ".funaseignore";
    private static final String GLOB_PREFIX = "glob:";

    private File topLevelDirectory;
    private Map<File, Set<String>> ignoredPatternsCache = new HashMap<>();
    private Map<File, Set<String>> allowedPatternsCache = new HashMap<>();
    private Map<File, Boolean> ignoreFileExistenceCache = new ConcurrentHashMap<>();
    private Map<String, PathMatcher> pathMatcherCache = new ConcurrentHashMap<>();

    public FileFilterProcessor(File topLevelDirectory) {
        this.topLevelDirectory = topLevelDirectory;
    }

    public Set<File> findFilesToLint() {
        try {
            return Files.walk(topLevelDirectory.toPath())
                    .filter(this::isPptxFile)
                    .map(Path::toFile)
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

    private boolean isFileLintable(File file) {
        File ignoreFile = findNearestConfigurationFile(file);
        if (ignoreFile != null && !ignoredPatternsCache.containsKey(ignoreFile)) {
            processConfigurationFile(ignoreFile);
        }
        String relativePath = topLevelDirectory.toPath().relativize(file.toPath()).toString();

        return matchesAllowedPatterns(ignoreFile, relativePath) || !matchesIgnoredPatterns(ignoreFile, relativePath);
    }

    private File findNearestConfigurationFile(File file) {
        File current = file.getParentFile();
        while (current != null && !current.equals(topLevelDirectory.getParentFile())) {
            File ignoreFile = new File(current, IGNORE_FILE_NAME);
            Boolean exists = ignoreFileExistenceCache.computeIfAbsent(ignoreFile, File::exists);
            if (exists) {
                return ignoreFile;
            }
            current = current.getParentFile();
        }
        return null;
    }

    private void processConfigurationFile(File ignoreFile) {
        Set<String> patterns;
        try {
            patterns = Files.lines(ignoreFile.toPath())
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

    private boolean matchesAllowedPatterns(File ignoreFile, String path) {
        return matchesPatterns(allowedPatternsCache, ignoreFile, path);
    }

    private boolean matchesIgnoredPatterns(File ignoreFile, String path) {
        return matchesPatterns(ignoredPatternsCache, ignoreFile, path);
    }

    private boolean matchesPatterns(Map<File, Set<String>> patternsCache, File ignoreFile, String path) {
        Path absolutePath = topLevelDirectory.toPath().resolve(path).normalize();
        return patternsCache.getOrDefault(ignoreFile, Collections.emptySet()).stream().anyMatch(pattern -> {
            String globPattern;
            if (pattern.endsWith("/")) {
                pattern = pattern.substring(0, pattern.length() - 1);
                globPattern = GLOB_PREFIX + "**/" + pattern + "/**";
            } else {
                globPattern = GLOB_PREFIX + "**/" + pattern;
            }
            PathMatcher matcher = getPathMatcher(globPattern);
            return matcher.matches(absolutePath);
        });
    }

    private PathMatcher getPathMatcher(String globPattern) {
        return pathMatcherCache.computeIfAbsent(globPattern,
                pattern -> FileSystems.getDefault().getPathMatcher(pattern));
    }
}
