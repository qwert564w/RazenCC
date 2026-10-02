package org.ryzen.utils.render.target;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_12137;
import net.minecraft.class_1309;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.HurtUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Textures;

@Environment(EnvType.CLIENT)
public final class AuraMarkerRenderer {
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec4().get();
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60654("ryzen:pipeline/world/aura_marker"))
      .withVertexShader(class_2960.method_60654("ryzen:core/aura_marker"))
      .withFragmentShader(class_2960.method_60654("ryzen:core/aura_marker"))
      .withUniform("Projection", class_10789.field_60031)
      .withSampler("texSampler")
      .withUniform("params", class_10789.field_60031)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
      .withDepthWrite(false)
      .withVertexFormat(class_290.field_1585, class_5596.field_27380)
      .withCull(false)
      .build();
   private GpuBuffer paramsBuffer;
   private class_1309 lastTarget;
   private float alpha;
   private long lastFrameTime;

   public void render(class_1309 activeTarget, float tickDelta, int baseColor) {
      class_310 mc = class_310.method_1551();
      if (mc != null && mc.field_1724 != null && mc.field_1687 != null && mc.field_1773 != null) {
         this.updateAnimation(activeTarget);
         class_1309 target = this.lastTarget;
         if (target != null) {
            if (this.alpha <= 0.01F) {
               this.reset();
            } else {
               AuraMarkerRenderer.MarkerGeometry geometry = this.markerGeometry(mc, target, tickDelta);
               if (geometry != null) {
                  this.renderMarker(mc, geometry, HurtUtil.blend(baseColor, target, this.alpha));
               }
            }
         }
      } else {
         this.reset();
      }
   }

   public void reset() {
      this.lastTarget = null;
      this.alpha = 0.0F;
      this.lastFrameTime = 0L;
   }

   private void updateAnimation(class_1309 activeTarget) {
      long now = System.currentTimeMillis();
      float delta = this.lastFrameTime == 0L ? 0.016F : (float)Math.min(100L, now - this.lastFrameTime) / 1000.0F;
      this.lastFrameTime = now;
      if (valid(activeTarget)) {
         this.lastTarget = activeTarget;
      }

      float targetAlpha = valid(activeTarget) ? 1.0F : 0.0F;
      float step = class_3532.method_15363(delta * 8.0F, 0.0F, 1.0F);
      this.alpha = this.alpha + (targetAlpha - this.alpha) * step;
   }

   private AuraMarkerRenderer.MarkerGeometry markerGeometry(class_310 mc, class_1309 target, float tickDelta) {
      class_4184 camera = mc.field_1773.method_19418();
      if (camera != null && camera.method_19332()) {
         class_243 position = Render3DUtil.interpolatedPosition(target, tickDelta);
         float widthScale;
         if (target.method_17681() < 1.0F) {
            widthScale = 0.95F;
         } else if (target.method_17681() > 2.0F) {
            widthScale = 1.45F;
         } else {
            widthScale = 1.0F;
         }

         float halfSize = 0.5F * widthScale * this.alpha * HurtUtil.scale(target, 0.12F);
         if (halfSize <= 0.001F) {
            return null;
         }

         Matrix4f pose = Render3DUtil.buildBillboardPose(
            camera, position, target.method_17682() / 2.0, (float)(Math.sin(System.currentTimeMillis() / 1000.0) * 360.0)
         );
         return new AuraMarkerRenderer.MarkerGeometry(pose, halfSize);
      } else {
         return null;
      }
   }

   private void renderMarker(class_310 mc, AuraMarkerRenderer.MarkerGeometry geometry, int color) {
      class_276 target = mc.method_1522();
      GpuTextureView colorView = target != null ? target.method_71639() : null;
      if (colorView != null) {
         class_1044 texture = mc.method_1531().method_4619(Textures.TARGET);
         GpuTextureView textureView = texture != null ? texture.method_71659() : null;
         if (textureView != null) {
            this.ensureParamsBuffer();
            this.writeParams(color);
            class_9801 mesh = this.buildMesh(geometry);
            GpuBuffer vertexBuffer = null;

            try {
               vertexBuffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen Aura Marker Vertices", 32, mesh.method_60818());
               class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
               RenderPass pass = RenderSystem.getDevice()
                  .createCommandEncoder()
                  .createRenderPass(() -> "Ryzen Aura Marker Pass", colorView, OptionalInt.empty());

               try {
                  pass.setPipeline(PIPELINE);
                  pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                  pass.setUniform("params", this.paramsBuffer);
                  pass.bindTexture("texSampler", textureView, sampler);
                  pass.setVertexBuffer(0, vertexBuffer);
                  pass.draw(0, 4);
               } catch (Throwable var19) {
                  if (pass != null) {
                     try {
                        pass.close();
                     } catch (Throwable var18) {
                        var19.addSuppressed(var18);
                     }
                  }

                  throw var19;
               }

               if (pass != null) {
                  pass.close();
               }
            } finally {
               if (vertexBuffer != null) {
                  vertexBuffer.close();
               }

               mesh.close();
            }
         }
      }
   }

   private class_9801 buildMesh(AuraMarkerRenderer.MarkerGeometry geometry) {
      float halfSize = geometry.halfSize;
      class_287 builder = new class_287(class_9799.method_72201(4 * class_290.field_1585.getVertexSize()), class_5596.field_27380, class_290.field_1585);
      builder.method_22918(geometry.pose, -halfSize, -halfSize, 0.0F).method_22913(0.0F, 1.0F);
      builder.method_22918(geometry.pose, -halfSize, halfSize, 0.0F).method_22913(0.0F, 0.0F);
      builder.method_22918(geometry.pose, halfSize, -halfSize, 0.0F).method_22913(1.0F, 1.0F);
      builder.method_22918(geometry.pose, halfSize, halfSize, 0.0F).method_22913(1.0F, 0.0F);
      return builder.method_60800();
   }

   private void ensureParamsBuffer() {
      if (this.paramsBuffer == null) {
         this.paramsBuffer = RenderSystem.getDevice().createBuffer(() -> "Ryzen Aura Marker UBO", 136, UNIFORM_SIZE);
      }
   }

   private void writeParams(int color) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
            .putVec4(ColorUtil.red(color) / 255.0F, ColorUtil.green(color) / 255.0F, ColorUtil.blue(color) / 255.0F, ColorUtil.alpha(color) / 255.0F)
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.paramsBuffer.slice(), data);
      } catch (Throwable var6) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var5) {
               var6.addSuppressed(var5);
            }
         }

         throw var6;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private static boolean valid(class_1309 entity) {
      return entity != null && entity.method_5805() && !entity.method_31481();
   }

   public void release() {
      if (this.paramsBuffer != null) {
         this.paramsBuffer.close();
         this.paramsBuffer = null;
      }
   }

   @Environment(EnvType.CLIENT)
   private record MarkerGeometry(Matrix4f pose, float halfSize) {
   }
}
