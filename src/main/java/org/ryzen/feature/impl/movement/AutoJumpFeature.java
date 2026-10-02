package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_4081;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;

@Environment(EnvType.CLIENT)
public final class AutoJumpFeature extends Feature {
   public final BooleanSetting aura = this.register(new BooleanSetting("Aura", true));
   public final BooleanSetting negativeEffects = this.register(new BooleanSetting("Negative Effects", true));

   public AutoJumpFeature() {
      super("AutoJump", "Automatically jumps from ground", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && event.getClient().field_1687 != null) {
         if (!PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.MOVEMENT)
            && !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.NAVIGATION)) {
            if (player.method_24828() && !event.getClient().field_1690.field_1903.method_1434()) {
               if (!player.method_31549().field_7479 && !player.method_6128()) {
                  if (!player.method_5799() && !player.method_5771() && !player.method_6101()) {
                     if (this.shouldJumpForAura(player) || this.shouldJumpForNegativeEffects(player)) {
                        player.method_6043();
                     }
                  }
               }
            }
         }
      }
   }

   private boolean shouldJumpForAura(class_746 player) {
      if (!this.aura.getValue()) {
         return false;
      }

      AuraFeature aura = FeatureManager.INSTANCE.getFeature(AuraFeature.class);
      return aura != null && aura.shouldAutoJump(player);
   }

   private boolean shouldJumpForNegativeEffects(class_746 player) {
      return !this.negativeEffects.getValue() ? false : player.field_3913.method_3128().method_35587() > 0.0F && this.hasNegativeEffects(player);
   }

   private boolean hasNegativeEffects(class_746 player) {
      for (class_1293 effect : player.method_6026()) {
         if (((class_1291)effect.method_5579().comp_349()).method_18792() == class_4081.field_18272) {
            return true;
         }
      }

      return false;
   }
}
