package org.ryzen.mixin.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_11659;
import net.minecraft.class_12075;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_4587;
import net.minecraft.class_897;
import net.minecraft.class_898;
import org.ryzen.utils.render.EntityEspDispatcherBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(class_898.class)
public abstract class EntityRenderDispatcherMixin implements EntityEspDispatcherBridge {
   @Shadow
   public abstract <S extends class_10017> class_897<?, ? super S> method_68832(S var1);

   @Unique
   @Override
   public <S extends class_10017> void submitForGlow(
      S state, class_12075 cameraState, double x, double y, double z, class_4587 poseStack, class_11659 submitNodeCollector
   ) {
      class_897<?, ? super S> renderer = this.method_68832(state);
      class_243 renderOffset = renderer.method_23169(state);
      poseStack.method_22903();
      poseStack.method_22904(x + renderOffset.method_10216(), y + renderOffset.method_10214(), z + renderOffset.method_10215());
      class_2561 nameTag = state.field_53337;
      state.field_53337 = null;

      try {
         renderer.method_3936(state, poseStack, submitNodeCollector, cameraState);
      } finally {
         state.field_53337 = nameTag;
         poseStack.method_22909();
      }
   }
}
