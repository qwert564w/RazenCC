package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class DragonFlyFeature extends Feature implements MinecraftContext {
   private static final String MODE_DEFAULT = "Default";
   private static final String MODE_CUSTOM = "Custom";
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Default", "Default", "Custom"));
   public final NumberSetting speedX = this.register(new NumberSetting("Horizontal", 1.012, 0.1, 5.0, 0.001, "x"));
   public final NumberSetting speedY = this.register(new NumberSetting("Vertical", 1.0, 0.1, 5.0, 0.001, "x"));
   public final NumberSetting diagonalSpeed = this.register(
      new NumberSetting("Diagonal", 1.0109, 0.1, 5.0, 0.001, "x").visibleWhen(() -> this.mode.is("Custom"))
   );

   public DragonFlyFeature() {
      super("DragonFly", "Speeds up creative flight", FeatureCategory.MOVEMENT, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && player.method_31549().field_7479) {
         double horizontal = this.mode.is("Custom") && isDiagonal(client) ? this.diagonalSpeed.getValue() : this.speedX.getValue();
         class_243 motion = player.method_18798();
         player.method_18800(motion.field_1352 * horizontal, motion.field_1351 * this.speedY.getValue(), motion.field_1350 * horizontal);
      }
   }

   private static boolean isDiagonal(class_310 client) {
      if (client.field_1690 == null) {
         return false;
      }

      int pressed = 0;
      if (client.field_1690.field_1894.method_1434()) {
         pressed++;
      }

      if (client.field_1690.field_1881.method_1434()) {
         pressed++;
      }

      if (client.field_1690.field_1913.method_1434()) {
         pressed++;
      }

      if (client.field_1690.field_1849.method_1434()) {
         pressed++;
      }

      return pressed >= 2;
   }
}
