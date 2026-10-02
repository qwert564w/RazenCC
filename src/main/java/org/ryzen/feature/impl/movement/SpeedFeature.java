package org.ryzen.feature.impl.movement;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_238;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2663;
import net.minecraft.class_2708;
import net.minecraft.class_2848;
import net.minecraft.class_746;
import net.minecraft.class_9279;
import net.minecraft.class_9334;
import net.minecraft.class_2848.class_2849;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.TimerUtil;

@Environment(EnvType.CLIENT)
public final class SpeedFeature extends Feature implements PlayerContext {
   private static final String MODE_GRIM_NEW = "Grim New";
   private static final String MODE_COLLISION = "Collision";
   private static final String MODE_COLLISION_2 = "Collision2";
   private static final String MODE_HOLY_WORLD = "HolyWorld";
   private static final String MODE_META_HVH = "MetaHvH";
   private static final String MODE_GRIM_TELEPORT = "Grim Teleport";
   private static final String MODE_REALLYWORLD = "Reallyworld";
   private static final String META_DEFAULT = "Default";
   private static final String META_CUSTOM = "Custom";
   private static final int BOUNCE_WARMUP_TICKS = 3;
   private static final double BOUNCE_STEP = 0.03;
   private static final double BOUNCE_LIFT = 0.03;
   private static final double GRIM_GROUND_STEP = 0.0855;
   private static final double REALLY_GROUND_STEP = 0.085;
   private static final float GRIM_TICK_TIMER = 1.6F;
   private static final float GRIM_FLY_TIMER = 0.4F;
   private static final float REALLY_DISABLER_TIMER = 1.7F;
   private static final float REALLY_FLY_TIMER = 0.3F;
   private static final long TELEPORT_WARMUP_MS = 100L;
   private static final long TELEPORT_CYCLE_MS = 1400L;
   private static final float TELEPORT_TIMER_EVEN = 1.5F;
   private static final float TELEPORT_TIMER_ODD = 1.2F;
   private static final double COLLISION_SCALE = 0.2;
   private static final double HOLY_SCALE = 10.0;
   private static final double HOLY_VERTICAL_SLACK = 0.15;
   private static final float META_SLOWNESS_SCALE = 0.835F;
   private static final float META_AIRBORNE_SCALE = 1.435F;
   private static final float META_HEAD_DIVISOR = 1.3F;
   private static final float META_BOOST_SCALE = 10.0F;
   private static final float META_AMP_2_TOTEM = 0.49665F;
   private static final float META_AMP_2 = 0.41598004F;
   private static final float META_AMP_1_TOTEM = 0.43F;
   private static final float META_AMP_1 = 0.36F;
   private static final float META_BASE_TOTEM = 0.2924F;
   private static final float META_BASE = 0.24480002F;
   private static final String[] META_TOTEMS = new String[]{"Шар Геракла 2", "Шар CHAMPION", "Шар GOD", "Талисман Венома", "КУБИК-РУБИК"};
   private static final double[] META_HEAD_ARMOUR = new double[]{3.0, 3.5};
   private static final int HURT_STATUS = 2;
   public final ModeSetting mode = this.register(
      new ModeSetting("Mode", "Grim New", "Grim New", "Collision", "Collision2", "HolyWorld", "MetaHvH", "Grim Teleport", "Reallyworld")
   );
   public final NumberSetting collisionRange = this.register(
      new NumberSetting("Target Range", 1.5, 0.1, 3.0, 0.01, "").visibleWhen(() -> this.mode.is("Collision") || this.mode.is("Collision2"))
   );
   public final NumberSetting collisionSpeed = this.register(
      new NumberSetting("Shove Speed", 0.15, 0.1, 1.0, 0.01, "").visibleWhen(() -> this.mode.is("Collision") || this.mode.is("Collision2"))
   );
   public final NumberSetting holyRange = this.register(
      new NumberSetting("Overlap Range", 0.35, 0.2, 0.95, 0.01, "").visibleWhen(() -> this.mode.is("HolyWorld"))
   );
   public final NumberSetting holySpeed = this.register(
      new NumberSetting("Overlap Speed", 0.35, 0.3, 1.0, 0.05, "").visibleWhen(() -> this.mode.is("HolyWorld"))
   );
   public final ModeSetting metaMode = this.register(new ModeSetting("Meta Mode", "Default", "Default", "Custom").visibleWhen(() -> this.mode.is("MetaHvH")));
   public final NumberSetting metaSpeed = this.register(
      new NumberSetting("Meta Speed", 0.2, 0.2, 1.05, 0.01, "").visibleWhen(() -> this.mode.is("MetaHvH") && this.metaMode.is("Custom"))
   );
   public final BooleanSetting metaDamageBoost = this.register(new BooleanSetting("Boost On Damage", false).visibleWhen(() -> this.mode.is("MetaHvH")));
   public final NumberSetting metaBoostAmount = this.register(
      new NumberSetting("Boost Amount", 0.7, 0.1, 5.0, 0.1, "").visibleWhen(() -> this.mode.is("MetaHvH") && this.metaDamageBoost.getValue())
   );
   public final NumberSetting metaBoostDuration = this.register(
      new NumberSetting("Boost Duration", 700.0, 100.0, 2000.0, 100.0, "ms").visibleWhen(() -> this.mode.is("MetaHvH") && this.metaDamageBoost.getValue())
   );
   public final BooleanSetting reallyDisabler = this.register(new BooleanSetting("Disabler", true).visibleWhen(() -> this.mode.is("Reallyworld")));
   public final BooleanSetting lowerFps = this.register(new BooleanSetting("Lower FPS", false).visibleWhen(() -> this.mode.is("Reallyworld")));
   public final NumberSetting fpsLimit = this.register(
      new NumberSetting("FPS Limit", 30.0, 15.0, 30.0, 1.0, "").visibleWhen(() -> this.mode.is("Reallyworld") && this.lowerFps.getValue())
   );
   private int grimTicks;
   private int reallyTicks;
   private int grimCeilingTicks;
   private int reallyCeilingTicks;
   private boolean timerTouched;
   private long teleportCycleStart;
   private boolean teleportBoosting;
   private long damageBoostStart;
   private boolean damageBoosted;
   private boolean fpsOverridden;
   private int savedFpsLimit;

