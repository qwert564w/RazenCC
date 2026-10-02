package org.ryzen.utils.cosmetics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
final class CosmeticFirstPerson {
   private static final Logger LOGGER = LoggerFactory.getLogger(CosmeticFirstPerson.class);
   private static final String AVATAR_FILE = "avatar.json";
   private static final String AUTO_SCRIPTS = "autoScripts";
   private static final List<String> OWN_SCRIPTS = List.of("ryzen_fit", "ryzen_first_person");
   private static final String BLOCK_START = "-- >>> ryzen first person";
   private static final String BLOCK_END = "-- <<< ryzen first person";
   private static final String MAIN_SCRIPT = "script.lua";

   private CosmeticFirstPerson() {
   }

   static void installHide(Path folder) {
      if (folder != null) {
         try {
            if (!Files.isRegularFile(folder.resolve("avatar.json"))) {
               return;
            }

            Path script = folder.resolve("script.lua");
            String existing = Files.exists(script) ? Files.readString(script, StandardCharsets.UTF_8) : "";
            String body = stripBlock(existing);
            Files.writeString(script, body + hideBlock(), StandardCharsets.UTF_8);
         } catch (Exception exception) {
            LOGGER.warn("Could not add the first-person hide to {}", folder, exception);
         }
      }
   }

   private static String stripBlock(String script) {
      int start = script.indexOf("-- >>> ryzen first person");
      if (start < 0) {
         return !script.isEmpty() && !script.endsWith("\n") ? script + "\n" : script;
      }

      int end = script.indexOf("-- <<< ryzen first person", start);
      String tail = end < 0 ? "" : script.substring(end + "-- <<< ryzen first person".length());
      String head = script.substring(0, start);
      String joined = head + tail;
      return !joined.isEmpty() && !joined.endsWith("\n") ? joined + "\n" : joined;
   }

   private static String hideBlock() {
      return "-- >>> ryzen first person\n-- Written by Ryzen. Hides this avatar from its wearer's own\n-- camera; other players keep seeing it. Registered rather than\n-- assigned, so the pack's own tick handler keeps running.\nlocal ryzenHidden = nil\n\nevents.TICK:register(function()\n    if not host:isHost() then\n        return\n    end\n    local hide = renderer:isFirstPerson()\n    if hide ~= ryzenHidden then\n        ryzenHidden = hide\n        models:setVisible(not hide)\n    end\nend)\n-- <<< ryzen first person\n";
   }

   static void repair(Path folder) {
      if (folder != null) {
         try {
            Path avatarFile = folder.resolve("avatar.json");
            if (!Files.isRegularFile(avatarFile)) {
               return;
            }

            for (String name : OWN_SCRIPTS) {
               Files.deleteIfExists(folder.resolve(name + ".lua"));
            }

            repairAvatar(folder, avatarFile);
         } catch (Exception exception) {
            LOGGER.warn("Could not clean Ryzen's edits out of {}", folder, exception);
         }
      }
   }

   private static void repairAvatar(Path folder, Path avatarFile) throws Exception {
      JsonObject avatar = readAvatar(avatarFile);
      if (avatar != null && avatar.has("autoScripts") && avatar.get("autoScripts").isJsonArray()) {
         JsonArray scripts = avatar.getAsJsonArray("autoScripts");
         JsonArray kept = new JsonArray();
         boolean removed = false;

         for (JsonElement element : scripts) {
            if (element.isJsonPrimitive() && OWN_SCRIPTS.contains(element.getAsString())) {
               removed = true;
            } else {
               kept.add(element);
            }
         }

         if (removed) {
            if (matchesEveryScript(folder, kept)) {
               avatar.remove("autoScripts");
            } else {
               avatar.add("autoScripts", kept);
            }

            Files.writeString(avatarFile, avatar.toString(), StandardCharsets.UTF_8);
         }
      }
   }

   private static boolean matchesEveryScript(Path folder, JsonArray scripts) throws Exception {
      List<String> present = new ArrayList<>();

      try (Stream<Path> files = Files.walk(folder)) {
         files.filter(x$0 -> Files.isRegularFile(x$0)).filter(path -> path.getFileName().toString().endsWith(".lua")).forEach(path -> {
            String relative = folder.relativize(path).toString().replace('\\', '/');
            present.add(relative.substring(0, relative.length() - ".lua".length()));
         });
      }

      if (present.size() != scripts.size()) {
         return false;
      }

      for (JsonElement element : scripts) {
         if (!element.isJsonPrimitive() || !present.contains(element.getAsString())) {
            return false;
         }
      }

      return true;
   }

   private static JsonObject readAvatar(Path avatarFile) {
      try {
         JsonElement parsed = JsonParser.parseString(Files.readString(avatarFile, StandardCharsets.UTF_8));
         return parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
      } catch (Exception exception) {
         LOGGER.warn("Avatar descriptor {} is not readable JSON; left alone", avatarFile);
         return null;
      }
   }
}
