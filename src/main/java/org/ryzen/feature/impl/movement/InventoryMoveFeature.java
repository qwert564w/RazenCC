package org.ryzen.feature.impl.movement;

import java.util.ArrayDeque;
import java.util.Deque;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_2596;
import net.minecraft.class_2813;
import net.minecraft.class_304;
import net.minecraft.class_3675;
import net.minecraft.class_490;
import net.minecraft.class_746;
import net.minecraft.class_3675.class_306;
import net.minecraft.class_3675.class_307;
import org.lwjgl.glfw.GLFW;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.event.events.screen.ScreenCloseEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.mixin.accessor.KeyMappingAccessor;

@Environment(EnvType.CLIENT)
public final class InventoryMoveFeature extends Feature implements MinecraftContext {
   public final BooleanSetting tickDilation = this.register(new BooleanSetting("Tick Dilation", false));
   private final Deque<class_2596<?>> deferredClicks = new ArrayDeque<>();
   private InventoryMoveFeature.ReplayState replayState = InventoryMoveFeature.ReplayState.IDLE;
   private int phaseTicks;

   public InventoryMoveFeature() {
      super("InventoryMove", "Walk around while your inventory is open", FeatureCategory.MOVEMENT, -1);
   }

   public static InventoryMoveFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(InventoryMoveFeature.class);
   }

   public static class_10185 screenInput() {
      InventoryMoveFeature feature = getEnabled();
      return feature != null && mc.field_1724 != null && feature.screen() instanceof class_490
         ? new class_10185(
            isKeyDown(mc.field_1690.field_1894),
            isKeyDown(mc.field_1690.field_1881),
            isKeyDown(mc.field_1690.field_1913),
            isKeyDown(mc.field_1690.field_1849),
            isKeyDown(mc.field_1690.field_1903),
            isKeyDown(mc.field_1690.field_1832),
            isKeyDown(mc.field_1690.field_1867)
         )
         : null;
   }

   public static boolean shouldStopMovement() {
      InventoryMoveFeature feature = getEnabled();
      return feature != null && feature.replayState != InventoryMoveFeature.ReplayState.IDLE;
   }

   public static boolean isClickPipelineBusy() {
      InventoryMoveFeature feature = getEnabled();
      return feature != null && (feature.replayState != InventoryMoveFeature.ReplayState.IDLE || !feature.deferredClicks.isEmpty());
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPhase() == PacketSendEvent.Phase.PRE
         && event.getPacket() instanceof class_2813
         && this.replayState == InventoryMoveFeature.ReplayState.IDLE
         && this.screen() instanceof class_490
         && (this.isMoving() || !this.deferredClicks.isEmpty())) {
         this.deferredClicks.addLast(event.getPacket());
         event.cancel();
      }
   }

   @EventTarget
   public void onScreenClose(ScreenCloseEvent event) {
      if (event.getScreen() instanceof class_490) {
         class_304.method_1424();
         if (!this.deferredClicks.isEmpty() && this.replayState == InventoryMoveFeature.ReplayState.IDLE) {
            this.replayState = InventoryMoveFeature.ReplayState.PREPARING;
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.replayState != InventoryMoveFeature.ReplayState.IDLE) {
         class_746 player = this.player();
         if (player == null) {
            this.reset();
         } else {
            this.phaseTicks++;
            if (this.phaseTicks >= (this.tickDilation.getValue() ? 2 : 1)) {
               this.phaseTicks = 0;
               switch (this.replayState) {
                  case PREPARING:
                     this.replayState = InventoryMoveFeature.ReplayState.SENDING;
                     break;
                  case SENDING:
                     class_2596<?> packet = this.deferredClicks.pollFirst();
                     if (packet != null) {
                        player.field_3944.method_52787(packet);
                     }

                     if (this.deferredClicks.isEmpty()) {
                        this.replayState = InventoryMoveFeature.ReplayState.SETTLING;
                     }
                     break;
                  case SETTLING:
                     this.reset();
                     break;
                  default:
                     this.reset();
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
   }

   @Override
   protected void onDisable() {
      class_746 player = this.player();
      if (player != null) {
         while (!this.deferredClicks.isEmpty()) {
            player.field_3944.method_52787(this.deferredClicks.pollFirst());
         }
      }

      this.reset();
   }

   private boolean isMoving() {
      class_746 player = this.player();
      return player != null && player.field_3913 != null && player.field_3913.method_3128().method_35587() > 1.0E-4F;
   }

   private void reset() {
      this.deferredClicks.clear();
      this.replayState = InventoryMoveFeature.ReplayState.IDLE;
      this.phaseTicks = 0;
   }

   private static boolean isKeyDown(class_304 mapping) {
      class_306 key = ((KeyMappingAccessor)mapping).getKey();
      return key.method_1442() == class_307.field_1672
         ? GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), key.method_1444()) == 1
         : class_3675.method_15987(mc.method_22683(), key.method_1444());
   }

   @Environment(EnvType.CLIENT)
   private enum ReplayState {
      IDLE,
      PREPARING,
      SENDING,
      SETTLING;
   }
}
