package org.ryzen.feature.impl.player;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AimingItemsFeature extends Feature implements PlayerContext {
   private static final String TARGET_HEADS = "Heads";
   private static final String TARGET_ELYTRA = "Elytra";
   private static final double ITEM_Y_OFFSET = 0.15;
   public final MultiSelectSetting targets = this.register(new MultiSelectSetting("Aim At", List.of("Heads", "Elytra"), "Heads", "Elytra"));
   public final NumberSetting range = this.register(new NumberSetting("Range", 128.0, 16.0, 512.0, 8.0, " blocks"));
   private class_1542 target;
   private boolean announced;

   public AimingItemsFeature() {
      super("AimingItems", "Aims at dropped heads and elytras so you fly straight to them", FeatureCategory.PLAYER, -1);
   }

   @Override
   protected void onDisable() {
      this.target = null;
      this.announced = false;
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.target = null;
      this.announced = false;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null) {
         double rangeSqr = this.range.getValue() * this.range.getValue();
         class_1542 best = null;
         double bestDistance = Double.MAX_VALUE;

         for (class_1297 entity : client.field_1687.method_18112()) {
            if (entity instanceof class_1542 item && this.isTarget(item.method_6983())) {
               double distance = player.method_5858(item);
               if (distance < bestDistance && distance <= rangeSqr) {
                  bestDistance = distance;
                  best = item;
               }
            }
         }

         if (best == null) {
            this.target = null;
            this.announced = false;
         } else {
            this.target = best;
            this.aimAt(player, best);
            if (!this.announced) {
               this.announced = true;
               ChatUtil.info("AimingItems: лечу на " + best.method_6983().method_7964().getString());
            }
         }
      } else {
         this.target = null;
      }
   }

   private void aimAt(class_746 player, class_1542 item) {
      class_243 delta = item.method_73189().method_1031(0.0, 0.15, 0.0).method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
      float pitch = class_3532.method_15363((float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal))), -90.0F, 90.0F);
      player.method_36456(yaw);
      player.method_36457(pitch);
      player.field_6241 = yaw;
   }

   private boolean isTarget(class_1799 stack) {
      if (stack.method_7960()) {
         return false;
      }

      String name = stack.method_7964().getString().toLowerCase(Locale.ROOT);
      return !this.targets.isSelected("Heads") || !stack.method_31574(class_1802.field_8575) && !name.contains("head") && !name.contains("голов")
         ? this.targets.isSelected("Elytra") && (stack.method_31574(class_1802.field_8833) || name.contains("elytra") || name.contains("элитр"))
         : true;
   }
}
