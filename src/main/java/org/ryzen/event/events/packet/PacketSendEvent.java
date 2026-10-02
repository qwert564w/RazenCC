package org.ryzen.event.events.packet;

import java.util.Objects;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.ryzen.event.CancellableEvent;

@Environment(EnvType.CLIENT)
public final class PacketSendEvent extends CancellableEvent {
   private final class_2535 connection;
   private class_2596<?> packet;
   private final PacketSendEvent.Phase phase;

   public PacketSendEvent(class_2535 connection, class_2596<?> packet, PacketSendEvent.Phase phase) {
      this.connection = connection;
      this.packet = packet;
      this.phase = phase;
   }

   public void setPacket(class_2596<?> packet) {
      if (this.isCompleted()) {
         throw new IllegalStateException("Cannot replace a packet after event dispatch.");
      }

      this.packet = Objects.requireNonNull(packet, "packet");
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
   public PacketSendEvent.Phase getPhase() {
      return this.phase;
   }

   @Environment(EnvType.CLIENT)
   public enum Phase {
      PRE,
      POST;
   }
}
