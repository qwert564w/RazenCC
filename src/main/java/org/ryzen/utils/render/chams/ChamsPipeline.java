package org.ryzen.utils.render.chams;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11658;
import org.ryzen.feature.impl.visual.ChamsFeature;

@Environment(EnvType.CLIENT)
public final class ChamsPipeline {
   private final ChamsMaskRenderer maskRenderer = new ChamsMaskRenderer();
   private final ChamsCompositeEffect composite = new ChamsCompositeEffect();

   public void render(class_11658 levelRenderState, ChamsFeature feature) {
      this.maskRenderer.renderGroups(levelRenderState, feature, frame -> this.composite.render(frame, feature));
   }

   public void release() {
      this.maskRenderer.release();
      this.composite.release();
   }
}
