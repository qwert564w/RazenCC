package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class OffhandGappleFeature extends Feature {
   public final NumberSetting health = this.register(new NumberSetting("Health", 14.0, 1.0, 20.0, 0.5, " HP"));
   public final BooleanSetting countAbsorption = this.register(new BooleanSetting("Golden Hearts", false));
   private boolean holdingUse;

   public OffhandGappleFeature() {
      super("Auto GApple", "Eats the golden apple already held in your offhand at low health", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.release();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.release();
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPost()) {
         class_746 player = event.getPlayer();
         if (player != null && isApple(player.method_6079()) && !(this.effectiveHealth(player) > this.health.getValue())) {
            class_310 client = class_310.method_1551();
            this.holdingUse = true;
            if (client.field_1755 != null) {
               if (!player.method_6115() && client.field_1761 != null) {
                  client.field_1761.method_2919(player, class_1268.field_5810);
               }
            } else {
               client.field_1690.field_1904.method_23481(true);
            }
         } else {
            this.release();
         }
      }
   }

   private void release() {
      if (this.holdingUse) {
         this.holdingUse = false;
         class_310.method_1551().field_1690.field_1904.method_23481(false);
      }
   }

   private double effectiveHealth(class_746 player) {
      return player.method_6032() + (this.countAbsorption.getValue() ? player.method_6067() : 0.0F);
   }

   private static boolean isApple(class_1799 stack) {
      return stack.method_31574(class_1802.field_8463) || stack.method_31574(class_1802.field_8367);
   }
}
