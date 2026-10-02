package org.ryzen.utils.render.world;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11658;
import net.minecraft.class_12075;
import org.joml.Vector4f;

@Environment(EnvType.CLIENT)
public record WorldEffectContext(class_11658 levelRenderState, class_12075 cameraRenderState, float tickDelta, Vector4f skyColor) {
   public WorldEffectContext(class_11658 levelRenderState, class_12075 cameraRenderState, float tickDelta) {
      this(levelRenderState, cameraRenderState, tickDelta, null);
   }
}
