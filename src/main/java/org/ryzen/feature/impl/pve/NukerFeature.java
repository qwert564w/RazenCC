package org.ryzen.feature.impl.pve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_3959;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_239.class_240;
import net.minecraft.class_3959.class_242;
import net.minecraft.class_3959.class_3960;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.PlayerTickEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.mining.MiningInventory;
import org.ryzen.pve.mining.MiningParsers;
import org.ryzen.pve.mining.MiningSessionSnapshot;
import org.ryzen.pve.mining.MiningTargetSelector;

@Environment(EnvType.CLIENT)
public final class NukerFeature extends PveFeature {
   private static final double MAX_REACH_SQUARED = 25.0;
   private static final int INSTANT_LIMIT = 8;
   public final NumberSetting radiusXz = this.register(new NumberSetting("Radius XZ", 3.0, 1.0, 6.0, 1.0, " blocks"));
   public final NumberSetting radiusY = this.register(new NumberSetting("Radius Y", 3.0, 1.0, 6.0, 1.0, " blocks"));
   public final ModeSetting workMode = this.register(new ModeSetting("Work Mode", "Everywhere", "Everywhere", "Only Mine"));
   public final TextSetting mineRegion = this.register(new TextSetting("Mine Region", "", 96));
   public final ModeSetting diggingMode = this.register(new ModeSetting("Digging Mode", "Everyone", "Everyone", "Ore Priority", "Only Ore"));
   public final NumberSetting yawSpeed = this.register(
      new NumberSetting("Yaw Speed", 180.0, 1.0, 180.0, 1.0, " deg/tick").visibleWhen(PveManagerFeature.INSTANCE.rotate::getValue)
   );
   public final NumberSetting pitchSpeed = this.register(
      new NumberSetting("Pitch Speed", 180.0, 1.0, 180.0, 1.0, " deg/tick").visibleWhen(PveManagerFeature.INSTANCE.rotate::getValue)
   );
   public final BooleanSetting throughWalls = this.register(new BooleanSetting("Through Walls", false));
   public final BooleanSetting mineNeighbor = this.register(new BooleanSetting("Mine Neighbor", false));
   public final BooleanSetting mineDown = this.register(new BooleanSetting("Mine Down", false));
   public final BooleanSetting instant = this.register(new BooleanSetting("Instant", false));
   private final MiningSessionSnapshot snapshot = new MiningSessionSnapshot();
   private class_2338 currentTarget;

   public NukerFeature() {
      super("Nuker", "Breaks nearby blocks using safe target filtering", -1, AutomationPriority.FEATURE, AutomationResource.ROTATION);
   }

   @Override
   protected void onPveEnable() {
      this.currentTarget = null;
      this.snapshot.capture(class_310.method_1551().field_1724);
   }

   @Override
   protected void onPveDisable() {
      this.cleanup();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cleanup();
   }

   @EventTarget
   public void onPlayerTick(PlayerTickEvent event) {
      if (event.isPre()) {
         class_310 client = class_310.method_1551();
         class_746 player = event.getPlayer();
         class_638 level = client.field_1687;
         if (player != null && level != null && client.field_1761 != null && player.method_5805() && !player.method_7325() && client.field_1755 == null) {
            this.snapshot.capture(player);
            List<MiningTargetSelector.Candidate<class_2338>> candidates = this.collectCandidates(level, player);
            Optional<MiningTargetSelector.Candidate<class_2338>> selected = MiningTargetSelector.select(
               candidates, this.resolvedDiggingMode(), this.throughWalls.getValue()
            );
            if (selected.isEmpty()) {
               this.cancelBreaking(client);
            } else {
               MiningTargetSelector.Candidate<class_2338> target = selected.get();
               if (this.mineNeighbor.getValue()) {
                  List<MiningTargetSelector.Candidate<class_2338>> neighbors = this.collectNeighbors(level, player, target.value());
                  target = MiningTargetSelector.easierNeighbor(target, neighbors, this.throughWalls.getValue()).orElse(target);
               }

               if (this.instant.getValue() && this.instantBreak(client, level, player, candidates)) {
                  this.currentTarget = target.value();
               } else {
                  this.breakTarget(client, level, player, target.value());
               }
            }
         } else {
            this.cancelBreaking(client);
         }
      }
   }

