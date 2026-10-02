package org.ryzen.pve.mining;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;

@Environment(EnvType.CLIENT)
public final class MiningParsers {
   private static final Pattern IDENTIFIER = Pattern.compile("(?:[a-z0-9_.-]+:)?[a-z0-9_./-]+");
   private static final int MAX_HORIZONTAL_COORDINATE = 30000000;
   private static final int MAX_VERTICAL_COORDINATE = 2048;

   private MiningParsers() {
   }

   public static Set<String> identifiers(String value) {
      if (value != null && !value.isBlank()) {
         LinkedHashSet<String> result = new LinkedHashSet<>();

         for (String token : value.split("[,;\\s]+")) {
            String id = token.trim().toLowerCase(Locale.ROOT);
            if (!id.isEmpty() && IDENTIFIER.matcher(id).matches()) {
               result.add(id.contains(":") ? id : "minecraft:" + id);
            }
         }

         return Set.copyOf(result);
      } else {
         return Set.of();
      }
   }

   public static List<MiningParsers.GridPoint> route(String value) {
      if (value != null && !value.isBlank()) {
         List<MiningParsers.GridPoint> route = new ArrayList<>();

         for (String token : value.split("[;|]")) {
            parsePoint(token).ifPresent(route::add);
         }

         return List.copyOf(route);
      } else {
         return List.of();
      }
   }

   public static Optional<MiningParsers.GridPoint> point(String value) {
      return parsePoint(value);
   }

   public static Optional<MiningParsers.Region> region(String value) {
      if (value != null && !value.isBlank()) {
         String[] endpoints = value.trim().split("\\s*(?:->|:)\\s*", 2);
         if (endpoints.length != 2) {
            return Optional.empty();
         }

         Optional<MiningParsers.GridPoint> first = parsePoint(endpoints[0]);
         Optional<MiningParsers.GridPoint> second = parsePoint(endpoints[1]);
         return !first.isEmpty() && !second.isEmpty() ? Optional.of(new MiningParsers.Region(first.get(), second.get())) : Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   private static Optional<MiningParsers.GridPoint> parsePoint(String value) {
      if (value == null) {
         return Optional.empty();
      }

      String[] components = value.trim().split("\\s*,\\s*");
      if (components.length != 3) {
         return Optional.empty();
      }

      try {
         int x = Integer.parseInt(components[0]);
         int y = Integer.parseInt(components[1]);
         int z = Integer.parseInt(components[2]);
         return Math.abs((long)x) <= 30000000L && Math.abs((long)z) <= 30000000L && Math.abs((long)y) <= 2048L
            ? Optional.of(new MiningParsers.GridPoint(x, y, z))
            : Optional.empty();
      } catch (NumberFormatException ignored) {
         return Optional.empty();
      }
   }

   @Environment(EnvType.CLIENT)
   public record GridPoint(int x, int y, int z) {
      public class_2338 toBlockPos() {
         return new class_2338(this.x, this.y, this.z);
      }
   }

   @Environment(EnvType.CLIENT)
   public record Region(MiningParsers.GridPoint first, MiningParsers.GridPoint second) {
      public boolean contains(class_2338 position) {
         return position.method_10263() >= Math.min(this.first.x, this.second.x)
            && position.method_10263() <= Math.max(this.first.x, this.second.x)
            && position.method_10264() >= Math.min(this.first.y, this.second.y)
            && position.method_10264() <= Math.max(this.first.y, this.second.y)
            && position.method_10260() >= Math.min(this.first.z, this.second.z)
            && position.method_10260() <= Math.max(this.first.z, this.second.z);
      }
   }
}
