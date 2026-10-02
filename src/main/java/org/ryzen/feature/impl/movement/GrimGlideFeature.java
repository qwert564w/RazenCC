package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_2886;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class GrimGlideFeature extends Feature implements PlayerContext {
   private static final long FIREWORK_GRACE_MS = 150L;
   private static final double SPEED_EVEN = 0.079;
   private static final double SPEED_ODD = 0.088;
   private static final double AHEAD_DOT = 0.35;
   private long lastFireworkAt;

   public GrimGlideFeature() {
      super("GrimGlide", "Moves on an elytra without spending fireworks", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onEnable() {
      this.lastFireworkAt = 0L;
      ChatUtil.info("GrimGlide: на Really World долгое использование может привести к кику");
   }

   @Override
   protected void onDisable() {
      this.lastFireworkAt = 0L;
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPhase() == PacketSendEvent.Phase.PRE && event.getPacket() instanceof class_2886) {
         this.lastFireworkAt = System.currentTimeMillis();
      }
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPre()) {
         class_746 player = event.getPlayer();
         if (player != null && player.method_6128()) {
            if (System.currentTimeMillis() - this.lastFireworkAt > 150L && !this.targetIsAhead(player)) {
               double speed = player.field_6012 % 2 == 0 ? 0.079 : 0.088;
               double yawRadians = Math.toRadians(player.method_36454());
               double x = -Math.sin(yawRadians) * speed;
               double z = Math.cos(yawRadians) * speed;
               player.method_18800(x, player.method_18798().field_1351, z);
               if (player.field_6012 % 2 == 0) {
                  player.method_5814(player.method_23317() + x, player.method_23318(), player.method_23321() + z);
               }
            }
         }
      }
   }

   private boolean targetIsAhead(class_746 player) {
      AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
      if (aura == null) {
         return false;
      }

      class_1309 target = aura.getCurrentTarget();
      if (target != null && target.method_6128()) {
         class_243 velocity = target.method_18798();
         class_243 heading = new class_243(velocity.field_1352, 0.0, velocity.field_1350);
         if (heading.method_1027() < 1.0E-6) {
            class_243 look = target.method_5720();
            heading = new class_243(look.field_1352, 0.0, look.field_1350);
         }

         if (heading.method_1027() < 1.0E-6) {
            return false;
         }

         heading = heading.method_1029();
         class_243 toSelf = new class_243(player.method_23317() - target.method_23317(), 0.0, player.method_23321() - target.method_23321());
         return toSelf.method_1026(heading) > 0.35;
      } else {
         return false;
      }
   }
}
