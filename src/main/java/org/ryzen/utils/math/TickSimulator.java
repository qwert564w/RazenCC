package org.ryzen.utils.math;

import java.util.List;
import java.util.Objects;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12127;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1294;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1320;
import net.minecraft.class_1657;
import net.minecraft.class_1937;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_3481;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3610;
import net.minecraft.class_3611;
import net.minecraft.class_5134;
import net.minecraft.class_6862;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import net.minecraft.class_9334;
import net.minecraft.class_2338.class_2339;
import org.ryzen.mixin.accessor.LivingEntityAccessor;

@Environment(EnvType.CLIENT)
public final class TickSimulator {
   private static final double COLLISION_EPSILON = 1.0E-7;
   private static final float DEFAULT_FRICTION = 0.6F;
   private static final double BELOW_OFFSET = 0.5000001;

   private TickSimulator() {
   }

   public static void simulateNextTick(TickSimulator.SimState state, class_1937 level) {
      Objects.requireNonNull(state, "state");
      Objects.requireNonNull(level, "level");
      if (state.noJumpDelay > 0) {
         state.noJumpDelay--;
      }

      updateFluidState(state, level);
      clampSmallMovement(state);
      tickJump(state, level);
      if (hasEffect(state, class_1294.field_5906) || hasEffect(state, class_1294.field_5902)) {
         state.fallDistance = 0.0F;
      }

      boolean useFluidTravel = (state.inWater || state.inLava) && !state.flying && !canStandOnFluid(state);
      if (useFluidTravel) {
         if (state.inWater) {
            travelInWater(state, level);
         } else {
            travelInLava(state, level);
         }
      } else {
         travelInAir(state, level);
      }
   }

   public static TickSimulator.SimState getPredictedState(class_1309 entity, int ticks, class_1937 level) {
      if (ticks < 0) {
         throw new IllegalArgumentException("Prediction tick count cannot be negative");
      }

      Objects.requireNonNull(level, "level");
      TickSimulator.SimState state = new TickSimulator.SimState(entity);

      for (int i = 0; i < ticks; i++) {
         simulateNextTick(state, level);
      }

      return state;
   }

   public static TickSimulator.SimState simulateLocalPlayer(class_746 player, int ticks, class_1937 level) {
      if (ticks < 0) {
         throw new IllegalArgumentException("Prediction tick count cannot be negative");
      }

      Objects.requireNonNull(level, "level");
      TickSimulator.SimState state = new TickSimulator.SimState(player).withLocalInput(player);

      for (int i = 0; i < ticks; i++) {
         simulateNextTick(state, level);
      }

      return state;
   }

