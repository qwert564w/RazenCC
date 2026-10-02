package org.ryzen.menu.i18n;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1076;
import net.minecraft.class_310;
import org.ryzen.menu.core.MenuConfigStore;

@Environment(EnvType.CLIENT)
public enum UiLanguage {
   ENGLISH("english", "English", "en_us"),
   RUSSIAN("russian", "Русский", "ru_ru"),
   UKRAINIAN("ukrainian", "Українська", "uk_ua"),
   KAZAKH("kazakh", "Қазақша", "kk_kz"),
   POLISH("polish", "Polski", "pl_pl");

   private static final UiLanguage[] VALUES = values();
   private static UiLanguage current = ENGLISH;
   private final String storageKey;
   private final String canonicalName;
   private final String minecraftCode;

   UiLanguage(String storageKey, String canonicalName, String minecraftCode) {
      this.storageKey = storageKey;
      this.canonicalName = canonicalName;
      this.minecraftCode = minecraftCode;
   }

   public String storageKey() {
      return this.storageKey;
   }

   public String canonicalName() {
      return this.canonicalName;
   }

   public String minecraftCode() {
      return this.minecraftCode;
   }

   public static UiLanguage current() {
      return current;
   }

   public static void set(UiLanguage language) {
      current = language == null ? ENGLISH : language;
   }

   public static void select(UiLanguage language) {
      UiLanguage next = language == null ? ENGLISH : language;
      set(next);
      MenuConfigStore.save(data -> data.addProperty("language", next.storageKey()));
      applyToMinecraft(next);
   }

   private static void applyToMinecraft(UiLanguage language) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft != null && minecraft.field_1690 != null) {
         class_1076 manager = minecraft.method_1526();
         String code = language.minecraftCode;
         if (manager != null && manager.method_4668(code) != null && !code.equals(manager.method_4669())) {
            manager.method_4667(code);
            minecraft.field_1690.field_1883 = code;
            minecraft.field_1690.method_1640();
            minecraft.method_1521();
         }
      }
   }

   public static void loadSaved() {
      String stored = MenuConfigStore.getString("language", "");
      if (!stored.isBlank()) {
         set(byStorageKey(stored));
      } else {
         set(byIndex(MenuConfigStore.getInt("languageMode", 0)));
      }
   }

   public static UiLanguage byIndex(int index) {
      return index >= 0 && index < VALUES.length ? VALUES[index] : ENGLISH;
   }

   public static UiLanguage byStorageKey(String key) {
      for (UiLanguage language : VALUES) {
         if (language.storageKey.equalsIgnoreCase(key)) {
            return language;
         }
      }

      return ENGLISH;
   }

   public static String[] canonicalNames() {
      String[] names = new String[VALUES.length];

      for (int i = 0; i < VALUES.length; i++) {
         names[i] = VALUES[i].canonicalName;
      }

      return names;
   }
}
