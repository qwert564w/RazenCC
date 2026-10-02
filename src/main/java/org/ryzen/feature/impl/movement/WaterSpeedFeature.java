package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3610;
import net.minecraft.class_746;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.TimerUtil;

@Environment(EnvType.CLIENT)
public final class WaterSpeedFeature extends Feature implements PlayerContext {
   private static final String MODE_SIMPLE = "Simple";
   private static final String MODE_LEGIT = "Legit";
   private static final String MODE_MOTION = "Motion";
   private static final String MODE_SWIM_BOOST = "Swim Boost";
   private static final double LEGIT_SPEED_CAP = 0.209;
   private static final double SURFACE_TOLERANCE = 0.2;
   private static final double SURFACE_LIFT = 0.2;
   private static final double SURFACE_TIMER_SCALE = 5.0;
   private static final int BOB_PERIOD = 10;
   private static final int BOB_JUMP_TICKS = 5;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Simple", "Simple", "Legit", "Motion", "Swim Boost"));
   public final NumberSetting boost = this.register(new NumberSetting("Boost", 1.05, 1.01, 1.2, 0.01, "x").visibleWhen(() -> this.mode.is("Simple")));
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 0.5, 0.1, 2.0, 0.05, "").visibleWhen(() -> this.mode.is("Motion")));
   public final BooleanSetting autoBob = this.register(new BooleanSetting("Auto Jump/Sneak", false).visibleWhen(() -> this.mode.is("Legit")));
   public final BooleanSetting stayUnder = this.register(new BooleanSetting("Stay Under", false).visibleWhen(this.autoBob::getValue));
   private int waterTicks;
   private boolean bobbing;
   private boolean timerActive;

   public WaterSpeedFeature() {
      super("WaterSpeed", "Swim faster", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onEnable() {
      this.waterTicks = 0;
   }

   @Override
   protected void onDisable() {
      this.waterTicks = 0;
      this.releaseBob();
      this.releaseTimer();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player == null || client.field_1687 == null) {
         this.releaseTimer();
      } else if (!player.method_5799()) {
         this.releaseBob();
         this.releaseTimer();
         this.waterTicks = 0;
      } else {
         switch ((String)this.mode.getValue()) {
            case "Legit":
               if (this.autoBob.getValue()) {
                  this.runBob(client, player);
               }

               if (player.method_5681()) {
                  this.nudgeLegit(player);
               }
               break;
            case "Motion":
               if (player.method_5681()) {
                  this.driveMotion(player);
               }
               break;
            case "Swim Boost":
               this.rideSurface(client, player);
               break;
            default:
               if (player.method_5681() && this.isMoving()) {
                  class_243 movement = player.method_18798();
                  player.method_18800(movement.field_1352 * this.boost.getValue(), movement.field_1351, movement.field_1350 * this.boost.getValue());
               }
         }
      }
   }

   private void nudgeLegit(class_746 player) {
      if (player.field_6012 % (15 + player.method_5628() % 11) == 0) {
         class_243 movement = player.method_18798();
         double horizontal = Math.sqrt(movement.field_1352 * movement.field_1352 + movement.field_1350 * movement.field_1350);
         if (!(horizontal <= 0.01)) {
            double factor = 1.01 + Math.random() * 0.015;
            if (horizontal * factor > 0.209) {
               factor = 0.209 / horizontal;
            }

            player.method_18800(movement.field_1352 * factor, movement.field_1351, movement.field_1350 * factor);
         }
      }
   }

   private void driveMotion(class_746 player) {
      float forward = player.field_3913.method_3128().field_1342;
      float strafe = player.field_3913.method_3128().field_1343;
      if (forward != 0.0F || strafe != 0.0F) {
         double yawRadians = Math.toRadians(player.method_36454());
         double speedValue = this.speed.getValue();
         double x = -Math.sin(yawRadians) * speedValue;
         double z = Math.cos(yawRadians) * speedValue;
         if (forward < 0.0F) {
            x = -x;
            z = -z;
         }

         player.method_18800(x, player.method_18798().field_1351, z);
      }
   }

   private void rideSurface(class_310 client, class_746 player) {
      if (!client.field_1690.field_1903.method_1434()) {
         this.releaseTimer();
      } else {
         class_2338 pos = player.method_24515();
         class_3610 fluid = client.field_1687.method_8316(pos);
         if (!fluid.method_15767(class_3486.field_15517)) {
            this.releaseTimer();
         } else {
            double surfaceY = pos.method_10264() + fluid.method_15763(client.field_1687, pos);
            double eyeY = player.method_23320();
            if (!(eyeY < surfaceY - 0.2) && !(eyeY > surfaceY + 0.2)) {
               class_243 movement = player.method_18798();
               player.method_18800(movement.field_1352, 0.2, movement.field_1350);
               double horizontal = Math.sqrt(movement.field_1352 * movement.field_1352 + movement.field_1350 * movement.field_1350);
               TimerUtil.setTimer((float)Math.max(1.0, horizontal * 5.0));
               this.timerActive = true;
            } else {
               this.releaseTimer();
            }
         }
      }
   }

   private void runBob(class_310 client, class_746 player) {
      this.bobbing = true;
      this.waterTicks++;
      boolean holdDown = this.stayUnder.getValue() && client.field_1687.method_8320(player.method_24515().method_10084()).method_26215();
      if (!holdDown && this.waterTicks % 10 < 5) {
         client.field_1690.field_1903.method_23481(true);
         client.field_1690.field_1832.method_23481(false);
      } else {
         client.field_1690.field_1903.method_23481(false);
         client.field_1690.field_1832.method_23481(true);
      }
   }

   private void releaseBob() {
      if (this.bobbing) {
         this.bobbing = false;
         class_310 client = class_310.method_1551();
         client.field_1690.field_1903.method_23481(false);
         client.field_1690.field_1832.method_23481(false);
      }
   }

   private void releaseTimer() {
      if (this.timerActive) {
         TimerUtil.resetTimer();
         this.timerActive = false;
      }
   }
}
