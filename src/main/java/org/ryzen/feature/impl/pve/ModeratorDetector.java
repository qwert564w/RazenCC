package org.ryzen.feature.impl.pve;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_310;
import net.minecraft.class_640;
import net.minecraft.class_746;
import org.ryzen.utils.StaffManager;

@Environment(EnvType.CLIENT)
public final class ModeratorDetector {
   private static final Pattern FORMATTING_CODE = Pattern.compile("(?i)§[0-9A-FK-ORX]");
   private static final Pattern ROLE = Pattern.compile(
      "(?<![\\p{L}\\p{N}])(?:admin(?:istrator)?|moderator|mod|staff|helper|curator|админ(?:истратор)?|модер(?:атор)?|хелпер|куратор|персонал|стаж[её]р)(?![\\p{L}\\p{N}])",
      66
   );

   private ModeratorDetector() {
   }

   public static Optional<String> find(class_310 client, class_746 self) {
      if (client != null && self != null && client.method_1562() != null) {
         for (class_640 info : client.method_1562().method_2880()) {
            if (!self.method_5667().equals(info.method_2966().id())) {
               String name = info.method_2966().name();
               if ((containsRole(roleText(info)) || StaffManager.INSTANCE.isStaff(name)) && name != null) {
                  return Optional.of(name);
               }
            }
         }

         if (client.field_1687 != null) {
            for (class_1657 player : client.field_1687.method_18456()) {
               if (player != self && !self.method_5667().equals(player.method_5667()) && containsRole(roleText(player.method_5476(), player.method_5781()))) {
                  String name = player.method_7334().name();
                  if (name != null) {
                     return Optional.of(name);
                  }
               }
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public static boolean containsRole(String value) {
      if (value != null && !value.isBlank()) {
         String normalized = Normalizer.normalize(value, Form.NFKC);
         normalized = FORMATTING_CODE.matcher(normalized).replaceAll("");
         normalized = normalized.toLowerCase(Locale.ROOT);
         return ROLE.matcher(normalized).find();
      } else {
         return false;
      }
   }

   private static String roleText(class_640 info) {
      return roleText(info.method_2971(), info.method_2955());
   }

   private static String roleText(class_2561 displayName, class_268 team) {
      StringBuilder text = new StringBuilder();
      append(text, displayName);
      if (team != null) {
         append(text, team.method_1144());
         append(text, team.method_1136());
         text.append(' ').append(team.method_1197());
      }

      return text.toString();
   }

   private static void append(StringBuilder target, class_2561 component) {
      if (component != null) {
         target.append(' ').append(component.getString());
      }
   }
}
