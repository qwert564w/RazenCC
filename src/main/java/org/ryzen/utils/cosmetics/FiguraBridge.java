package org.ryzen.utils.cosmetics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import org.ryzen.context.MinecraftContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class FiguraBridge {
   private static final Logger LOGGER = LoggerFactory.getLogger(FiguraBridge.class);
   private static final String MOD_ID = "figura";
   private static final String AVATAR_MANAGER = "org.figuramc.figura.avatar.AvatarManager";
   private static final String CONFIGS = "org.figuramc.figura.config.Configs";
   private static Boolean available;
   private static Method loadLocalAvatar;
   private static Method clearAvatars;
   private static String appliedId = "";

   private FiguraBridge() {
   }

   public static boolean isAvailable() {
      if (available == null) {
         available = resolve();
      }

      return available;
   }

   private static boolean resolve() {
      if (!FabricLoader.getInstance().isModLoaded("figura")) {
         return false;
      }

      try {
         Class<?> manager = Class.forName("org.figuramc.figura.avatar.AvatarManager");
         loadLocalAvatar = manager.getMethod("loadLocalAvatar", Path.class);
         clearAvatars = manager.getMethod("clearAvatars", UUID.class);
         return true;
      } catch (Exception exception) {
         LOGGER.error("Failed to resolve Figura AvatarManager methods", exception);
         loadLocalAvatar = null;
         clearAvatars = null;
         return false;
      }
   }

   public static boolean disablePopupMenu() {
      if (!FabricLoader.getInstance().isModLoaded("figura")) {
         return false;
      }

      try {
         Object popupButton = Class.forName("org.figuramc.figura.config.Configs").getField("POPUP_BUTTON").get(null);
         if (popupButton.getClass().getField("keyBind").get(popupButton) instanceof class_304 mapping && !mapping.method_1415()) {
            mapping.method_1422(class_3675.field_16237);
            class_304.method_1426();
            return true;
         } else {
            return false;
         }
      } catch (Exception exception) {
         LOGGER.error("Could not unbind the Figura popup menu", exception);
         return false;
      }
   }

   public static boolean disableFirstPersonMatrices() {
      if (!FabricLoader.getInstance().isModLoaded("figura")) {
         return false;
      }

      try {
         Object config = Class.forName("org.figuramc.figura.config.Configs").getField("FIRST_PERSON_MATRICES").get(null);
         Field value = config.getClass().getField("value");
         if (Boolean.FALSE.equals(value.get(config))) {
            return false;
         }

         value.set(config, Boolean.FALSE);
         return true;
      } catch (Exception exception) {
         LOGGER.error("Could not turn off Figura's first-person matrices", exception);
         return false;
      }
   }

   public static String appliedId() {
      return appliedId;
   }

   public static boolean isApplied(CosmeticEntry entry) {
      return entry != null && entry.id().equals(appliedId);
   }

   public static boolean apply(CosmeticEntry entry) {
      if (entry == null) {
         LOGGER.error("Attempted to apply a null cosmetic entry.");
         return false;
      }

      Path avatarFile = entry.folder().resolve("avatar.json");
      if (!Files.isRegularFile(avatarFile)) {
         LOGGER.error("Model not found! Missing avatar.json for cosmetic '{}' at {}", entry.id(), avatarFile.toAbsolutePath());
         return false;
      }

      if (isAvailable()) {
         try {
            LOGGER.info("Found model for cosmetic '{}' at {}", entry.id(), entry.folder().toAbsolutePath());
            CosmeticFirstPerson.repair(entry.folder());
            if (entry.kind() != CosmeticEntry.Kind.WEAPON) {
               CosmeticFirstPerson.installHide(entry.folder());
            }

            loadLocalAvatar.invoke(null, entry.folder());
            appliedId = entry.id();
            LOGGER.info("Successfully loaded avatar for cosmetic '{}'.", entry.id());
            return true;
         } catch (Exception exception) {
            LOGGER.error("Failed to apply cosmetic '{}' due to a reflection or loading error.", entry.id(), exception);
            return false;
         }
      } else {
         LOGGER.error("Failed to apply cosmetic '{}': Figura is not available or installed.", entry.id());
         return false;
      }
   }

   public static boolean clear() {
      appliedId = "";
      if (isAvailable() && MinecraftContext.mc.field_1724 != null) {
         try {
            clearAvatars.invoke(null, MinecraftContext.mc.field_1724.method_5667());
            return true;
         } catch (Exception exception) {
            LOGGER.error("Failed to clear the applied cosmetic", exception);
            return false;
         }
      } else {
         return false;
      }
   }
}
