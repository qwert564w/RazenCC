package org.ryzen.feature.impl.player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1301;
import net.minecraft.class_1309;
import net.minecraft.class_1675;
import net.minecraft.class_1684;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3966;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.setting.BooleanSetting;

@Environment(EnvType.CLIENT)
public final class PearlTargetFeature extends Feature implements MinecraftContext {
   private static final float ROTATION_TOLERANCE = 1.0F;
   private static final double THROW_SPEED = 1.5;
   private static final double AIR_INERTIA = 0.99;
   private static final double WATER_INERTIA = 0.8;
   private static final double THROWABLE_GRAVITY = 0.03;
   private static final double WATER_GRAVITY = 0.015707963267948967;
   private static final int MAX_SIMULATION_TICKS = 600;
   private static final double ENTITY_EXPAND = 0.3;
   private static final double VELOCITY_LEAD_FACTOR = 0.02;
   private static final float PITCH_MIN = -89.0F;
   private static final float PITCH_MAX = 89.0F;
   public final BooleanSetting onlyAuraTarget = this.register(new BooleanSetting("Only Aura Target", false));
   private final Set<UUID> seenPearls = new HashSet<>();
   private UUID auraTargetId;
   private UUID armedTargetId;
   private class_243 aimPoint;
   private boolean armed;

   public PearlTargetFeature() {
      super("PearlTarget", "Throws an ender pearl after the aura target's pearl", FeatureCategory.PLAYER, -1);
   }

