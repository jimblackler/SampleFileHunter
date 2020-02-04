package net.jimblackler.samplehunter;

import com.google.common.base.Strings;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Main {

  public static void main(String[] args) throws IOException {
    Random random = new Random(10);
    Multimap<String, Path> filesByType = HashMultimap.create();

    Files.walkFileTree(Paths.get("/"), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String typeGroup = Files.probeContentType(file);

        if (Strings.isNullOrEmpty(typeGroup)) {
          typeGroup = com.google.common.io.Files.getFileExtension(file.toString());
          if (typeGroup.toLowerCase().equals(typeGroup.toUpperCase()) || typeGroup.length() > 10) {
            typeGroup = "unknown";
          }
        }
        filesByType.put(typeGroup, file);
        Collection<Path> paths = filesByType.get(typeGroup);
        if (paths.size() > 5) {
          paths.remove(Iterables.get(paths, random.nextInt(paths.size())));
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.printf("Visiting failed for %s\n", file);
        return FileVisitResult.SKIP_SUBTREE;
      }
    });


    long lastSize = 0;

    Map<Path, Long> sizes = new HashMap<>();

    for (Path file : filesByType.values()) {
      long length = file.toFile().length();
      if (length == 0) {
        continue;
      }
      sizes.put(file, length);
    }

    Map<Path, Long> pathLongMap = MapUtil.sortByValue(sizes);
    for (Map.Entry<Path, Long> entry : pathLongMap.entrySet()) {
      Path file = entry.getKey();
      long thisSize = entry.getValue();
      if (lastSize * 1.05f + 10 <= thisSize) {
        try {
          Files.copy(file, Paths.get("/Users/jimblackler/Downloads/samples",
              file.getFileName().toString()), StandardCopyOption.REPLACE_EXISTING);
          System.out.println(file);
        } catch (NoSuchFileException | AccessDeniedException e) {
          e.printStackTrace();
        }
        lastSize = thisSize;
      }
    }
  }

}
