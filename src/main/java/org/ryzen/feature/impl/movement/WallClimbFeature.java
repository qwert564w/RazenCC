package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class WallClimbFeature extends Feature {
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 0.6, 0.1, 1.0, 0.05, ""));

   public WallClimbFeature() {
      super("WallClimb", "Climb walls like a ladder", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && !player.method_7325() && player.field_5976) {
         class_243 movement = player.method_18798();
         player.method_18800(movement.field_1352, this.speed.getValue(), movement.field_1350);
      }
   }
}
