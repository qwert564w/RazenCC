package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.impl.combat.ElytraTargetFeature;
import org.ryzen.feature.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public final class ElytraMotionFeature extends Feature implements MinecraftContext {
   private static final String MODE_NEW = "New";
   private static final String MODE_OLD = "Old";
   private static final double HOLD_DISTANCE = 4.0;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "New", "New", "Old"));
   private boolean frozen;
   private class_243 savedPos;
   private class_243 savedVelocity;

   public ElytraMotionFeature() {
      super("ElytraMotion", "Freezes elytra motion near the aura target", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onDisable() {
      class_746 player = mc.field_1724;
      if (player != null) {
         player.method_5875(false);
      }

      this.reset();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null) {
         if (this.mode.is("New")) {
            this.newMode(player);
         } else {
            this.oldMode(player);
         }
      }
   }

   private void newMode(class_746 player) {
      if (!player.method_6128()) {
         this.reset();
      } else {
         class_1309 target = this.auraTarget();
         if (this.shouldHold(player, target)) {
            if (!this.frozen) {
               this.savedPos = player.method_73189();
               this.savedVelocity = player.method_18798();
            }

            this.frozen = true;
            player.method_18800(0.0, 0.0, 0.0);
            if (this.savedPos != null) {
               player.method_5814(this.savedPos.field_1352, this.savedPos.field_1351, this.savedPos.field_1350);
            }

            player.field_6037 = true;
         } else {
            if (this.savedVelocity != null) {
               player.method_18800(this.savedVelocity.field_1352, 0.0, this.savedVelocity.field_1350);
            }

            this.reset();
         }
      }
   }

   private void oldMode(class_746 player) {
      class_1309 target = this.auraTarget();
      if (this.shouldHold(player, target)) {
         player.method_18800(0.0, 0.0, 0.0);
         player.method_5875(true);
      } else {
         player.method_5875(false);
         this.reset();
      }
   }

   private boolean shouldHold(class_746 player, class_1309 target) {
      if (target != null && player.method_6128()) {
         ElytraTargetFeature elytraTarget = FeatureManager.INSTANCE.getEnabled(ElytraTargetFeature.class);
         if (elytraTarget != null && elytraTarget.isChasing(target)) {
            return false;
         }

         class_243 aim = target.method_5829().method_1005().method_1031(0.0, (target.method_23318() - target.field_5971) * 2.0, 0.0);
         return player.method_33571().method_1022(aim) < 4.0;
      } else {
         return false;
      }
   }

   private class_1309 auraTarget() {
      AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
      return aura == null ? null : aura.getCurrentTarget();
   }

   private void reset() {
      this.frozen = false;
      this.savedPos = null;
      this.savedVelocity = null;
   }
}
