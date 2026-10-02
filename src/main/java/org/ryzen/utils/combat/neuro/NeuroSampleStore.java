package org.ryzen.utils.combat.neuro;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ConfigIO;

@Environment(EnvType.CLIENT)
public final class NeuroSampleStore {
   private static final Pattern VALID_NAME = Pattern.compile("[A-Za-z0-9_\\-]{1,32}");
   private static final String EXTENSION = ".neuro";

   public static Path neuroDir() {
      return ConfigIO.resolve("neuro");
   }

   public static Path fileFor(String name) {
      return neuroDir().resolve(name + ".neuro");
   }

   public static boolean isValidName(String name) {
      return name != null && VALID_NAME.matcher(name).matches();
   }

   public static List<String> listNames() {
      Path dir = neuroDir();
      List<String> names = new ArrayList<>();
      if (!Files.isDirectory(dir)) {
         return names;
      }

      try (Stream<Path> stream = Files.list(dir)) {
         stream.filter(path -> path.toString().endsWith(".neuro")).forEach(path -> names.add(NeuroRotationData.stripExtension(path.getFileName().toString())));
      } catch (IOException var7) {
      }

      names.sort(String.CASE_INSENSITIVE_ORDER);
      return names;
   }

   public static NeuroRotationData load(String name) {
      return !isValidName(name) ? null : NeuroRotationData.load(fileFor(name));
   }

   public static boolean save(NeuroRotationData data) {
      return !isValidName(data.name()) ? false : data.save(fileFor(data.name()));
   }

   public static boolean delete(String name) {
      if (!isValidName(name)) {
         return false;
      }

      try {
         return Files.deleteIfExists(fileFor(name));
      } catch (IOException exception) {
         return false;
      }
   }

   public static int sampleCount(String name) {
      NeuroRotationData data = load(name);
      return data == null ? 0 : data.sampleCount();
   }

   public static String sanitize(String raw) {
      String trimmed = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
      return trimmed.replaceAll("[^a-z0-9_\\-]", "");
   }
}
