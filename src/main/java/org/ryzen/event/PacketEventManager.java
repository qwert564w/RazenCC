package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;

@Environment(EnvType.CLIENT)
public final class PacketEventManager {
   private PacketEventManager() {
   }

   public static boolean hasSendListeners() {
      return EventManager.hasListeners(PacketSendEvent.class);
   }

   public static boolean hasReceiveListeners() {
      return EventManager.hasListeners(PacketReceiveEvent.class);
   }

   public static PacketSendEvent callSendPre(class_2535 connection, class_2596<?> packet) {
      return EventManager.call(new PacketSendEvent(connection, packet, PacketSendEvent.Phase.PRE));
   }

   public static void callSendPost(class_2535 connection, class_2596<?> packet) {
      EventManager.call(new PacketSendEvent(connection, packet, PacketSendEvent.Phase.POST));
   }

   public static boolean callReceivePre(class_2535 connection, class_2596<?> packet) {
      return EventManager.call(new PacketReceiveEvent(connection, packet, PacketReceiveEvent.Phase.PRE)).isCancelled();
   }

   public static void callReceivePost(class_2535 connection, class_2596<?> packet) {
      EventManager.call(new PacketReceiveEvent(connection, packet, PacketReceiveEvent.Phase.POST));
   }
}
