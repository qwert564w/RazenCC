package org.ryzen.feature.impl.combat;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1309;
import net.minecraft.class_1743;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2846;
import net.minecraft.class_2848;
import net.minecraft.class_2868;
import net.minecraft.class_310;
import net.minecraft.class_3486;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_465;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_2846.class_2847;
import net.minecraft.class_2848.class_2849;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.combat.AttackWindow;
import org.ryzen.utils.combat.CombatTargets;
import org.ryzen.utils.combat.ServerSprintTracker;
import org.ryzen.utils.combat.ServerTickSync;
import org.ryzen.utils.combat.SprintManager;
import org.ryzen.utils.combat.TargetFilter;
import org.ryzen.utils.combat.TargetUtil;

@Environment(EnvType.CLIENT)
public final class TriggerBotFeature extends Feature implements MinecraftContext {
   private static final float READY_CHARGE = 0.9F;
   private static final String SPRINT_LEGIT = "Legit";
   private static final String SPRINT_RAGE = "Rage";
   private static final String BLOCK_CONTAINER = "Container Open";
   private static final String BLOCK_EATING = "Using Item";
   private static final int MISS_MIN = 30;
   private static final int MISS_MAX = 41;
   public final MultiSelectSetting targets = this.register(
      new MultiSelectSetting(
         "Targets", List.of("Players", "Naked Players"), "Players", "Friends", "Naked Players", "Invisibles", "Monsters", "Animals", "Villagers"
      )
   );
   public final MultiSelectSetting blockers = this.register(
      new MultiSelectSetting("Do Not Attack While", List.of("Container Open"), "Container Open", "Using Item")
   );
   public final ModeSetting sprintReset = this.register(new ModeSetting("Sprint Reset", "Legit", "Legit", "Rage"));
   public final NumberSetting distance = this.register(new NumberSetting("Distance", 3.0, 0.5, 5.0, 0.1, " blocks"));
   public final BooleanSetting noWalls = this.register(new BooleanSetting("No Walls", false));
   public final BooleanSetting wallBypass = this.register(new BooleanSetting("Wall Bypass", true).visibleWhen(() -> !this.noWalls.getValue()));
   public final BooleanSetting criticalsOnly = this.register(new BooleanSetting("Only Criticals", false));
   public final BooleanSetting whileJumping = this.register(new BooleanSetting("Only While Jumping", false).visibleWhen(this.criticalsOnly::getValue));
   public final BooleanSetting tpsSync = this.register(new BooleanSetting("TPS Sync", false));
   public final BooleanSetting missSometimes = this.register(new BooleanSetting("Miss Sometimes", false));
   public final BooleanSetting breakShield = this.register(new BooleanSetting("Break Shields", true));
   public final NumberSetting delay = this.register(new NumberSetting("Delay", 0.0, 0.0, 1000.0, 10.0, " ms"));
   public final BooleanSetting randomDelay = this.register(new BooleanSetting("Random Delay", false).visibleWhen(() -> this.delay.getValue() > 0.0));
   private class_1309 currentTarget;
   private boolean blockedByWall;
   private long nextAttackAtMs;
   private int hitsSinceMiss;
   private int missAfterHits = ThreadLocalRandom.current().nextInt(30, 41);

   public TriggerBotFeature() {
      super("TriggerBot", "Attacks the entity you are aiming at", FeatureCategory.COMBAT, -1);
   }

