package org.ryzen.context;

import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public final class RotationContext implements MinecraftContext {
   private static final float MOUSE_TURN_SCALE = 0.15F;
   private static float freeYaw;
   private static float freePitch;
   private static boolean active;
   private static boolean hasLastAngle;
   private static float lastYaw;
   private static float lastPitch;
   private static float lastHeadYaw;
   private static float lastBodyYaw;

   private RotationContext() {
   }

   public static void apply(float yawDelta, float pitchDelta) {
      class_746 player = mc.field_1724;
      if (player != null) {
         if (!active) {
            freeYaw = player.method_36454();
            freePitch = player.method_36455();
            active = true;
         }

         rememberRenderAngles(player);
         float nextYaw = player.field_6241 + yawDelta;
         float nextPitch = class_3532.method_15363(player.method_36455() + pitchDelta, -90.0F, 90.0F);
         player.method_36456(nextYaw);
         player.method_36457(nextPitch);
         player.field_6241 = nextYaw;
      }
   }

   public static void setRotation(float yaw, float pitch) {
      class_746 player = mc.field_1724;
      if (player != null) {
         apply(class_3532.method_15393(yaw - player.field_6241), class_3532.method_15393(pitch - player.method_36455()));
      }
   }

   public static void rotateTo(class_746 player, class_1297 target) {
      class_243 delta = target.method_33571().method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float yaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      setRotation(yaw, pitch);
   }

   public static void rotateToPosition(class_746 player, class_243 pos) {
      class_243 delta = pos.method_1020(player.method_33571());
      double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
      float yaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
      setRotation(yaw, pitch);
   }

   public static float getFovToEntity(class_746 player, class_1297 entity) {
      double diffX = entity.method_23317() - player.method_23317();
      double diffZ = entity.method_23321() - player.method_23321();
      float yaw = (float)(Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0);
      float deltaYaw = class_3532.method_15393(yaw - mc.field_1773.method_19418().method_19330());
      return Math.abs(deltaYaw);
   }

   public static void syncFreeLook(float yaw, float pitch) {
      if (!active) {
         freeYaw = yaw;
         freePitch = pitch;
      }
   }

   public static boolean onMouseTurn(double yawInput, double pitchInput) {
      if (!active) {
         return false;
      }

      freeYaw += (float)yawInput * 0.15F;
      freePitch = class_3532.method_15363(freePitch + (float)pitchInput * 0.15F, -90.0F, 90.0F);
      return true;
   }

   public static void applyRenderInterpolation() {
      if (active && hasLastAngle) {
         class_746 player = mc.field_1724;
         if (player == null) {
            hasLastAngle = false;
         } else {
            player.field_5982 = lastYaw;
            player.field_6004 = lastPitch;
            player.field_6259 = lastHeadYaw;
            player.field_6220 = lastBodyYaw;
         }
      }
   }

   public static void clear() {
      class_746 player = mc.field_1724;
      if (player != null) {
         if (active) {
            player.method_36456(freeYaw);
            player.method_36457(freePitch);
            player.field_6241 = freeYaw;
         }

         freeYaw = player.method_36454();
         freePitch = player.method_36455();
      }

      active = false;
      hasLastAngle = false;
   }

   public static void clearKeepCamera() {
      // Keep the camera at the last rotation position (don't reset to player's current angles)
      // This allows the camera to stay where the aura was looking when disabled
      active = false;
      hasLastAngle = false;
      // Don't reset freeYaw/freePitch - keep them at last values
   }

   private static void rememberRenderAngles(class_746 player) {
      lastYaw = player.method_36454();
      lastPitch = player.method_36455();
      lastHeadYaw = player.field_6241;
      lastBodyYaw = player.field_6283;
      hasLastAngle = true;
   }

   @Generated
   public static float getFreeYaw() {
      return freeYaw;
   }

   @Generated
   public static float getFreePitch() {
      return freePitch;
   }

   @Generated
   public static boolean isActive() {
      return active;
   }
}
