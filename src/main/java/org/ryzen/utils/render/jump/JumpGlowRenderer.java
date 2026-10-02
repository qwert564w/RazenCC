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
import net.minecraft.class_10789;
import net.minecraft.class_12137;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_287;
import net.minecraft.class_290;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import net.minecraft.class_6367;
import net.minecraft.class_9799;
import net.minecraft.class_9801;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;

@Environment(EnvType.CLIENT)
public final class JumpGlowRenderer {
   private static final int SEGMENTS = 48;
   private static final int VERTEX_COUNT = 288;
   private static final float GLOW_HEIGHT = 1.0F;
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec4().putVec4().get();
   private static final RenderPipeline PIPELINE = RenderPipeline.builder(new Snippet[0])
      .withLocation(class_2960.method_60654("ryzen:pipeline/world/jump_glow"))
      .withVertexShader(class_2960.method_60654("ryzen:core/jump_glow"))
      .withFragmentShader(class_2960.method_60654("ryzen:core/jump_glow"))
      .withUniform("Projection", class_10789.field_60031)
      .withSampler("SceneSampler")
      .withUniform("JumpGlowUniforms", class_10789.field_60031)
      .withBlend(BlendFunction.TRANSLUCENT)
      .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
      .withDepthWrite(false)
      .withVertexFormat(class_290.field_1585, class_5596.field_27379)
      .withCull(false)
      .build();
   private final SceneSnapshot scene = new SceneSnapshot("blade-jump-glow-scene");

   public void render(class_243 center, float radius, float ringProgress, int color, float fade, float time) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 != null && mc.field_1724 != null && !(fade <= 0.003F) && !(radius <= 0.001F)) {
         class_276 target = mc.method_1522();
         GpuTextureView colorView = target != null ? target.method_71639() : null;
         if (colorView != null) {
            class_6367 sceneCopy = this.scene.capture();
            if (sceneCopy != null) {
               float currentRadius = Math.max(0.001F, radius * ringProgress);
               GpuDevice device = RenderSystem.getDevice();
               class_9801 meshData = this.buildMesh(mc, center, currentRadius);
               GpuBuffer vertexBuffer = null;
               GpuBuffer uniformBuffer = null;

               try {
                  vertexBuffer = device.createBuffer(() -> "Ryzen Jump Glow Vertices", 40, meshData.method_60818());
                  uniformBuffer = this.uploadUniform(color, fade, ringProgress, time);
                  class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                  GpuTextureView depthView = target.method_71640();
                  RenderPass pass = depthView != null
                     ? device.createCommandEncoder()
                        .createRenderPass(() -> "Ryzen Jump Glow Pass", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())
                     : device.createCommandEncoder().createRenderPass(() -> "Ryzen Jump Glow Pass", colorView, OptionalInt.empty());

                  try {
                     pass.setPipeline(PIPELINE);
                     pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                     pass.setUniform("JumpGlowUniforms", uniformBuffer);
                     pass.bindTexture("SceneSampler", sceneCopy.method_71639(), sampler);
                     pass.setVertexBuffer(0, vertexBuffer);
                     pass.draw(0, 288);
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
      class_243 base = center.method_1031(0.0, 0.02, 0.0);
      class_287 builder = new class_287(class_9799.method_72201(288 * class_290.field_1585.getVertexSize()), class_5596.field_27379, class_290.field_1585);

      for (int i = 0; i < 48; i++) {
         float a0 = (float)((Math.PI * 2) * i / 48.0);
         float a1 = (float)((Math.PI * 2) * (i + 1) / 48.0);
         float u0 = i / 48.0F;
         float u1 = (i + 1) / 48.0F;
         class_243 b0 = base.method_1031(Math.cos(a0) * radius, 0.0, Math.sin(a0) * radius);
         class_243 b1 = base.method_1031(Math.cos(a1) * radius, 0.0, Math.sin(a1) * radius);
         class_243 t0 = b0.method_1031(0.0, 1.0, 0.0);
         class_243 t1 = b1.method_1031(0.0, 1.0, 0.0);
         Vector4f vb0 = Render3DUtil.toViewSpace(b0, cameraPos, pose);
         Vector4f vb1 = Render3DUtil.toViewSpace(b1, cameraPos, pose);
         Vector4f vt0 = Render3DUtil.toViewSpace(t0, cameraPos, pose);
         Vector4f vt1 = Render3DUtil.toViewSpace(t1, cameraPos, pose);
         this.addVertex(builder, vb0, u0, 0.0F);
         this.addVertex(builder, vb1, u1, 0.0F);
         this.addVertex(builder, vt1, u1, 1.0F);
         this.addVertex(builder, vb0, u0, 0.0F);
         this.addVertex(builder, vt1, u1, 1.0F);
         this.addVertex(builder, vt0, u0, 1.0F);
      }

      return builder.method_60800();
   }

   private void addVertex(class_287 builder, Vector4f point, float u, float v) {
      builder.method_22912(point.x, point.y, point.z).method_22913(u, v);
   }

   private GpuBuffer uploadUniform(int color, float fade, float ringProgress, float time) {
      GpuDevice device = RenderSystem.getDevice();
      GpuBuffer buffer = device.createBuffer(() -> "Ryzen Jump Glow UBO", 136, UNIFORM_SIZE);
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
            .putVec4(ColorUtil.red(color) / 255.0F, ColorUtil.green(color) / 255.0F, ColorUtil.blue(color) / 255.0F, ColorUtil.alpha(color) / 255.0F)
            .putVec4(fade, time, 0.0F, ringProgress)
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

   public void release() {
      this.scene.release();
   }
}
