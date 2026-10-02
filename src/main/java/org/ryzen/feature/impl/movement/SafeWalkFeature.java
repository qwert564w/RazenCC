package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class SafeWalkFeature extends Feature implements MinecraftContext {
   public SafeWalkFeature() {
      super("SafeWalk", "Prevents walking off block edges", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onInput(PlayerInputEvent event) {
      class_746 player = this.player();
      if (player != null && this.level() != null && player.method_24828()) {
         class_2338 below = player.method_24515().method_10074();
         if (this.level().method_8320(below).method_26220(this.level(), below).method_1110()) {
            event.setShift(true);
         }
      }
   }
}
