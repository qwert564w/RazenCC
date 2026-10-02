package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_238;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class ParkourFeature extends Feature {
   private static final double EDGE_PROBE_DEPTH = 0.001;

   public ParkourFeature() {
      super("Parkour", "Automatically jumps at block edges", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && event.getClient().field_1687 != null) {
         if (player.method_24828() && !player.method_5715() && !player.method_31549().field_7479) {
            if (this.isAtEdge(event, player)) {
               player.method_6043();
            }
         }
      }
   }

   private boolean isAtEdge(GameTickEvent event, class_746 player) {
      class_238 probe = player.method_5829().method_989(0.0, -0.001, 0.0);
      return !event.getClient().field_1687.method_20812(player, probe).iterator().hasNext();
   }
}
