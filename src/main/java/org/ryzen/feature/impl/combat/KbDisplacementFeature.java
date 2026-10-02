package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_2848;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_2848.class_2849;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public final class KbDisplacementFeature extends Feature {
   private static final double PITCH_JITTER_CHANCE = 0.01;
   private static final float PITCH_JITTER = 0.5F;
   public final BooleanSetting fakeSprint = this.register(new BooleanSetting("Fake Sprint", false));
   private boolean restoreLook;
   private boolean resendSprint;
   private class_1297 delayedTarget;
   private int delayTicks;
   private boolean replaying;

   public KbDisplacementFeature() {
      super("KBDisplacement", "Aims the knockback you deal by driving sprint and look around the hit", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null) {
         if (!this.fakeSprint.getValue()) {
            forceSprint(client, player);
            this.releaseDelayedAttack(client, player);
         }

         if (this.restoreLook) {
            this.restoreLook = false;
            sendLook(player, player.method_36454(), player.method_36455());
         }

         if (this.resendSprint) {
            this.resendSprint = false;
            sendSprint(player, class_2849.field_12981);
         }
      }
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_1297 target = client.field_1692;
      if (player != null && client.field_1687 != null && target != null && !event.isCancelled()) {
         if (this.fakeSprint.getValue()) {
            if (player.method_5624()) {
               sendSprint(player, class_2849.field_12985);
            }

            sendSprint(player, class_2849.field_12981);
            if (player.method_5624()) {
               this.resendSprint = true;
            }

            this.aimAt(player, target);
         } else {
            boolean wasSprinting = player.method_5624();
            forceSprint(client, player);
            if (!this.replaying && !wasSprinting && canSprint(client, player)) {
               this.delayedTarget = target;
               this.delayTicks = 1;
               event.cancel();
            } else if (player.method_5624()) {
               this.aimAt(player, target);
            }
         }
      }
   }

   private void releaseDelayedAttack(class_310 client, class_746 player) {
      if (this.delayedTarget != null && client.field_1761 != null) {
         if (!this.delayedTarget.method_5805() || !canSprint(client, player)) {
            this.delayedTarget = null;
            this.delayTicks = 0;
         } else if (this.delayTicks > 0) {
            this.delayTicks--;
         } else {
            class_1297 target = this.delayedTarget;
            this.delayedTarget = null;
            this.replaying = true;

            try {
               client.field_1761.method_2918(player, target);
               player.method_6104(class_1268.field_5808);
            } finally {
               this.replaying = false;
            }
         }
      }
   }

   private void aimAt(class_746 player, class_1297 target) {
      class_243 delta = target.method_73189().method_1020(player.method_73189());
      float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
      float pitch = player.method_36455();
      if (Math.random() < 0.01) {
         pitch += 0.5F;
      }

      sendLook(player, yaw, pitch);
      this.restoreLook = true;
   }

   private static void forceSprint(class_310 client, class_746 player) {
      client.field_1690.field_1867.method_23481(true);
      if (!player.method_5624() && canSprint(client, player)) {
         player.method_5728(true);
      }
   }

   private static boolean canSprint(class_310 client, class_746 player) {
      return player.field_3913 != null
         && player.field_3913.field_54155.comp_3159()
         && !player.field_5976
         && !player.method_5715()
         && !player.method_5799()
         && !player.method_5771()
         && !player.method_5869()
         && (player.method_7344().method_7586() > 6 || player.method_31549().field_7478);
   }

   private static void sendLook(class_746 player, float yaw, float pitch) {
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2831(yaw, pitch, player.method_24828(), player.field_5976));
      }
   }

   private static void sendSprint(class_746 player, class_2849 action) {
      if (player.field_3944 != null) {
         player.field_3944.method_52787(new class_2848(player, action));
      }
   }

   private void reset() {
      this.restoreLook = false;
      this.resendSprint = false;
      this.delayedTarget = null;
      this.delayTicks = 0;
      this.replaying = false;
   }
}
