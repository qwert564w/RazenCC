package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2848;
import net.minecraft.class_746;
import net.minecraft.class_2848.class_2849;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public final class ElytraBounceFeature extends Feature implements MinecraftContext {
   private static final String MODE_NEW = "New";
   private static final String MODE_OLD = "Old";
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "New", "New", "Old"));

   public ElytraBounceFeature() {
      super("ElytraBounce", "Bounces off the ground back into an elytra glide", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && mc.field_1687 != null) {
         if (!this.inWater(player) && this.hasHeadroom(player) && this.hasElytra(player)) {
            if (!this.mode.is("Old") || this.isMoving(player)) {
               if (player.method_24828()) {
                  player.method_6043();
               } else if (!player.method_6128() && !this.inWater(player) && this.canBounce(player)) {
                  this.deploy(player);
               }
            }
         }
      }
   }

   private void deploy(class_746 player) {
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2848(player, class_2849.field_12982));
         player.method_23668();
      }
   }

   private boolean isMoving(class_746 player) {
      return player.field_3913.method_3128().method_35587() > 0.0F;
   }

   private boolean inWater(class_746 player) {
      return player.method_5799() || player.method_5869() || player.method_5681();
   }

   private boolean hasHeadroom(class_746 player) {
      class_2338 base = player.method_24515();

      for (int i = 1; i <= 2; i++) {
         class_2338 pos = base.method_10086(i);
         if (!mc.field_1687.method_8320(pos).method_26220(mc.field_1687, pos).method_1110()) {
            return false;
         }
      }

      return true;
   }

   private boolean hasElytra(class_746 player) {
      return player.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833);
   }

   private boolean canBounce(class_746 player) {
      if (this.hasElytra(player) && !player.method_5869() && !player.method_5771() && !player.method_5765()) {
         class_1799 chest = player.method_6118(class_1304.field_6174);
         return chest.method_31574(class_1802.field_8833) && chest.method_7919() < chest.method_7936() - 1;
      } else {
         return false;
      }
   }
}
