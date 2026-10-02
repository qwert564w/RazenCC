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
import net.minecraft.class_6367;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryStack;
import org.ryzen.feature.impl.visual.WorldTweaksFeature;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.post.FullscreenQuad;
import org.ryzen.utils.render.post.PostFx;
import org.ryzen.utils.render.post.PostPipelines;

@Environment(EnvType.CLIENT)
public final class WorldTweaksRenderer {
   private static final int SKY_UNIFORM_SIZE = new Std140SizeCalculator().putMat4f().putVec4().putVec4().putVec4().putVec4().get();
   private static final int SATURATION_UNIFORM_SIZE = new Std140SizeCalculator().putVec4().get();
   private final GpuBuffer skyUniforms = uniformBuffer("Ryzen World Sky UBO", SKY_UNIFORM_SIZE);
   private final GpuBuffer saturationUniforms = uniformBuffer("Ryzen World Saturation UBO", SATURATION_UNIFORM_SIZE);
   private class_6367 sceneCopy;
   private class_6367 skyClouds;

   public void renderSky(WorldTweaksFeature feature, class_12075 cameraState) {
      class_310 minecraft = class_310.method_1551();
      class_276 target = minecraft.method_1522();
      if (valid(target) && cameraState != null && cameraState.field_63079) {
         Matrix4f projection = Render3DUtil.levelProjectionCopy();
         if (projection != null) {
            Matrix4f inverseViewProjection = projection.mul(new Matrix4f().rotation(new Quaternionf(cameraState.field_63081).conjugate())).invert();
            this.writeSkyUniforms(feature, inverseViewProjection, target.field_1482, target.field_1481);
            this.ensureSkyClouds(target.field_1482, target.field_1481);
            RenderPipeline cloudsPipeline;
            RenderPipeline compositePipeline;
            switch ((String)feature.skyEffect.getValue()) {
               case "Nebula":
                  cloudsPipeline = PostPipelines.WORLD_SKY_CLOUDS_NEBULA;
                  compositePipeline = PostPipelines.WORLD_SKY_NEBULA;
                  break;
               case "Plasma":
                  cloudsPipeline = PostPipelines.WORLD_SKY_CLOUDS_PLASMA;
                  compositePipeline = PostPipelines.WORLD_SKY_PLASMA;
                  break;
               default:
                  cloudsPipeline = PostPipelines.WORLD_SKY_CLOUDS_DEEP_SPACE;
                  compositePipeline = PostPipelines.WORLD_SKY_DEEP_SPACE;
            }

            class_12137 depthSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.NEAREST);
            class_12137 cloudSampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            RenderPass pass = RenderSystem.getDevice()
               .createCommandEncoder()
               .createRenderPass(() -> "Ryzen WorldTweaks sky clouds", this.skyClouds.method_71639(), OptionalInt.empty());

            try {
               pass.setPipeline(cloudsPipeline);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("WorldSkyUniforms", this.skyUniforms);
               pass.bindTexture("DepthSampler", target.method_71640(), depthSampler);
               drawFullscreen(pass);
            } catch (Throwable var17) {
               if (pass != null) {
                  try {
                     pass.close();
                  } catch (Throwable var15) {
                     var17.addSuppressed(var15);
                  }
               }

               throw var17;
            }

            if (pass != null) {
               pass.close();
            }

            pass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(() -> "Ryzen WorldTweaks sky", target.method_71639(), OptionalInt.empty());

            try {
               pass.setPipeline(compositePipeline);
               RenderSystem.bindDefaultUniforms(pass);
               pass.setUniform("WorldSkyUniforms", this.skyUniforms);
               pass.bindTexture("DepthSampler", target.method_71640(), depthSampler);
               pass.bindTexture("CloudSampler", this.skyClouds.method_71639(), cloudSampler);
               drawFullscreen(pass);
            } catch (Throwable var16) {
               if (pass != null) {
                  try {
                     pass.close();
                  } catch (Throwable var14) {
                     var16.addSuppressed(var14);
                  }
               }

               throw var16;
            }

            if (pass != null) {
               pass.close();
            }
         }
      }
   }

   public void renderSaturation(WorldTweaksFeature feature) {
      class_310 minecraft = class_310.method_1551();
      class_276 target = minecraft.method_1522();
      if (valid(target)) {
         this.ensureSceneCopy(target.field_1482, target.field_1481);
         RenderSystem.getDevice()
            .createCommandEncoder()
            .copyTextureToTexture(target.method_30277(), this.sceneCopy.method_30277(), 0, 0, 0, 0, 0, target.field_1482, target.field_1481);
         MemoryStack stack = MemoryStack.stackPush();

         try {
            ByteBuffer data = Std140Builder.onStack(stack, SATURATION_UNIFORM_SIZE)
               .putVec4(Math.clamp(1.0F + feature.saturationAmount.getValue().floatValue(), 0.0F, 3.0F), 0.0F, 0.0F, 0.0F)
               .get();
            RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.saturationUniforms.slice(), data);
         } catch (Throwable var10) {
            if (stack != null) {
               try {
                  stack.close();
               } catch (Throwable var8) {
                  var10.addSuppressed(var8);
               }
            }

            throw var10;
         }

         if (stack != null) {
            stack.close();
         }

         RenderPass pass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(() -> "Ryzen WorldTweaks saturation", target.method_71639(), OptionalInt.empty());

         try {
            pass.setPipeline(PostPipelines.WORLD_SATURATION);
            RenderSystem.bindDefaultUniforms(pass);
            class_12137 sampler = RenderSystem.getSamplerCache().method_75294(FilterMode.LINEAR);
            pass.setUniform("SaturationUniforms", this.saturationUniforms);
            pass.bindTexture("SceneSampler", this.sceneCopy.method_71639(), sampler);
            drawFullscreen(pass);
         } catch (Throwable var9) {
            if (pass != null) {
               try {
                  pass.close();
               } catch (Throwable var7) {
                  var9.addSuppressed(var7);
               }
            }

            throw var9;
         }

         if (pass != null) {
            pass.close();
         }
      }
   }

   private void writeSkyUniforms(WorldTweaksFeature feature, Matrix4f inverseViewProjection, int width, int height) {
      int primary = feature.skyColor1.getValue();
      int secondary = feature.skyColor2.getValue();
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, SKY_UNIFORM_SIZE)
            .putMat4f(inverseViewProjection)
            .putVec4(ColorUtil.red(primary) / 255.0F, ColorUtil.green(primary) / 255.0F, ColorUtil.blue(primary) / 255.0F, 1.0F)
            .putVec4(ColorUtil.red(secondary) / 255.0F, ColorUtil.green(secondary) / 255.0F, ColorUtil.blue(secondary) / 255.0F, 1.0F)
            .putVec4(PostFx.shaderTime(), feature.skyIntensity.getValue().floatValue(), feature.skySpeed.getValue().floatValue(), 0.0F)
            .putVec4(1.0F / width, 1.0F / height, 0.0F, 0.0F)
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.skyUniforms.slice(), data);
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
   }

   private static void drawFullscreen(RenderPass pass) {
      pass.setVertexBuffer(0, FullscreenQuad.buffer());
      pass.draw(0, FullscreenQuad.vertexCount());
   }

   private void ensureSceneCopy(int width, int height) {
      if (this.sceneCopy == null || this.sceneCopy.field_1482 != width || this.sceneCopy.field_1481 != height) {
         if (this.sceneCopy != null) {
            this.sceneCopy.method_1238();
         }

         this.sceneCopy = new class_6367("blade-world-tweaks-scene", width, height, false);
      }
   }

   private void ensureSkyClouds(int width, int height) {
      int halfWidth = Math.max(1, width / 2);
      int halfHeight = Math.max(1, height / 2);
      if (this.skyClouds == null || this.skyClouds.field_1482 != halfWidth || this.skyClouds.field_1481 != halfHeight) {
         if (this.skyClouds != null) {
            this.skyClouds.method_1238();
         }

         this.skyClouds = new class_6367("blade-world-tweaks-sky-clouds", halfWidth, halfHeight, false);
      }
   }

   private static boolean valid(class_276 target) {
      return target != null
         && target.field_1482 > 0
         && target.field_1481 > 0
         && target.method_30277() != null
         && target.method_71639() != null
         && target.method_30278() != null
         && target.method_71640() != null;
   }

   private static GpuBuffer uniformBuffer(String label, int size) {
      return RenderSystem.getDevice().createBuffer(() -> label, 136, size);
   }

   public void release() {
      if (this.sceneCopy != null) {
         this.sceneCopy.method_1238();
         this.sceneCopy = null;
      }

      if (this.skyClouds != null) {
         this.skyClouds.method_1238();
         this.skyClouds = null;
      }

      this.skyUniforms.close();
      this.saturationUniforms.close();
   }
}
