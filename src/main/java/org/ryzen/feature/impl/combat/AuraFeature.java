package org.ryzen.feature.impl.combat;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1294;
import net.minecraft.class_1309;
import net.minecraft.class_1802;
import net.minecraft.class_1839;
import net.minecraft.class_241;
import net.minecraft.class_243;
import net.minecraft.class_2868;
import net.minecraft.class_2879;
import net.minecraft.class_310;
import net.minecraft.class_476;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_9362;
import org.ryzen.context.MinecraftContext;
import org.ryzen.context.RotationContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.combat.AttackReach;
import org.ryzen.utils.combat.AttackTiming;
import org.ryzen.utils.combat.AuraAttackController;
import org.ryzen.utils.combat.AuraRaycast;
import org.ryzen.utils.combat.CombatTargets;
import org.ryzen.utils.combat.LocalPlayerHistory;
import org.ryzen.utils.combat.ServerSprintTracker;
import org.ryzen.utils.combat.SprintManager;
import org.ryzen.utils.combat.TargetFilter;
import org.ryzen.utils.combat.TargetUtil;
import org.ryzen.utils.combat.neuro.NeuroManager;
import org.ryzen.utils.combat.rotations.AuraRotation;
import org.ryzen.utils.combat.rotations.BuilderRotation;
import org.ryzen.utils.combat.rotations.ExpensiveRotation;
import org.ryzen.utils.combat.rotations.GrimRotation;
import org.ryzen.utils.combat.rotations.HolyWorldThreeRotation;
import org.ryzen.utils.combat.rotations.MatrixVulcanRotation;
import org.ryzen.utils.combat.rotations.PolarRotation;
import org.ryzen.utils.combat.rotations.SlothRotation;
import org.ryzen.utils.combat.rotations.SmoothRotation;
import org.ryzen.utils.combat.rotations.SolutionRotation;
import org.ryzen.utils.math.TickSimulator;

