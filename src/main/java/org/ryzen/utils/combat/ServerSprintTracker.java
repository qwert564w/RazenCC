package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2848;
import net.minecraft.class_2848.class_2849;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;

@Environment(EnvType.CLIENT)
public final class ServerSprintTracker {
   public static final ServerSprintTracker INSTANCE = new ServerSprintTracker();
   private volatile boolean sprinting;

   private ServerSprintTracker() {
   }

   public static boolean isServerSprinting() {
      return INSTANCE.sprinting;
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPhase() == PacketSendEvent.Phase.POST && event.getPacket() instanceof class_2848 packet) {
         if (packet.method_12365() == class_2849.field_12981) {
            this.sprinting = true;
         } else if (packet.method_12365() == class_2849.field_12985) {
            this.sprinting = false;
         }
      }
   }

   @EventTarget
   public void onWorldJoin(WorldJoinEvent event) {
      this.sprinting = false;
      LocalPlayerHistory.reset();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.sprinting = false;
      LocalPlayerHistory.reset();
   }
}
