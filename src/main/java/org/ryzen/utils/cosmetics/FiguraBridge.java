package org.ryzen.utils.cosmetics;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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
   private static Object avatarManagerInstance;
   private static String appliedId = "";

   private FiguraBridge() {
   }

   public static boolean isAvailable() {
      if (available == null) {
         available = resolve();
      }
      return available;
   }

   private static Method findMethod(Class<?> clazz, String name, Class<?>[]... signatures) {
      for (Class<?>[] sig : signatures) {
         try {
            return clazz.getMethod(name, sig);
         } catch (NoSuchMethodException ignored) {
         }
      }
      for (Method m : clazz.getDeclaredMethods()) {
         if (m.getName().equals(name)) {
            LOGGER.debug("FiguraBridge: found method {} via declared scan, params={}", name, java.util.Arrays.toString(m.getParameterTypes()));
            m.setAccessible(true);
            return m;
         }
      }
      return null;
   }

   private static boolean resolve() {
      if (!FabricLoader.getInstance().isModLoaded("figura")) {
         LOGGER.debug("FiguraBridge: figura mod not loaded");
         return false;
      }

      try {
         Class<?> manager = Class.forName(AVATAR_MANAGER);

         loadLocalAvatar = findMethod(manager, "loadLocalAvatar",
            new Class<?>[]{Path.class},
            new Class<?>[]{Path.class, boolean.class});

         clearAvatars = findMethod(manager, "clearAvatars",
            new Class<?>[]{UUID.class},
            new Class<?>[]{});

         if (loadLocalAvatar == null) {
            LOGGER.error("FiguraBridge: could not find loadLocalAvatar method in AvatarManager");
            return false;
         }

         LOGGER.debug("FiguraBridge: loadLocalAvatar found: static={}, params={}",
            Modifier.isStatic(loadLocalAvatar.getModifiers()),
            java.util.Arrays.toString(loadLocalAvatar.getParameterTypes()));

         if (clearAvatars != null) {
            LOGGER.debug("FiguraBridge: clearAvatars found: static={}, params={}",
               Modifier.isStatic(clearAvatars.getModifiers()),
               java.util.Arrays.toString(clearAvatars.getParameterTypes()));
         }

         if (!Modifier.isStatic(loadLocalAvatar.getModifiers()) ||
             (clearAvatars != null && !Modifier.isStatic(clearAvatars.getModifiers()))) {
            try {
               Method getInstance = manager.getMethod("getInstance");
               avatarManagerInstance = getInstance.invoke(null);
               LOGGER.debug("FiguraBridge: obtained AvatarManager instance via getInstance()");
            } catch (Exception e) {
               try {
                  Field instanceField = manager.getField("INSTANCE");
                  avatarManagerInstance = instanceField.get(null);
                  LOGGER.debug("FiguraBridge: obtained AvatarManager instance via INSTANCE field");
               } catch (Exception e2) {
                  LOGGER.warn("FiguraBridge: could not obtain AvatarManager instance", e2);
               }
            }
         }

         return true;
      } catch (Exception exception) {
         LOGGER.error("FiguraBridge: resolve failed", exception);
         loadLocalAvatar = null;
         clearAvatars = null;
         avatarManagerInstance = null;
         return false;
      }
   }

   public static boolean disablePopupMenu() {
      if (!FabricLoader.getInstance().isModLoaded("figura")) {
         return false;
      }
      try {
         Object popupButton = Class.forName(CONFIGS).getField("POPUP_BUTTON").get(null);
         if (popupButton.getClass().getField("keyBind").get(popupButton) instanceof class_304 mapping && !mapping.method_1415()) {
            mapping.method_1422(class_3675.field_16237);
            class_304.method_1426();
            return true;
         } else {
            return false;
         }
      } catch (Exception exception) {
         LOGGER.debug("Could not unbind the Figura popup menu", exception);
         return false;
      }
   }

   public static boolean disableFirstPersonMatrices() {
      if (!FabricLoader.getInstance().isModLoaded("figura")) {
         return false;
      }
      try {
         Object config = Class.forName(CONFIGS).getField("FIRST_PERSON_MATRICES").get(null);
         Field value = config.getClass().getField("value");
         if (Boolean.FALSE.equals(value.get(config))) {
            return false;
         }
         value.set(config, Boolean.FALSE);
         return true;
      } catch (Exception exception) {
         LOGGER.debug("Could not turn off Figura's first-person matrices", exception);
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
         LOGGER.error("Attempted to apply null cosmetic entry.");
         return false;
      }

      java.nio.file.Path avatarFile = entry.folder().resolve("avatar.json");
      if (!java.nio.file.Files.isRegularFile(avatarFile)) {
         LOGGER.error("Model not found! Missing avatar.json for cosmetic '{}' at {}", entry.id(), avatarFile.toAbsolutePath());
         return false;
      }

      if (!isAvailable()) {
         LOGGER.error("Failed to apply cosmetic '{}': Figura is not available or installed.", entry.id());
         return false;
      }

      try {
         LOGGER.info("Found and loading model for cosmetic '{}' from {}", entry.id(), entry.folder().toAbsolutePath());
         CosmeticFirstPerson.repair(entry.folder());
         if (entry.kind() != CosmeticEntry.Kind.WEAPON) {
            CosmeticFirstPerson.installHide(entry.folder());
         }

         Object target = Modifier.isStatic(loadLocalAvatar.getModifiers()) ? null : avatarManagerInstance;
         Class<?>[] paramTypes = loadLocalAvatar.getParameterTypes();

         LOGGER.debug("FiguraBridge: invoking loadLocalAvatar with target={}, paramCount={}", target, paramTypes.length);

         if (paramTypes.length == 1) {
            loadLocalAvatar.invoke(target, entry.folder());
         } else if (paramTypes.length == 2) {
            loadLocalAvatar.invoke(target, entry.folder(), true);
         } else {
            LOGGER.error("FiguraBridge: unexpected loadLocalAvatar param count: {}", paramTypes.length);
            return false;
         }

         appliedId = entry.id();
         LOGGER.info("Successfully loaded avatar for cosmetic '{}'.", entry.id());
         return true;
      } catch (Exception exception) {
         LOGGER.error("Failed to apply cosmetic '{}' due to reflection or loading error.", entry.id(), exception);
         return false;
      }
   }

   public static boolean clear() {
      appliedId = "";
      if (isAvailable() && MinecraftContext.mc.field_1724 != null) {
         if (clearAvatars == null) {
            LOGGER.warn("FiguraBridge: clearAvatars method not found, skipping clear");
            return true;
         }
         try {
            Object target = Modifier.isStatic(clearAvatars.getModifiers()) ? null : avatarManagerInstance;
            Class<?>[] paramTypes = clearAvatars.getParameterTypes();

            if (paramTypes.length == 1) {
               clearAvatars.invoke(target, MinecraftContext.mc.field_1724.method_5667());
            } else if (paramTypes.length == 0) {
               clearAvatars.invoke(target);
            } else {
               LOGGER.error("FiguraBridge: unexpected clearAvatars param count: {}", paramTypes.length);
               return false;
            }
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
