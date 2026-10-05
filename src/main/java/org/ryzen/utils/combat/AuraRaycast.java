package org.ryzen.utils.combat;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;

@Environment(EnvType.CLIENT)
public final class AuraRaycast {
   private static final float PARTIAL_TICK = 1.0F;
   private static final double EPSILON = 1.0E-7;

   private AuraRaycast() {
   }

   public static boolean rotationIntersectsTarget(
      class_746 player, class_1309 target, float yaw, float pitch, double maxReach, double minReach, double margin, boolean throughWalls
   ) {
      class_243 eye = player.method_5836(1.0F);
      class_243 direction = class_243.method_1030(pitch, yaw);
      double castDistance = maxReach + margin;
      class_243 end = eye.method_1019(direction.method_1021(castDistance));
      class_238 box = target.method_5829().method_1014(target.method_5871());
      if (box.method_49271(eye) <= 1.0E-7) {
         return minReach - margin <= 1.0E-7;
      }

      Optional<class_243> clip = box.method_992(eye, end);
      if (clip.isEmpty()) {
         return false;
      }

      class_243 hit = clip.get();
      double distance = hit.method_1022(eye);
      return !(distance > castDistance + 1.0E-7) && !(distance < minReach - margin - 1.0E-7) ? throughWalls || isBlockPathClear(player, eye, hit) : false;
   }

   public static boolean canSeeTarget(class_746 player, class_1309 target, double range) {
      return findAimPoint(player, target, target.method_73189(), range, false, class_243.field_1353) != null;
   }

   public static class_243 findAimPoint(class_746 player, class_1309 target, class_243 predictedPosition, double range, boolean throughWalls, class_243 jitter) {
      class_243 eye = player.method_5836(1.0F);
      class_243 preference = eye.method_1019(jitter);
      class_243 movement = predictedPosition.method_1020(target.method_73189());
      class_238 box = target.method_5829().method_997(movement).method_1014(target.method_5871());
      double rangeSqr = range * range;
      if (box.method_49271(eye) <= 1.0E-7) {
         class_243 center = box.method_1005();
         if (center.method_1025(eye) <= 1.0E-7) {
            center = eye.method_1019(class_243.method_1030(player.method_36455(), player.method_36454()).method_1021(0.01));
         }

         return center;
      } else {
         List<class_243> candidates = aimCandidates(box, eye, predictedPosition.field_1351 + target.method_5751());
         candidates.sort(Comparator.comparingDouble(preference::method_1025));

         for (class_243 candidate : candidates) {
            if (!(candidate.method_1025(eye) > rangeSqr + 1.0E-7) && (throughWalls || isBlockPathClear(player, eye, candidate))) {
               return candidate;
            }
         }

         return null;
      }
   }

   public static double predictedHitboxDistanceSqr(class_746 player, class_1309 target, class_243 predictedPosition) {
      class_243 movement = predictedPosition.method_1020(target.method_73189());
      class_238 box = target.method_5829().method_997(movement).method_1014(target.method_5871());
      return box.method_49271(player.method_5836(1.0F));
   }

   private static boolean isBlockPathClear(class_746 player, class_243 from, class_243 to) {
      class_3965 blockHit = player.method_73183().method_17742(new class_3959(from, to, class_3960.field_17559, class_242.field_1348, player));
      return blockHit.method_17783() == class_240.field_1333 || blockHit.method_17784().method_1025(from) + 1.0E-7 >= to.method_1025(from);
   }

   private static List<class_243> aimCandidates(class_238 box, class_243 eye, double predictedEyeY) {
      List<class_243> points = new ArrayList<>(85);

      // Priority points (closest to eye level)
      points.add(
         new class_243(
            class_3532.method_15350(eye.field_1352, box.field_1323, box.field_1320),
            class_3532.method_15350(eye.field_1351, box.field_1322, box.field_1325),
            class_3532.method_15350(eye.field_1350, box.field_1321, box.field_1324)
         )
      );
      points.add(box.method_1005());
      points.add(
         new class_243(
            (box.field_1323 + box.field_1320) * 0.5,
            class_3532.method_15350(predictedEyeY, box.field_1322, box.field_1325),
            (box.field_1321 + box.field_1324) * 0.5
         )
      );

      // Body zones: head, upper body, center, lower body, legs
      double[] yFactors = new double[]{0.05, 0.2, 0.35, 0.5, 0.65, 0.8, 0.95};
      double[] xzFactors = new double[]{0.2, 0.4, 0.5, 0.6, 0.8};

      for (double y : yFactors) {
         for (double x : xzFactors) {
            for (double z : xzFactors) {
               points.add(
                  new class_243(
                     class_3532.method_16436(x, box.field_1323, box.field_1320),
                     class_3532.method_16436(y, box.field_1322, box.field_1325),
                     class_3532.method_16436(z, box.field_1321, box.field_1324)
                  )
               );
            }
         }
      }

      // Edge points (corners and edges of hitbox)
      double[] edgeFactors = new double[]{0.0, 1.0};
      double[] midFactors = new double[]{0.5};

      for (double x : edgeFactors) {
         for (double y : midFactors) {
            for (double z : edgeFactors) {
               points.add(
                  new class_243(
                     class_3532.method_16436(x, box.field_1323, box.field_1320),
                     class_3532.method_16436(y, box.field_1322, box.field_1325),
                     class_3532.method_16436(z, box.field_1321, box.field_1324)
                  )
               );
            }
         }
      }

      // Biomechanical offsets (simulate human aiming imperfection)
      double centerX = (box.field_1323 + box.field_1320) * 0.5;
      double centerZ = (box.field_1321 + box.field_1324) * 0.5;
      double[] offsets = new double[]{-0.1, 0.1};

      for (double dx : offsets) {
         for (double dz : offsets) {
            points.add(
               new class_243(
                  centerX + dx,
                  class_3532.method_15350(predictedEyeY, box.field_1322, box.field_1325),
                  centerZ + dz
               )
            );
         }
      }

      return points;
   }
}
