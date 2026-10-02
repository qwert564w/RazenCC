package org.ryzen.utils.render.particles;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_1058;
import net.minecraft.class_1087;
import net.minecraft.class_11659;
import net.minecraft.class_11785;
import net.minecraft.class_11791;
import net.minecraft.class_12075;
import net.minecraft.class_1297;
import net.minecraft.class_1921;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3879;
import net.minecraft.class_4587;
import net.minecraft.class_4588;
import net.minecraft.class_5481;
import net.minecraft.class_630;
import net.minecraft.class_777;
import net.minecraft.class_811;
import net.minecraft.class_10017.class_10018;
import net.minecraft.class_10017.class_11680;
import net.minecraft.class_10444.class_10445;
import net.minecraft.class_11659.class_11660;
import net.minecraft.class_11659.class_11947;
import net.minecraft.class_11683.class_11792;
import net.minecraft.class_327.class_6415;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.ryzen.utils.render.EntityEspDispatcherBridge;

@Environment(EnvType.CLIENT)
public final class ModelPointSampler implements class_11659 {
   private final List<Vector3f[]> quads = new ArrayList<>();
   private final ModelPointSampler.CapturingConsumer consumer = new ModelPointSampler.CapturingConsumer();

   private ModelPointSampler() {
   }

   public static List<Vector3f> sample(class_10017 state, class_12075 cameraState, int budget, Random random) {
      class_310 mc = class_310.method_1551();
      if (cameraState != null && mc.field_1724 != null && mc.method_1561() instanceof EntityEspDispatcherBridge bridge) {
         mc.method_1561().method_3941(mc.field_1773.method_19418(), (class_1297)(mc.field_1692 != null ? mc.field_1692 : mc.field_1724));
         ModelPointSampler sampler = new ModelPointSampler();

         try {
            bridge.submitForGlow(state, cameraState, 0.0, 0.0, 0.0, new class_4587(), sampler);
         } catch (Exception ignored) {
            return List.of();
         }

         return sampler.scatterPoints(budget, random);
      } else {
         return List.of();
      }
   }

   private List<Vector3f> scatterPoints(int budget, Random random) {
      if (this.quads.isEmpty()) {
         return List.of();
      }

      float totalArea = 0.0F;
      float[] areas = new float[this.quads.size()];

      for (int i = 0; i < this.quads.size(); i++) {
         areas[i] = quadArea(this.quads.get(i));
         totalArea += areas[i];
      }

      if (totalArea <= 1.0E-4F) {
         return List.of();
      }

      List<Vector3f> points = new ArrayList<>(budget);

      for (int i = 0; i < this.quads.size(); i++) {
         Vector3f[] quad = this.quads.get(i);
         float share = areas[i] / totalArea * budget;
         int count = (int)share + (random.nextFloat() < share - (int)share ? 1 : 0);

         for (int p = 0; p < count; p++) {
            float u = random.nextFloat();
            float v = random.nextFloat();
            points.add(bilinear(quad, u, v));
         }
      }

      return points;
   }

   private static float quadArea(Vector3f[] quad) {
      Vector3f edge1 = new Vector3f(quad[1]).sub(quad[0]);
      Vector3f edge2 = new Vector3f(quad[3]).sub(quad[0]);
      return new Vector3f(edge1).cross(edge2).length();
   }

   private static Vector3f bilinear(Vector3f[] quad, float u, float v) {
      Vector3f top = new Vector3f(quad[0]).lerp(quad[1], u);
      Vector3f bottom = new Vector3f(quad[3]).lerp(quad[2], u);
      return top.lerp(bottom, v);
   }

   public class_11785 method_73529(int order) {
      return this;
   }

   public <S> void method_73490(
      class_3879<? super S> model,
      S state,
      class_4587 poseStack,
      class_1921 renderType,
      int light,
      int overlay,
      int color,
      class_1058 sprite,
      int outlineColor,
      class_11792 crumblingOverlay
   ) {
      model.method_2819(state);
      model.method_62100(poseStack, this.consumer, light, overlay, -1);
      this.consumer.finishQuad();
      model.method_63514();
   }

   public void method_73479(class_4587 poseStack, float alpha, List<class_11680> pieces) {
   }

   public void method_73482(
      class_4587 poseStack, class_243 offset, int light, class_2561 text, boolean seeThrough, int color, double distance, class_12075 cameraState
   ) {
   }

   public void method_73478(
      class_4587 poseStack,
      float x,
      float y,
      class_5481 text,
      boolean dropShadow,
      class_6415 displayMode,
      int light,
      int color,
      int backgroundColor,
      int outlineColor
   ) {
   }

   public void method_73488(class_4587 poseStack, class_10017 state, Quaternionf rotation) {
   }

   public void method_73486(class_4587 poseStack, class_10018 leashState) {
   }

   public void method_73494(
      class_630 modelPart,
      class_4587 poseStack,
      class_1921 renderType,
      int light,
      int overlay,
      class_1058 sprite,
      boolean hasFoil,
      boolean glint,
      int color,
      class_11792 crumblingOverlay,
      int outlineColor
   ) {
   }

   public void method_73481(class_4587 poseStack, class_2680 state, int light, int overlay, int outlineColor) {
   }

   public void method_73485(class_4587 poseStack, class_11791 state) {
   }

   public void method_73484(
      class_4587 poseStack, class_1921 renderType, class_1087 model, float red, float green, float blue, int light, int overlay, int outlineColor
   ) {
   }

   public void method_73480(
      class_4587 poseStack,
      class_811 context,
      int light,
      int overlay,
      int outlineColor,
      int[] tints,
      List<class_777> quads,
      class_1921 renderType,
      class_10445 foilType
   ) {
   }

   public void method_73483(class_4587 poseStack, class_1921 renderType, class_11660 renderer) {
   }

   public void method_74315(class_11947 renderer) {
   }

   @Environment(EnvType.CLIENT)
   private final class CapturingConsumer implements class_4588 {
      private final Vector3f[] pending = new Vector3f[4];
      private int pendingCount;

      public class_4588 method_22912(float x, float y, float z) {
         this.pending[this.pendingCount++] = new Vector3f(x, y, z);
         if (this.pendingCount == 4) {
            ModelPointSampler.this.quads.add((Vector3f[])this.pending.clone());
            this.pendingCount = 0;
         }

         return this;
      }

      private void finishQuad() {
         this.pendingCount = 0;
      }

      public class_4588 method_1336(int red, int green, int blue, int alpha) {
         return this;
      }

      public class_4588 method_39415(int color) {
         return this;
      }

      public class_4588 method_22913(float u, float v) {
         return this;
      }

      public class_4588 method_60796(int u, int v) {
         return this;
      }

      public class_4588 method_22921(int u, int v) {
         return this;
      }

      public class_4588 method_22914(float x, float y, float z) {
         return this;
      }

      public class_4588 method_75298(float width) {
         return this;
      }
   }
}
