package org.ryzen.utils.render.world;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import org.ryzen.utils.ColorUtil;

@Environment(EnvType.CLIENT)
public final class ZoneGizmos {
   private static final float CROSS_ALPHA = 0.6F;
   private static final float CROSS_WIDTH_SCALE = 0.8F;
   private static final double STRATUM_HEIGHT = 5.0;
   private static final float STRATUM_SLAB_WIDTH = 3.0F;
   private static final float STRATUM_EDGE_WIDTH = 2.0F;
   private static final float STRATUM_CROSS_WIDTH = 1.6F;

   private ZoneGizmos() {
   }

   public static class_12175 cube(class_238 box, int strokeColor, int fillColor, float strokeWidth) {
      return (primitives, alpha) -> crossBox(primitives, box, strokeColor, fillColor, strokeWidth);
   }

   public static class_12175 ring(class_243 center, double radius, int strokeColor, int fillColor, float strokeWidth) {
      return (primitives, alpha) -> {
         int cells = (int)Math.ceil(radius) + 1;

         for (int dx = -cells; dx <= cells; dx++) {
            for (int dz = -cells; dz <= cells; dz++) {
               if (onRadiusEdge(dx, dz, radius)) {
                  double x = center.field_1352 + dx;
                  double z = center.field_1350 + dz;
                  crossBox(
                     primitives,
                     new class_238(x - 0.5, center.field_1351, z - 0.5, x + 0.5, center.field_1351 + 1.0, z + 0.5),
                     strokeColor,
                     fillColor,
                     strokeWidth
                  );
               }
            }
         }
      };
   }

   public static class_12175 stratum(
      class_2338 playerPos, class_243 smooth, float yRot, float xRot, class_2350 nearestViewDirection, int strokeColor, int fillColor
   ) {
      return (primitives, alpha) -> {
         float yaw = class_3532.method_15393(yRot);
         if (Math.abs(xRot) > 60.0F) {
            class_2338 anchor = playerPos.method_10084().method_10079(nearestViewDirection, 3);
            slab(
               primitives,
               anchor.method_10089(3).method_10077(3).method_10074(),
               anchor.method_10088(2).method_10076(2).method_10084(),
               smooth,
               strokeColor,
               fillColor
            );
         } else if (yaw <= -157.5F || yaw >= 157.5F) {
            class_2338 anchor = playerPos.method_10076(3).method_10084();
            slab(primitives, anchor.method_10087(2).method_10089(3), anchor.method_10086(3).method_10088(2).method_10077(2), smooth, strokeColor, fillColor);
         } else if (yaw <= -112.5F) {
            steppedWall(primitives, playerPos.method_10089(5).method_10072().method_10074(), smooth, strokeColor, fillColor, -1, true);
         } else if (yaw <= -67.5F) {
            class_2338 anchor = playerPos.method_10089(2).method_10084();
            slab(primitives, anchor.method_10087(2).method_10077(3), anchor.method_10086(3).method_10076(2).method_10089(2), smooth, strokeColor, fillColor);
         } else if (yaw <= -22.5F) {
            steppedWall(primitives, playerPos.method_10089(5).method_10074(), smooth, strokeColor, fillColor, 1, false);
         } else if (yaw <= 22.5F) {
            class_2338 anchor = playerPos.method_10077(2).method_10084();
            slab(primitives, anchor.method_10087(2).method_10089(3), anchor.method_10086(3).method_10088(2).method_10077(2), smooth, strokeColor, fillColor);
         } else if (yaw <= 67.5F) {
            steppedWall(primitives, playerPos.method_10088(4).method_10074(), smooth, strokeColor, fillColor, 1, true);
         } else if (yaw <= 112.5F) {
            class_2338 anchor = playerPos.method_10088(3).method_10084();
            slab(primitives, anchor.method_10087(2).method_10077(3), anchor.method_10086(3).method_10076(2).method_10089(2), smooth, strokeColor, fillColor);
         } else if (yaw <= 157.5F) {
            steppedWall(primitives, playerPos.method_10088(4).method_10072().method_10074(), smooth, strokeColor, fillColor, -1, false);
         }
      };
   }