@Environment(EnvType.CLIENT)
public final class AuraFeature extends Feature implements MinecraftContext {
   private static final String PRIORITY_DISTANCE = "Distance";
   private static final String PRIORITY_HEALTH = "Health";
   private static final String PRIORITY_ARMOR = "Armor";
   private static final String PRIORITY_FOV = "FOV";
   private static final String PRIORITY_ALL = "All";
   private static final String ROTATION_MATRIX_VULCAN = "Matrix Vulcan";
   private static final String ROTATION_GRIM = "Grim";
   private static final String ROTATION_GRIM_LEGACY = "Grim 1";
   private static final String ROTATION_SMOOTH = "Smooth";
   private static final String ROTATION_POLAR = "Polar";
   private static final String ROTATION_SOLUTION = "Solution";
   private static final String ROTATION_NEURO_NOT_AI = "NeuroNotAiTreinig";
   private static final String ROTATION_SLOTH = "Sloth";
   private static final String ROTATION_HOLY_WORLD_3 = "HolyWorld 3";
   private static final String ROTATION_SPOOKY_TIME_2 = "SpookyTime 2";
   private static final String ROTATION_NEURO = "Neuro";
   private static final String ROTATION_BUILDER = "Builder";
   private static final String ROTATION_FUNTIME_NEW = "FunTime New";
   private static final String ROTATION_FUNTIME_FOV = "FunTime FOV";
   private static final String ROTATION_LEGIT = "Legit";
   private static final String TARGET_ESP_MARKER = "Marker";
   private static final String TARGET_ESP_GHOSTS = "Ghosts";
   private static final String TARGET_ESP_CIRCLE = "Circle";
   private static final String MOVE_CORRECTION_OFF = "Off";
   private static final String MOVE_CORRECTION_CAMERA = "Camera";
   private static final int PREDICTION_SCAN_TICKS = 3;
   private static final int WATER_PREDICTION_SCAN_TICKS = 8;
   private static final double AIM_JITTER_RANGE = 0.3;
   private static final double BUNNY_HOP_FALL_SPEED = -0.2000000000020557;
   public final NumberSetting attackRange = this.register(new NumberSetting("Attack Range", 0.0, -2.0, 3.0, 0.5, " blocks").warning(0.5, 2.5));
   public final NumberSetting aimRange = this.register(new NumberSetting("Aim Range", 1.0, 0.0, 5.0, 0.5, " blocks"));
   public final ModeSetting targetEsp = this.register(new ModeSetting("Target ESP", "Marker", "Marker", "Ghosts", "Circle"));
   public final ModeSetting targetEspColorMode = this.register(ColorMode.setting());
   public final ColorSetting targetEspColor = this.register(
      new ColorSetting("Target ESP Color", -15400961).visibleWhen(() -> ColorMode.isCustom(this.targetEspColorMode))
   );
   public final MultiSelectSetting targets = this.register(
      new MultiSelectSetting(
         "Targets", List.of("Players", "Naked Players"), "Players", "Friends", "Naked Players", "Invisibles", "Monsters", "Animals", "Villagers"
      )
   );
   public final BooleanSetting criticalsOnly = this.register(new BooleanSetting("Only Criticals", true));
   public final BooleanSetting smartCriticals = this.register(
      new BooleanSetting("Smart Criticals", true).configKey("combat.attackaura.critsWithSpace").visibleWhen(() -> this.criticalsOnly.getValue())
   );
   public final BooleanSetting tpsSync = this.register(new BooleanSetting("TPS Sync", true).configKey("combat.attackaura.tpsSync"));
   public final BooleanSetting dontHitWhileEating = this.register(
      new BooleanSetting("Don't Hit While Eating", true).configKey("combat.attackaura.noHitWhileEatingOffhand")
   );
   public final BooleanSetting releaseShield = this.register(new BooleanSetting("Release Shield", true).configKey("combat.attackaura.unPressShield"));
   public final BooleanSetting breakShield = this.register(new BooleanSetting("Break Shield", false).configKey("combat.attackaura.breakShield"));
   public final NumberSetting fov = this.register(new NumberSetting("FOV", 360.0, 30.0, 360.0, 5.0, "").configKey("combat.attackaura.fov"));
   public final ModeSetting movementCorrection = this.register(
      new ModeSetting("Move Correction", "Camera", "Off", "Camera").configKey("combat.attackaura.moveCorrection")
   );
   public final BooleanSetting throughWalls = this.register(new BooleanSetting("Through Walls", false));
   public final ModeSetting priority = this.register(new ModeSetting("Priority", "All", "Distance", "Health", "Armor", "FOV", "All"));
   public final ModeSetting rotation = this.register(
      new ModeSetting(
            "Rotation",
            "FunTime New",
            "FunTime New",
            "FunTime FOV",
            "Legit",
            "Matrix Vulcan",
            "Grim",
            "HolyWorld 3",
            "SpookyTime 2",
            "Smooth",
            "Polar",
            "Solution",
            "NeuroNotAiTreinig",
            "Sloth",
            "Neuro",
            "Builder"
         )
         .chips()
         .renamedFrom("Grim 1", "Grim")
         .renamedFrom("ФанТайм", "FunTime New")
         .renamedFrom("ФанТайм ФОВ", "FunTime FOV")
         .renamedFrom("Легит", "Legit")
   );
   private final AuraRotation matrixVulcanRotation = new MatrixVulcanRotation();
   private final AuraRotation grimRotation = new GrimRotation();
   private final AuraRotation holyWorldThreeRotation = new HolyWorldThreeRotation();
   private final AuraRotation slothRotation = new SlothRotation();
   private final AuraRotation smoothRotation = new SmoothRotation();
   private final AuraRotation polarRotation = new PolarRotation();
   private final AuraRotation solutionRotation = new SolutionRotation();
   private final AuraRotation neuroNotAiRotation = new PolarRotation();
   private final AuraRotation spookyTimeTwoRotation = ExpensiveRotation.spookyTime();
   private final AuraRotation neuroRotation = NeuroManager.activeRotation();
   private final AuraRotation builderRotation = new BuilderRotation();
   private final AttackTiming timing = new AttackTiming();
   private final AuraAttackController attackController = new AuraAttackController();
   private class_1309 target;
   private boolean targetGraced;
   private class_243 aimJitter = class_243.field_1353;

   public AuraFeature() {
      super("Aura", "Automatically attacks entities around you.", FeatureCategory.COMBAT, 82);
   }