   @Override
   protected void onDisable() {
      this.reset();
      this.auraTargetId = null;
      this.seenPearls.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
      this.auraTargetId = null;
      this.seenPearls.clear();
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPost()) {
         class_746 player = mc.field_1724;
         if (player != null && mc.field_1687 != null) {
            class_1309 target = this.resolveTarget(player);
            if (target != null && target.method_5805()) {
               if (!this.armed) {
                  class_243 landing = this.trackIncomingPearl(player, target);
                  if (landing == null) {
                     return;
                  }

                  this.armed = true;
                  this.armedTargetId = target.method_5667();
                  this.aimPoint = landing;
               }

               if (this.armedTargetId == null || !this.armedTargetId.equals(target.method_5667()) || this.aimPoint == null) {
                  this.reset();
               } else if (!player.method_7357().method_7904(class_1802.field_8634.method_7854()) && this.hasPearl(player)) {
                  class_243 aim = this.adjustAim(player, this.aimPoint, target, null);
                  float[] rotation = this.solveThrow(player, aim, target);
                  RotationContext.setRotation(rotation[0], rotation[1]);
                  float yawDiff = Math.abs(class_3532.method_15393(rotation[0] - player.method_36454()));
                  float pitchDiff = Math.abs(rotation[1] - player.method_36455());
                  if (!(yawDiff > 1.0F) && !(pitchDiff > 1.0F)) {
                     this.throwPearl(player);
                     this.reset();
                  }
               } else {
                  this.reset();
               }
            } else {
               if (this.armed) {
                  this.reset();
               }
            }
         } else {
            this.reset();
         }
      }
   }

   private class_1309 resolveTarget(class_746 player) {
      AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
      class_1309 auraTarget = aura == null ? null : aura.getCurrentTarget();
      if (this.onlyAuraTarget.getValue()) {
         if (auraTarget != null && auraTarget.method_5805()) {
            this.auraTargetId = auraTarget.method_5667();
            return auraTarget;
         } else {
            this.auraTargetId = null;
            return null;
         }
      } else {
         if (this.armedTargetId != null) {
            class_1309 entity = this.byUuid(this.armedTargetId);
            if (entity != null && entity.method_5805()) {
               return entity;
            }
         }

         if (auraTarget != null && auraTarget.method_5805()) {
            this.auraTargetId = auraTarget.method_5667();
            return auraTarget;
         }

         if (this.auraTargetId != null) {
            class_1309 entity = this.byUuid(this.auraTargetId);
            if (entity != null && entity.method_5805()) {
               return entity;
            }
         }

         return null;
      }
   }

   private class_1309 byUuid(UUID id) {
      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1309 living && entity.method_5667().equals(id)) {
            return living;
         }
      }

      return null;
   }

   private boolean hasPearl(class_746 player) {
      if (!player.method_6047().method_31574(class_1802.field_8634) && !player.method_6079().method_31574(class_1802.field_8634)) {
         for (int slot = 0; slot < 36; slot++) {
            if (player.method_31548().method_5438(slot).method_31574(class_1802.field_8634)) {
               return true;
            }
         }

         return false;
      } else {
         return true;
      }
   }

   private class_243 trackIncomingPearl(class_746 player, class_1309 target) {
      if (this.seenPearls.size() > 256) {
         this.seenPearls.clear();
      }

      for (class_1297 entity : mc.field_1687.method_18112()) {
         if (entity instanceof class_1684 pearl && pearl.field_6012 <= 2) {
            UUID id = pearl.method_5667();
            if (!this.seenPearls.contains(id)) {
               class_1297 owner = pearl.method_24921();
               if (owner != null && owner.method_5667().equals(target.method_5667())) {
                  this.seenPearls.add(id);
                  return this.adjustAim(player, this.simulate(player, pearl.method_73189(), pearl.method_18798(), pearl), target, pearl.method_18798());
               }
            }
         }
      }

      return null;
   }

   private class_243 simulate(class_746 player, class_243 start, class_243 velocity, class_1297 thrower) {
      class_243 pos = start;
      class_243 vel = velocity;

      for (int step = 0; step <= 600; step++) {
         class_243 next = pos.method_1019(vel);
         class_239 block = mc.field_1687.method_61717(new class_3959(pos, next, class_3960.field_17558, class_242.field_1348, player));
         class_243 end = block.method_17783() != class_240.field_1333 ? block.method_17784() : next;
         class_238 box = new class_238(pos, end).method_1014(0.3);
         class_3966 hit = class_1675.method_18075(
            thrower, pos, end, box, candidate -> class_1301.field_52443.test(candidate) && candidate != thrower && candidate != player, pos.method_1025(end)
         );
         if (hit != null) {
            return hit.method_17784();
         }

         if (block.method_17783() != class_240.field_1333) {
            return block.method_17784();
         }

         pos = next;
         vel = this.applyDrag(pos, vel);
      }

      return pos;
   }

   private class_243 applyDrag(class_243 pos, class_243 vel) {
      return this.inWater(pos) ? vel.method_1021(0.8).method_1023(0.0, 0.015707963267948967, 0.0) : vel.method_1021(0.99).method_1023(0.0, 0.03, 0.0);
   }

   private boolean inWater(class_243 pos) {
      return mc.field_1687.method_8316(class_2338.method_49638(pos)).method_15767(class_3486.field_15517);
   }

   private class_243 predictTargetPos(class_746 player, class_1309 target) {
      class_243 velocity = target.method_18798();
      double lead = Math.min(0.35 + target.method_5858(player) * 0.02, 1.0);
      return target.method_73189()
         .method_1031(0.0, target.method_17682() * 0.5, 0.0)
         .method_1031(velocity.field_1352 * lead, velocity.field_1351 * lead, velocity.field_1350 * lead);
   }

   private class_243 adjustAim(class_746 player, class_243 landing, class_1309 target, class_243 pearlVelocity) {
      if (target == null || landing == null) {
         return landing;
      } else {
         return !this.nearTarget(landing, target, pearlVelocity) && !this.nearSelf(player, landing) ? landing : this.predictTargetPos(player, target);
      }
   }

   private boolean nearTarget(class_243 landing, class_1309 target, class_243 pearlVelocity) {
      if (target.method_5829().method_1009(0.35, 0.15, 0.35).method_1006(landing)) {
         return true;
      } else {
         class_243 targetPos = target.method_73189();
         double horizontal = Math.hypot(landing.field_1352 - targetPos.field_1352, landing.field_1350 - targetPos.field_1350);
         double dy = landing.field_1351 - targetPos.field_1351;
         if (horizontal <= 2.0 && dy >= -0.35 && dy <= 1.5) {
            return true;
         } else {
            return pearlVelocity == null
               ? false
               : Math.hypot(pearlVelocity.field_1352, pearlVelocity.field_1350) < 0.35 && pearlVelocity.field_1351 < -0.15 && horizontal <= 2.5;
         }
      }
   }

   private boolean nearSelf(class_746 player, class_243 landing) {
      class_243 playerPos = player.method_73189();
      double horizontal = Math.hypot(landing.field_1352 - playerPos.field_1352, landing.field_1350 - playerPos.field_1350);
      double dy = landing.field_1351 - playerPos.field_1351;
      return horizontal <= 1.75 && dy >= -0.35 && dy <= 1.5;
   }

   private float[] solveThrow(class_746 player, class_243 aim, class_1309 target) {
      class_243 eye = player.method_33571();
      class_243 delta = aim.method_1020(eye);
      double horizontal = Math.hypot(delta.field_1352, delta.field_1350);
      if (horizontal < 1.0E-4 && target != null) {
         delta = this.predictTargetPos(player, target).method_1020(eye);
         horizontal = Math.hypot(delta.field_1352, delta.field_1350);
      }

      float yaw = class_3532.method_15393((float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F);
      class_243 base = this.baseVelocity(player);
      float bestPitch = horizontal < 1.0E-4 ? 0.0F : class_3532.method_15363((float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal))), -89.0F, 89.0F);
      double bestDistance = Double.MAX_VALUE;
      float step = this.inLiquid(player) ? 0.5F : 1.0F;

      for (float pitch = -89.0F; pitch <= 89.0F; pitch += step) {
         class_243 direction = directionOf(pitch, yaw);
         class_243 velocity = direction.method_1021(1.5).method_1019(base);
         class_243 landing = this.simulate(player, eye, velocity, player);
         double distance = landing.method_1025(aim);
         if (distance < bestDistance) {
            bestDistance = distance;
            bestPitch = pitch;
         }
      }

      return new float[]{yaw, class_3532.method_15363(bestPitch, -89.0F, 89.0F)};
   }

   private class_243 baseVelocity(class_746 player) {
      class_243 velocity = player.method_18798();
      return this.inLiquid(player) ? new class_243(velocity.field_1352, 0.0, velocity.field_1350) : velocity;
   }

   private boolean inLiquid(class_746 player) {
      return player.method_5799() || player.method_5869() || player.method_5771() || player.method_5681();
   }

   private static class_243 directionOf(float pitch, float yaw) {
      float pitchRad = pitch * (float) (Math.PI / 180.0);
      float yawRad = yaw * (float) (Math.PI / 180.0);
      float cosPitch = class_3532.method_15362(pitchRad);
      return new class_243(-class_3532.method_15374(yawRad) * cosPitch, -class_3532.method_15374(pitchRad), class_3532.method_15362(yawRad) * cosPitch);
   }

   private void throwPearl(class_746 player) {
      class_310 client = mc;
      if (client.field_1761 != null) {
         if (player.method_6047().method_31574(class_1802.field_8634)) {
            client.field_1761.method_2919(player, class_1268.field_5808);
            player.method_6104(class_1268.field_5808);
         } else if (player.method_6079().method_31574(class_1802.field_8634)) {
            client.field_1761.method_2919(player, class_1268.field_5810);
            player.method_6104(class_1268.field_5810);
         }
      }
   }

   private void reset() {
      this.armed = false;
      this.armedTargetId = null;
      this.aimPoint = null;
   }
}
