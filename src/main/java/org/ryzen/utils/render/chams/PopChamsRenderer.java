package org.ryzen.utils.render.chams;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_11658;
import net.minecraft.class_11661;
import net.minecraft.class_11684;
import net.minecraft.class_1921;
import net.minecraft.class_276;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4599;
import net.minecraft.class_4608;
import net.minecraft.class_5602;
import net.minecraft.class_591;
import net.minecraft.class_6367;
import net.minecraft.class_7833;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.ryzen.feature.impl.visual.PopChamsFeature;
import org.ryzen.utils.ColorUtil;

@Environment(EnvType.CLIENT)
public final class PopChamsRenderer {
   private static final int FULL_BRIGHT = 15728880;
   private static final float MODEL_OFFSET_Y = -1.501F;
   private static final float MODEL_DILATION = -0.2F;
   private static final float SCALE_AMOUNT = 0.7F;
   private static final float RISE_AMOUNT = 0.45F;
   private static final int CLEAR = 0;
   private final PopChamsCompositeEffect composite = new PopChamsCompositeEffect();
   private class_6367 maskBuffer;
   private class_11661 storage;
   private class_11684 dispatcher;
   private class_591 model;

   public void render(class_11658 levelRenderState, PopChamsFeature feature) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft.field_1687 != null && minecraft.field_1724 != null && minecraft.field_1773 != null && levelRenderState != null) {
         long nowNanos = System.nanoTime();
         List<PopChamsFeature.Snapshot> snapshots = feature.activeSnapshots(nowNanos);
         if (!snapshots.isEmpty()) {
            class_276 output = minecraft.method_1522();
            if (output != null && output.method_71639() != null) {
               this.ensureResources(minecraft, output.field_1482, output.field_1481);
               if (this.maskBuffer != null && this.dispatcher != null && this.model != null) {
                  RenderSystem.getDevice().createCommandEncoder().clearColorTexture(this.maskBuffer.method_30277(), 0);
                  Matrix4fStack modelViewStack = RenderSystem.getModelViewStack();
                  modelViewStack.pushMatrix();
                  modelViewStack.mul(new Matrix4f().rotation(new Quaternionf(levelRenderState.field_63082.field_63081).conjugate()));

                  try {
                     this.submitPass(snapshots, levelRenderState, feature, nowNanos, output.method_71639(), false);
                     this.submitPass(snapshots, levelRenderState, feature, nowNanos, this.maskBuffer.method_71639(), true);
                  } finally {
                     modelViewStack.popMatrix();
                  }

                  this.composite.render(this.maskBuffer, output, feature.effectiveGlowRadius());
               }
            }
         }
      }
   }

   private void submitPass(
      List<PopChamsFeature.Snapshot> snapshots, class_11658 levelRenderState, PopChamsFeature feature, long nowNanos, GpuTextureView output, boolean mask
   ) {
      class_11661 storage = this.storage;
      class_4587 poseStack = new class_4587();
      double cameraX = levelRenderState.field_63082.field_63078.method_10216();
      double cameraY = levelRenderState.field_63082.field_63078.method_10214();
      double cameraZ = levelRenderState.field_63082.field_63078.method_10215();
      int submitted = 0;

      for (PopChamsFeature.Snapshot snapshot : snapshots) {
         float animation = snapshot.animation(nowNanos);
         if (!(animation <= 0.0F)) {
            float eased = easeOutQuart(1.0F - animation);
            float scale = 1.0F + eased * 0.7F;
            float rise = eased * 0.45F;
            class_10055 renderState = buildRenderState(snapshot);
            this.model.method_62110(renderState);
            poseStack.method_22903();
            poseStack.method_22904(snapshot.x() - cameraX, snapshot.y() - cameraY + rise, snapshot.z() - cameraZ);
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(180.0F - snapshot.bodyYaw()));
            poseStack.method_22905(-scale, -scale, scale);
            poseStack.method_46416(0.0F, -1.501F, 0.0F);
            int renderColor = mask ? ColorUtil.withAlpha(snapshot.baseColor(), 255) : ColorUtil.multiplyAlpha(snapshot.baseColor(), animation);
            this.dispatcherSubmit(
               storage,
               renderState,
               poseStack,
               mask
                  ? PopChamsRenderTypes.mask(snapshot.texture(), snapshot.textured())
                  : PopChamsRenderTypes.model(snapshot.texture(), snapshot.textured(), feature.blending.getValue()),
               renderColor
            );
            poseStack.method_22909();
            submitted++;
         }
      }

      if (submitted != 0) {
         GpuTextureView previousColor = RenderSystem.outputColorTextureOverride;
         GpuTextureView previousDepth = RenderSystem.outputDepthTextureOverride;

         try {
            RenderSystem.outputColorTextureOverride = output;
            RenderSystem.outputDepthTextureOverride = null;
            this.dispatcher.method_73002();
         } finally {
            RenderSystem.outputColorTextureOverride = previousColor;
            RenderSystem.outputDepthTextureOverride = previousDepth;
         }
      }
   }

   private void dispatcherSubmit(class_11661 storage, class_10055 renderState, class_4587 poseStack, class_1921 renderType, int color) {
      storage.method_73490(this.model, renderState, poseStack, renderType, 15728880, class_4608.field_21444, color, null, 0, null);
   }

   private static class_10055 buildRenderState(PopChamsFeature.Snapshot snapshot) {
      class_10055 state = new class_10055();
      state.field_53446 = snapshot.bodyYaw();
      state.field_53447 = snapshot.relativeHeadYaw();
      state.field_53448 = snapshot.pitch();
      state.field_53450 = snapshot.limbProgress();
      state.field_53451 = snapshot.limbSpeed();
      return state;
   }

   private void ensureResources(class_310 minecraft, int width, int height) {
      if (this.dispatcher == null) {
         class_4599 buffers = minecraft.method_22940();
         this.storage = new class_11661();
         this.dispatcher = new class_11684(
            this.storage,
            minecraft.method_1541(),
            buffers.method_23000(),
            minecraft.method_72703(),
            buffers.method_23003(),
            buffers.method_23001(),
            minecraft.field_1772
         );
      }

      if (this.model == null) {
         this.model = new class_591(minecraft.method_31974().method_32072(class_5602.field_27577), false);
         this.model.method_63512().method_41924(new Vector3f(-0.2F, -0.2F, -0.2F));
      }

      if (this.maskBuffer == null || this.maskBuffer.field_1482 != width || this.maskBuffer.field_1481 != height) {
         if (this.maskBuffer != null) {
            this.maskBuffer.method_1238();
         }

         this.maskBuffer = new class_6367("blade-popchams-mask", width, height, false);
      }
   }

   private static float easeOutQuart(float value) {
      float inverse = 1.0F - value;
      return 1.0F - inverse * inverse * inverse * inverse;
   }

   public void release() {
      if (this.maskBuffer != null) {
         this.maskBuffer.method_1238();
         this.maskBuffer = null;
      }

      this.composite.release();
   }
}