   public class_2338 getCurrentTarget() {
      return this.currentTarget;
   }

   private List<MiningTargetSelector.Candidate<class_2338>> collectCandidates(class_638 level, class_746 player) {
      int horizontal = this.radiusXz.getValue().intValue();
      int vertical = this.radiusY.getValue().intValue();
      class_2338 origin = player.method_24515();
      int minimumY = this.mineDown.getValue() ? origin.method_10264() - vertical : origin.method_10264();
      List<MiningTargetSelector.Candidate<class_2338>> candidates = new ArrayList<>();

      for (int x = origin.method_10263() - horizontal; x <= origin.method_10263() + horizontal; x++) {
         for (int y = minimumY; y <= origin.method_10264() + vertical; y++) {
            for (int z = origin.method_10260() - horizontal; z <= origin.method_10260() + horizontal; z++) {
               class_2338 position = new class_2338(x, y, z);
               this.candidate(level, player, origin, position).ifPresent(candidates::add);
            }
         }
      }

      return candidates;
   }

   private List<MiningTargetSelector.Candidate<class_2338>> collectNeighbors(class_638 level, class_746 player, class_2338 position) {
      List<MiningTargetSelector.Candidate<class_2338>> neighbors = new ArrayList<>();
      class_2338 origin = player.method_24515();

      for (class_2350 direction : class_2350.values()) {
         this.candidate(level, player, origin, position.method_10093(direction))
            .filter(candidate -> !this.diggingMode.is("Only Ore") || candidate.ore())
            .ifPresent(neighbors::add);
      }

      return neighbors;
   }

   private Optional<MiningTargetSelector.Candidate<class_2338>> candidate(class_638 level, class_746 player, class_2338 origin, class_2338 position) {
      if (!this.mineDown.getValue() && position.method_10264() < origin.method_10264()) {
         return Optional.empty();
      }

      class_2680 state = level.method_8320(position);
      boolean inWorkArea = this.isInWorkArea(position);
      boolean safe = this.isSafe(level, position, state) && distanceToBlockSquared(player.method_33571(), position) <= 25.0;
      if (!safe) {
         return Optional.empty();
      }

      boolean visible = this.isVisible(level, player, position);
      return Optional.of(
         new MiningTargetSelector.Candidate<>(
            position.method_10062(),
            true,
            MiningInventory.isOre(state.method_26204()),
            visible,
            inWorkArea,
            position.method_10264() - origin.method_10264(),
            player.method_5707(class_243.method_24953(position)),
            state.method_26214(level, position)
         )
      );
   }

   private boolean isSafe(class_638 level, class_2338 position, class_2680 state) {
      return !state.method_26215()
         && state.method_26227().method_15769()
         && !state.method_31709()
         && MiningInventory.isSafeBreakTarget(state.method_26204())
         && state.method_26214(level, position) >= 0.0F
         && !state.method_26220(level, position).method_1110();
   }

   private boolean isInWorkArea(class_2338 position) {
      return !this.workMode.is("Only Mine") ? true : MiningParsers.region(this.mineRegion.getValue()).map(region -> region.contains(position)).orElse(false);
   }

   private boolean isVisible(class_638 level, class_746 player, class_2338 position) {
      class_3965 hit = level.method_17742(
         new class_3959(player.method_33571(), class_243.method_24953(position), class_3960.field_17559, class_242.field_1348, player)
      );
      return hit.method_17783() == class_240.field_1333 || hit.method_17777().equals(position);
   }

   private void breakTarget(class_310 client, class_638 level, class_746 player, class_2338 target) {
      if (PveManagerFeature.INSTANCE.rotate.getValue()) {
         this.rotateToward(player, target);
      }

      if (!target.equals(this.currentTarget)) {
         this.cancelBreaking(client);
         this.currentTarget = target.method_10062();
         client.field_1761.method_2910(target, this.hitDirection(level, player, target));
      }

      client.field_1761.method_2902(target, this.hitDirection(level, player, target));
      player.method_6104(class_1268.field_5808);
   }

