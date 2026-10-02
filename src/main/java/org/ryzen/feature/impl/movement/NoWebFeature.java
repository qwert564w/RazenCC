package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_2246;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class NoWebFeature extends Feature implements MinecraftContext {
   private static final String MODE_CANCEL = "Cancel";
   private static final String MODE_SPEED = "Speed";
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Cancel", "Cancel", "Speed"));
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 1.0, 0.1, 2.0, 0.1, "x").visibleWhen(() -> this.mode.is("Speed")));

   public NoWebFeature() {
      super("NoWeb", "Removes the cobweb slowdown", FeatureCategory.MOVEMENT, -1);
   }

   public static NoWebFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(NoWebFeature.class);
   }

   public static boolean shouldCancelWeb(class_1657 player, class_2680 state) {
      NoWebFeature feature = getEnabled();
      return feature != null && player == MinecraftContext.mc.field_1724 && state.method_27852(class_2246.field_10343) && feature.mode.is("Cancel");
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null && this.mode.is("Speed") && inWeb(player)) {
         double factor = this.speed.getValue();
         class_243 motion = player.method_18798();
         player.method_18800(motion.field_1352 * factor, motion.field_1351, motion.field_1350 * factor);
      }
   }

   private static boolean inWeb(class_746 player) {
      return player.method_73183().method_8320(player.method_24515()).method_27852(class_2246.field_10343)
         || player.method_73183().method_8320(player.method_24515().method_10084()).method_27852(class_2246.field_10343);
   }
}