   public static int ticksUntilCriticalWindow(class_1309 entity, int maxTicks, class_1937 level) {
      TickSimulator.SimState state = entity instanceof class_746 player
         ? new TickSimulator.SimState(player).withLocalInput(player)
         : new TickSimulator.SimState(entity);
      if (!state.mobilityRestricted && !state.passenger) {
         for (int tick = 0; tick <= maxTicks; tick++) {
            if (state.isFallingCriticalWindow()) {
               return tick;
            }

            if (tick < maxTicks) {
               simulateNextTick(state, level);
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private static void clampSmallMovement(TickSimulator.SimState state) {
      double dx = state.motion.field_1352;
      double dy = state.motion.field_1351;
      double dz = state.motion.field_1350;
      if (state.source instanceof class_1657) {
         if (state.motion.method_37268() < 9.0E-6) {
            dx = 0.0;
            dz = 0.0;
         }
      } else {
         if (Math.abs(dx) < 0.003) {
            dx = 0.0;
         }

         if (Math.abs(dz) < 0.003) {
            dz = 0.0;
         }
      }

      if (Math.abs(dy) < 0.003) {
         dy = 0.0;
      }

      state.motion = new class_243(dx, dy, dz);
   }

   private static void tickJump(TickSimulator.SimState state, class_1937 level) {
      if (!state.jumpHeld) {
         state.noJumpDelay = 0;
      } else {
         double fluidHeight = state.inLava ? state.lavaHeight : state.waterHeight;
         boolean inWaterWithHeight = state.inWater && fluidHeight > 0.0;
         double jumpThreshold = fluidJumpThreshold(state);
         if (!inWaterWithHeight || state.onGround && !(fluidHeight > jumpThreshold)) {
            if (!state.inLava || state.onGround && !(fluidHeight > jumpThreshold)) {
               if ((state.onGround || inWaterWithHeight && fluidHeight <= jumpThreshold) && state.noJumpDelay == 0) {
                  jumpFromGround(state, level);
                  state.noJumpDelay = 10;
               }
            } else {
               state.motion = state.motion.method_1031(0.0, 0.04, 0.0);
            }
         } else {
            state.motion = state.motion.method_1031(0.0, 0.04, 0.0);
         }
      }
   }

   private static void jumpFromGround(TickSimulator.SimState state, class_1937 level) {
      float jumpPower = (float)attribute(state, class_5134.field_23728) * blockJumpFactor(state, level) + jumpBoostPower(state);
      if (!(jumpPower <= 1.0E-5F)) {
         state.motion = new class_243(state.motion.field_1352, Math.max(jumpPower, state.motion.field_1351), state.motion.field_1350);
         if (state.isSprinting) {
            float angle = state.yaw * (float) (Math.PI / 180.0);
            state.motion = state.motion.method_1031(-class_3532.method_15374(angle) * 0.2, 0.0, class_3532.method_15362(angle) * 0.2);
         }

         state.onGround = false;
      }
   }

   private static void travelInAir(TickSimulator.SimState state, class_1937 level) {
      class_2338 posBelow = blockPosBelow(state);
      float blockFriction = state.onGround ? level.method_8320(posBelow).method_26204().method_9499() : 1.0F;
      moveRelative(state, frictionInfluencedSpeed(state, blockFriction));
      state.motion = handleOnClimbable(state, level);
      move(state, level);
      class_243 movement = state.motion;
      if ((state.horizontalCollision || state.jumpHeld) && state.climbing) {
         movement = new class_243(movement.field_1352, 0.2, movement.field_1350);
      }

      double movementY = movement.field_1351;
      class_1293 levitation = effect(state, class_1294.field_5902);
      if (levitation != null) {
         movementY += (0.05 * (levitation.method_5578() + 1) - movement.field_1351) * 0.2;
         state.fallDistance = 0.0F;
      } else {
         movementY -= effectiveGravity(state, movement.field_1351);
      }

      float airDrag = 0.91F;
      float friction = blockFriction * airDrag;
      float verticalDrag = 0.98F;
      state.motion = new class_243(movement.field_1352 * friction, movementY * verticalDrag, movement.field_1350 * friction);
   }

   private static void travelInWater(TickSimulator.SimState state, class_1937 level) {
      boolean isFalling = state.motion.field_1351 <= 0.0;
      double oldY = state.pos.field_1351;
      double gravity = effectiveGravity(state, state.motion.field_1351);
      float slowDown = state.isSprinting ? 0.9F : 0.8F;
      float speed = 0.02F;
      float waterEfficiency = (float)attribute(state, class_5134.field_51578);
      if (!state.onGround) {
         waterEfficiency *= 0.5F;
      }

      if (waterEfficiency > 0.0F) {
         slowDown += (0.54600006F - slowDown) * waterEfficiency;
         speed += (speedAttribute(state) - speed) * waterEfficiency;
      }

      if (hasEffect(state, class_1294.field_5900)) {
         slowDown = 0.96F;
      }

      moveRelative(state, speed);
      move(state, level);
      class_243 movement = state.motion;
      if (state.horizontalCollision && state.climbing) {
         movement = new class_243(movement.field_1352, 0.2, movement.field_1350);
      }

      movement = movement.method_18805(slowDown, 0.8, slowDown);
      state.motion = fluidFallingAdjustedMovement(state, gravity, isFalling, movement);
      jumpOutOfFluid(state, level, oldY);
   }

   private static void travelInLava(TickSimulator.SimState state, class_1937 level) {
      boolean isFalling = state.motion.field_1351 <= 0.0;
      double oldY = state.pos.field_1351;
      double gravity = effectiveGravity(state, state.motion.field_1351);
      moveRelative(state, 0.02F);
      move(state, level);
      if (state.lavaHeight <= fluidJumpThreshold(state)) {
         state.motion = state.motion.method_18805(0.5, 0.8, 0.5);
         state.motion = fluidFallingAdjustedMovement(state, gravity, isFalling, state.motion);
      } else {
         state.motion = state.motion.method_1021(0.5);
      }

      if (gravity != 0.0) {
         state.motion = state.motion.method_1031(0.0, -gravity / 4.0, 0.0);
      }

      jumpOutOfFluid(state, level, oldY);
   }

   private static void move(TickSimulator.SimState state, class_1937 level) {
      class_243 delta = state.motion;
      if (state.stuckSpeedMultiplier.method_1027() > 1.0E-7) {
         delta = delta.method_18806(state.stuckSpeedMultiplier);
         state.stuckSpeedMultiplier = class_243.field_1353;
         state.motion = class_243.field_1353;
      }

      class_243 resolved = class_1297.method_20736(state.source, delta, state.boundingBox, level, List.of());
      boolean collidedX = differs(delta.field_1352, resolved.field_1352);
      boolean collidedY = differs(delta.field_1351, resolved.field_1351);
      boolean collidedZ = differs(delta.field_1350, resolved.field_1350);
      state.horizontalCollision = collidedX || collidedZ;
      state.verticalCollision = collidedY;
      state.verticalCollisionBelow = collidedY && delta.field_1351 < 0.0;
      state.onGround = state.verticalCollisionBelow;
      state.boundingBox = state.boundingBox.method_997(resolved);
      state.pos = state.pos.method_1019(resolved);
      if (!state.inWater && resolved.field_1351 < 0.0) {
         state.fallDistance = state.fallDistance + (float)(-resolved.field_1351);
      }

      if (state.onGround) {
         state.fallDistance = 0.0F;
      }

      state.motion = new class_243(collidedX ? 0.0 : delta.field_1352, collidedY ? 0.0 : delta.field_1351, collidedZ ? 0.0 : delta.field_1350);
      float speedFactor = blockSpeedFactor(state, level);
      state.motion = state.motion.method_18805(speedFactor, 1.0, speedFactor);
      applyStuckBlocks(state, level);
      state.climbing = isClimbing(state, level);
   }

   private static void applyStuckBlocks(TickSimulator.SimState state, class_1937 level) {
      state.inCobweb = false;
      class_238 box = state.boundingBox.method_1011(1.0E-7);
      int minX = class_3532.method_15357(box.field_1323);
      int maxX = class_3532.method_15357(box.field_1320);
      int minY = class_3532.method_15357(box.field_1322);
      int maxY = class_3532.method_15357(box.field_1325);
      int minZ = class_3532.method_15357(box.field_1321);
      int maxZ = class_3532.method_15357(box.field_1324);
      class_2339 cursor = new class_2339();

      for (int x = minX; x <= maxX; x++) {
         for (int y = minY; y <= maxY; y++) {
            for (int z = minZ; z <= maxZ; z++) {
               cursor.method_10103(x, y, z);
               class_2680 blockState = level.method_8320(cursor);
               if (blockState.method_27852(class_2246.field_10343)) {
                  state.inCobweb = true;
                  state.stuckSpeedMultiplier = new class_243(0.25, 0.05, 0.25);
                  state.fallDistance = 0.0F;
               } else if (blockState.method_27852(class_2246.field_27879)) {
                  state.stuckSpeedMultiplier = new class_243(0.9, 1.5, 0.9);
                  state.fallDistance = 0.0F;
               } else if (blockState.method_27852(class_2246.field_16999)) {
                  state.stuckSpeedMultiplier = new class_243(0.8, 0.75, 0.8);
                  state.fallDistance = 0.0F;
               }
            }
         }
      }
   }

   private static void updateFluidState(TickSimulator.SimState state, class_1937 level) {
      class_238 box = state.boundingBox.method_1011(0.001);
      state.waterHeight = fluidHeight(level, box, class_3486.field_15517);
      state.lavaHeight = fluidHeight(level, box, class_3486.field_15518);
      state.inWater = state.waterHeight > 0.0;
      state.inLava = state.lavaHeight > 0.0;
      double eyeY = state.pos.field_1351 + state.source.method_5751();
      state.submergedInWater = state.inWater && box.field_1322 + state.waterHeight > eyeY;
      if (state.inWater) {
         state.fallDistance = 0.0F;
      }
   }

   private static double fluidHeight(class_1937 level, class_238 box, class_6862<class_3611> tag) {
      int minX = class_3532.method_15357(box.field_1323);
      int maxX = class_3532.method_15384(box.field_1320);
      int minY = class_3532.method_15357(box.field_1322);
      int maxY = class_3532.method_15384(box.field_1325);
      int minZ = class_3532.method_15357(box.field_1321);
      int maxZ = class_3532.method_15384(box.field_1324);
      double highest = 0.0;
      class_2339 cursor = new class_2339();

      for (int x = minX; x < maxX; x++) {
         for (int y = minY; y < maxY; y++) {
            for (int z = minZ; z < maxZ; z++) {
               cursor.method_10103(x, y, z);
               class_3610 fluid = level.method_8316(cursor);
               if (fluid.method_15767(tag)) {
                  double surface = y + fluid.method_15763(level, cursor);
                  if (surface >= box.field_1322) {
                     highest = Math.max(highest, surface - box.field_1322);
                  }
               }
            }
         }
      }

      return highest;
   }

   private static class_243 handleOnClimbable(TickSimulator.SimState state, class_1937 level) {
      class_243 delta = state.motion;
      if (!state.climbing) {
         return delta;
      }

      state.fallDistance = 0.0F;
      double xd = class_3532.method_15350(delta.field_1352, -0.15, 0.15);
      double zd = class_3532.method_15350(delta.field_1350, -0.15, 0.15);
      double yd = Math.max(delta.field_1351, -0.15);
      if (yd < 0.0
         && state.source instanceof class_1657 player
         && player.method_21754()
         && !level.method_8320(class_2338.method_49638(state.pos)).method_27852(class_2246.field_16492)) {
         yd = 0.0;
      }

      return new class_243(xd, yd, zd);
   }

   private static void jumpOutOfFluid(TickSimulator.SimState state, class_1937 level, double oldY) {
      if (state.horizontalCollision) {
         class_243 movement = state.motion;
         class_243 climbStep = new class_243(movement.field_1352, movement.field_1351 + 0.6 - state.pos.field_1351 + oldY, movement.field_1350);
         class_243 resolved = class_1297.method_20736(state.source, climbStep, state.boundingBox, level, List.of());
         if (resolved.equals(climbStep)) {
            state.motion = new class_243(movement.field_1352, 0.3, movement.field_1350);
         }
      }
   }

   private static class_243 fluidFallingAdjustedMovement(TickSimulator.SimState state, double gravity, boolean isFalling, class_243 movement) {
      if (gravity != 0.0 && !state.isSprinting) {
         double yd;
         if (isFalling && Math.abs(movement.field_1351 - 0.005) >= 0.003 && Math.abs(movement.field_1351 - gravity / 16.0) < 0.003) {
            yd = -0.003;
         } else {
            yd = movement.field_1351 - gravity / 16.0;
         }

         return new class_243(movement.field_1352, yd, movement.field_1350);
      } else {
         return movement;
      }
   }

   private static void moveRelative(TickSimulator.SimState state, float speed) {
      class_243 input = new class_243(state.impulseX, 0.0, state.impulseZ);
      double lengthSqr = input.method_1027();
      if (!(lengthSqr < 1.0E-7)) {
         class_243 scaled = (lengthSqr > 1.0 ? input.method_1029() : input).method_1021(speed);
         float sin = class_3532.method_15374(state.yaw * (float) (Math.PI / 180.0));
         float cos = class_3532.method_15362(state.yaw * (float) (Math.PI / 180.0));
         state.motion = state.motion
            .method_1031(scaled.field_1352 * cos - scaled.field_1350 * sin, scaled.field_1351, scaled.field_1350 * cos + scaled.field_1352 * sin);
      }
   }

   private static float frictionInfluencedSpeed(TickSimulator.SimState state, float blockFriction) {
      if (state.onGround) {
         float speed = speedAttribute(state);
         return blockFriction > 0.6F ? speed * (0.21600002F / (blockFriction * blockFriction * blockFriction)) : speed;
      } else {
         return state.isSprinting ? 0.025999999F : 0.02F;
      }
   }

   private static float speedAttribute(TickSimulator.SimState state) {
      return (float)attribute(state, class_5134.field_23719);
   }

   private static double effectiveGravity(TickSimulator.SimState state, double motionY) {
      double gravity = attribute(state, class_5134.field_49078);
      boolean isFalling = motionY <= 0.0;
      if (isFalling && hasEffect(state, class_1294.field_5906)) {
         state.fallDistance = 0.0F;
         return Math.min(gravity, 0.01);
      } else {
         return gravity;
      }
   }

   private static float blockSpeedFactor(TickSimulator.SimState state, class_1937 level) {
      if (state.flying) {
         return 1.0F;
      }

      class_2680 inState = level.method_8320(class_2338.method_49638(state.pos));
      float factor = inState.method_26204().method_23349();
      if (!inState.method_27852(class_2246.field_10382) && !inState.method_27852(class_2246.field_10422) && factor == 1.0F) {
         factor = level.method_8320(blockPosBelow(state)).method_26204().method_23349();
      }

      float efficiency = (float)attribute(state, class_5134.field_51582);
      return class_3532.method_16439(efficiency, factor, 1.0F);
   }

   private static float blockJumpFactor(TickSimulator.SimState state, class_1937 level) {
      float inFactor = level.method_8320(class_2338.method_49638(state.pos)).method_26204().method_23350();
      return inFactor == 1.0F ? level.method_8320(blockPosBelow(state)).method_26204().method_23350() : inFactor;
   }

   private static float jumpBoostPower(TickSimulator.SimState state) {
      class_1293 jumpBoost = effect(state, class_1294.field_5913);
      return jumpBoost == null ? 0.0F : 0.1F * (jumpBoost.method_5578() + 1.0F);
   }

   private static boolean isClimbing(TickSimulator.SimState state, class_1937 level) {
      class_2680 inState = level.method_8320(class_2338.method_49638(state.pos));
      return inState.method_26164(class_3481.field_22414);
   }

   private static boolean canStandOnFluid(TickSimulator.SimState state) {
      return false;
   }

   private static double fluidJumpThreshold(TickSimulator.SimState state) {
      return state.source.method_5751() < 0.4 ? 0.0 : 0.4;
   }

   private static class_2338 blockPosBelow(TickSimulator.SimState state) {
      return class_2338.method_49637(state.pos.field_1352, state.boundingBox.field_1322 - 0.5000001, state.pos.field_1350);
   }

   private static float modifiedFriction(float friction, float modifier) {
      return class_3532.method_15363(1.0F - (1.0F - friction) * modifier, 0.0F, 1.0F);
   }

   private static double attribute(TickSimulator.SimState state, class_6880<class_1320> attribute) {
      return state.source.method_45325(attribute);
   }

   private static class_1293 effect(TickSimulator.SimState state, class_6880<class_1291> effect) {
      return state.source.method_6112(effect);
   }

   private static boolean hasEffect(TickSimulator.SimState state, class_6880<class_1291> effect) {
      return state.source.method_6059(effect);
   }

   private static float itemUseSpeedMultiplier(class_746 player) {
      class_12127 useEffects = (class_12127)player.method_6030().method_58694(class_9334.field_63634);
      return useEffects == null ? 1.0F : useEffects.comp_4978();
   }

   private static boolean differs(double requested, double resolved) {
      return Math.abs(requested - resolved) > 1.0E-7;
   }

   @Environment(EnvType.CLIENT)
   public static final class SimState {
      public final class_1309 source;
      public class_243 pos;
      public class_243 motion;
      public class_238 boundingBox;
      public boolean onGround;
      public boolean horizontalCollision;
      public boolean verticalCollision;
      public boolean verticalCollisionBelow;
      public float fallDistance;
      public boolean isSprinting;
      public boolean inWater;
      public boolean submergedInWater;
      public boolean swimming;
      public boolean inLava;
      public boolean climbing;
      public boolean inCobweb;
      public boolean mobilityRestricted;
      public boolean passenger;
      public boolean flying;
      public int noJumpDelay;
      public class_243 stuckSpeedMultiplier = class_243.field_1353;
      public double waterHeight;
      public double lavaHeight;
      public float impulseX;
      public float impulseZ;
      public boolean jumpHeld;
      public float yaw;

      public SimState(class_1309 entity) {
         this.source = Objects.requireNonNull(entity, "entity");
         this.pos = entity.method_73189();
         this.motion = entity.method_18798();
         this.boundingBox = entity.method_5829();
         this.onGround = entity.method_24828();
         this.horizontalCollision = entity.field_5976;
         this.verticalCollision = entity.field_5992;
         this.verticalCollisionBelow = entity.field_36331;
         this.fallDistance = (float)entity.field_6017;
         this.isSprinting = entity.method_5624();
         this.inWater = entity.method_5799();
         this.submergedInWater = entity.method_5869();
         this.swimming = entity.method_5681();
         this.inLava = entity.method_5771();
         this.climbing = entity.method_6101();
         this.mobilityRestricted = entity instanceof class_1657 player && player.method_74025();
         this.passenger = entity.method_5765();
         this.flying = entity instanceof class_1657 player && player.method_31549().field_7479;
         this.yaw = entity.method_36454();
         this.waterHeight = entity.method_5861(class_3486.field_15517);
         this.lavaHeight = entity.method_5861(class_3486.field_15518);
         this.noJumpDelay = ((LivingEntityAccessor)entity).getNoJumpDelay();
         if (entity instanceof class_746 player && player.field_3913 != null) {
            this.jumpHeld = player.field_3913.field_54155.comp_3163();
         }

         TickSimulator.applyStuckBlocks(this, entity.method_73183());
      }

      public TickSimulator.SimState withLocalInput(class_746 player) {
         if (player.field_3913 != null) {
            float scale = 0.98F;
            if (player.method_6115() && !player.method_5765()) {
               scale *= TickSimulator.itemUseSpeedMultiplier(player);
            }

            if (player.method_20303()) {
               scale *= (float)player.method_45325(class_5134.field_51584);
            }

            float inputX = player.field_3913.method_3128().field_1343 * scale;
            float inputZ = player.field_3913.method_3128().field_1342 * scale;
            float length = (float)Math.sqrt(inputX * inputX + inputZ * inputZ);
            if (length > 0.0F) {
               float absX = Math.abs(inputX / length);
               float absZ = Math.abs(inputZ / length);
               float tangent = absZ > absX ? absX / absZ : absZ / absX;
               float toUnitSquare = (float)Math.sqrt(1.0F + tangent * tangent);
               float modified = Math.min(length * toUnitSquare, 1.0F) / length;
               inputX *= modified;
               inputZ *= modified;
            }

            this.impulseX = inputX;
            this.impulseZ = inputZ;
            this.jumpHeld = player.field_3913.field_54155.comp_3163();
         }

         return this;
      }

      public boolean isFallingCriticalWindow() {
         return this.fallDistance > 0.0F && !this.onGround && !this.inWater && !this.climbing && !this.mobilityRestricted && !this.passenger;
      }
   }
}
