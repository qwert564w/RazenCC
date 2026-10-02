package org.ryzen.feature.impl.misc;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import net.minecraft.class_2743;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class FlagDetectorFeature extends Feature implements PlayerContext {
   private static final double VELOCITY_EPSILON = 0.0025;
   public final BooleanSetting logToChat = this.register(new BooleanSetting("Log To Chat", true));
   public final NumberSetting setbackDistance = this.register(new NumberSetting("Setback Distance", 0.5, 0.1, 5.0, 0.1, " blocks"));
   public final MultiSelectSetting disable = this.register(
      new MultiSelectSetting(
         "Disable On Flag",
         List.of("Flight", "Timer", "Blink", "BedrockClip", "AirStuck"),
         "Flight",
         "Timer",
         "Blink",
         "BedrockClip",
         "AirStuck",
         "Spider",
         "GrimGlide",
         "SuperFirework",
         "WaterSpeed",
         "InventoryMove",
         "Velocity"
      )
   );

   public FlagDetectorFeature() {
      super("FlagDetector", "Turns off movement features when the server sets you back", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         class_746 player = this.localPlayer();
         if (player != null) {
            if (event.getPacket() instanceof class_2708 packet) {
               class_243 destination = packet.comp_3228().comp_3148();
               double distance = player.method_73189().method_1022(destination);
               if (distance >= this.setbackDistance.getValue()) {
                  this.trigger("откат позиции на " + String.format(Locale.ROOT, "%.1f", distance) + " блоков");
               }
            } else {
               if (event.getPacket() instanceof class_2743 packet && packet.method_11818() == player.method_5628() && isZero(packet)) {
                  this.trigger("обнуление скорости");
               }
            }
         }
      }
   }

   private static boolean isZero(class_2743 packet) {
      return packet.method_73085().method_1027() < 6.25E-6;
   }

   private void trigger(String reason) {
      StringBuilder disabled = new StringBuilder();

      for (String name : this.disable.getValue()) {
         Feature feature = FeatureManager.INSTANCE.getFeature(name);
         if (feature != null && feature.isEnabled()) {
            feature.setEnabled(false);
            if (!disabled.isEmpty()) {
               disabled.append(", ");
            }

            disabled.append(feature.getName());
         }
      }

      if (this.logToChat.getValue()) {
         ChatUtil.error("[Flag] " + reason + (disabled.isEmpty() ? "" : " — выключено: " + disabled));
      }
   }
}
