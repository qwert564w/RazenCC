package org.ryzen.feature;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.impl.pve.PveManagerFeature;

@Environment(EnvType.CLIENT)
final class PveManagerConfigMigration {
   private PveManagerConfigMigration() {
   }

   static boolean migrate(JsonObject features, PveManagerFeature manager) {
      if (features != null && manager != null && !features.has(manager.getName())) {
         boolean migrated = false;
         Boolean rotate = booleanValue(features, "Nuker", "Rotate");
         if (rotate != null) {
            manager.rotate.setValue(rotate);
            migrated = true;
         }

         String server = preferredServer(stringValue(features, "BaseFinder", "Server Mode"), stringValue(features, "MineHelper", "Server Mode"));
         if (server != null) {
            manager.serverProfile.setValue(server);
            migrated = true;
         }

         String home = firstNonBlank(stringValue(features, "AppleFarmer", "Home Name"));
         if (home != null) {
            manager.homeName.setValue(home);
            migrated = true;
         }

         Double anarchy = numberValue(features, "BaseFinder", "Anarchy");
         if (anarchy != null) {
            manager.anarchy.setValue(anarchy);
            migrated = true;
         }

         Double minimumHealth = maximum(numberValue(features, "BaseFinder", "Minimum Health"));
         if (minimumHealth != null) {
            manager.minimumHealth.setValue(minimumHealth);
            migrated = true;
         }

         Double minimumDurability = maximum(
            numberValue(features, "BaseFinder", "Minimum Pickaxe Durability"), numberValue(features, "MineHelper", "Pickaxe Guard")
         );
         if (minimumDurability != null) {
            manager.minimumToolDurability.setValue(minimumDurability);
            migrated = true;
         }

         Double baseFinderRadius = numberValue(features, "BaseFinder", "Avoid Players");
         if (baseFinderRadius != null) {
            manager.playerRadius.setValue(baseFinderRadius);
            manager.pauseNearPlayers.setValue(baseFinderRadius > 0.0);
            migrated = true;
         }

         return migrated;
      } else {
         return false;
      }
   }

   private static String preferredServer(String... values) {
      String auto = null;

      for (String value : values) {
         if (value != null && !value.isBlank()) {
            String canonical = canonicalServer(value);
            if (canonical != null) {
               if (!"Auto".equals(canonical)) {
                  return canonical;
               }

               auto = canonical;
            }
         }
      }

      return auto;
   }

   private static String canonicalServer(String value) {
      return switch (value.trim().toLowerCase(Locale.ROOT)) {
         case "auto" -> "Auto";
         case "generic" -> "Generic";
         case "funtime" -> "FunTime";
         case "holyworld" -> "HolyWorld";
         case "reallyworld" -> "ReallyWorld";
         default -> null;
      };
   }

   private static String firstNonBlank(String... values) {
      for (String value : values) {
         if (value != null && !value.isBlank()) {
            return value.trim();
         }
      }

      return null;
   }

   private static Double maximum(Double... values) {
      List<Double> valid = new ArrayList<>();

      for (Double value : values) {
         if (value != null && Double.isFinite(value)) {
            valid.add(value);
         }
      }

      return valid.stream().max(Double::compareTo).orElse(null);
   }

   private static Boolean booleanValue(JsonObject features, String feature, String setting) {
      JsonElement value = setting(features, feature, setting);
      return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isBoolean() ? value.getAsBoolean() : null;
   }

   private static Double numberValue(JsonObject features, String feature, String setting) {
      JsonElement value = setting(features, feature, setting);
      if (value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isNumber()) {
         double number = value.getAsDouble();
         return Double.isFinite(number) ? number : null;
      } else {
         return null;
      }
   }

   private static String stringValue(JsonObject features, String feature, String setting) {
      JsonElement value = setting(features, feature, setting);
      return value != null && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString() ? value.getAsString() : null;
   }

   private static JsonElement setting(JsonObject features, String feature, String setting) {
      JsonElement featureElement = features.get(feature);
      if (featureElement != null && featureElement.isJsonObject()) {
         JsonElement settingsElement = featureElement.getAsJsonObject().get("settings");
         return settingsElement != null && settingsElement.isJsonObject() ? settingsElement.getAsJsonObject().get(setting) : null;
      } else {
         return null;
      }
   }
}
