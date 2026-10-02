package org.ryzen.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.setting.BindSetting;

@Environment(EnvType.CLIENT)
public final class KeyNames {
   private static final Map<String, Integer> SPECIAL = Map.ofEntries(
      Map.entry("SPACE", 32),
      Map.entry("TAB", 258),
      Map.entry("ENTER", 257),
      Map.entry("BACKSPACE", 259),
      Map.entry("INSERT", 260),
      Map.entry("DELETE", 261),
      Map.entry("HOME", 268),
      Map.entry("END", 269),
      Map.entry("PAGEUP", 266),
      Map.entry("PAGEDOWN", 267),
      Map.entry("UP", 265),
      Map.entry("DOWN", 264),
      Map.entry("LEFT", 263),
      Map.entry("RIGHT", 262),
      Map.entry("LSHIFT", 340),
      Map.entry("RSHIFT", 344),
      Map.entry("LCTRL", 341),
      Map.entry("RCTRL", 345),
      Map.entry("LALT", 342),
      Map.entry("RALT", 346),
      Map.entry("CAPSLOCK", 280),
      Map.entry("GRAVE", 96),
      Map.entry("MINUS", 45),
      Map.entry("EQUAL", 61),
      Map.entry("COMMA", 44),
      Map.entry("PERIOD", 46),
      Map.entry("SLASH", 47),
      Map.entry("SEMICOLON", 59),
      Map.entry("APOSTROPHE", 39),
      Map.entry("LBRACKET", 91),
      Map.entry("RBRACKET", 93),
      Map.entry("BACKSLASH", 92)
   );
   private static final List<String> SUGGESTIONS = createSuggestions();

   public static List<String> suggestions() {
      return SUGGESTIONS;
   }

   public static int parse(String raw) {
      if (raw != null && !raw.isBlank()) {
         String name = raw.trim().toUpperCase(Locale.ROOT).replace("_", "");
         int mouse = parseMouse(name);
         if (mouse != -1) {
            return mouse;
         }

         if (name.length() == 1) {
            char c = name.charAt(0);
            if (c >= 'A' && c <= 'Z' || c >= '0' && c <= '9') {
               return BindSetting.key(c);
            }
         }

         if (name.length() >= 2 && name.charAt(0) == 'F') {
            try {
               int fn = Integer.parseInt(name.substring(1));
               if (fn >= 1 && fn <= 25) {
                  return BindSetting.key(290 + fn - 1);
               }
            } catch (NumberFormatException var4) {
            }
         }

         Integer special = SPECIAL.get(name);
         return special != null ? BindSetting.key(special) : -1;
      } else {
         return -1;
      }
   }

   private static int parseMouse(String name) {
      return switch (name) {
         case "LMB", "MOUSELEFT", "MOUSE1" -> BindSetting.mouse(0);
         case "RMB", "MOUSERIGHT", "MOUSE2" -> BindSetting.mouse(1);
         case "MMB", "MOUSEMIDDLE", "MOUSE3" -> BindSetting.mouse(2);
         case "MOUSE4", "M4" -> BindSetting.mouse(3);
         case "MOUSE5", "M5" -> BindSetting.mouse(4);
         case "MOUSE6" -> BindSetting.mouse(5);
         case "MOUSE7" -> BindSetting.mouse(6);
         case "MOUSE8" -> BindSetting.mouse(7);
         default -> -1;
      };
   }

   private static List<String> createSuggestions() {
      List<String> names = new ArrayList<>();

      for (char key = 'A'; key <= 'Z'; key++) {
         names.add(String.valueOf(key));
      }

      for (char key = '0'; key <= '9'; key++) {
         names.add(String.valueOf(key));
      }

      for (int key = 1; key <= 25; key++) {
         names.add("F" + key);
      }

      SPECIAL.keySet().stream().sorted(Comparator.naturalOrder()).forEach(names::add);
      names.addAll(List.of("LMB", "RMB", "MMB", "MOUSE4", "MOUSE5", "MOUSE6", "MOUSE7", "MOUSE8"));
      return List.copyOf(names);
   }

   @Generated
   private KeyNames() {
      throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
   }
}
