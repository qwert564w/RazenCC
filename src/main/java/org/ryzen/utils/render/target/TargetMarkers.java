package org.ryzen.utils.render.target;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1309;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.impl.combat.TriggerBotFeature;

@Environment(EnvType.CLIENT)
public final class TargetMarkers {
   private final AuraMarkerRenderer marker = new AuraMarkerRenderer();
   private final GhostTargetRenderer ghost = new GhostTargetRenderer();
   private final CircleTargetRenderer circle = new CircleTargetRenderer();

   public void render(AuraFeature feature, float tickDelta) {
      class_1309 target = feature.getCurrentTarget();
      if (target == null) {
         TriggerBotFeature triggerBot = TriggerBotFeature.getEnabled();
         if (triggerBot != null) {
            target = triggerBot.getCurrentTarget();
         }
      }

      boolean ghosts = feature.usesGhostTargetEsp();
      boolean circles = feature.usesCircleTargetEsp();
      int color = feature.getMarkerColor();
      this.marker.render(!ghosts && !circles ? target : null, tickDelta, color);
      this.ghost.render(ghosts ? target : null, tickDelta, color);
      this.circle.render(circles ? target : null, tickDelta, color);
   }

   public void release() {
      this.marker.release();
   }
}
