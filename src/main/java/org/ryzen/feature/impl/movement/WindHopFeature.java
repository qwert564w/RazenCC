package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_239;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_2886;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2828.class_2831;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.game.PlayerJumpEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.ModeSetting;

@Environment(EnvType.CLIENT)
public final class WindHopFeature extends Feature implements PlayerContext {
   private static final String MODE_AFTER_USE = "After Use";
   private static final String MODE_AUTO = "Auto";
   private static final String MODE_BIND = "Bind";
   private static final int OFFHAND_MARKER = 40;
   private static final float WALL_SCAN_PITCH = 75.0F;
   private static final float DOWNWARD_PITCH = 90.0F;
   private static final double WALL_SCAN_DISTANCE = 1.5;
   private static final double RISING_SPEED = 0.4;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "After Use", "After Use", "Auto", "Bind"));
   public final InputBindSetting key = this.register(new InputBindSetting("Key", -1).visibleWhen(() -> this.mode.is("Bind")));
   public final BooleanSetting autoJump = this.register(new BooleanSetting("Auto Jump", true));
   public final BooleanSetting predictLanding = this.register(new BooleanSetting("Predict Landing", true));
   public final BooleanSetting lookDown = this.register(new BooleanSetting("Look Down", true).visibleWhen(() -> this.mode.is("After Use")));
   private boolean pending;
   private int jumpDelay = -1;

   public WindHopFeature() {
      super("Wind Hop", "Jumps after a wind charge or uses one for an extra jump", FeatureCategory.PLAYER, -1);
      this.renamedFrom("WindHop");
   }

   @Override
   protected void onDisable() {
      this.pending = false;
      this.jumpDelay = -1;
   }

   @EventTarget
   public void onJump(PlayerJumpEvent event) {
      if (this.mode.is("Auto")) {
         this.useCharge(class_310.method_1551(), event.getPlayer());
      }
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (this.mode.is("After Use") && event.getPhase() == PacketSendEvent.Phase.PRE && event.getPacket() instanceof class_2886 packet) {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null && player.method_5998(packet.method_12551()).method_31574(class_1802.field_49098)) {
            if (this.lookDown.getValue() && player.field_3944 != null) {
               player.field_3944.method_52787(new class_2831(player.method_36454(), 90.0F, player.method_24828(), player.field_5976));
            }

            this.jumpDelay = 2;
         }
      }
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      if (this.mode.is("After Use") && this.jumpDelay == 0) {
         event.setJump(true);
         this.jumpDelay = -1;
      }
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (this.mode.is("Bind") && event.getAction() == 0 && this.key.matches(event.getKey())) {
         this.pending = true;
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (this.mode.is("Bind") && event.getAction() == 0 && this.key.matchesMouse(event.getButton())) {
         this.pending = true;
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (this.jumpDelay > 0) {
         this.jumpDelay--;
      }

      if (player != null && client.field_1687 != null) {
         if (!this.mode.is("After Use")) {
            if (findChargeSlot(player) != -1) {
               if (this.mode.is("Bind")) {
                  if (this.pending) {
                     this.pending = false;
                     this.useCharge(client, player);
                  }
               } else {
                  boolean shouldFire = false;
                  if (this.isRisingInOpenAir(client, player)) {
                     shouldFire = this.scanForWall(client, player) != null;
                  } else if (this.predictLanding.getValue()) {
                     shouldFire = !blockAt(client, player, -1.0).method_26215() && player.field_6017 > 2.0;
                  }

                  if (shouldFire && (client.field_1690.field_1903.method_1434() || this.autoJump.getValue())) {
                     this.useCharge(client, player);
                  }

                  if (player.method_24828() && this.autoJump.getValue()) {
                     player.method_6043();
                  }
               }
            }
         }
      }
   }

   private void useCharge(class_310 client, class_746 player) {
      if (player != null && player.field_3944 != null && client.field_1687 != null) {
         int slot = findChargeSlot(player);
         if (slot != -1) {
            boolean offhand = slot == 40;
            class_1268 hand = offhand ? class_1268.field_5810 : class_1268.field_5808;
            int previousSlot = player.method_31548().method_67532();
            boolean switched = !offhand && slot != previousSlot;
            if (switched) {
               player.field_3944.method_52787(new class_2868(slot));
            }

            float yaw = player.method_36454();
            float pitch = 90.0F;
            if (this.isRisingInOpenAir(client, player)) {
               Float wallYaw = this.scanForWall(client, player);
               if (wallYaw != null) {
                  yaw = wallYaw;
                  pitch = 75.0F;
               }
            }

            player.field_3944.method_52787(new class_2886(hand, 0, yaw, pitch));
            player.method_6104(hand);
            if (switched) {
               player.field_3944.method_52787(new class_2868(previousSlot));
            }
         }
      }
   }

   private Float scanForWall(class_310 client, class_746 player) {
      class_243 eye = player.method_33571();

      for (int offset = 0; offset < 360; offset += 45) {
         class_243 direction = class_243.method_1030(75.0F, offset);
         class_239 hit = client.field_1687
            .method_17742(new class_3959(eye, eye.method_1019(direction.method_1021(1.5)), class_3960.field_17558, class_242.field_1348, player));
         if (hit.method_17783() == class_240.field_1332) {
            return class_3532.method_15393(player.method_36454() + offset);
         }
      }

      return null;
   }

   private boolean isRisingInOpenAir(class_310 client, class_746 player) {
      return !player.method_24828() && blockAt(client, player, -2.0).method_26215() && player.method_18798().field_1351 > 0.4;
   }

   private static class_2680 blockAt(class_310 client, class_746 player, double offsetY) {
      return client.field_1687.method_8320(class_2338.method_49638(player.method_73189().method_1031(0.0, offsetY, 0.0)));
   }

   private static int findChargeSlot(class_746 player) {
      if (player.method_6079().method_31574(class_1802.field_49098)) {
         return 40;
      }

      for (int slot = 0; slot < 9; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(class_1802.field_49098)) {
            return slot;
         }
      }

      return -1;
   }
}
