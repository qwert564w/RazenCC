package org.ryzen.utils.render.chams;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_11658;
import net.minecraft.class_11661;
import net.minecraft.class_11684;
import net.minecraft.class_1297;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_6367;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.ryzen.feature.impl.visual.ChamsFeature;
import org.ryzen.utils.render.EntityEspDispatcherBridge;
import org.ryzen.utils.render.EntityEspStateCache;
import org.ryzen.utils.render.HurtUtil;

@Environment(EnvType.CLIENT)
public final class ChamsMaskRenderer {
   private static final int HURT_BUCKETS = 3;
   private class_6367 maskBuffer;
   private class_11661 isolatedStorage;
   private class_11684 isolatedDispatcher;

   public void renderGroups(class_11658 levelRenderState, ChamsFeature feature, Consumer<ChamsMaskRenderer.MaskFrame> composite) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft.field_1687 != null && minecraft.field_1724 != null && minecraft.field_1773 != null && levelRenderState != null) {
         List<class_10017> states = EntityEspStateCache.currentStates();
         List<class_1297> targets = ChamsTargetMatcher.collectTargets(minecraft, feature);
         if (!states.isEmpty() && !targets.isEmpty()) {
            class_276 mainTarget = minecraft.method_1522();
            if (mainTarget != null) {
               this.ensureResources(minecraft, mainTarget.field_1482, mainTarget.field_1481);
               if (this.maskBuffer != null && this.isolatedDispatcher != null && minecraft.method_1561() instanceof EntityEspDispatcherBridge bridge) {
                  ArrayList var15 = new ArrayList(4);

                  for (int poseStack = 0; poseStack <= 3; poseStack++) {
                     var15.add(null);
                  }

                  for (class_10017 state : states) {
                     class_1297 target = ChamsTargetMatcher.matchingTarget(state, targets);
                     if (target != null) {
                        int bucket = Math.round(HurtUtil.easedFactor(target) * 3.0F);
                        List<class_10017> group = (List<class_10017>)var15.get(bucket);
                        if (group == null) {
                           group = new ArrayList<>(4);
                           var15.set(bucket, group);
                        }

                        group.add(state);
                     }
                  }

                  minecraft.method_1561()
                     .method_3941(minecraft.field_1773.method_19418(), (class_1297)(minecraft.field_1692 != null ? minecraft.field_1692 : minecraft.field_1724));
                  class_4587 poseStack = new class_4587();

                  for (int bucket = 0; bucket <= 3; bucket++) {
                     List<class_10017> group = (List<class_10017>)var15.get(bucket);
                     if (group != null && this.renderMask(levelRenderState, bridge, poseStack, group)) {
                        composite.accept(new ChamsMaskRenderer.MaskFrame(this.maskBuffer, mainTarget, bucket / 3.0F));
                     }
                  }
               }
            }
         }
      }
   }

   private boolean renderMask(class_11658 levelRenderState, EntityEspDispatcherBridge bridge, class_4587 poseStack, List<class_10017> group) {
      RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(this.maskBuffer.method_30277(), 0, this.maskBuffer.method_30278(), 1.0);
      class_11661 storage = this.isolatedStorage;
      double cameraX = levelRenderState.field_63082.field_63078.method_10216();
      double cameraY = levelRenderState.field_63082.field_63078.method_10214();
      double cameraZ = levelRenderState.field_63082.field_63078.method_10215();
      GpuTextureView previousColor = RenderSystem.outputColorTextureOverride;
      GpuTextureView previousDepth = RenderSystem.outputDepthTextureOverride;
      Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();

      try {
         RenderSystem.outputColorTextureOverride = this.maskBuffer.method_71639();
         RenderSystem.outputDepthTextureOverride = this.maskBuffer.method_71640();
         modelViewStack.pushMatrix();
         modelViewStack.mul(new Matrix4f().rotation(new Quaternionf(levelRenderState.field_63082.field_63081).conjugate()));

         for (class_10017 state : group) {
            bridge.submitForGlow(
               state, levelRenderState.field_63082, state.field_53325 - cameraX, state.field_53326 - cameraY, state.field_53327 - cameraZ, poseStack, storage
            );
         }

         this.isolatedDispatcher.method_73002();
      } finally {
         modelViewStack.popMatrix();
         RenderSystem.outputColorTextureOverride = previousColor;
         RenderSystem.outputDepthTextureOverride = previousDepth;
      }

      return true;
   }

   private void ensureResources(class_310 minecraft, int width, int height) {
      if (this.isolatedDispatcher == null) {
         class_4599 buffers = minecraft.method_22940();
         this.isolatedStorage = new class_11661();
         this.isolatedDispatcher = new class_11684(
            this.isolatedStorage,
            minecraft.method_1541(),
            buffers.method_23000(),
            minecraft.method_72703(),
            buffers.method_23003(),
            buffers.method_23001(),
            minecraft.field_1772
         );
      }

      if (this.maskBuffer == null || this.maskBuffer.field_1482 != width || this.maskBuffer.field_1481 != height) {
         if (this.maskBuffer != null) {
            this.maskBuffer.method_1238();
         }

         this.maskBuffer = new class_6367("blade-chams-mask", width, height, true);
      }
   }

   public void release() {
      if (this.maskBuffer != null) {
         this.maskBuffer.method_1238();
         this.maskBuffer = null;
      }
   }

   @Environment(EnvType.CLIENT)
   public record MaskFrame(class_276 mask, class_276 output, float hurtFactor) {
   }
}
