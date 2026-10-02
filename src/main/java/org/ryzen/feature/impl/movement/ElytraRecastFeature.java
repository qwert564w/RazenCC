package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1802;
import net.minecraft.class_2848;
import net.minecraft.class_746;
import net.minecraft.class_2848.class_2849;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class ElytraRecastFeature extends Feature implements PlayerContext {
   public ElytraRecastFeature() {
      super("ElytraRecast", "Holds jump and re-starts the glide whenever the elytra drops", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      class_746 player = this.localPlayer();
      if (player != null && player.method_6118(class_1304.field_6174).method_31574(class_1802.field_8833)) {
         if (player.method_24828()) {
            event.setJump(true);
         } else if (!player.method_6128()) {
            if (player.field_3944 != null) {
               player.field_3944.method_52787(new class_2848(player, class_2849.field_12982));
            }

            player.method_23669();
         }
      }
   }
}
