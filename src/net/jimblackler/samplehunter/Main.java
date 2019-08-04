package net.jimblackler.samplehunter;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Main {

  public static void main(String[] args) throws IOException {
    Random random = new Random(1);
    Map<String, Map<Integer, Set<String>>> filesByMimeType = new HashMap<>();

    Files.walkFileTree(Paths.get("/Library"), new SimpleFileVisitor<>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String mimeType = Files.probeContentType(file);

        if (mimeType == null) {
          return FileVisitResult.CONTINUE;
        }

        if (!filesByMimeType.containsKey(mimeType)) {
          filesByMimeType.put(mimeType, new HashMap<>());
        }
        Map<Integer, Set<String>> files = filesByMimeType.get(mimeType);

        int sizeGroup = (int) (Math.log(file.toFile().length()) / Math.log(15));
        if (sizeGroup >= 7) {
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

    for (Map<Integer, Set<String>> files : filesByMimeType.values()) {
      for (Set<String> f2 : files.values()) {
        int item = random.nextInt(f2.size());
        int idx = 0;
        for (String str : f2) {
          if (idx == item) {
            System.out.println(str);
          }
          idx++;
        }
      }
    }
  }

}
