package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1536;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public final class NoPushFeature extends Feature {
   public final BooleanSetting entity = this.register(new BooleanSetting("Entity", true));
   public final BooleanSetting blocks = this.register(new BooleanSetting("Blocks", true));
   public final BooleanSetting water = this.register(new BooleanSetting("Water", true));
   public final BooleanSetting fishingHook = this.register(new BooleanSetting("Fishing Hook", true));

   public NoPushFeature() {
      super("NoPush", "Prevents the player from being pushed", FeatureCategory.MOVEMENT, -1);
   }

   public static NoPushFeature getInstance() {
      return FeatureManager.INSTANCE.getFeature(NoPushFeature.class);
   }

   public static NoPushFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(NoPushFeature.class);
   }

   public static boolean shouldCancelEntityPush(class_1297 self, class_1297 other) {
      NoPushFeature feature = getEnabled();
      if (feature != null && feature.entity.getValue()) {
         class_1657 player = localPlayer();
         return player != null && self == player && other != player;
      } else {
         return false;
      }
   }

   public static boolean shouldCancelBlockPush(class_1657 player, class_2680 state) {
      NoPushFeature feature = getEnabled();
      if (feature == null || !feature.blocks.getValue()) {
         return false;
      }

      if (state != null && state.method_27852(class_2246.field_10343)) {
         return false;
      }

      class_1657 local = localPlayer();
      return local != null && player == local && state != null && !state.method_26215();
   }

   public static boolean shouldCancelClosestSpacePush(class_746 player) {
      NoPushFeature feature = getEnabled();
      if (feature != null && feature.blocks.getValue()) {
         class_1657 local = localPlayer();
         return local != null && player == local;
      } else {
         return false;
      }
   }

   public static boolean shouldCancelFluidPush(class_1657 player) {
      NoPushFeature feature = getEnabled();
      if (feature != null && feature.water.getValue()) {
         class_1657 local = localPlayer();
         return local != null && player == local;
      } else {
         return false;
      }
   }

   public static boolean shouldCancelFishingHookPull(class_1536 hook, class_1297 target) {
      NoPushFeature feature = getEnabled();
      if (feature != null && feature.fishingHook.getValue()) {
         class_1657 local = localPlayer();
         return local != null && target == local && hook != null && hook.method_24921() != local;
      } else {
         return false;
      }
   }

   private static class_1657 localPlayer() {
      return class_310.method_1551().field_1724;
   }
}
