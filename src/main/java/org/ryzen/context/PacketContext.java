package org.ryzen.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import net.minecraft.class_634;
import org.ryzen.event.PacketEventManager;

@Environment(EnvType.CLIENT)
public interface PacketContext extends MinecraftContext {
   default class_634 packetListener() {
      return this.client().method_1562();
   }

   default class_2535 connection() {
      class_634 packetListener = this.packetListener();
      return packetListener != null ? packetListener.method_48296() : null;
   }

   default boolean hasConnection() {
      return this.connection() != null;
   }

   default boolean isConnected() {
      class_2535 connection = this.connection();
      return connection != null && connection.method_10758();
   }

   default void sendPacket(class_2596<?> packet) {
      class_2535 connection = this.connection();
      if (connection != null && packet != null) {
         connection.method_10743(packet);
      }
   }

   default boolean hasPacketSendListeners() {
      return PacketEventManager.hasSendListeners();
   }

   default boolean hasPacketReceiveListeners() {
      return PacketEventManager.hasReceiveListeners();
   }
}
