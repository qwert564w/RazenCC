package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.combat.SprintManager;

@Environment(EnvType.CLIENT)
public final class SprintFeature extends Feature {
   public final BooleanSetting omniDirectional = this.register(new BooleanSetting("Omni Directional", false));
   public final BooleanSetting keepSprint = this.register(new BooleanSetting("Keep Sprint", true));

   public SprintFeature() {
      super("Sprint", "Automatically sprints while moving", FeatureCategory.MOVEMENT, 86);
   }

   public static SprintFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(SprintFeature.class);
   }

   public static boolean shouldForceSprintKey() {
      return getEnabled() != null && !pveControlsMovement();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.omniDirectional.getValue() && !pveControlsMovement()) {
         class_746 player = event.getClient().field_1724;
         if (player != null && !player.method_7325() && !player.method_6115()) {
            if (!SprintManager.shouldFreezeMovementInput()) {
               if (player.field_3913.method_3128().method_35587() > 0.0F && (this.keepSprint.getValue() || !player.method_5624())) {
                  player.method_5728(true);
               }
            }
         }
      }
   }

   private static boolean pveControlsMovement() {
      return PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.MOVEMENT)
         || PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.NAVIGATION);
   }
}
