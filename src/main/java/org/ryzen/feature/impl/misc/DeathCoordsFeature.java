package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_418;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.screen.ScreenOpenEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class DeathCoordsFeature extends Feature {
   public final BooleanSetting copyToClipboard = this.register(new BooleanSetting("Copy To Clipboard", false));
   private boolean reported;
   private class_243 lastDeathPosition;
   private String lastDeathDimension;

   public DeathCoordsFeature() {
      super("DeathCoords", "Shows your coordinates on death", FeatureCategory.MISC, -1);
   }

   public static DeathCoordsFeature get() {
      return FeatureManager.INSTANCE.getFeature(DeathCoordsFeature.class);
   }

   public class_243 getLastDeathPosition() {
      return this.lastDeathPosition;
   }

   public String getLastDeathDimension() {
      return this.lastDeathDimension;
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reported = false;
   }

   @EventTarget
   public void onScreenOpen(ScreenOpenEvent event) {
      if (event.getScreen() instanceof class_418) {
         this.report(event.getClient(), event.getClient().field_1724);
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player == null) {
         this.reported = false;
      } else if (player.method_5805() && player.method_6032() > 0.0F) {
         this.reported = false;
      } else {
         this.report(client, player);
      }
   }

   private void report(class_310 client, class_746 player) {
      if (!this.reported && player != null) {
         this.reported = true;
         class_243 position = player.method_73189();
         this.lastDeathPosition = position;
         this.lastDeathDimension = client.field_1687 == null ? null : client.field_1687.method_27983().method_29177().method_12832();
         class_2338 pos = class_2338.method_49638(position);
         String coords = pos.method_10263() + " " + pos.method_10264() + " " + pos.method_10260();
         String dimension = this.lastDeathDimension == null ? "" : " (" + this.lastDeathDimension + ")";
         ChatUtil.error("Смерть на " + coords + dimension);
         if (this.copyToClipboard.getValue()) {
            client.field_1774.method_1455(coords);
         }
      }
   }
}
