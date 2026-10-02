package org.ryzen.event.events.packet;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class PacketReceiveEvent extends CancellableEvent {
   private final class_2535 connection;
   private final class_2596<?> packet;
   private final PacketReceiveEvent.Phase phase;

   public PacketReceiveEvent(class_2535 connection, class_2596<?> packet, PacketReceiveEvent.Phase phase) {
      this.connection = connection;
      this.packet = packet;
      this.phase = phase;
   }

   @Generated
   public class_2535 getConnection() {
      return this.connection;
   }

   @Generated
   public class_2596<?> getPacket() {
      return this.packet;
   }

   @Generated
   public PacketReceiveEvent.Phase getPhase() {
      return this.phase;
   }

   @Environment(EnvType.CLIENT)
   public enum Phase {
      PRE,
      POST;
   }
}