   public static TriggerBotFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(TriggerBotFeature.class);
   }

   public class_1309 getCurrentTarget() {
      return this.isEnabled() ? this.currentTarget : null;
   }

   @Override
   protected void onDisable() {
      this.reset();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 mc = event.getClient();
      class_746 player = mc.field_1724;
      class_1309 next = player != null && mc.field_1687 != null ? this.findTarget(mc, player) : null;
      if (next != this.currentTarget) {
         this.currentTarget = next;
         this.scheduleNextAttack();
      }

      this.blockedByWall = next != null && this.noWalls.getValue() && !this.hasLineOfSight(mc, player, next);
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPost()) {
         class_746 player = event.getPlayer();
         class_1309 target = this.currentTarget;
         if (player != null && target != null && target.method_5805() && !this.blockedByWall && !this.isBlocked()) {
            if (this.canAttack(player, 1) && System.currentTimeMillis() >= this.nextAttackAtMs) {
               boolean rage = this.sprintReset.is("Rage");
               if (!rage) {
                  SprintManager.markAttackImminent();
                  if (!this.canAttack(player, 0) || ServerSprintTracker.isServerSprinting()) {
                     return;
                  }
               } else if (!this.canAttack(player, 0)) {
                  return;
               }

               if (this.findTarget(mc, player) == target) {
                  if (rage && player.method_5624() && !isInFluidOrGliding(player)) {
                     player.method_5728(false);
                     if (player.field_3944 != null) {
                        player.field_3944.method_52787(new class_2848(player, class_2849.field_12985));
                     }
                  }

                  if (this.missSometimes.getValue() && ++this.hitsSinceMiss >= this.missAfterHits) {
                     this.hitsSinceMiss = 0;
                     this.missAfterHits = ThreadLocalRandom.current().nextInt(30, 41);
                     player.method_6104(class_1268.field_5808);
                     this.scheduleNextAttack();
                  } else {
                     if (!this.noWalls.getValue() && this.wallBypass.getValue() && !player.method_6057(target)) {
                        this.sendWallBypass(player, target);
                     }

                     AttackWindow.attack(target);
                     if (this.breakShield.getValue()) {
                        this.breakShield(player, target);
                     }

                     this.scheduleNextAttack();
                  }
               }
            }
         }
      }
   }

   private class_1309 findTarget(class_310 mc, class_746 player) {
      double range = this.distance.getValue();
      class_243 eye = player.method_5836(1.0F);
      class_243 end = eye.method_1019(player.method_5828(1.0F).method_1021(range));
      class_1309 best = null;
      double bestDistance = Double.MAX_VALUE;

      for (class_1309 candidate : TargetUtil.getTargets(player, mc.field_1687, range + 2.0, this.filter())) {
         class_238 box = candidate.method_5829();
         if (box.method_1006(eye) || !box.method_992(eye, end).isEmpty()) {
            double distance = player.method_5858(candidate);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = candidate;
            }
         }
      }

      return best;
   }

   private boolean hasLineOfSight(class_310 mc, class_746 player, class_1309 target) {
      class_243 eye = player.method_5836(1.0F);
      class_243 end = eye.method_1019(player.method_5828(1.0F).method_1021(this.distance.getValue()));
      Optional<class_243> entry = target.method_5829().method_992(eye, end);
      class_243 contact = entry.orElse(eye);
      return mc.field_1687.method_17742(new class_3959(eye, contact, class_3960.field_17559, class_242.field_1348, player)).method_17783()
         == class_240.field_1333;
   }

   private void sendWallBypass(class_746 player, class_1309 target) {
      if (player.field_3944 != null) {
         class_243 eye = player.method_33571();
         class_243 delta = target.method_33571().method_1020(eye);
         double length = delta.method_1033();
         if (!(length < 0.01)) {
            int steps = Math.max(1, (int)Math.ceil(length * 2.0));
            Set<class_2338> visited = new HashSet<>();

            for (int step = 0; step <= steps; step++) {
               double progress = (double)step / steps;
               class_2338 pos = class_2338.method_49637(
                  eye.field_1352 + delta.field_1352 * progress, eye.field_1351 + delta.field_1351 * progress, eye.field_1350 + delta.field_1350 * progress
               );
               if (visited.add(pos) && !player.method_73183().method_8320(pos).method_26215()) {
                  class_2350 face = dominantFace(eye.method_1020(class_243.method_24953(pos)));
                  player.field_3944.method_52787(new class_2846(class_2847.field_12968, pos, face, 0));
                  player.field_3944.method_52787(new class_2846(class_2847.field_12973, pos, face, 0));
               }
            }
         }
      }
   }

   private static class_2350 dominantFace(class_243 offset) {
      double absX = Math.abs(offset.field_1352);
      double absY = Math.abs(offset.field_1351);
      double absZ = Math.abs(offset.field_1350);
      if (absY >= absX && absY >= absZ) {
         return offset.field_1351 > 0.0 ? class_2350.field_11036 : class_2350.field_11033;
      } else if (absX >= absZ) {
         return offset.field_1352 > 0.0 ? class_2350.field_11034 : class_2350.field_11039;
      } else {
         return offset.field_1350 > 0.0 ? class_2350.field_11035 : class_2350.field_11043;
      }
   }

   private void breakShield(class_746 player, class_1309 target) {
      if (target.method_6079().method_31574(class_1802.field_8255) || target.method_6047().method_31574(class_1802.field_8255)) {
         int axeSlot = -1;

         for (int slot = 0; slot < 9; slot++) {
            class_1799 stack = player.method_31548().method_5438(slot);
            if (stack.method_7909() instanceof class_1743) {
               axeSlot = slot;
               break;
            }
         }

         if (axeSlot != -1 && player.field_3944 != null && mc.field_1761 != null) {
            int originalSlot = player.method_31548().method_67532();
            if (axeSlot != originalSlot) {
               player.field_3944.method_52787(new class_2868(axeSlot));
               mc.field_1761.method_2918(player, target);
               player.method_6104(class_1268.field_5808);
               player.field_3944.method_52787(new class_2868(originalSlot));
            }
         }
      }
   }

   private void scheduleNextAttack() {
      long wait = this.delay.getValue().longValue();
      if (this.tpsSync.getValue()) {
         float tps = class_3532.method_15363(ServerTickSync.INSTANCE.effectiveTps(), 1.0F, 20.0F);
         wait = Math.round((float)wait * (20.0F / tps));
      }

      if (wait > 0L && this.randomDelay.getValue()) {
         wait = ThreadLocalRandom.current().nextLong(wait / 2L + 1L, wait + 1L);
      }

      this.nextAttackAtMs = wait <= 0L ? 0L : System.currentTimeMillis() + wait;
   }

   private boolean isBlocked() {
      class_746 player = this.player();
      if (player == null) {
         return true;
      } else {
         return this.blockers.isSelected("Using Item") && player.method_6115()
            ? true
            : this.blockers.isSelected("Container Open") && mc.field_1755 instanceof class_465;
      }
   }

   private boolean canAttack(class_746 player, int ticksAhead) {
      if (player.method_75202(player.method_6047(), ticksAhead)) {
         return false;
      }

      float charge = this.tpsSync.getValue()
         ? class_3532.method_15363(20.0F / Math.max(1.0F, ServerTickSync.INSTANCE.effectiveTps()), 0.5F, 2.0F) * (ticksAhead + 0.5F)
         : ticksAhead + 0.5F;
      return player.method_7261(charge) <= 0.9F ? false : this.canAttackEnvironment(player);
   }

   private TargetFilter filter() {
      return CombatTargets.groups(TargetFilter.builder(), this.targets).build();
   }

   private boolean canAttackEnvironment(class_746 player) {
      if (isInFluidOrGliding(player) || player.method_6101()) {
         return false;
      } else if (!this.criticalsOnly.getValue()) {
         return true;
      } else {
         return this.whileJumping.getValue() && !mc.field_1690.field_1903.method_1434() ? false : !player.method_24828() && player.field_6017 > 0.0;
      }
   }

   private static boolean isInFluidOrGliding(class_746 player) {
      return player.method_5799() && player.method_5777(class_3486.field_15517) || player.method_5771() || player.method_5681() || player.method_6128();
   }

   private void reset() {
      this.currentTarget = null;
      this.blockedByWall = false;
      this.nextAttackAtMs = 0L;
      this.hitsSinceMiss = 0;
      SprintManager.reset();
   }
}