   @Override
   protected void onDisable() {
      this.resetCombatState(mc.field_1724);
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.resetCombatState(event.getClient().field_1724);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 mc = event.getClient();
      class_746 player = mc.field_1724;
      class_638 level = mc.field_1687;
      if (player == null || level == null || !player.method_5805()) {
         this.resetCombatState(player);
      } else if (pveControlsCombat()) {
         this.resetCombatState(player);
      } else {
         this.attackController.tick(player);
         this.selectTarget(mc, player, level);
         if (this.target == null) {
            this.clearRotationState();
         } else {
            int predictionTicks = this.ticksUntilAttackReady(player);
            TickSimulator.SimState predictedState = TickSimulator.getPredictedState(this.target, predictionTicks, level);
            class_243 aimPosition = predictionTicks == 0 ? this.target.method_73189() : predictedState.pos;
            class_243 aimPoint = AuraRaycast.findAimPoint(
               player, this.target, aimPosition, this.aimRangeBlocks(player), this.throughWalls.getValue(), this.aimJitter
            );
            if (aimPoint == null) {
               this.clearRotationState();
               this.target = null;
            } else {
               boolean attackLikely = !this.targetGraced
                  && AuraRaycast.predictedHitboxDistanceSqr(player, this.target, aimPosition) <= squared(this.attackRangeBlocks(player));
               this.selectedRotation().tick(player, this.target, aimPoint, attackLikely);
            }
         }
      }
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPost()) {
         if (!pveControlsCombat()) {
            this.timing.tick();
            class_746 player = event.getPlayer();
            if (player != null && this.target != null && this.target.method_5805()) {
               if (this.canAttack(player, 1)) {
                  SprintManager.markAttackImminent();
                  if (this.releaseShield.getValue()) {
                     this.attackController.releaseShieldBeforeAttack(player);
                  }
               }

               float serverYaw = player.method_36454();
               float serverPitch = player.method_36455();
               boolean rotationOnTarget = AuraRaycast.rotationIntersectsTarget(
                  player,
                  this.target,
                  serverYaw,
                  serverPitch,
                  this.attackRangeBlocks(player),
                  AttackReach.min(player),
                  AttackReach.margin(player),
                  this.throughWalls.getValue()
               );
               if (rotationOnTarget && this.canAttack(player, 0) && !ServerSprintTracker.isServerSprinting()) {
                  if (this.attackController.attack(player, this.target, this.breakShield.getValue())) {
                     this.timing.onAttack();
                     this.rollAimJitter();
                     this.selectedRotation().onAttack();
                  }
               }
            }
         }
      }
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPhase() == PacketSendEvent.Phase.POST && this.target != null) {
         if (event.getPacket() instanceof class_2879 || event.getPacket() instanceof class_2868) {
            boolean usePattern = !this.rotation.is("Grim");
            this.timing.onSwingPacket(usePattern, this.tpsSync.getValue());
         }
      }
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      class_746 player = mc.field_1724;
      if (player != null
         && this.target != null
         && this.target.method_5805()
         && this.movementCorrection.is("Camera")
         && RotationContext.isActive()
         && !(event.getMoveVector().method_35587() <= 1.0E-6F)) {
         class_241 input = event.getMoveVector();
         double radians = Math.toRadians(RotationContext.getFreeYaw() - player.method_36454());
         float cosine = (float)Math.cos(radians);
         float sine = (float)Math.sin(radians);
         event.setMoveVector(new class_241(input.field_1343 * cosine - input.field_1342 * sine, input.field_1342 * cosine + input.field_1343 * sine));
      }
   }

   private void selectTarget(class_310 mc, class_746 player, class_638 level) {
      TargetFilter filter = CombatTargets.groups(TargetFilter.builder(), this.targets)
         .distanceWeight(this.distanceWeight())
         .healthWeight(this.healthWeight())
         .armorWeight(this.armorWeight())
         .fovWeight(this.fovWeight())
         .build();
      double attackRangeBlocks = this.attackRangeBlocks(player);
      double aimRangeBlocks = this.aimRangeBlocks(player);
      double attackRangeSqr = squared(attackRangeBlocks);
      double aimRangeSqr = squared(aimRangeBlocks);
      Predicate<class_1309> visibleTarget = this.throughWalls.getValue() ? entity -> true : entity -> AuraRaycast.canSeeTarget(player, entity, aimRangeBlocks);
      Predicate<class_1309> eligibleTarget = entity -> this.isWithinFov(player, entity) && visibleTarget.test(entity);
      if (this.target != null && TargetUtil.isValidTarget(player, this.target, aimRangeSqr, filter) && eligibleTarget.test(this.target)) {
         this.targetGraced = player.method_5858(this.target) > attackRangeSqr;
      } else {
         class_1309 picked = null;
         if (mc.field_1692 instanceof class_1309 crosshairTarget
            && TargetUtil.isValidTarget(player, crosshairTarget, attackRangeSqr, filter)
            && eligibleTarget.test(crosshairTarget)) {
            picked = crosshairTarget;
         }

         if (picked == null) {
            picked = TargetUtil.getBestTarget(player, level, attackRangeBlocks, filter, eligibleTarget);
         }

         boolean graced = false;
         if (picked == null) {
            picked = TargetUtil.getBestTarget(player, level, aimRangeBlocks, filter, eligibleTarget);
            graced = picked != null;
         }

         this.target = picked;
         this.targetGraced = graced;
      }
   }

   private boolean canAttack(class_746 player, int ticksAhead) {
      for (int tick = 0; tick <= ticksAhead; tick++) {
         if (this.canAttackAt(player, tick)) {
            return true;
         }
      }

      return false;
   }

   private boolean canAttackAt(class_746 player, int tick) {
      if (this.target == null || !this.target.method_5805()) {
         return false;
      } else if (mc.field_1755 instanceof class_476) {
         return false;
      } else if (player.method_3144()) {
         return false;
      } else if (this.usingItemBlocksAttack(player)) {
         return false;
      } else if (player.method_75202(player.method_6047(), tick)) {
         return false;
      } else {
         return AuraRaycast.findAimPoint(
                  player, this.target, this.target.method_73189(), this.attackRangeBlocks(player), this.throughWalls.getValue(), this.aimJitter
               )
               == null
            ? false
            : this.canCrit(player, tick);
      }
   }

   private boolean canCrit(class_746 player, int ticksAhead) {
      if (!this.timing.cooldownReady(player, ticksAhead)) {
         return false;
      }

      if (!this.criticalsOnly.getValue()) {
         return true;
      }

      TickSimulator.SimState sim = TickSimulator.simulateLocalPlayer(player, ticksAhead, player.method_73183());
      boolean jumpHeld = player.field_3913 != null && player.field_3913.field_54155.comp_3163();
      if (!sim.inWater) {
         if (this.smartCriticals.getValue() && sim.onGround && !jumpHeld) {
            return true;
         } else {
            return player.method_6047().method_31574(class_1802.field_49814)
               ? class_9362.method_58659(player)
               : this.criticalStateValid(player, ticksAhead, sim);
         }
      } else {
         return sim.swimming || sim.submergedInWater;
      }
   }

   private boolean criticalStateValid(class_746 player, int ticksAhead, TickSimulator.SimState sim) {
      if (player.method_6059(class_1294.field_5902) || player.method_6059(class_1294.field_5919) || player.method_6059(class_1294.field_5906)) {
         return true;
      } else if (!sim.inCobweb && !sim.inLava && !sim.climbing && !player.method_31549().field_7479) {
         TickSimulator.SimState next = TickSimulator.simulateLocalPlayer(player, ticksAhead + 1, player.method_73183());
         TickSimulator.SimState afterNext = TickSimulator.simulateLocalPlayer(player, ticksAhead + 2, player.method_73183());
         boolean landingWithFall = sim.fallDistance > 1.0F && next.onGround;
         boolean fallMatchesVelocity = sim.fallDistance == (float)sim.motion.field_1351 && next.onGround;
         boolean bunnyHopLanding = sim.motion.field_1351 < -0.2000000000020557
            && afterNext.onGround
            && (
               LocalPlayerHistory.verticalCollisionBelow(4, player.field_36331)
                  || LocalPlayerHistory.verticalCollisionBelow(5, player.field_36331)
                  || LocalPlayerHistory.verticalCollisionBelow(6, player.field_36331)
                  || LocalPlayerHistory.verticalCollisionBelow(7, player.field_36331)
            );
         return !sim.onGround && sim.fallDistance > 0.0F && !bunnyHopLanding && !fallMatchesVelocity && !landingWithFall;
      } else {
         return true;
      }
   }

   private boolean usingItemBlocksAttack(class_746 player) {
      if (!player.method_6115()) {
         return false;
      } else {
         class_1839 animation = player.method_6030().method_7976();
         boolean blockingAnimation = animation == class_1839.field_8950
            || animation == class_1839.field_8946
            || animation == class_1839.field_8947
            || animation == class_1839.field_8951
            || animation == class_1839.field_63380
            || animation == class_1839.field_8953;
         if (!blockingAnimation) {
            return false;
         } else {
            return player.method_6058() == class_1268.field_5808 ? true : this.dontHitWhileEating.getValue();
         }
      }
   }

   private int ticksUntilAttackReady(class_746 player) {
      int horizon = player.method_5799() ? 8 : 3;

      for (int tick = 0; tick < horizon; tick++) {
         if (this.canAttackAt(player, tick)) {
            return tick;
         }
      }

      return horizon;
   }

   private void rollAimJitter() {
      ThreadLocalRandom random = ThreadLocalRandom.current();
      this.aimJitter = new class_243(random.nextDouble(-0.3, 0.3), random.nextDouble(-0.3, 0.3), random.nextDouble(-0.3, 0.3));
   }

   public static AuraFeature getMarkerFeature() {
      return FeatureManager.INSTANCE.getFeature("Aura") instanceof AuraFeature auraFeature ? auraFeature : null;
   }

   public class_1309 getCurrentTarget() {
      return this.isEnabled() ? this.target : null;
   }

   public int getMarkerColor() {
      return ColorMode.resolve(this.targetEspColorMode, this.targetEspColor);
   }

   public boolean usesGhostTargetEsp() {
      return "Ghosts".equals(this.targetEsp.getValue());
   }

   public boolean usesCircleTargetEsp() {
      return "Circle".equals(this.targetEsp.getValue());
   }

   public boolean shouldAutoJump(class_746 player) {
      return this.isEnabled() && player != null && this.target != null && this.target.method_5805() && !this.targetGraced
         ? this.criticalsOnly.getValue() && player.method_24828()
         : false;
   }

   private boolean isWithinFov(class_746 player, class_1309 entity) {
      double cone = this.fov.getValue();
      if (cone >= 360.0) {
         return true;
      }

      float viewYaw = RotationContext.isActive() ? RotationContext.getFreeYaw() : player.method_36454();
      float viewPitch = RotationContext.isActive() ? RotationContext.getFreePitch() : player.method_36455();
      class_243 look = class_243.method_1030(viewPitch, viewYaw);
      class_243 toTarget = entity.method_5829().method_1005().method_1020(player.method_33571()).method_1029();
      return look.method_1026(toTarget) > Math.cos(Math.toRadians(cone * 0.5));
   }

   private void resetCombatState(class_746 player) {
      this.clearRotationState();
      SprintManager.reset();
      this.target = null;
      this.targetGraced = false;
      this.timing.reset();
      this.attackController.reset(player);
   }

   private void clearRotationState() {
      RotationContext.clear();
      this.resetRotations();
   }

   private AuraRotation selectedRotation() {
      return switch ((String)this.rotation.getValue()) {
         case "FunTime New", "FunTime FOV" -> this.matrixVulcanRotation;
         case "Legit" -> this.smoothRotation;
         case "Grim" -> this.grimRotation;
         case "HolyWorld 3" -> this.holyWorldThreeRotation;
         case "Sloth" -> this.slothRotation;
         case "Smooth" -> this.smoothRotation;
         case "Polar" -> this.polarRotation;
         case "Solution" -> this.solutionRotation;
         case "NeuroNotAiTreinig" -> this.neuroNotAiRotation;
         case "SpookyTime 2" -> this.spookyTimeTwoRotation;
         case "Neuro" -> this.neuroRotation;
         case "Builder" -> this.builderRotation;
         default -> this.matrixVulcanRotation;
      };
   }

   private void resetRotations() {
      this.matrixVulcanRotation.reset();
      this.grimRotation.reset();
      this.holyWorldThreeRotation.reset();
      this.slothRotation.reset();
      this.smoothRotation.reset();
      this.polarRotation.reset();
      this.solutionRotation.reset();
      this.neuroNotAiRotation.reset();
      this.spookyTimeTwoRotation.reset();
      this.builderRotation.reset();
   }

   private double attackRangeBlocks(class_746 player) {
      double baseRange = AttackReach.max(player);
      HitBoxesFeature hitBoxes = HitBoxesFeature.getEnabled();
      double extraRange = hitBoxes == null ? 0.0 : hitBoxes.getAuraExpansion();
      return Math.max(0.0, baseRange + this.attackRange.getValue() + extraRange);
   }

   private double aimRangeBlocks(class_746 player) {
      return this.attackRangeBlocks(player) + this.aimRange.getValue();
   }

   private static double squared(double value) {
      return value * value;
   }

   private static boolean pveControlsCombat() {
      return PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.COMBAT)
         || PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.ROTATION)
         || PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.INVENTORY);
   }

   private double distanceWeight() {
      return switch ((String)this.priority.getValue()) {
         case "Distance" -> 1.0;
         case "All" -> 0.5;
         default -> 0.0;
      };
   }

   private double healthWeight() {
      return switch ((String)this.priority.getValue()) {
         case "Health" -> 1.0;
         case "All" -> 1.2;
         default -> 0.0;
      };
   }

   private double armorWeight() {
      return switch ((String)this.priority.getValue()) {
         case "Armor" -> 1.0;
         case "All" -> 1.0;
         default -> 0.0;
      };
   }

   private double fovWeight() {
      return switch ((String)this.priority.getValue()) {
         case "FOV" -> 1.0;
         case "All" -> 0.25;
         default -> 0.0;
      };
   }
}
