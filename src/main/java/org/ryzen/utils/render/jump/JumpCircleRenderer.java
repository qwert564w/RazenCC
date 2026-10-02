package org.ryzen.utils.render.jump;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1044;
import net.minecraft.class_10789;
import net.minecraft.class_12137;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Textures;

@Environment(EnvType.CLIENT)
public final class JumpCircleRenderer {
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60654("ryzen:pipeline/world/jump_circle"))
      .withVertexShader(class_2960.method_60654("ryzen:core/jump_circle"))
      .withFragmentShader(class_2960.method_60654("ryzen:core/jump_circle"))
      .withUniform("Projection", class_10789.field_60031)
      .withSampler("iChannel0")
      .withUniform("JumpCircleUniforms", class_10789.field_60031)
      .withBlend(BlendFunction.LIGHTNING)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withVertexFormat(class_290.field_1585, class_5596.field_27379)
      .withCull(false)
      .build();

   public void render(class_243 center, float radius, float alpha, float time, int color, boolean glowEdge) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null && !(radius <= 0.001F) && !(alpha <= 0.003F)) {
         class_276 target = mc.method_1522();
         GpuTextureView colorView = target != null ? target.method_71639() : null;
         if (colorView != null) {
            class_1044 frequencyTexture = mc.method_1531().method_4619(Textures.Shader.JUMP_FREQUENCY);
            GpuTextureView frequencyView = frequencyTexture != null ? frequencyTexture.method_71659() : null;
            if (frequencyView != null) {
               GpuDevice device = RenderSystem.getDevice();
               class_9801 meshData = this.buildMesh(mc, center, radius);
               GpuBuffer vertexBuffer = null;
               GpuBuffer uniformBuffer = null;

               try {
                  vertexBuffer = device.createBuffer(() -> "Ryzen Jump Circle Vertices", 40, meshData.method_60818());
                  uniformBuffer = this.uploadUniform(color, time, alpha, glowEdge);
                  class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                  GpuTextureView depthView = target.method_71640();
                  RenderPass pass = depthView != null
                     ? device.createCommandEncoder()
                        .createRenderPass(() -> "Ryzen Jump Circle Pass", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())
                     : device.createCommandEncoder().createRenderPass(() -> "Ryzen Jump Circle Pass", colorView, OptionalInt.empty());

                  try {
                     pass.setPipeline(PIPELINE);
                     pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                     pass.setUniform("JumpCircleUniforms", uniformBuffer);
                     pass.bindTexture("iChannel0", frequencyView, sampler);
                     pass.setVertexBuffer(0, vertexBuffer);
                     pass.draw(0, 6);
                  } finally {
                     pass.close();
                  }
               } finally {
                  if (uniformBuffer != null) {
                     uniformBuffer.close();
                  }

                  if (vertexBuffer != null) {
                     vertexBuffer.close();
                  }

                  meshData.close();
               }
            }
         }
      }
   }

   private class_9801 buildMesh(class_310 mc, class_243 center, float radius) {
      class_4184 camera = mc.field_1773.method_19418();
      class_243 cameraPos = camera.method_71156();
      Matrix4f pose = Render3DUtil.cameraViewPose(camera);
      class_243 xAxis = new class_243(radius, 0.0, 0.0);
      class_243 zAxis = new class_243(0.0, 0.0, radius);
      class_243 lifted = center.method_1031(0.0, 0.04, 0.0);
      Vector4f p1 = Render3DUtil.toViewSpace(lifted.method_1019(xAxis).method_1019(zAxis), cameraPos, pose);
      Vector4f p2 = Render3DUtil.toViewSpace(lifted.method_1019(xAxis).method_1020(zAxis), cameraPos, pose);
      Vector4f p3 = Render3DUtil.toViewSpace(lifted.method_1020(xAxis).method_1020(zAxis), cameraPos, pose);
      Vector4f p4 = Render3DUtil.toViewSpace(lifted.method_1020(xAxis).method_1019(zAxis), cameraPos, pose);
      class_287 builder = new class_287(class_9799.method_72201(6 * class_290.field_1585.getVertexSize()), class_5596.field_27379, class_290.field_1585);
      this.addVertex(builder, p1, 1.0F, 1.0F);
      this.addVertex(builder, p2, 1.0F, 0.0F);
      this.addVertex(builder, p3, 0.0F, 0.0F);
      this.addVertex(builder, p1, 1.0F, 1.0F);
      this.addVertex(builder, p3, 0.0F, 0.0F);
      this.addVertex(builder, p4, 0.0F, 1.0F);
      return builder.method_60800();
   }

   private void addVertex(class_287 builder, Vector4f point, float u, float v) {
      builder.method_22912(point.x, point.y, point.z).method_22913(u, v);
   }

   private GpuBuffer uploadUniform(int color, float time, float alpha, boolean glowEdge) {
      GpuDevice device = RenderSystem.getDevice();
      GpuBuffer buffer = device.createBuffer(() -> "Ryzen Jump Circle UBO", 136, UNIFORM_SIZE);
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
            .putVec4(ColorUtil.red(color) / 255.0F, ColorUtil.green(color) / 255.0F, ColorUtil.blue(color) / 255.0F, ColorUtil.alpha(color) / 255.0F)
            .putVec4(time, glowEdge ? 1.0F : 0.0F, 0.0F, alpha)
            .get();
         device.createCommandEncoder().writeToBuffer(buffer.slice(), data);
      } catch (Throwable var11) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var10) {
               var11.addSuppressed(var10);
            }
         }

         throw var11;
      }

      if (stack != null) {
         stack.close();
      }

      return buffer;
   }
}