   private static boolean onRadiusEdge(int dx, int dz, double radius) {
      boolean inside = false;
      boolean outside = false;

      for (double offsetX = -0.5; offsetX <= 0.5; offsetX++) {
         for (double offsetZ = -0.5; offsetZ <= 0.5; offsetZ++) {
            double x = dx + offsetX;
            double z = dz + offsetZ;
            if (Math.sqrt(x * x + z * z) <= radius) {
               inside = true;
            } else {
               outside = true;
            }
         }
      }

      return inside && outside;
   }

   private static void slab(class_12177 primitives, class_2338 from, class_2338 to, class_243 smooth, int strokeColor, int fillColor) {
      class_243 min = class_243.method_24954(from).method_1019(smooth);
      class_243 max = class_243.method_24954(to).method_1019(smooth);
      crossBox(primitives, new class_238(min, max), strokeColor, fillColor, 3.0F);
   }

   private static void steppedWall(class_12177 primitives, class_2338 anchor, class_243 smooth, int strokeColor, int fillColor, int step, boolean mirrored) {
      class_243 origin = class_243.method_24954(anchor).method_1019(smooth);
      int crossColor = ColorUtil.multiplyAlpha(strokeColor, 0.6F);
      double x = mirrored ? step : -step;
      List<class_243> outline = new ArrayList<>();
      class_243 current = origin;
      outline.add(current);
      current = current.method_1031(x, 0.0, 0.0);
      outline.add(current);

      for (int i = 0; i < 4; i++) {
         class_243 var19 = current.method_1031(0.0, 0.0, step);
         outline.add(var19);
         current = var19.method_1031(x, 0.0, 0.0);
         outline.add(current);
      }

      current = current.method_1031(0.0, 0.0, step);
      outline.add(current);
      current = current.method_1031(x * -2.0, 0.0, 0.0);
      outline.add(current);

      for (int i = 0; i < 3; i++) {
         class_243 var22 = current.method_1031(0.0, 0.0, -step);
         outline.add(var22);
         current = var22.method_1031(-x, 0.0, 0.0);
         outline.add(current);
      }

      outline.add(current.method_1031(0.0, 0.0, step * -2.0));

      for (class_243 point : outline) {
         primitives.method_75473(point, point.method_1031(0.0, 5.0, 0.0), strokeColor, 2.0F);
      }

      for (int i = 0; i < outline.size() - 1; i++) {
         class_243 first = outline.get(i);
         class_243 second = outline.get(i + 1);
         class_243 firstTop = first.method_1031(0.0, 5.0, 0.0);
         class_243 secondTop = second.method_1031(0.0, 5.0, 0.0);
         primitives.method_75473(first, second, strokeColor, 2.0F);
         primitives.method_75473(firstTop, secondTop, strokeColor, 2.0F);
         quad(primitives, first, second, secondTop, firstTop, fillColor);
         primitives.method_75473(first, secondTop, crossColor, 1.6F);
         primitives.method_75473(second, firstTop, crossColor, 1.6F);
      }

      stratumFloor(primitives, origin, x, step, fillColor, crossColor);
      stratumFloor(primitives, origin.method_1031(0.0, 5.0, 0.0), x, step, fillColor, crossColor);
   }

   private static void stratumFloor(class_12177 primitives, class_243 origin, double x, int step, int fillColor, int crossColor) {
      class_243 cell = origin;
      stratumCell(primitives, cell, x, step * 2.0, fillColor, crossColor);

      for (int i = 0; i < 3; i++) {
         cell = cell.method_1031(x, 0.0, step);
         stratumCell(primitives, cell, x, step * 2.0, fillColor, crossColor);
      }

      stratumCell(primitives, cell.method_1031(x, 0.0, step), x, step, fillColor, crossColor);
   }

   private static void stratumCell(class_12177 primitives, class_243 corner, double x, double depth, int fillColor, int crossColor) {
      class_243 alongX = corner.method_1031(x, 0.0, 0.0);
      class_243 opposite = corner.method_1031(x, 0.0, depth);
      class_243 alongZ = corner.method_1031(0.0, 0.0, depth);
      quad(primitives, corner, alongX, opposite, alongZ, fillColor);
      primitives.method_75473(corner, opposite, crossColor, 1.6F);
      primitives.method_75473(alongX, alongZ, crossColor, 1.6F);
   }

