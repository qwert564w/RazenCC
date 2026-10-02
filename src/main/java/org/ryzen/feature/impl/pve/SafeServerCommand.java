package org.ryzen.feature.impl.pve;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class SafeServerCommand {
   private static final int MAX_COMMAND_LENGTH = 64;
   private static final Set<String> SAFE_ROOTS = Set.of("an", "anarchy", "home", "hub", "leave", "lobby", "quit", "server", "spawn");
   private static final Pattern ARGUMENT = Pattern.compile("[A-Za-z0-9_.:-]{1,32}");

   private SafeServerCommand() {
   }

   public static Optional<String> normalize(String value) {
      if (value == null) {
         return Optional.empty();
      }

      String command = value.trim();
      if (command.startsWith("/")) {
         command = command.substring(1).trim();
      }

      if (!command.isEmpty() && command.length() <= 64 && !hasControlCharacter(command)) {
         String[] parts = command.split("\\s+");
         String root = parts[0].toLowerCase(Locale.ROOT);
         if (!SAFE_ROOTS.contains(root)) {
            return Optional.empty();
         }

         for (int index = 1; index < parts.length; index++) {
            if (!ARGUMENT.matcher(parts[index]).matches()) {
               return Optional.empty();
            }
         }

         if (!hasSafeArity(root, parts.length - 1)) {
            return Optional.empty();
         }

         StringBuilder normalized = new StringBuilder(root);

         for (int index = 1; index < parts.length; index++) {
            normalized.append(' ').append(parts[index]);
         }

         return Optional.of(normalized.toString());
      } else {
         return Optional.empty();
      }
   }

   private static boolean hasControlCharacter(String value) {
      return value.codePoints().anyMatch(Character::isISOControl);
   }

   private static boolean hasSafeArity(String root, int argumentCount) {
      return switch (root) {
         case "an", "anarchy", "server" -> argumentCount == 1;
         case "home" -> argumentCount <= 1;
         default -> argumentCount == 0;
      };
   }
}
