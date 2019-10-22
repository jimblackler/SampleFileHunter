package net.jimblackler.samplehunter;

import com.google.common.base.Strings;

import java.io.File;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Main {

  public static void main(String[] args) throws IOException {
    Random random = new Random(9);
    Map<String, Map<Integer, Set<String>>> filesByType = new HashMap<>();

    Files.walkFileTree(Paths.get("/"), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String typeGroup = Files.probeContentType(file);

        if (Strings.isNullOrEmpty(typeGroup)) {
          typeGroup = com.google.common.io.Files.getFileExtension(file.toString());
          if (typeGroup.length() > 4 || Strings.isNullOrEmpty(typeGroup)) {
            return FileVisitResult.CONTINUE;
          }
        }

        if (!filesByType.containsKey(typeGroup)) {
          filesByType.put(typeGroup, new HashMap<>());
        }
        Map<Integer, Set<String>> files = filesByType.get(typeGroup);

        long length = file.toFile().length();
        if (length == 0) {
          return FileVisitResult.CONTINUE;
        }
        int sizeGroup = (int) (Math.log(length) / Math.log(40));
        if (sizeGroup == 0 || sizeGroup >= 5) {
          return FileVisitResult.CONTINUE;
        }

        if (!files.containsKey(sizeGroup)) {
          files.put(sizeGroup, new HashSet<>());
        }
        Set<String> f2 = files.get(sizeGroup);
        f2.add(file.toString());

        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.printf("Visiting failed for %s\n", file);
        return FileVisitResult.SKIP_SUBTREE;
      }
    });

    for (Map<Integer, Set<String>> files : filesByType.values()) {
      for (Set<String> f2 : files.values()) {
        int item = random.nextInt(f2.size());
        int idx = 0;
        for (String str : f2) {
          if (idx == item) {
            try {
              Path source = Paths.get(str);
              Files.copy(source, Paths.get("/Users/jimblackler/Downloads/samples/" + new File(str).getName()),
                  StandardCopyOption.REPLACE_EXISTING);
              System.out.println(str);
            } catch (NoSuchFileException | AccessDeniedException e) {
              e.printStackTrace();
            }
          }
          idx++;
        }
      }
    }
  }

}
