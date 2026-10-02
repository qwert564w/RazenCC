package org.ryzen.utils.render.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11244;
import net.minecraft.class_8030;
import org.joml.Matrix3x2fc;

@Environment(EnvType.CLIENT)
abstract class UiElementRenderState implements class_11244 {
   private final float m00;
   private final float m01;
   private final float m10;
   private final float m11;
   private final float m20;
   private final float m21;
   private final class_8030 bounds;
   private final class_8030 scissor;

   protected UiElementRenderState(Matrix3x2fc pose, class_8030 scissor, float x, float y, float width, float height) {
      this.m00 = pose.m00();
      this.m01 = pose.m01();
      this.m10 = pose.m10();
      this.m11 = pose.m11();
      this.m20 = pose.m20();
      this.m21 = pose.m21();
      this.scissor = scissor;
      this.bounds = new class_8030(Math.round(x), Math.round(y), Math.max(1, Math.round(width)), Math.max(1, Math.round(height))).method_71523(pose);
   }

   protected final float transformX(float x, float y) {
      return this.m00 * x + this.m10 * y + this.m20;
   }

   protected final float transformY(float x, float y) {
      return this.m01 * x + this.m11 * y + this.m21;
   }

   public final class_8030 comp_4069() {
      return this.scissor;
   }

   public final class_8030 comp_4274() {
      return this.bounds;
   }
}
