package org.ryzen.utils.render.particles;

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
import java.util.List;
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
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Textures;

@Environment(EnvType.CLIENT)
public final class WorldParticleRenderer {
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putVec4().get();
   private static final RenderPipeline PIPELINE = buildPipeline("world_particle", BlendFunction.ADDITIVE, true);
   private static final RenderPipeline THROUGH_WALLS_PIPELINE = buildPipeline("world_particle_through_walls", BlendFunction.ADDITIVE, false);
   private static final RenderPipeline ALPHA_PIPELINE = buildPipeline("world_particle_alpha", BlendFunction.TRANSLUCENT, true);
   private static final RenderPipeline ALPHA_THROUGH_WALLS_PIPELINE = buildPipeline("world_particle_alpha_through_walls", BlendFunction.TRANSLUCENT, false);

   private static RenderPipeline buildPipeline(String name, BlendFunction blend, boolean depthTest) {
      return RenderPipeline.builder(new Snippet[0])
         .withLocation(class_2960.method_60654("ryzen:pipeline/world/" + name))
         .withVertexShader(class_2960.method_60654("ryzen:core/world_particle"))
         .withFragmentShader(class_2960.method_60654("ryzen:core/world_particle"))
         .withUniform("Projection", class_10789.field_60031)
         .withSampler("BloomSampler")
         .withUniform("WorldParticleUniforms", class_10789.field_60031)
         .withBlend(blend)
         .withDepthTestFunction(depthTest ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withVertexFormat(class_290.field_1575, class_5596.field_27379)
         .withCull(false)
         .build();
   }

   public void render(List<WorldParticleRenderer.Sprite> sprites, float brightness) {
      this.render(sprites, brightness, false, true);
   }

   public void render(List<WorldParticleRenderer.Sprite> sprites, float brightness, boolean throughWalls) {
      this.render(sprites, brightness, throughWalls, true);
   }

   public void render(List<WorldParticleRenderer.Sprite> sprites, float brightness, boolean throughWalls, boolean additive) {
      class_310 mc = class_310.method_1551();
      if (!sprites.isEmpty() && mc.field_1687 != null) {
         class_276 target = mc.method_1522();
         GpuTextureView colorView = target != null ? target.method_71639() : null;
         if (colorView != null) {
            class_1044 bloom = mc.method_1531().method_4619(Textures.Shader.BLOOM);
            GpuTextureView bloomView = bloom != null ? bloom.method_71659() : null;
            if (bloomView != null) {
               class_9801 meshData = this.buildMesh(mc, sprites);
               if (meshData != null) {
                  GpuDevice device = RenderSystem.getDevice();
                  GpuBuffer vertexBuffer = null;
                  GpuBuffer uniformBuffer = null;

                  try {
                     vertexBuffer = device.createBuffer(() -> "Ryzen World Particles Vertices", 40, meshData.method_60818());
                     uniformBuffer = this.uploadUniform(brightness);
                     class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
                     GpuTextureView depthView = target.method_71640();
                     RenderPass pass = !throughWalls && depthView != null
                        ? device.createCommandEncoder()
                           .createRenderPass(() -> "Ryzen World Particles Pass", colorView, OptionalInt.empty(), depthView, OptionalDouble.empty())
                        : device.createCommandEncoder().createRenderPass(() -> "Ryzen World Particles Pass", colorView, OptionalInt.empty());

                     try {
                        RenderPipeline pipeline = additive
                           ? (throughWalls ? THROUGH_WALLS_PIPELINE : PIPELINE)
                           : (throughWalls ? ALPHA_THROUGH_WALLS_PIPELINE : ALPHA_PIPELINE);
                        pass.setPipeline(pipeline);
                        pass.setUniform("Projection", RenderSystem.getProjectionMatrixBuffer());
                        pass.setUniform("WorldParticleUniforms", uniformBuffer);
                        pass.bindTexture("BloomSampler", bloomView, sampler);
                        pass.setVertexBuffer(0, vertexBuffer);
                        pass.draw(0, sprites.size() * 6);
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
   }

   private GpuBuffer uploadUniform(float brightness) {
      GpuDevice device = RenderSystem.getDevice();
      GpuBuffer buffer = device.createBuffer(() -> "Ryzen World Particles UBO", 136, UNIFORM_SIZE);
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE).putVec4(brightness, 0.0F, 0.0F, 0.0F).get();
         device.createCommandEncoder().writeToBuffer(buffer.slice(), data);
      } catch (Throwable var8) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }
         }

         throw var8;
      }

      if (stack != null) {
         stack.close();
      }

      return buffer;
   }

   private class_9801 buildMesh(class_310 mc, List<WorldParticleRenderer.Sprite> sprites) {
      class_4184 camera = mc.field_1773.method_19418();
      class_243 cameraPos = camera.method_71156();
      Matrix4f pose = Render3DUtil.cameraViewPose(camera);
      class_287 builder = new class_287(
         class_9799.method_72201(sprites.size() * 6 * class_290.field_1575.getVertexSize()), class_5596.field_27379, class_290.field_1575
      );

      for (WorldParticleRenderer.Sprite sprite : sprites) {
         Vector4f center = Render3DUtil.toViewSpace(sprite.position(), cameraPos, pose);
         float half = sprite.halfSize();
         int color = sprite.color();
         this.addVertex(builder, center, -half, -half, 0.0F, 0.0F, color);
         this.addVertex(builder, center, -half, half, 0.0F, 1.0F, color);
         this.addVertex(builder, center, half, half, 1.0F, 1.0F, color);
         this.addVertex(builder, center, -half, -half, 0.0F, 0.0F, color);
         this.addVertex(builder, center, half, half, 1.0F, 1.0F, color);
         this.addVertex(builder, center, half, -half, 1.0F, 0.0F, color);
      }

      return builder.method_60794();
   }

   private void addVertex(class_287 builder, Vector4f center, float dx, float dy, float u, float v, int color) {
      builder.method_22912(center.x + dx, center.y + dy, center.z).method_22913(u, v).method_39415(color);
   }

   @Environment(EnvType.CLIENT)
   public record Sprite(class_243 position, float halfSize, int color) {
   }
}
