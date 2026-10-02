package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11659;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4587;
import net.minecraft.class_742;
import net.minecraft.class_746;
import net.minecraft.class_759;
import net.minecraft.class_7833;
import org.ryzen.context.RotationContext;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.visual.SwingAnimationFeature;
import org.ryzen.feature.impl.visual.ViewModelFeature;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_759.class)
public abstract class ItemInHandRendererMixin {
   @Shadow
   @Final
   private class_310 field_4050;

   @Inject(method = "method_3228", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = Shift.AFTER))
   private void applyViewModelOffset(
      class_742 player,
      float partialTick,
      float pitch,
      class_1268 hand,
      float swingProgress,
      class_1799 stack,
      float equippedProgress,
      class_4587 poseStack,
      class_11659 collector,
      int light,
      CallbackInfo ci
   ) {
      ViewModelFeature viewModel = FeatureManager.INSTANCE.getEnabled(ViewModelFeature.class);
      if (viewModel != null && !stack.method_7960() && (!player.method_6115() || player.method_6058() != hand)) {
         class_1306 arm = hand == class_1268.field_5808 ? player.method_6068() : player.method_6068().method_5928();
         poseStack.method_46416(viewModel.offsetX(arm), viewModel.offsetY(arm), viewModel.offsetZ(arm));
      }
   }

   @Inject(method = "method_65816", at = @At("HEAD"), cancellable = true)
   private void customSwingArm(float swingProgress, class_4587 poseStack, int direction, class_1306 arm, CallbackInfo ci) {
      SwingAnimationFeature swing = FeatureManager.INSTANCE.getEnabled(SwingAnimationFeature.class);
      if (swing != null) {
         float progress = class_3532.method_15363(swingProgress, 0.0F, 1.0F);
         float side = direction < 0 ? -1.0F : 1.0F;
         poseStack.method_46416(side * 0.56F, -0.52F, -0.72F);
         switch (swing.style()) {
            case SLICE: {
               float wave = class_3532.method_15374(class_3532.method_15355(progress) * (float) Math.PI);
               poseStack.method_22907(class_7833.field_40714.rotationDegrees(wave * -80.0F));
               poseStack.method_22907(class_7833.field_40716.rotationDegrees(side * wave * -30.0F));
               poseStack.method_22907(class_7833.field_40718.rotationDegrees(side * wave * 10.0F));
               break;
            }
            case SPIRAL:
               float angle = progress * (float) Math.PI * 2.0F;
               poseStack.method_22907(class_7833.field_40716.rotationDegrees(side * class_3532.method_15374(angle) * 60.0F));
               poseStack.method_22907(class_7833.field_40714.rotationDegrees(class_3532.method_15374(angle * 0.5F) * -70.0F));
               break;
            case THRUST: {
               float wave = class_3532.method_15374(progress * (float) Math.PI);
               float shrink = 1.0F - wave * 0.15F;
               poseStack.method_46416(0.0F, 0.0F, -wave * 0.4F);
               poseStack.method_22905(shrink, shrink, 1.0F);
               poseStack.method_22907(class_7833.field_40714.rotationDegrees(wave * -15.0F));
               break;
            }
            case SPEAR: {
               float wave = class_3532.method_15374(progress * (float) Math.PI);
               poseStack.method_22907(class_7833.field_40714.rotationDegrees(wave * -95.0F));
               poseStack.method_22907(class_7833.field_40718.rotationDegrees(side * wave * -10.0F));
               poseStack.method_46416(0.0F, wave * 0.4F, 0.0F);
            }
         }

         ci.cancel();
      }
   }

   @Redirect(method = "method_22976", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_61414(F)F"))
   private float useFreeLookPitch(class_746 player, float partialTick) {
      return RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_61414(partialTick);
   }

   @Redirect(method = "method_22976", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5695(F)F"))
   private float useCameraPitch(class_746 player, float partialTick) {
      return this.field_4050.field_1773.method_19418().method_19329();
   }

   @Redirect(method = "method_22976", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_746;method_5705(F)F"))
   private float useCameraYaw(class_746 player, float partialTick) {
      return this.field_4050.field_1773.method_19418().method_19330();
   }
}
