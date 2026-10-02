package org.ryzen.mixin.core;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2535;
import net.minecraft.class_2596;
import org.ryzen.event.PacketEventManager;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(class_2535.class)
public abstract class ConnectionMixin {
   @WrapMethod(method = "method_52906(Lnet/minecraft/class_2596;Lio/netty/channel/ChannelFutureListener;Z)V")
   private void wrapSend(class_2596<?> packet, ChannelFutureListener listener, boolean flush, Operation<Void> original) {
      if (!PacketEventManager.hasSendListeners()) {
         original.call(new Object[]{packet, listener, flush});
      } else {
         PacketSendEvent event = PacketEventManager.callSendPre((class_2535)(Object)this, packet);
         if (!event.isCancelled()) {
            class_2596<?> dispatchedPacket = event.getPacket();
            original.call(new Object[]{dispatchedPacket, listener, flush});
            PacketEventManager.callSendPost((class_2535)(Object)this, dispatchedPacket);
         }
      }
   }

   @WrapMethod(method = "method_10770(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/class_2596;)V")
   private void wrapReceive(ChannelHandlerContext context, class_2596<?> packet, Operation<Void> original) {
      if (!PacketEventManager.hasReceiveListeners()) {
         original.call(new Object[]{context, packet});
      } else if (!PacketEventManager.callReceivePre((class_2535)(Object)this, packet)) {
         original.call(new Object[]{context, packet});
         PacketEventManager.callReceivePost((class_2535)(Object)this, packet);
      }
   }
}