   private static void crossBox(class_12177 primitives, class_238 box, int strokeColor, int fillColor, float strokeWidth) {
      class_243 downNorthWest = new class_243(box.field_1323, box.field_1322, box.field_1321);
      class_243 downNorthEast = new class_243(box.field_1320, box.field_1322, box.field_1321);
      class_243 downSouthEast = new class_243(box.field_1320, box.field_1322, box.field_1324);
      class_243 downSouthWest = new class_243(box.field_1323, box.field_1322, box.field_1324);
      class_243 upNorthWest = new class_243(box.field_1323, box.field_1325, box.field_1321);
      class_243 upNorthEast = new class_243(box.field_1320, box.field_1325, box.field_1321);
      class_243 upSouthEast = new class_243(box.field_1320, box.field_1325, box.field_1324);
      class_243 upSouthWest = new class_243(box.field_1323, box.field_1325, box.field_1324);
      quad(primitives, downNorthWest, downNorthEast, downSouthEast, downSouthWest, fillColor);
      quad(primitives, upNorthWest, upSouthWest, upSouthEast, upNorthEast, fillColor);
      quad(primitives, downNorthWest, upNorthWest, upNorthEast, downNorthEast, fillColor);
      quad(primitives, downSouthWest, downSouthEast, upSouthEast, upSouthWest, fillColor);
      quad(primitives, downNorthWest, downSouthWest, upSouthWest, upNorthWest, fillColor);
      quad(primitives, downNorthEast, upNorthEast, upSouthEast, downSouthEast, fillColor);
      primitives.method_75473(downNorthWest, downNorthEast, strokeColor, strokeWidth);
      primitives.method_75473(downNorthEast, downSouthEast, strokeColor, strokeWidth);
      primitives.method_75473(downSouthEast, downSouthWest, strokeColor, strokeWidth);
      primitives.method_75473(downSouthWest, downNorthWest, strokeColor, strokeWidth);
      primitives.method_75473(downNorthWest, upNorthWest, strokeColor, strokeWidth);
      primitives.method_75473(downNorthEast, upNorthEast, strokeColor, strokeWidth);
      primitives.method_75473(downSouthEast, upSouthEast, strokeColor, strokeWidth);
      primitives.method_75473(downSouthWest, upSouthWest, strokeColor, strokeWidth);
      primitives.method_75473(upNorthWest, upNorthEast, strokeColor, strokeWidth);
      primitives.method_75473(upNorthEast, upSouthEast, strokeColor, strokeWidth);
      primitives.method_75473(upSouthEast, upSouthWest, strokeColor, strokeWidth);
      primitives.method_75473(upSouthWest, upNorthWest, strokeColor, strokeWidth);
      int crossColor = ColorUtil.multiplyAlpha(strokeColor, 0.6F);
      float crossWidth = strokeWidth * 0.8F;
      primitives.method_75473(downNorthWest, downSouthEast, crossColor, crossWidth);
      primitives.method_75473(downNorthEast, downSouthWest, crossColor, crossWidth);
      primitives.method_75473(upNorthWest, upSouthEast, crossColor, crossWidth);
      primitives.method_75473(upNorthEast, upSouthWest, crossColor, crossWidth);
      primitives.method_75473(downNorthWest, upNorthEast, crossColor, crossWidth);
      primitives.method_75473(downNorthEast, upNorthWest, crossColor, crossWidth);
      primitives.method_75473(downSouthWest, upSouthEast, crossColor, crossWidth);
      primitives.method_75473(downSouthEast, upSouthWest, crossColor, crossWidth);
      primitives.method_75473(downNorthWest, upSouthWest, crossColor, crossWidth);
      primitives.method_75473(downSouthWest, upNorthWest, crossColor, crossWidth);
      primitives.method_75473(downNorthEast, upSouthEast, crossColor, crossWidth);
      primitives.method_75473(downSouthEast, upNorthEast, crossColor, crossWidth);
   }

   private static void quad(class_12177 primitives, class_243 first, class_243 second, class_243 third, class_243 fourth, int fillColor) {
      primitives.method_75474(first, second, third, fourth, fillColor);
      primitives.method_75474(fourth, third, second, first, fillColor);
   }
}
