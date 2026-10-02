package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10039;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_1542;
import net.minecraft.class_238;
import net.minecraft.class_4587;
import net.minecraft.class_7833;
import net.minecraft.class_916;
import org.joml.Quaternionfc;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.visual.ItemPhysicsFeature;
import org.ryzen.utils.render.ItemEntityRenderStateAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_916.class)
public abstract class ItemEntityRendererMixin {
   private static final float TUMBLE_SPIN_DEGREES = 300.0F;
   private static final float FLAT_ROTATION_DEGREES = 90.0F;
   private static final float GROUND_EPSILON = 0.002F;

   @Inject(method = "method_62470(Lnet/minecraft/class_1542;Lnet/minecraft/class_10039;F)V", at = @At("TAIL"))
   private void captureOnGround(class_1542 entity, class_10039 state, float partialTick, CallbackInfo ci) {
      ((ItemEntityRenderStateAccess)state).setOnGround(entity.method_24828());
   }

   @Redirect(
      method = "method_3996(Lnet/minecraft/class_10039;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_46416(FFF)V")
   )
   private void skipHoverTranslate(
      class_4587 poseStack, float x, float y, float z, class_10039 state, class_4587 poseStackArg, class_11659 collector, class_12075 cameraState
   ) {
      if (!physicsApplies(state)) {
         poseStack.method_46416(x, y, z);
      }
   }

   @Redirect(
      method = "method_3996(Lnet/minecraft/class_10039;Lnet/minecraft/class_4587;Lnet/minecraft/class_11659;Lnet/minecraft/class_12075;)V",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22907(Lorg/joml/Quaternionfc;)V")
   )
   private void applyPhysicsTransform(
      class_4587 poseStack, Quaternionfc spinRotation, class_10039 state, class_4587 poseStackArg, class_11659 collector, class_12075 cameraState
   ) {
      if (!physicsApplies(state)) {
         poseStack.method_22907(spinRotation);
      } else {
         class_238 box = state.field_55310.method_72173();
         float yaw = (state.field_55312 % 360 + 360) % 360;
         if (((ItemEntityRenderStateAccess)state).isOnGround()) {
            poseStack.method_46416(0.0F, (float)box.field_1324 + 0.002F, 0.0F);
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(yaw));
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(90.0F));
         } else {
            float centerY = (float)((box.field_1322 + box.field_1325) * 0.5);
            float halfHeight = (float)((box.field_1325 - box.field_1322) * 0.5);
            float spin = class_1542.method_27314(state.field_53328, state.field_53435);
            poseStack.method_46416(0.0F, halfHeight, 0.0F);
            poseStack.method_22907(class_7833.field_40716.rotationDegrees(yaw));
            poseStack.method_22907(class_7833.field_40714.rotationDegrees(spin * 300.0F));
            poseStack.method_46416(0.0F, -centerY, 0.0F);
         }
      }
   }

   @Unique
   private static boolean physicsApplies(class_10039 state) {
      return FeatureManager.INSTANCE.getEnabled(ItemPhysicsFeature.class) != null && !state.field_55310.method_65606();
   }
}
