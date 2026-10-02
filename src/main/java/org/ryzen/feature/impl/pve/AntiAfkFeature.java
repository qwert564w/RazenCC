package org.ryzen.feature.impl.pve;

import java.util.EnumSet;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_638;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;

@Environment(EnvType.CLIENT)
public final class AntiAfkFeature extends PveFeature {
   private static final float TURN_DEGREES = 3.0F;
   private static final float ROTATION_EPSILON = 0.01F;
   public final NumberSetting actionInterval = this.register(new NumberSetting("Action Interval", 20.0, 1.0, 300.0, 1.0, " s"));
   public final BooleanSetting turnHead = this.register(new BooleanSetting("Turn Head", true));
   public final BooleanSetting jump = this.register(new BooleanSetting("Jump", true));
   public final BooleanSetting sendChatMessage = this.register(new BooleanSetting("Send Chat Message", false));
   public final TextSetting chatText = this.register(new TextSetting("Chat Text", "Still here", 256).visibleWhen(this.sendChatMessage::getValue));
   private final AntiAfkActionTimer timer = new AntiAfkActionTimer();
   private class_638 observedLevel;
   private boolean orientationKnown;
   private float lastYaw;
   private float lastPitch;
   private float turnDirection = 1.0F;

   public AntiAfkFeature() {
      super("AntiAFK", "Performs small idle actions at a configurable interval", -1, AutomationPriority.BACKGROUND);
   }

   @Override
   protected void onPveEnable() {
      this.reset(null);
   }

   @Override
   protected void onPveDisable() {
      this.reset(null);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      class_638 level = client.field_1687;
      if (player == null || level == null) {
         this.reset(null);
      } else if (level != this.observedLevel) {
         this.reset(level);
         this.rememberOrientation(player);
      } else if (client.field_1755 != null) {
         this.timer.reset();
         this.rememberOrientation(player);
      } else {
         this.timer.advance();
         if (this.hasManualActivity(client, player)) {
            this.timer.reset();
         } else if (!this.hasConfiguredAction()) {
            this.timer.reset();
         } else {
            long intervalTicks = AntiAfkActionTimer.secondsToTicks(this.actionInterval.getValue());
            if (this.timer.isDue(intervalTicks) && !this.automationIsBusy()) {
               boolean shouldTurn = this.turnHead.getValue();
               boolean shouldJump = this.jump.getValue() && this.canJumpSafely(player);
               String message = this.chatText.getValue().trim();
               boolean shouldChat = this.sendChatMessage.getValue() && !message.isEmpty();
               EnumSet<AutomationResource> resources = EnumSet.noneOf(AutomationResource.class);
               if (shouldTurn) {
                  resources.add(AutomationResource.ROTATION);
               }

               if (shouldJump) {
                  resources.add(AutomationResource.MOVEMENT);
               }

               if (shouldChat) {
                  resources.add(AutomationResource.CHAT);
               }

               if (!resources.isEmpty() && PveAutomationCoordinator.INSTANCE.acquire(this, AutomationPriority.BACKGROUND, resources)) {
                  try {
                     if (shouldTurn) {
                        this.turnSlightly(player);
                     }

                     if (shouldJump) {
                        player.method_6043();
                     }

                     if (shouldChat) {
                        player.field_3944.method_45729(message);
                     }

                     this.timer.actionPerformed();
                  } finally {
                     PveAutomationCoordinator.INSTANCE.release(this);
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset(null);
   }

   @EventTarget
   public void onWorldJoin(WorldJoinEvent event) {
      this.reset(event.getLevel());
   }

   private boolean hasManualActivity(class_310 client, class_746 player) {
      boolean keyActivity = client.field_1690.field_1894.method_1434()
         || client.field_1690.field_1881.method_1434()
         || client.field_1690.field_1913.method_1434()
         || client.field_1690.field_1849.method_1434()
         || client.field_1690.field_1903.method_1434()
         || client.field_1690.field_1832.method_1434()
         || client.field_1690.field_1867.method_1434()
         || client.field_1690.field_1886.method_1434()
         || client.field_1690.field_1904.method_1434();
      if (!this.orientationKnown) {
         this.rememberOrientation(player);
         return keyActivity;
      } else {
         boolean rotationActivity = Math.abs(class_3532.method_15393(player.method_36454() - this.lastYaw)) > 0.01F
            || Math.abs(player.method_36455() - this.lastPitch) > 0.01F;
         this.rememberOrientation(player);
         return keyActivity || rotationActivity;
      }
   }

   private boolean automationIsBusy() {
      PveAutomationCoordinator coordinator = PveAutomationCoordinator.INSTANCE;
      return coordinator.isClaimedByOther(this, AutomationResource.MOVEMENT)
         || coordinator.isClaimedByOther(this, AutomationResource.ROTATION)
         || coordinator.isClaimedByOther(this, AutomationResource.CHAT);
   }

   private boolean hasConfiguredAction() {
      return this.turnHead.getValue() || this.jump.getValue() || this.sendChatMessage.getValue() && !this.chatText.getValue().isBlank();
   }

   private boolean canJumpSafely(class_746 player) {
      return player.method_5805()
         && player.method_24828()
         && !player.method_5765()
         && !player.method_18276()
         && !player.method_7325()
         && !player.method_31549().field_7479
         && !player.method_6128()
         && !player.method_5799()
         && !player.method_5771()
         && !player.method_6101();
   }

   private void turnSlightly(class_746 player) {
      float yaw = class_3532.method_15393(player.method_36454() + 3.0F * this.turnDirection);
      this.turnDirection = -this.turnDirection;
      player.method_36456(yaw);
      player.method_5847(yaw);
      this.rememberOrientation(player);
   }

   private void rememberOrientation(class_746 player) {
      this.lastYaw = player.method_36454();
      this.lastPitch = player.method_36455();
      this.orientationKnown = true;
   }

   private void reset(class_638 level) {
      this.observedLevel = level;
      this.timer.reset();
      this.orientationKnown = false;
      this.lastYaw = 0.0F;
      this.lastPitch = 0.0F;
      this.turnDirection = 1.0F;
   }
}
