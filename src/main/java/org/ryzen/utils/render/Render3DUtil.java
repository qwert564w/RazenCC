package org.ryzen.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public final class Render3DUtil {
   private static final Matrix4f LEVEL_PROJECTION = new Matrix4f();
   private static boolean levelProjectionCaptured;

   private Render3DUtil() {
   }

   public static class_243 interpolatedPosition(class_1297 entity, float tickDelta) {
      return new class_243(
         class_3532.method_16436(tickDelta, entity.field_6038, entity.method_23317()),
         class_3532.method_16436(tickDelta, entity.field_5971, entity.method_23318()),
         class_3532.method_16436(tickDelta, entity.field_5989, entity.method_23321())
      );
   }

   public static Matrix4f buildBillboardPose(class_4184 camera, class_243 worldPos, double offsetY, float zRotationDegrees) {
      class_243 cameraPos = camera.method_71156();
      class_4587 poseStack = new class_4587();
      poseStack.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(camera.method_19330() + 180.0F));
      poseStack.method_22904(
         worldPos.field_1352 - cameraPos.field_1352, worldPos.field_1351 - cameraPos.field_1351 + offsetY, worldPos.field_1350 - cameraPos.field_1350
      );
      poseStack.method_22907(class_7833.field_40716.rotationDegrees(-camera.method_19330()));
      poseStack.method_22907(class_7833.field_40714.rotationDegrees(camera.method_19329()));
      if (zRotationDegrees != 0.0F) {
         poseStack.method_22907(class_7833.field_40718.rotationDegrees(zRotationDegrees));
      }

      return new Matrix4f(poseStack.method_23760().method_23761());
   }

   public static Matrix4f cameraViewPose(class_4184 camera) {
      return new Matrix4f().rotation(new Quaternionf(camera.method_23767()).conjugate());
   }

   public static Vector4f toViewSpace(class_243 point, class_243 cameraPos, Matrix4f pose) {
      return new Vector4f(
            (float)(point.field_1352 - cameraPos.field_1352),
            (float)(point.field_1351 - cameraPos.field_1351),
            (float)(point.field_1350 - cameraPos.field_1350),
            1.0F
         )
         .mul(pose);
   }

   public static void captureLevelProjection(Matrix4fc projection) {
      LEVEL_PROJECTION.set(projection);
      levelProjectionCaptured = true;
   }

   public static Matrix4f levelProjectionCopy() {
      return levelProjectionCaptured ? new Matrix4f(LEVEL_PROJECTION) : null;
   }

   public static Render3DUtil.ScreenPoint projectToScreen(class_310 mc, class_243 pos) {
      if (pos != null && mc.method_22683() != null && mc.field_1773 != null && levelProjectionCaptured) {
         class_12075 cameraState = mc.field_1773.method_72912().field_63082;
         if (!cameraState.field_63079) {
            return null;
         }

         int guiWidth = mc.method_22683().method_4486();
         int guiHeight = mc.method_22683().method_4502();
         if (guiWidth > 0 && guiHeight > 0) {
            Vector4f clip = new Vector4f(
               (float)(pos.field_1352 - cameraState.field_63078.method_10216()),
               (float)(pos.field_1351 - cameraState.field_63078.method_10214()),
               (float)(pos.field_1350 - cameraState.field_63078.method_10215()),
               1.0F
            );
            new Quaternionf(cameraState.field_63081).conjugate().transform(clip);
            LEVEL_PROJECTION.transform(clip);
            if (clip.w <= 1.0E-4F) {
               return null;
            } else {
               float ndcX = clip.x / clip.w;
               float ndcY = clip.y / clip.w;
               if (!(Math.abs(ndcX) > 2.0F) && !(Math.abs(ndcY) > 2.0F)) {
                  float screenX = (ndcX + 1.0F) * 0.5F * guiWidth;
                  float screenY = (1.0F - ndcY) * 0.5F * guiHeight;
                  return new Render3DUtil.ScreenPoint(screenX, screenY);
               } else {
                  return null;
               }
            }
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   public static Render3DUtil.ScreenBounds includeInBounds(class_310 mc, Render3DUtil.ScreenBounds bounds, double x, double y, double z) {
      Render3DUtil.ScreenPoint point = projectToScreen(mc, new class_243(x, y, z));
      if (point == null) {
         return bounds;
      } else {
         return bounds == null
            ? new Render3DUtil.ScreenBounds(point.x(), point.y(), point.x(), point.y())
            : new Render3DUtil.ScreenBounds(
               Math.min(bounds.minX(), point.x()), Math.min(bounds.minY(), point.y()), Math.max(bounds.maxX(), point.x()), Math.max(bounds.maxY(), point.y())
            );
      }
   }

   public static Render3DUtil.ScreenBounds projectBoxBounds(class_310 mc, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
      Render3DUtil.ScreenBounds bounds = null;
      bounds = includeInBounds(mc, bounds, minX, minY, minZ);
      bounds = includeInBounds(mc, bounds, minX, minY, maxZ);
      bounds = includeInBounds(mc, bounds, minX, maxY, minZ);
      bounds = includeInBounds(mc, bounds, minX, maxY, maxZ);
      bounds = includeInBounds(mc, bounds, maxX, minY, minZ);
      bounds = includeInBounds(mc, bounds, maxX, minY, maxZ);
      bounds = includeInBounds(mc, bounds, maxX, maxY, minZ);
      return includeInBounds(mc, bounds, maxX, maxY, maxZ);
   }

   @Environment(EnvType.CLIENT)
   public record ScreenBounds(float minX, float minY, float maxX, float maxY) {
      public float width() {
         return this.maxX - this.minX;
      }

      public float height() {
         return this.maxY - this.minY;
      }
   }

   @Environment(EnvType.CLIENT)
   public record ScreenPoint(float x, float y) {
   }
}
