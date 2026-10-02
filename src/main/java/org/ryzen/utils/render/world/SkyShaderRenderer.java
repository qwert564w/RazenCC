package org.ryzen.utils.render.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import java.nio.ByteBuffer;
import java.util.OptionalInt;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12075;
import net.minecraft.class_12137;
import net.minecraft.class_276;
import net.minecraft.class_310;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.ryzen.feature.impl.visual.SkyShaderFeature;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.post.FullscreenQuad;
import org.ryzen.utils.render.post.PostFx;
import org.ryzen.utils.render.post.PostPipelines;

@Environment(EnvType.CLIENT)
public final class SkyShaderRenderer {
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putMat4f().putVec4().putVec4().putVec4().putVec4().putVec4().putVec4().get();
   private final GpuBuffer uniforms = RenderSystem.getDevice().createBuffer(() -> "Ryzen SkyShader UBO", 136, UNIFORM_SIZE);

   public void render(SkyShaderFeature feature, class_12075 cameraState, Vector4f vanillaSkyColor) {
      class_310 minecraft = class_310.method_1551();
      class_276 target = minecraft.method_1522();
      if (valid(target) && cameraState != null && cameraState.field_63079) {
         Matrix4f projection = Render3DUtil.levelProjectionCopy();
         if (projection != null) {
            Matrix4f inverseViewProjection = projection.mul(new Matrix4f().rotation(new Quaternionf(cameraState.field_63081).conjugate())).invert();
            this.writeUniforms(feature, inverseViewProjection, vanillaSkyColor);
            class_12137 depthSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
            RenderPass pass = RenderSystem.getDevice()
               .createCommandEncoder()
               .createRenderPass(() -> "Ryzen SkyShader", target.method_71639(), OptionalInt.empty());

            try {
               pass.setPipeline(pipelineFor(feature.getMode()));
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("SkyShaderUniforms", this.uniforms);
               pass.bindTexture("DepthSampler", target.method_71640(), depthSampler);
               pass.setVertexBuffer(0, FullscreenQuad.buffer());
               pass.draw(0, FullscreenQuad.vertexCount());
            } catch (Throwable var13) {
               if (pass != null) {
                  try {
                     pass.close();
                  } catch (Throwable var12) {
                     var13.addSuppressed(var12);
                  }
               }

               throw var13;
            }

            if (pass != null) {
               pass.close();
            }
         }
      }
   }

   private static RenderPipeline pipelineFor(String mode) {
      return switch (mode) {
         case "Summer" -> PostPipelines.SKYSHADER_SUMMER;
         case "Plasma" -> PostPipelines.SKYSHADER_PLASMA;
         case "Pulsar" -> PostPipelines.SKYSHADER_PULSAR;
         case "Sakura" -> PostPipelines.SKYSHADER_SAKURA;
         default -> PostPipelines.SKYSHADER_SPACE;
      };
   }

   private void writeUniforms(SkyShaderFeature feature, Matrix4f inverseViewProjection, Vector4f vanillaSkyColor) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         Std140Builder builder = Std140Builder.onStack(stack, UNIFORM_SIZE).putMat4f(inverseViewProjection);
         putColor(builder, feature.primaryColor());
         putColor(builder, feature.secondaryColor());
         putColor(builder, feature.backgroundColor());
         builder.putVec4(
            vanillaSkyColor == null ? 0.5F : vanillaSkyColor.x,
            vanillaSkyColor == null ? 0.7F : vanillaSkyColor.y,
            vanillaSkyColor == null ? 1.0F : vanillaSkyColor.z,
            1.0F
         );
         builder.putVec4(PostFx.shaderTime(), feature.effectiveSpeed(), feature.effectiveIntensity(), feature.effectiveScale());
         builder.putVec4(feature.effectiveOverlay(), feature.isSummerNight() ? 1.0F : 0.0F, 0.0F, 0.0F);
         ByteBuffer data = builder.get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniforms.slice(), data);
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
   }

   private static void putColor(Std140Builder builder, int color) {
      builder.putVec4(ColorUtil.red(color) / 255.0F, ColorUtil.green(color) / 255.0F, ColorUtil.blue(color) / 255.0F, 1.0F);
   }

   private static boolean valid(class_276 target) {
      return target != null && target.field_1482 > 0 && target.field_1481 > 0 && target.method_71639() != null && target.method_71640() != null;
   }

   public void release() {
      this.uniforms.close();
   }
}
