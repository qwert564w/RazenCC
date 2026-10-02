package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.InputBindSetting;

@Environment(EnvType.CLIENT)
public final class AutoEnderChestFeature extends Feature implements MinecraftContext {
   private static final int SEARCH_RADIUS = 6;
   private static final double MAX_DISTANCE_SQ = 36.0;
   public final InputBindSetting openKey = this.register(new InputBindSetting("Open Key", -1));
   private class_2338 target;

   public AutoEnderChestFeature() {
      super("AutoEnderChest", "Opens the nearest ender chest on a key press", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onDisable() {
      this.target = null;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1 && this.openKey.matches(event.getKey())) {
         this.locate();
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (event.getAction() == 1 && this.openKey.matchesMouse(event.getButton())) {
         this.locate();
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_2338 chest = this.target;
      this.target = null;
      if (chest != null) {
         class_310 client = event.getClient();
         class_746 player = client.field_1724;
         if (player != null && client.field_1687 != null && client.field_1761 != null) {
            if (client.field_1687.method_8320(chest).method_27852(class_2246.field_10443)) {
               class_243 center = class_243.method_24953(chest);
               RotationContext.rotateToPosition(player, center);
               client.field_1761.method_2896(player, class_1268.field_5808, new class_3965(center, class_2350.field_11036, chest, false));
            }
         }
      }
   }

   private void locate() {
      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      if (client.field_1687 != null && player != null && client.field_1755 == null) {
         class_2338 origin = player.method_24515();
         class_2338 best = null;
         double bestDistance = 36.0;

         for (class_2338 pos : class_2338.method_10097(origin.method_10069(-6, -6, -6), origin.method_10069(6, 6, 6))) {
            if (client.field_1687.method_8320(pos).method_27852(class_2246.field_10443)) {
               double distance = player.method_73189().method_1025(class_243.method_24953(pos));
               if (distance < bestDistance) {
                  bestDistance = distance;
                  best = pos.method_10062();
               }
            }
         }

         this.target = best;
      }
   }
}
