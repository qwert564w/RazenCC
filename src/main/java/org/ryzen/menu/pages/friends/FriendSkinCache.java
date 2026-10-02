package org.ryzen.menu.pages.friends;

import com.mojang.authlib.GameProfile;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1068;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_640;
import net.minecraft.class_8685;

@Environment(EnvType.CLIENT)
final class FriendSkinCache {
   private static final ConcurrentHashMap<String, Supplier<class_8685>> SKINS = new ConcurrentHashMap<>();
   private static final Set<String> RESOLVING = ConcurrentHashMap.newKeySet();

   private FriendSkinCache() {
   }

   static class_2960 texture(class_310 minecraft, String name) {
      class_640 online = onlineInfo(minecraft, name);
      String key = normalize(name);
      if (online != null) {
         Supplier<class_8685> skin = online::method_52810;
         SKINS.put(key, skin);
         return skin.get().comp_1626().comp_3627();
      } else {
         Supplier<class_8685> fallback = SKINS.computeIfAbsent(key, ignored -> defaultSkin(name));
         resolve(minecraft, name, key);
         return fallback.get().comp_1626().comp_3627();
      }
   }

   static boolean isOnline(class_310 minecraft, String name) {
      return onlineInfo(minecraft, name) != null;
   }

   private static class_640 onlineInfo(class_310 minecraft, String name) {
      return minecraft.method_1562() == null
         ? null
         : minecraft.method_1562().method_2880().stream().filter(info -> info.method_2966().name().equalsIgnoreCase(name)).findFirst().orElse(null);
   }

   private static void resolve(class_310 minecraft, String name, String key) {
      if (RESOLVING.add(key)) {
         CompletableFuture.<Optional<GameProfile>>supplyAsync(() -> minecraft.method_73361().comp_4624().method_73289(name), class_156.method_55473())
            .thenAccept(
               profile -> profile.ifPresent(resolved -> minecraft.execute(() -> SKINS.put(key, minecraft.method_1582().method_73544((GameProfile) resolved, false))))
            )
            .whenComplete((unused, throwable) -> RESOLVING.remove(key));
      }
   }

   private static Supplier<class_8685> defaultSkin(String name) {
      UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
      GameProfile profile = new GameProfile(uuid, name);
      class_8685 skin = class_1068.method_52854(profile);
      return () -> skin;
   }

   private static String normalize(String name) {
      return name.toLowerCase(Locale.ROOT);
   }
}
