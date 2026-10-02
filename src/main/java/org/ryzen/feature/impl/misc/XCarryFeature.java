package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2815;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;

@Environment(EnvType.CLIENT)
public final class XCarryFeature extends Feature {
   private static final int PLAYER_INVENTORY_CONTAINER_ID = 0;

   public XCarryFeature() {
      super("XCarry", "Store items in the crafting grid", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPhase() == PacketSendEvent.Phase.PRE) {
         if (event.getPacket() instanceof class_2815 packet && packet.method_36168() == 0) {
            event.cancel();
         }
      }
   }
}
