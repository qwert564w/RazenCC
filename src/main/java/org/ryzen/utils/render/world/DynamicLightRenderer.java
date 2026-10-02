package org.ryzen.utils.render.world;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.systems.RenderSystem;
import java.nio.ByteBuffer;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_6367;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.lwjgl.system.MemoryStack;
import org.ryzen.feature.impl.player.FullBrightFeature;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.post.PostFx;
import org.ryzen.utils.render.post.PostPipelines;
import org.ryzen.utils.render.post.PostTarget;

@Environment(EnvType.CLIENT)
public final class DynamicLightRenderer {
   private static final int MAX_LIGHTS = 16;
   private static final int UNIFORM_SIZE = new Std140SizeCalculator().putMat4f().putVec4().get();
   private static final int LIGHTS_SIZE = lightsSize();
   private final GpuBuffer uniforms = PostFx.createUniforms("Ryzen Point Light UBO", UNIFORM_SIZE);
   private final GpuBuffer lights = PostFx.createUniforms("Ryzen Point Light Array UBO", LIGHTS_SIZE);
   private final PostTarget sceneCopy = new PostTarget("blade-dynamic-light-scene", PostPipelines.EFFECT_FORMAT, false);

   public void render(FullBrightFeature feature, class_12075 cameraState, float partialTick) {
      class_310 minecraft = class_310.method_1551();
      class_276 mainTarget = minecraft.method_1522();
      if (valid(mainTarget) && cameraState != null && cameraState.field_63079) {
         class_243 cameraPosition = new class_243(
            cameraState.field_63078.method_10216(), cameraState.field_63078.method_10214(), cameraState.field_63078.method_10215()
         );
         List<DynamicLightManager.RenderLight> renderLights = DynamicLightManager.INSTANCE.shaderLights(feature, partialTick, cameraPosition);
         if (!renderLights.isEmpty()) {
            class_6367 scene = this.sceneCopy.ensure(mainTarget.field_1482, mainTarget.field_1481);
            if (scene != null && scene.method_30277() != null) {
               RenderSystem.getDevice()
                  .createCommandEncoder()
                  .copyTextureToTexture(mainTarget.method_30277(), scene.method_30277(), 0, 0, 0, 0, 0, mainTarget.field_1482, mainTarget.field_1481);
               Matrix4f levelProjection = Render3DUtil.levelProjectionCopy();
               if (levelProjection != null) {
                  Matrix4f inverseViewProjection = levelProjection.mul(new Matrix4f().rotation(new Quaternionf(cameraState.field_63081).conjugate())).invert();
                  this.writeUniforms(inverseViewProjection, feature.lightIntensity.getValue().floatValue(), renderLights.size(), false);
                  this.writeLights(renderLights, cameraPosition);
                  PostFx.pass("Ryzen Dynamic Point Lights", PostPipelines.POINT_LIGHTS, mainTarget, pass -> {
                     pass.setUniform("PointLightUniforms", this.uniforms);
                     pass.setUniform("PointLights", this.lights);
                     pass.bindTexture("SceneSampler", scene.method_71639(), PostFx.linearSampler());
                     pass.bindTexture("DepthSampler", mainTarget.method_71640(), PostFx.nearestSampler());
                  });
               }
            }
         }
      }
   }

   private void writeUniforms(Matrix4fc inverseViewProjection, float intensity, int lightCount, boolean depthZeroToOne) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         ByteBuffer data = Std140Builder.onStack(stack, UNIFORM_SIZE)
            .putMat4f(inverseViewProjection)
            .putVec4(PostFx.shaderTime(), intensity, lightCount, depthZeroToOne ? 1.0F : 0.0F)
            .get();
         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.uniforms.slice(), data);
      } catch (Throwable var9) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (stack != null) {
         stack.close();
      }
   }

   private void writeLights(List<DynamicLightManager.RenderLight> renderLights, class_243 cameraPosition) {
      MemoryStack stack = MemoryStack.stackPush();

      try {
         Std140Builder builder = Std140Builder.onStack(stack, LIGHTS_SIZE);

         for (int index = 0; index < 16; index++) {
            if (index < renderLights.size()) {
               DynamicLightManager.RenderLight light = renderLights.get(index);
               class_243 relative = light.position().method_1020(cameraPosition);
               builder.putVec4((float)relative.field_1352, (float)relative.field_1351, (float)relative.field_1350, light.radius());
            } else {
               builder.putVec4(0.0F, 0.0F, 0.0F, 0.0F);
            }
         }

         for (int index = 0; index < 16; index++) {
            if (index < renderLights.size()) {
               DynamicLightManager.RenderLight light = renderLights.get(index);
               builder.putVec4((light.rgb() >> 16 & 0xFF) / 255.0F, (light.rgb() >> 8 & 0xFF) / 255.0F, (light.rgb() & 0xFF) / 255.0F, light.flicker());
            } else {
               builder.putVec4(0.0F, 0.0F, 0.0F, 0.0F);
            }
         }

         for (int index = 0; index < 16; index++) {
            builder.putVec4(index < renderLights.size() ? renderLights.get(index).phase() : 0.0F, 0.0F, 0.0F, 0.0F);
         }

         RenderSystem.getDevice().createCommandEncoder().writeToBuffer(this.lights.slice(), builder.get());
      } catch (Throwable var9) {
         if (stack != null) {
            try {
               stack.close();
            } catch (Throwable var8) {
               var9.addSuppressed(var8);
            }
         }

         throw var9;
      }

      if (stack != null) {
         stack.close();
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

   private static int lightsSize() {
      Std140SizeCalculator calculator = new Std140SizeCalculator();

      for (int index = 0; index < 48; index++) {
         calculator.putVec4();
      }

      return calculator.get();
   }

   public void release() {
      this.sceneCopy.release();
      this.uniforms.close();
      this.lights.close();
   }
}
