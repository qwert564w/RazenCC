package org.ryzen.utils;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1132;
import net.minecraft.class_310;
import net.minecraft.class_320;
import net.minecraft.class_412;
import net.minecraft.class_442;
import net.minecraft.class_638;
import net.minecraft.class_639;
import net.minecraft.class_642;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.mixin.accessor.MinecraftAccessor;
import org.ryzen.mixin.accessor.MinecraftServerAccessor;

@Environment(EnvType.CLIENT)
public final class AccountSwitcher {
   public static final String DEFAULT_NAME = "Ryzen";
   private static boolean startupApplied;

   private AccountSwitcher() {
   }

   public static void applyStartupAccount() {
      if (!startupApplied) {
         startupApplied = true;
         String name = MenuConfigStore.getString("selectedAccount", "");
         if (name.isEmpty() || !name.matches("^[a-zA-Z0-9_]{3,16}$")) {
            name = "Ryzen";
         }

         switchTo(name);
      }
   }

   public static void switchTo(String name) {
      class_310 mc = class_310.method_1551();
      UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      class_320 user = new class_320(name, uuid, "0", Optional.empty(), Optional.empty());
      MinecraftAccessor accessor = (MinecraftAccessor)mc;
      accessor.setUser(user);
      accessor.setProfileFuture(CompletableFuture.completedFuture(new ProfileResult(new GameProfile(uuid, name))));
      mc.method_24288();
   }

   public static void relogin(String name) {
      class_310 mc = class_310.method_1551();
      class_642 server = mc.method_1558();
      String levelId = null;
      class_1132 integrated = mc.method_1576();
      if (mc.method_1542() && integrated != null) {
         levelId = ((MinecraftServerAccessor)integrated).getStorageSource().method_27005();
      }

      switchTo(name);
      if (mc.field_1687 != null) {
         mc.method_73360(class_638.field_61021);
         if (levelId != null) {
            mc.method_41735().method_57784(levelId, () -> mc.method_29970(new class_442()));
         } else if (server != null && !server.method_2994() && !server.method_52811()) {
            class_412.method_36877(new class_442(), mc, class_639.method_2950(server.field_3761), server, false, null);
         }
      }
   }
}
