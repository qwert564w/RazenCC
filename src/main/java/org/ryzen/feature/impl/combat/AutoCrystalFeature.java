package org.ryzen.feature.impl.combat;

import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1511;
import net.minecraft.class_1657;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3965;
import net.minecraft.class_636;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_12180.class_12181;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class AutoCrystalFeature extends Feature implements MinecraftContext {
   private static final long SWAP_REVERT_MS = 100L;
   private static final double PITCH_CAP = 89.0;
   private static final float LINE_WIDTH = 1.5F;
   public final NumberSetting range = this.register(new NumberSetting("Range", 5.0, 1.0, 6.0, 0.1, ""));
   public final NumberSetting placeDelay = this.register(new NumberSetting("Place Delay", 0.0, 0.0, 20.0, 1.0, "t"));
   public final NumberSetting breakDelay = this.register(new NumberSetting("Break Delay", 0.0, 0.0, 20.0, 1.0, "t"));
   public final BooleanSetting autoPlace = this.register(new BooleanSetting("Auto Place", true));
   public final BooleanSetting render = this.register(new BooleanSetting("Render", true));
   public final ModeSetting swapMode = this.register(new ModeSetting("Swap Mode", "Hand", "Hand", "Packet"));
   private class_2338 placePos;
   private class_1657 target;
   private int placeTicks;
   private int breakTicks;
   private int previousSlot = -1;
   private boolean pendingSwapRevert;
   private long swapRevertAt;
   private float renderYaw;
   private float renderPitch;

   public AutoCrystalFeature() {
      super("AutoCrystal", "Places and breaks end crystals on nearby players", FeatureCategory.COMBAT, -1);
   }

   @Override
   protected void onEnable() {
      class_746 player = mc.field_1724;
      if (player != null) {
         this.renderYaw = player.method_36454();
         this.renderPitch = player.method_36455();
      }

      this.resetSession();
   }

   @Override
   protected void onDisable() {
      class_746 player = mc.field_1724;
      if (player != null && this.previousSlot != -1 && this.previousSlot < 9) {
         player.method_31548().method_61496(this.previousSlot);
      }

      this.resetSession();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.resetSession();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = mc.field_1724;
      class_638 level = mc.field_1687;
      if (player != null && level != null) {
         this.placeTicks++;
         this.breakTicks++;
         this.handleSwapRevert(player);
         this.target = this.findTarget(player, level);
         if (this.target == null) {
            this.placePos = null;
            this.renderYaw = player.method_36454();
            this.renderPitch = player.method_36455();
         } else {
            if (this.autoPlace.getValue() && this.placeTicks >= this.placeDelay.getValue()) {
               this.placePos = this.findPlacePos(player, level);
               if (this.placePos != null) {
                  this.placeCrystal(player, this.placePos);
                  this.placeTicks = 0;
               }
            }

            if (this.breakTicks >= this.breakDelay.getValue()) {
               class_1511 crystal = this.findCrystalToBreak(player, level);
               if (crystal != null) {
                  this.rotateToCrystal(player, crystal);
                  this.attackCrystal(player, crystal);
                  this.breakTicks = 0;
               }
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.render.getValue() && mc.field_1724 != null && mc.field_1687 != null && event.getClient().field_1769 != null) {
         class_2338 pos = this.placePos;
         class_1657 currentTarget = this.target;
         if (pos != null && currentTarget != null) {
            class_243 crystalCenter = new class_243(pos.method_10263() + 0.5, pos.method_10264() + 1.0, pos.method_10260() + 0.5);
            double targetDist = currentTarget.method_73189().method_1025(crystalCenter);
            double selfDist = mc.field_1724.method_73189().method_1025(crystalCenter);
            int color;
            if (targetDist <= 3.5 && selfDist > targetDist) {
               color = -1275003086;
            } else if (targetDist <= 6.0 && selfDist >= targetDist * 0.7) {
               color = -1593835776;
            } else {
               color = 2029998080;
            }

            class_238 box = new class_238(pos);
            class_12181 ignored = event.getClient().field_1769.method_75414();

            try {
               class_12180.method_75553(new AutoCrystalFeature.CrystalGizmo(box, color)).method_75533();
            } catch (Throwable var15) {
               if (ignored != null) {
                  try {
                     ignored.close();
                  } catch (Throwable var14) {
                     var15.addSuppressed(var14);
                  }
               }

               throw var15;
            }

            if (ignored != null) {
               ignored.close();
            }
         }
      }
   }

   private void handleSwapRevert(class_746 player) {
      if (this.pendingSwapRevert && this.swapMode.is("Hand")) {
         if (System.currentTimeMillis() - this.swapRevertAt >= 100L) {
            if (this.previousSlot != -1 && this.previousSlot < 9) {
               player.method_31548().method_61496(this.previousSlot);
               this.previousSlot = -1;
            }

            this.pendingSwapRevert = false;
         }
      }
   }

   private void resetSession() {
      this.placePos = null;
      this.target = null;
      this.placeTicks = 0;
      this.breakTicks = 0;
      this.previousSlot = -1;
      this.pendingSwapRevert = false;
      this.swapRevertAt = 0L;
      class_746 player = mc.field_1724;
      if (player != null) {
         this.renderYaw = player.method_36454();
         this.renderPitch = player.method_36455();
      }
   }

   private int findCrystalHotbarSlot(class_746 player) {
      for (int slot = 0; slot < 9; slot++) {
         if (player.method_31548().method_5438(slot).method_31574(class_1802.field_8301)) {
            return slot;
         }
      }

      return -1;
   }

   private class_1657 findTarget(class_746 self, class_638 level) {
      class_1657 best = null;
      double bestDist = Double.MAX_VALUE;
      double maxDistSq = Math.pow(this.range.getValue() * 2.0, 2.0);

      for (class_1657 player : level.method_18456()) {
         if (player != self && !player.method_29504() && !(player.method_6032() <= 0.0F)) {
            double dist = self.method_5858(player);
            if (!(dist > maxDistSq) && !(dist >= bestDist)) {
               bestDist = dist;
               best = player;
            }
         }
      }

      return best;
   }

   private class_2338 findPlacePos(class_746 player, class_638 level) {
      if (this.target == null) {
         return null;
      }

      class_2338 origin = player.method_24515();
      int radius = (int)Math.ceil(this.range.getValue());
      double rangeSq = this.range.getValue() * this.range.getValue();
      class_2338 best = null;
      double bestTargetDist = Double.MAX_VALUE;

      for (int dx = -radius; dx <= radius; dx++) {
         for (int dz = -radius; dz <= radius; dz++) {
            for (int dy = -radius; dy <= radius; dy++) {
               class_2338 pos = origin.method_10069(dx, dy, dz);
               double px = pos.method_10263() + 0.5;
               double py = pos.method_10264() + 0.5;
               double pz = pos.method_10260() + 0.5;
               if (!(player.method_5649(px, py, pz) > rangeSq) && this.canPlaceCrystal(player, level, pos)) {
                  double targetDist = this.target.method_5649(pos.method_10263() + 0.5, pos.method_10264() + 1.0, pos.method_10260() + 0.5);
                  if (targetDist < bestTargetDist) {
                     bestTargetDist = targetDist;
                     best = pos.method_10062();
                  }
               }
            }
         }
      }

      return best;
   }

   private boolean canPlaceCrystal(class_746 player, class_638 level, class_2338 base) {
      class_2680 state = level.method_8320(base);
      if (!state.method_27852(class_2246.field_10540) && !state.method_27852(class_2246.field_9987)) {
         return false;
      }

      class_2338 feet = base.method_10084();
      if (level.method_8320(feet).method_26215() && level.method_8320(feet.method_10084()).method_26215()) {
         class_238 entityBox = new class_238(
            feet.method_10263(), feet.method_10264(), feet.method_10260(), feet.method_10263() + 1.0, feet.method_10264() + 2.0, feet.method_10260() + 1.0
         );

         for (class_1297 entity : level.method_18112()) {
            if (entity != player && entity.method_5829().method_994(entityBox)) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private class_1511 findCrystalToBreak(class_746 player, class_638 level) {
      if (this.target == null) {
         return null;
      }

      class_1511 best = null;
      double bestTargetDist = Double.MAX_VALUE;
      double rangeSq = this.range.getValue() * this.range.getValue();

      for (class_1297 entity : level.method_18112()) {
         if (entity instanceof class_1511 crystal && !(player.method_5858(crystal) > rangeSq)) {
            double targetDist = this.target.method_5858(crystal);
            if (targetDist < bestTargetDist) {
               bestTargetDist = targetDist;
               best = crystal;
            }
         }
      }

      return best;
   }

   private void placeCrystal(class_746 player, class_2338 pos) {
      int slot = this.findCrystalHotbarSlot(player);
      if (slot != -1) {
         class_310 client = mc;
         class_636 gameMode = client.field_1761;
         if (gameMode != null && player.field_3944 != null) {
            this.rotateToPlace(player, pos);
            int previous = player.method_31548().method_67532();
            if (this.swapMode.is("Hand")) {
               player.method_31548().method_61496(slot);
            } else {
               player.field_3944.method_52787(new class_2868(slot));
            }

            class_243 eyes = player.method_33571();
            class_238 blockBox = new class_238(pos);
            class_243 hit = closestPoint(eyes, blockBox);
            class_243 look = hit.method_1020(eyes).method_1029();
            class_2350 side = class_2350.method_10142(look.field_1352, look.field_1351, look.field_1350);
            class_3965 hitResult = new class_3965(hit, side, pos, false);
            gameMode.method_2896(player, class_1268.field_5808, hitResult);
            player.method_6104(class_1268.field_5808);
            if (this.swapMode.is("Hand")) {
               this.previousSlot = previous;
               this.pendingSwapRevert = true;
               this.swapRevertAt = System.currentTimeMillis();
            } else {
               player.field_3944.method_52787(new class_2868(previous));
            }
         }
      }
   }

   private void attackCrystal(class_746 player, class_1511 crystal) {
      if (crystal != null && mc.field_1761 != null) {
         mc.field_1761.method_2918(player, crystal);
         player.method_6104(class_1268.field_5808);
      }
   }

   private void rotateToPlace(class_746 player, class_2338 pos) {
      class_243 eyes = player.method_33571();
      class_243 delta = closestPoint(eyes, new class_238(pos)).method_1020(eyes);
      this.applyRotation(player, delta);
   }

   private void rotateToCrystal(class_746 player, class_1511 crystal) {
      class_243 eyes = player.method_33571();
      class_243 delta = closestPoint(eyes, crystal.method_5829()).method_1020(eyes);
      this.applyRotation(player, delta);
   }

   private void applyRotation(class_746 player, class_243 delta) {
      float yaw = (float)class_3532.method_15338(Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0);
      float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, Math.hypot(delta.field_1352, delta.field_1350))));
      this.smoothRotation(player, yaw, pitch);
   }

   private void smoothRotation(class_746 player, float targetYaw, float targetPitch) {
      float gcd = gcd();
      float yaw = this.renderYaw + class_3532.method_15393(targetYaw - this.renderYaw);
      float pitch = this.renderPitch + (targetPitch - this.renderPitch);
      yaw -= (yaw - this.renderYaw) % gcd;
      pitch -= (pitch - this.renderPitch) % gcd;
      pitch = (float)class_3532.method_15350(pitch, -89.0, 89.0);
      if (yaw == this.renderYaw && pitch == this.renderPitch) {
         int jitter = ThreadLocalRandom.current().nextInt(1, 4);
         float sign = ThreadLocalRandom.current().nextBoolean() ? 1.0F : -1.0F;
         if (ThreadLocalRandom.current().nextBoolean()) {
            yaw += gcd * jitter * sign;
         } else {
            pitch += gcd * jitter * sign;
         }

         pitch = (float)class_3532.method_15350(pitch, -89.0, 89.0);
      }

      RotationContext.setRotation(yaw, pitch);
      this.renderYaw = yaw;
      this.renderPitch = pitch;
   }

   private static float gcd() {
      double sensitivity = (Double)class_310.method_1551().field_1690.method_42495().method_41753() * 0.6 + 0.2;
      double factor = sensitivity * sensitivity * sensitivity * 1.2;
      return (float)(factor * 0.15);
   }

   private static class_243 closestPoint(class_243 point, class_238 box) {
      return new class_243(
         class_3532.method_15350(point.field_1352, box.field_1323, box.field_1320),
         class_3532.method_15350(point.field_1351, box.field_1322, box.field_1325),
         class_3532.method_15350(point.field_1350, box.field_1321, box.field_1324)
      );
   }

   @Environment(EnvType.CLIENT)
   private record CrystalGizmo(class_238 box, int color) implements class_12175 {
      public void method_75531(class_12177 primitives, float alpha) {
         class_243 a = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1321);
         class_243 b = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1321);
         class_243 c = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1324);
         class_243 d = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1324);
         class_243 e = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1321);
         class_243 f = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1321);
         class_243 g = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1324);
         class_243 h = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1324);
         int fill = this.color & 16777215 | Math.round((this.color >>> 24) * 0.3F) << 24;
         primitives.method_75474(a, b, c, d, fill);
         primitives.method_75474(e, f, g, h, fill);
         primitives.method_75474(a, b, f, e, fill);
         primitives.method_75474(d, c, g, h, fill);
         primitives.method_75474(a, d, h, e, fill);
         primitives.method_75474(b, c, g, f, fill);
         primitives.method_75473(a, b, this.color, 1.5F);
         primitives.method_75473(b, c, this.color, 1.5F);
         primitives.method_75473(c, d, this.color, 1.5F);
         primitives.method_75473(d, a, this.color, 1.5F);
         primitives.method_75473(e, f, this.color, 1.5F);
         primitives.method_75473(f, g, this.color, 1.5F);
         primitives.method_75473(g, h, this.color, 1.5F);
         primitives.method_75473(h, e, this.color, 1.5F);
         primitives.method_75473(a, e, this.color, 1.5F);
         primitives.method_75473(b, f, this.color, 1.5F);
         primitives.method_75473(c, g, this.color, 1.5F);
         primitives.method_75473(d, h, this.color, 1.5F);
      }
   }
}
