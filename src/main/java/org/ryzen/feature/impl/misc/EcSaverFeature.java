package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2586;
import net.minecraft.class_2611;
import net.minecraft.class_2818;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2831;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class EcSaverFeature extends Feature implements PlayerContext {
   public final InputBindSetting key = this.register(new InputBindSetting("Open Key", -1));
   private class_2338 pending;

   public EcSaverFeature() {
      super("EcSaver", "Opens the nearest ender chest on a key press", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onDisable() {
      this.pending = null;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 0 && this.key.matches(event.getKey())) {
         this.locate();
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (event.getAction() == 0 && this.key.matchesMouse(event.getButton())) {
         this.locate();
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_2338 target = this.pending;
      if (target != null) {
         this.pending = null;
         class_310 client = event.getClient();
         class_746 player = client.field_1724;
         if (player != null && client.field_1761 != null) {
            class_243 center = class_243.method_24953(target);
            class_243 delta = center.method_1020(player.method_33571());
            float yaw = class_3532.method_15393((float)(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0));
            float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350))));
            if (player.field_3944 != null) {
               player.field_3944.method_52787(new class_2831(yaw, pitch, player.method_24828(), player.field_5976));
            }

            client.field_1761.method_2896(player, class_1268.field_5808, new class_3965(center, class_2350.field_11036, target, false));
         }
      }
   }

   private void locate() {
      class_746 player = this.localPlayer();
      class_638 level = this.level();
      if (player != null && level != null) {
         class_2338 nearest = null;
         double nearestDistance = Double.MAX_VALUE;
         int viewDistance = class_310.method_1551().field_1690.method_38521();
         int chunkX = player.method_31476().field_9181;
         int chunkZ = player.method_31476().field_9180;

         for (int x = chunkX - viewDistance; x <= chunkX + viewDistance; x++) {
            for (int z = chunkZ - viewDistance; z <= chunkZ + viewDistance; z++) {
               class_2818 chunk = level.method_2935().method_12126(x, z, false);
               if (chunk != null) {
                  for (class_2586 blockEntity : chunk.method_12214().values()) {
                     if (blockEntity instanceof class_2611) {
                        class_2338 pos = blockEntity.method_11016();
                        double distance = player.method_24515().method_10262(pos);
                        if (distance < nearestDistance) {
                           nearestDistance = distance;
                           nearest = pos;
                        }
                     }
                  }
               }
            }
         }

         if (nearest == null) {
            ChatUtil.error("EcSaver: эндер-сундук не найден");
         } else {
            this.pending = nearest;
            ChatUtil.success("EcSaver: сундук на " + nearest.method_10263() + ", " + nearest.method_10264() + ", " + nearest.method_10260());
         }
      }
   }
}
