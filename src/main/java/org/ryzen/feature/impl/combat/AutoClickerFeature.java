package org.ryzen.feature.impl.combat;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1296;
import net.minecraft.class_1297;
import net.minecraft.class_1308;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_304;
import net.minecraft.class_310;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.mixin.accessor.KeyMappingAccessor;

@Environment(EnvType.CLIENT)
public final class AutoClickerFeature extends Feature {
   public final BooleanSetting leftMouse = this.register(new BooleanSetting("Left Mouse", true));
   public final BooleanSetting rightMouse = this.register(new BooleanSetting("Right Mouse", false));
   public final ModeSetting clickMode = this.register(new ModeSetting("Mode", "Normal", "Normal", "Jitter", "Butterfly"));
   public final MultiSelectSetting targets = this.register(
      new MultiSelectSetting("Targets", List.of("Players", "Mobs"), "Players", "Mobs", "Animals", "Invisible")
   );
   public final NumberSetting cps = this.register(new NumberSetting("CPS", 12.0, 1.0, 20.0, 1.0, ""));
   private long nextLeftClickAt;
   private long nextRightClickAt;
   private boolean butterflyFast;

   public AutoClickerFeature() {
      super("AutoClicker", "Automatic mouse clicking", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onDisable() {
      this.nextLeftClickAt = 0L;
      this.nextRightClickAt = 0L;
      this.butterflyFast = false;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      if (client.field_1724 != null && client.field_1687 != null && client.field_1755 == null && !client.field_1724.method_3144()) {
         long now = System.nanoTime();
         if (this.leftMouse.getValue() && now >= this.nextLeftClickAt && this.validCrosshairTarget(client.field_1692)) {
            click(client.field_1690.field_1886);
            this.nextLeftClickAt = now + this.nextDelayNanos();
         }

         if (this.rightMouse.getValue() && now >= this.nextRightClickAt) {
            click(client.field_1690.field_1904);
            this.nextRightClickAt = now + this.nextDelayNanos();
         }
      }
   }

   private boolean validCrosshairTarget(class_1297 entity) {
      if (!(entity instanceof class_1309 living && living.method_5805())) {
         return false;
      } else if (living.method_5767() && !this.targets.isSelected("Invisible")) {
         return false;
      } else if (living instanceof class_1657) {
         return this.targets.isSelected("Players");
      } else {
         return living instanceof class_1296 ? this.targets.isSelected("Animals") : living instanceof class_1308 && this.targets.isSelected("Mobs");
      }
   }

   private long nextDelayNanos() {
      double baseMillis = 1000.0 / this.cps.getValue();

      double multiplier = switch ((String)this.clickMode.getValue()) {
         case "Jitter" -> ThreadLocalRandom.current().nextDouble(0.85, 1.16);
         case "Butterfly" -> {
            this.butterflyFast = !this.butterflyFast;
            yield this.butterflyFast ? 0.65 : 1.35;
         }
         default -> 1.0;
      };
      return Math.max(1L, Math.round(baseMillis * multiplier * 1000000.0));
   }

   private static void click(class_304 mapping) {
      class_304.method_1420(((KeyMappingAccessor)mapping).getKey());
   }
}