   public SpeedFeature() {
      super("Speed", "Moves you faster than walking", FeatureCategory.MOVEMENT, -1);
   }

   public static SpeedFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(SpeedFeature.class);
   }

   @Override
   protected void onEnable() {
      this.grimTicks = 0;
      this.reallyTicks = 0;
      this.grimCeilingTicks = 0;
      this.reallyCeilingTicks = 0;
      this.teleportCycleStart = System.currentTimeMillis();
      this.teleportBoosting = false;
      this.damageBoosted = false;
      this.timerTouched = false;
   }

   @Override
   protected void onDisable() {
      if (this.timerTouched) {
         TimerUtil.resetTimer();
         this.timerTouched = false;
      }

      this.restoreFps();
      this.grimTicks = 0;
      this.reallyTicks = 0;
      this.grimCeilingTicks = 0;
      this.reallyCeilingTicks = 0;
      this.teleportBoosting = false;
      this.damageBoosted = false;
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPost()) {
         this.onPlayerTickPost(event);
      } else {
         class_746 player = event.getPlayer();
         if (player != null && this.level() != null) {
            if (!this.mode.is("Grim New")) {
               this.grimTicks = 0;
               this.grimCeilingTicks = 0;
            }

            if (!this.mode.is("Reallyworld")) {
               this.reallyTicks = 0;
               this.reallyCeilingTicks = 0;
            }

            boolean clockMode = this.mode.is("Grim New") || this.mode.is("Reallyworld") || this.mode.is("Grim Teleport");
            if (!clockMode && this.timerTouched) {
               TimerUtil.resetTimer();
               this.timerTouched = false;
            }

            this.applyFpsOverride(this.mode.is("Reallyworld") && this.lowerFps.getValue());
            switch ((String)this.mode.getValue()) {
               case "Grim New":
                  this.tickGrimNew(player);
                  break;
               case "Reallyworld":
                  this.tickReallyworld(player);
                  break;
               case "Grim Teleport":
                  this.tickGrimTeleport(player);
                  break;
               case "Collision":
                  this.tickCollision(player, false);
                  break;
               case "Collision2":
                  this.tickCollision(player, true);
                  break;
               case "HolyWorld":
                  this.tickHolyWorld(player);
                  break;
               case "MetaHvH":
                  this.tickMetaHvH(player);
            }
         }
      }
   }

   private void onPlayerTickPost(PlayerTickEvent event) {
      class_746 player = event.getPlayer();
      if (player != null && player.field_3944 != null) {
         if (this.mode.is("Reallyworld")) {
            if (this.reallyDisabler.getValue() && this.reallyTicks % 2 == 0) {
               this.setTimer(0.3F);
               this.sendFallFlyingPair(player);
            }
         } else {
            if (this.mode.is("Grim New") && this.grimTicks % 2 == 0) {
               this.setTimer(0.4F);
               this.sendFallFlyingPair(player);
            }
         }
      }
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      class_746 player = this.localPlayer();
      if (player != null) {
         if (this.mode.is("Reallyworld")) {
            this.reallyCeilingTicks = player.field_5992 ? this.reallyCeilingTicks + 1 : 0;
            if (this.reallyCeilingTicks >= 1) {
               player.method_6043();
            }
         } else {
            if (this.mode.is("Grim New")) {
               this.grimCeilingTicks = player.field_5992 ? this.grimCeilingTicks + 1 : 0;
               if (this.grimCeilingTicks >= 1) {
                  player.method_6043();
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         if (event.getPacket() instanceof class_2708) {
            if (this.mode.is("Grim New")) {
               if (this.grimTicks % 2 == 1) {
                  this.grimTicks++;
               }

               this.setTimer(1.0F);
            } else if (this.mode.is("Reallyworld")) {
               if (this.reallyTicks % 2 == 1) {
                  this.reallyTicks++;
               }

               this.setTimer(1.0F);
            }
         } else {
            if (this.mode.is("MetaHvH") && this.metaDamageBoost.getValue() && event.getPacket() instanceof class_2663 packet && packet.method_11470() == 2) {
               mc.execute(() -> this.markDamaged(packet));
            }
         }
      }
   }

   private void markDamaged(class_2663 packet) {
      if (this.isEnabled() && this.level() != null && packet.method_11469(this.level()) == this.player()) {
         this.damageBoosted = true;
         this.damageBoostStart = System.currentTimeMillis();
      }
   }

   private void tickGrimNew(class_746 player) {
      if (this.grimTicks > 3) {
         double step = 0.03;
         if (this.grimTicks % 2 == 0) {
            this.setTimer(1.6F);
            player.method_45319(new class_243(0.0, 0.03, 0.0));
            if (player.method_24828()) {
               step = 0.0855;
            }
         }

         pushAlongInput(player, step);
      }

      this.grimTicks++;
   }

   private void tickReallyworld(class_746 player) {
      if (this.reallyDisabler.getValue()) {
         this.setTimer(1.7F);
      }

      if (this.reallyTicks > 3) {
         double step = 0.03;
         if (this.reallyTicks % 2 == 0) {
            player.method_45319(new class_243(0.0, 0.03, 0.0));
            step = player.method_24828() ? 0.085 : 0.03;
         }

         pushAlongInput(player, step);
      }

      this.reallyTicks++;
   }

   private static void pushAlongInput(class_746 player, double step) {
      double radians = movementDirection(player, true);
      player.method_45319(new class_243(-Math.sin(radians) * step, 0.0, Math.cos(radians) * step));
   }

   private void sendFallFlyingPair(class_746 player) {
      for (int i = 0; i < 2; i++) {
         player.field_3944.method_52787(new class_2848(player, class_2849.field_12982));
      }
   }

   private void tickGrimTeleport(class_746 player) {
      long elapsed = System.currentTimeMillis() - this.teleportCycleStart;
      if (elapsed >= 100L) {
         this.teleportBoosting = true;
      }

      if (elapsed >= 1400L) {
         this.teleportBoosting = false;
         this.teleportCycleStart = System.currentTimeMillis();
      }

      if (!this.teleportBoosting) {
         this.setTimer(1.0F);
      } else {
         if (player.method_24828() && !mc.field_1690.field_1903.method_1434()) {
            player.method_6043();
         }

         this.setTimer(player.field_6012 % 2 == 0 ? 1.5F : 1.2F);
      }
   }

   private void tickCollision(class_746 player, boolean fallBackToLook) {
      if (player.field_6235 <= 0 && !player.method_24828()) {
         class_1309 target = auraTarget();
         if (target != null && target != player) {
            class_243 toTarget = target.method_73189().method_1020(player.method_73189());
            if (toTarget.method_1033() <= this.collisionRange.getValue()) {
               this.shove(player, toTarget);
               return;
            }
         }

         if (fallBackToLook) {
            class_243 look = player.method_5720();
            this.shove(player, new class_243(look.field_1352, 0.0, look.field_1350));
         }
      }
   }

   private void shove(class_746 player, class_243 direction) {
      double length = Math.sqrt(direction.field_1352 * direction.field_1352 + direction.field_1350 * direction.field_1350);
      if (!(length <= 0.0)) {
         double scale = this.collisionSpeed.getValue() * 0.2 / length;
         player.method_5762(direction.field_1352 * scale, 0.0, direction.field_1350 * scale);
      }
   }

   private static class_1309 auraTarget() {
      AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
      if (aura == null) {
         return null;
      }

      class_1309 target = aura.getCurrentTarget();
      return target != null && target.method_5805() ? target : null;
   }

   private void tickHolyWorld(class_746 player) {
      if (!player.method_31549().field_7479 && !player.method_24828()) {
         double range = this.holyRange.getValue();
         class_238 overlap = player.method_5829().method_1009(range, 0.15, range);
         boolean touching = false;

         for (class_1657 other : this.level().method_18456()) {
            if (other != player && other.method_5805() && overlap.method_994(other.method_5829())) {
               touching = true;
               break;
            }
         }

         if (touching) {
            double speed = Math.max(0.0, this.holySpeed.getValue() / 10.0);
            class_243 push = inputVelocity(player, speed);
            player.method_5762(push.field_1352, 0.0, push.field_1350);
         }
      }
   }

   private static class_243 inputVelocity(class_746 player, double speed) {
      class_241 move = player.field_3913.method_3128();
      float forward = move.field_1342;
      float strafe = move.field_1343;
      float yaw = player.method_36454();
      if (forward != 0.0F) {
         if (strafe > 0.0F) {
            yaw += forward > 0.0F ? -45.0F : 45.0F;
         } else if (strafe < 0.0F) {
            yaw += forward > 0.0F ? 45.0F : -45.0F;
         }

         strafe = 0.0F;
         forward = forward > 0.0F ? 1.0F : -1.0F;
      }

      double sin = Math.sin(Math.toRadians(yaw + 90.0F));
      double cos = Math.cos(Math.toRadians(yaw + 90.0F));
      return new class_243(forward * speed * cos + strafe * speed * sin, 0.0, forward * speed * sin - strafe * speed * cos);
   }

   private void tickMetaHvH(class_746 player) {
      if (!player.method_6128()) {
         float speed;
         if (this.metaMode.is("Custom")) {
            speed = this.metaSpeed.getValue().floatValue();
         } else {
            speed = defaultMetaSpeed(player);
         }

         if (player.method_6059(class_1294.field_5909)) {
            speed *= 0.835F;
         }

         if (!player.method_24828()) {
            speed *= 1.435F;
         }

         if (this.metaDamageBoost.getValue() && this.consumeDamageBoost()) {
            speed += this.metaBoostAmount.getValue().floatValue() / 10.0F;
         }

         if (wearsBoostedHead(player)) {
            speed /= 1.3F;
         }

         double radians = movementDirection(player, true);
         class_243 movement = player.method_18798();
         player.method_18800(-Math.sin(radians) * speed, movement.field_1351, Math.cos(radians) * speed);
      }
   }

   private static float defaultMetaSpeed(class_746 player) {
      boolean totem = holdsMetaTotem(player);
      class_1293 speedEffect = player.method_6112(class_1294.field_5904);
      if (speedEffect == null) {
         return totem ? 0.2924F : 0.24480002F;
      }

      return switch (speedEffect.method_5578()) {
         case 1 -> totem ? 0.43F : 0.36F;
         case 2 -> totem ? 0.49665F : 0.41598004F;
         default -> totem ? 0.2924F : 0.24480002F;
      };
   }

   private static boolean holdsMetaTotem(class_746 player) {
      String name = player.method_6079().method_7964().getString();

      for (String totem : META_TOTEMS) {
         if (name.contains(totem)) {
            return true;
         }
      }

      return false;
   }

   private boolean consumeDamageBoost() {
      if (!this.damageBoosted) {
         return false;
      } else if (System.currentTimeMillis() - this.damageBoostStart >= this.metaBoostDuration.getValue()) {
         this.damageBoosted = false;
         return false;
      } else {
         return true;
      }
   }

   private static boolean wearsBoostedHead(class_746 player) {
      class_1799 head = player.method_6118(class_1304.field_6169);
      if (!head.method_31574(class_1802.field_8575)) {
         return false;
      }

      class_9279 customData = (class_9279)head.method_58694(class_9334.field_49628);
      if (customData == null) {
         return false;
      }

      String nbt = customData.method_57461().toString();
      if (!nbt.contains("AttributeModifiers")) {
         return false;
      }

      for (double armour : META_HEAD_ARMOUR) {
         if (nbt.contains("Amount:" + armour + "d")) {
            return true;
         }
      }

      return false;
   }

   private static double movementDirection(class_746 player, boolean radians) {
      class_241 move = player.field_3913.method_3128();
      float forward = move.field_1342;
      float strafe = move.field_1343;
      float yaw = player.method_36454();
      if (forward < 0.0F) {
         yaw += 180.0F;
      }

      float strafeScale = 1.0F;
      if (forward < 0.0F) {
         strafeScale = -0.5F;
      } else if (forward > 0.0F) {
         strafeScale = 0.5F;
      }

      if (strafe > 0.0F) {
         yaw -= 90.0F * strafeScale;
      }

      if (strafe < 0.0F) {
         yaw += 90.0F * strafeScale;
      }

      return radians ? Math.toRadians(yaw) : yaw;
   }

   private void setTimer(float multiplier) {
      TimerUtil.setTimer(multiplier);
      this.timerTouched = true;
   }

   private void applyFpsOverride(boolean wanted) {
      if (!wanted) {
         this.restoreFps();
      } else {
         int limit = this.fpsLimit.getValue().intValue();
         if (!this.fpsOverridden) {
            this.savedFpsLimit = (Integer)mc.field_1690.method_42524().method_41753();
            this.fpsOverridden = true;
         }

         if ((Integer)mc.field_1690.method_42524().method_41753() != limit) {
            mc.field_1690.method_42524().method_41748(limit);
         }
      }
   }

   private void restoreFps() {
      if (this.fpsOverridden) {
         mc.field_1690.method_42524().method_41748(this.savedFpsLimit);
         this.fpsOverridden = false;
      }
   }
}
