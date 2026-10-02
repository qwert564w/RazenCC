package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1753;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3959;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.math.TickSimulator;

@Environment(EnvType.CLIENT)
public final class BowAimbotFeature extends Feature {
   private static final int MAX_FLIGHT_TICKS = 120;
   private static final double MAX_HIT_ERROR_SQR = 12.25;
   public final NumberSetting range = this.register(new NumberSetting("Range", 50.0, 5.0, 100.0, 5.0, " blocks"));

   public BowAimbotFeature() {
      super("BowAimbot", "Predicts target movement and arrow ballistics", FeatureCategory.COMBAT, 66);
   }

   @Override
   protected void onDisable() {
      RotationContext.clear();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 mc = event.getClient();
      class_746 player = mc.field_1724;
      class_638 level = mc.field_1687;
      if (player != null && level != null && player.method_6115() && player.method_6030().method_7909() instanceof class_1753) {
         class_1309 target = this.findClosestTarget(player, level);
         if (target == null) {
            RotationContext.clear();
         } else {
            float force = player.method_75120(0.0F) / 20.0F;
            force = Math.min(1.0F, (force * force + force * 2.0F) / 3.0F);
            if (force < 0.1F) {
               RotationContext.clear();
            } else {
               class_243 predictedPos = TickSimulator.getPredictedState(target, 15, level).pos;
               double targetY = predictedPos.field_1351 + target.method_17682() * 0.5;
               double diffX = predictedPos.field_1352 - player.method_23317();
               double diffZ = predictedPos.field_1350 - player.method_23321();
               double distanceXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
               float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
               double basePitch = -Math.toDegrees(Math.atan2(targetY - player.method_23320(), distanceXZ));
               double velocity = force * 3.0;
               float bestPitch = player.method_36455();
               double bestScore = Double.MAX_VALUE;
               double bestApexX = player.method_23317();
               double bestApexY = player.method_23320() - 0.1;
               double bestApexZ = player.method_23321();
               double bestEndX = bestApexX;
               double bestEndY = bestApexY;
               double bestEndZ = bestApexZ;

               for (float pitch = (float)basePitch + 5.0F; pitch > -85.0F; pitch -= 0.5F) {
                  double pitchRad = Math.toRadians(pitch);
                  double yawRad = Math.toRadians(yaw);
                  double motionX = -Math.sin(yawRad) * Math.cos(pitchRad) * velocity;
                  double motionY = -Math.sin(pitchRad) * velocity;
                  double motionZ = Math.cos(yawRad) * Math.cos(pitchRad) * velocity;
                  double arrowX = player.method_23317();
                  double arrowY = player.method_23320() - 0.1;
                  double arrowZ = player.method_23321();
                  double apexX = arrowX;
                  double apexY = arrowY;
                  double apexZ = arrowZ;
                  double closestX = arrowX;
                  double closestY = arrowY;
                  double closestZ = arrowZ;
                  double score = Double.MAX_VALUE;

                  for (int step = 0; step < 120; step++) {
                     arrowX += motionX;
                     arrowY += motionY;
                     arrowZ += motionZ;
                     if (arrowY > apexY) {
                        apexX = arrowX;
                        apexY = arrowY;
                        apexZ = arrowZ;
                     }

                     double dx = arrowX - predictedPos.field_1352;
                     double dy = arrowY - targetY;
                     double dz = arrowZ - predictedPos.field_1350;
                     double distanceSqr = dx * dx + dy * dy + dz * dz;
                     if (distanceSqr < score) {
                        score = distanceSqr;
                        closestX = arrowX;
                        closestY = arrowY;
                        closestZ = arrowZ;
                     }

                     motionX *= 0.99;
                     motionY = motionY * 0.99 - 0.05;
                     motionZ *= 0.99;
                     if (motionY < 0.0 && arrowY < predictedPos.field_1351 - 1.0) {
                        break;
                     }
                  }

                  if (score < bestScore) {
                     bestScore = score;
                     bestPitch = pitch;
                     bestApexX = apexX;
                     bestApexY = apexY;
                     bestApexZ = apexZ;
                     bestEndX = closestX;
                     bestEndY = closestY;
                     bestEndZ = closestZ;
                  }
               }

               if (bestScore < 12.25 && this.isPathClear(level, player, bestApexX, bestApexY, bestApexZ, bestEndX, bestEndY, bestEndZ)) {
                  RotationContext.setRotation(yaw, bestPitch);
               } else {
                  RotationContext.clear();
               }
            }
         }
      } else {
         RotationContext.clear();
      }
   }

   private boolean isPathClear(class_638 level, class_746 player, double apexX, double apexY, double apexZ, double endX, double endY, double endZ) {
      class_243 start = new class_243(player.method_23317(), player.method_23320() - 0.1, player.method_23321());
      class_243 apex = new class_243(apexX, apexY, apexZ);
      class_243 end = new class_243(endX, endY, endZ);
      return start.method_1025(apex) > 0.01 && this.isBlocked(level, player, start, apex)
         ? false
         : apex.method_1025(end) <= 0.01 || !this.isBlocked(level, player, apex, end);
   }

   private boolean isBlocked(class_638 level, class_746 player, class_243 from, class_243 to) {
      return level.method_17742(new class_3959(from, to, class_3960.field_17558, class_242.field_1348, player)).method_17783() == class_240.field_1332;
   }

   private class_1309 findClosestTarget(class_746 player, class_638 level) {
      double maxRange = this.range.getValue();
      double closestDistance = maxRange * maxRange;
      class_1309 closest = null;

      for (class_1297 entity : level.method_8333(player, player.method_5829().method_1014(maxRange), this::isTarget)) {
         double distance = player.method_5858(entity);
         if (distance < closestDistance && entity instanceof class_1309 living) {
            closestDistance = distance;
            closest = living;
         }
      }

      return closest;
   }

   private boolean isTarget(class_1297 entity) {
      return entity instanceof class_1309 && entity.method_5805() && entity.method_5732() && !entity.method_7325();
   }
}
