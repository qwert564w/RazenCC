package org.ryzen.feature.impl.player;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_2708;
import net.minecraft.class_2793;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class NoPearlTeleportFeature extends Feature implements PlayerContext {
   public final BooleanSetting checkDistance = this.register(new BooleanSetting("Check Distance", true));
   public final NumberSetting maxDistance = this.register(
      new NumberSetting("Max Distance", 64.0, 8.0, 256.0, 1.0, " blocks").visibleWhen(this.checkDistance::getValue)
   );

   public NoPearlTeleportFeature() {
      super("NoPearlTeleport", "Cancels the teleport an ender pearl would do", FeatureCategory.PLAYER, -1);
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_2708 packet) {
         class_746 player = this.localPlayer();
         if (player != null && player.field_3944 != null) {
            if (this.checkDistance.getValue()) {
               class_243 destination = packet.comp_3228().comp_3148();
               if (player.method_73189().method_1022(destination) > this.maxDistance.getValue()) {
                  return;
               }
            }

            event.cancel();
            player.field_3944.method_52787(new class_2793(packet.comp_3133()));
         }
      }
   }
}