   private boolean instantBreak(class_310 client, class_638 level, class_746 player, List<MiningTargetSelector.Candidate<class_2338>> candidates) {
      List<MiningTargetSelector.Candidate<class_2338>> instantTargets = candidates.stream()
         .filter(MiningTargetSelector.Candidate::safe)
         .filter(MiningTargetSelector.Candidate::inWorkArea)
         .filter(candidatex -> this.throughWalls.getValue() || candidatex.visible())
         .filter(candidatex -> !this.diggingMode.is("Only Ore") || candidatex.ore())
         .filter(
            candidatex -> player.method_68878()
               || level.method_8320((class_2338)candidatex.value()).method_26165(player, level, (class_2338)candidatex.value()) >= 1.0F
         )
         .sorted(
            Comparator.<MiningTargetSelector.Candidate<class_2338>>comparingInt(candidatex -> this.diggingMode.is("Ore Priority") && candidatex.ore() ? 0 : 1)
               .thenComparingDouble(MiningTargetSelector.Candidate::distanceSquared)
         )
         .limit(8L)
         .toList();

      for (MiningTargetSelector.Candidate<class_2338> candidate : instantTargets) {
         client.field_1761.method_2910(candidate.value(), class_2350.field_11036);
         player.method_6104(class_1268.field_5808);
      }

      return !instantTargets.isEmpty();
   }

   private class_2350 hitDirection(class_638 level, class_746 player, class_2338 position) {
      class_3965 hit = level.method_17742(
         new class_3959(player.method_33571(), class_243.method_24953(position), class_3960.field_17559, class_242.field_1348, player)
      );
      return hit.method_17783() == class_240.field_1332 && hit.method_17777().equals(position) ? hit.method_17780() : class_2350.field_11036;
   }

   private void rotateToward(class_746 player, class_2338 position) {
      class_243 difference = class_243.method_24953(position).method_1020(player.method_33571());
      double horizontal = Math.sqrt(difference.field_1352 * difference.field_1352 + difference.field_1350 * difference.field_1350);
      float targetYaw = (float)Math.toDegrees(Math.atan2(difference.field_1350, difference.field_1352)) - 90.0F;
      float targetPitch = (float)(-Math.toDegrees(Math.atan2(difference.field_1351, horizontal)));
      float yawDelta = class_3532.method_15393(targetYaw - player.method_36454());
      float pitchDelta = class_3532.method_15393(targetPitch - player.method_36455());
      player.method_36456(
         player.method_36454() + class_3532.method_15363(yawDelta, -this.yawSpeed.getValue().floatValue(), this.yawSpeed.getValue().floatValue())
      );
      player.method_36457(
         player.method_36455() + class_3532.method_15363(pitchDelta, -this.pitchSpeed.getValue().floatValue(), this.pitchSpeed.getValue().floatValue())
      );
   }

   private MiningTargetSelector.DiggingMode resolvedDiggingMode() {
      if (this.diggingMode.is("Only Ore")) {
         return MiningTargetSelector.DiggingMode.ONLY_ORE;
      } else {
         return this.diggingMode.is("Ore Priority") ? MiningTargetSelector.DiggingMode.ORE_PRIORITY : MiningTargetSelector.DiggingMode.EVERYONE;
      }
   }

   private static double distanceToBlockSquared(class_243 point, class_2338 position) {
      double x = point.field_1352 - class_3532.method_15350(point.field_1352, position.method_10263(), position.method_10263() + 1.0);
      double y = point.field_1351 - class_3532.method_15350(point.field_1351, position.method_10264(), position.method_10264() + 1.0);
      double z = point.field_1350 - class_3532.method_15350(point.field_1350, position.method_10260(), position.method_10260() + 1.0);
      return x * x + y * y + z * z;
   }

   private void cancelBreaking(class_310 client) {
      if (client != null && client.field_1761 != null) {
         client.field_1761.method_2925();
      }

      this.currentTarget = null;
   }

   private void cleanup() {
      class_310 client = class_310.method_1551();
      this.cancelBreaking(client);
      this.snapshot.restore(client);
   }
}
