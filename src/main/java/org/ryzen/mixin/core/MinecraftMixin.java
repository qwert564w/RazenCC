package org.ryzen.mixin.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_638;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.game.TickContext;
import org.ryzen.event.events.lifecycle.ClientStartEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.lifecycle.ResourceReloadEvent;
import org.ryzen.event.events.lifecycle.ShutdownEvent;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.screen.ScreenOpenEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(class_310.class)
public abstract class MinecraftMixin {
   private static final TickContext TICK_CONTEXT = new TickContext();
   @Unique
   private class_638 previousLevel;
   @Shadow
   public class_638 field_1687;

   @Inject(method = "method_51736(Lnet/minecraft/class_310$class_8764;)V", at = @At("TAIL"))
   private void onClientStart(CallbackInfo ci) {
      if (EventManager.hasListeners(ClientStartEvent.class)) {
         EventManager.call(Events.CLIENT_START.set((class_310)(Object)this));
      }
   }

   @Inject(method = "method_1574", at = @At("HEAD"))
   private void onTick(CallbackInfo ci) {
      if (EventManager.hasListeners(GameTickEvent.class)) {
         class_310 client = (class_310)(Object)this;
         EventManager.call(Events.GAME_TICK.set(client, TICK_CONTEXT.begin(client)));
      }
   }

   @Inject(method = "method_1536", at = @At("HEAD"), cancellable = true)
   private void onAttack(CallbackInfoReturnable<Boolean> cir) {
      if (EventManager.hasListeners(AttackEvent.class)) {
         if (EventManager.call(Events.ATTACK.set((class_310)(Object)this)).isCancelled()) {
            cir.setReturnValue(false);
         }
      }
   }

   @Inject(method = "method_1481", at = @At("HEAD"))
   private void capturePreviousLevel(class_638 level, CallbackInfo ci) {
      this.previousLevel = this.field_1687;
   }

   @Inject(method = "method_1481", at = @At("TAIL"))
   private void onSetLevel(class_638 level, CallbackInfo ci) {
      class_310 client = (class_310)(Object)this;
      class_638 previous = this.previousLevel;
      if (level == null) {
         RotationContext.clear();
      }

      if (previous == null && level != null && EventManager.hasListeners(WorldJoinEvent.class)) {
         EventManager.call(Events.WORLD_JOIN.set(client, level));
      }

      if (previous != null && level == null && EventManager.hasListeners(WorldLeaveEvent.class)) {
         EventManager.call(Events.WORLD_LEAVE.set(client, previous));
      }
   }

   @Inject(method = "method_18096(Lnet/minecraft/class_437;ZZ)V", at = @At("HEAD"))
   private void onDisconnect(class_437 screen, boolean transferring, boolean resetting, CallbackInfo ci) {
      if (EventManager.hasListeners(DisconnectEvent.class)) {
         EventManager.call(Events.DISCONNECT.set((class_310)(Object)this, screen, transferring, resetting));
      }
   }

   @Inject(method = "method_29970", at = @At("HEAD"), cancellable = true)
   private void onSetScreen(class_437 screen, CallbackInfo ci) {
      class_310 client = (class_310)(Object)this;
      if (screen != null && EventManager.hasListeners(ScreenOpenEvent.class)) {
         if (EventManager.call(Events.SCREEN_OPEN.set(client, screen)).isCancelled()) {
            ci.cancel();
         }
      }
   }

   @Inject(method = "method_1592", at = @At("HEAD"))
   private void onShutdown(CallbackInfo ci) {
      if (EventManager.hasListeners(ShutdownEvent.class)) {
         EventManager.call(Events.SHUTDOWN.set((class_310)(Object)this));
      }
   }

   @Inject(method = "method_53465(Lnet/minecraft/class_310$class_8764;)V", at = @At("TAIL"))
   private void onResourceReload(CallbackInfo ci) {
      if (EventManager.hasListeners(ResourceReloadEvent.class)) {
         EventManager.call(Events.RESOURCE_RELOAD.set((class_310)(Object)this));
      }
   }
}
