package org.ryzen.feature.impl.pve;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.mining.BaseFinderFsm;
import org.ryzen.pve.mining.ContainerClusterScanner;
import org.ryzen.pve.mining.MiningInventory;
import org.ryzen.pve.mining.MiningParsers;
import org.ryzen.pve.mining.MiningServerAdapter;
import org.ryzen.pve.mining.MiningServerAdapters;
import org.ryzen.pve.mining.MiningSessionSnapshot;
import org.ryzen.pve.navigation.BaritoneNavigator;
import org.ryzen.pve.navigation.NavigationOptions;
import org.ryzen.pve.navigation.Navigator;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class BaseFinderFeature extends PveFeature {
   private static final String DEFAULT_TARGETS = String.join(
      ",",
      "chest",
      "trapped_chest",
      "barrel",
      "shulker_box",
      "white_shulker_box",
      "orange_shulker_box",
      "magenta_shulker_box",
      "light_blue_shulker_box",
      "yellow_shulker_box",
      "lime_shulker_box",
      "pink_shulker_box",
      "gray_shulker_box",
      "light_gray_shulker_box",
      "cyan_shulker_box",
      "purple_shulker_box",
      "blue_shulker_box",
      "brown_shulker_box",
      "green_shulker_box",
      "red_shulker_box",
      "black_shulker_box",
      "ender_chest",
      "hopper",
      "spawner",
      "trial_spawner"
   );
   private static final int TRANSFER_WAIT_TICKS = 80;
   private static final int RTP_WAIT_TICKS = 120;
   private static final int SCAN_BUDGET_PER_TICK = 2048;
   private static final int STUCK_CHECK_TICKS = 160;
   public final TextSetting targets = this.register(new TextSetting("Targets", DEFAULT_TARGETS, 512));
   public final TextSetting route = this.register(new TextSetting("Route", "", 1024));
   public final NumberSetting scanRadius = this.register(new NumberSetting("Scan Radius", 24.0, 12.0, 48.0, 1.0, " blocks"));
   public final NumberSetting minContainers = this.register(new NumberSetting("Minimum Containers", 4.0, 1.0, 32.0, 1.0, ""));
   public final ModeSetting heightMode = this.register(new ModeSetting("Height Mode", "Fixed", "Fixed", "Smart"));
   public final NumberSetting searchHeight = this.register(new NumberSetting("Search Height", -32.0, -60.0, 30.0, 1.0, "Y"));
   public final NumberSetting segmentLength = this.register(new NumberSetting("Tunnel Segment", 48.0, 12.0, 128.0, 4.0, " blocks"));
   public final BooleanSetting serverTransfer = this.register(new BooleanSetting("Server Transfer", false));
   public final ModeSetting rtpMode = this.register(new ModeSetting("RTP Mode", "Big", "Small", "Big"));
   public final NumberSetting transferInterval = this.register(new NumberSetting("Transfer Interval", 20.0, 2.0, 120.0, 1.0, "min"));
   private final Navigator navigator;
   private final MiningSessionSnapshot snapshot = new MiningSessionSnapshot();
   private final ContainerClusterScanner scanner = new ContainerClusterScanner();
   private final PveStateMachine<BaseFinderFsm.State> machine = new PveStateMachine<>(BaseFinderFsm.State.IDLE);
   private MiningServerAdapter adapter;
   private Set<class_2248> targetBlocks = Set.of();
   private List<class_2338> routePoints = List.of();
   private class_2338 segmentGoal;
   private class_2338 foundPosition;
   private class_2338 lastProgressPosition;
   private class_2338 lastScanCenter;
   private long tick;
   private long nextScanTick;
   private long transferStartedTick;
   private long lastProgressTick;
   private long searchStartedTick;
   private float headingYaw;
   private int routeIndex;
   private int currentAnarchy;
   private int transferStage;
   private int bypassSide = 1;
   private int smartHeight = -60;
   private boolean stateActionStarted;
   private boolean usesServerTransfer;
   private boolean safetyPaused;
   private String pauseReason;
   private BaseFinderFsm.State resumeState = BaseFinderFsm.State.DESCENDING;

   public BaseFinderFeature() {
      this(BaritoneNavigator.INSTANCE);
   }

   BaseFinderFeature(Navigator navigator) {
      super(
         "BaseFinder",
         "Searches configurable routes for container clusters",
         -1,
         AutomationPriority.BOT,
         AutomationResource.MOVEMENT,
         AutomationResource.ROTATION,
         AutomationResource.INVENTORY,
         AutomationResource.CHAT,
         AutomationResource.NAVIGATION
      );
      this.navigator = navigator;
   }

   @Override
   protected void onPveEnable() {
      class_310 client = class_310.method_1551();
      this.tick = 0L;
      this.foundPosition = null;
      this.segmentGoal = null;
      this.routeIndex = 0;
      this.currentAnarchy = PveManagerFeature.INSTANCE.resolvedAnarchy();
      this.transferStage = 0;
      this.bypassSide = 1;
      this.smartHeight = -60;
      this.headingYaw = client.field_1724 == null ? 0.0F : client.field_1724.method_36454();
      this.adapter = MiningServerAdapters.forProfile(PveManagerFeature.INSTANCE.resolveServerProfile(client));
      this.targetBlocks = Set.copyOf(MiningInventory.resolveBlocks(MiningParsers.identifiers(this.targets.getValue())));
      if (this.targetBlocks.isEmpty()) {
         throw new IllegalStateException("BaseFinder has no valid scan targets");
      }

      this.routePoints = MiningParsers.route(this.route.getValue()).stream().map(MiningParsers.GridPoint::toBlockPos).toList();
      this.usesServerTransfer = this.serverTransfer.getValue()
         && this.adapter.anarchyCommand(this.currentAnarchy).isPresent()
         && this.adapter.randomTeleportCommand(this.rtpMode.getValue()).isPresent();
      if (!this.navigator.isAvailable()) {
         throw new IllegalStateException("Baritone is unavailable");
      }

      this.snapshot.capture(client.field_1724);
      this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(NavigationOptions.mining()));
      this.scanner.reset();
      this.machine.reset(this.tick);
      this.transition(BaseFinderFsm.next(this.machine.state(), BaseFinderFsm.Signal.START, this.usesServerTransfer));
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
   public void onTick(GameTickEvent event) {
      this.tick++;
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         this.snapshot.capture(player);
         String unsafe = this.safetyReason(client, player);
         if (unsafe != null) {
            this.pauseForSafety(unsafe);
         } else {
            if (this.machine.is(BaseFinderFsm.State.PAUSED)) {
               if (!this.safetyPaused) {
                  return;
               }

               this.transition(BaseFinderFsm.resume(this.resumeState, this.usesServerTransfer));
            }

            if (this.shouldTransferAgain()) {
               this.transition(BaseFinderFsm.State.SERVER_TRANSFER);
            }

            if (this.machine.state() != BaseFinderFsm.State.IDLE
               && this.machine.state() != BaseFinderFsm.State.ERROR
               && this.machine.state() != BaseFinderFsm.State.FOUND
               && this.machine.state() != BaseFinderFsm.State.SERVER_TRANSFER) {
               this.tickScanner(client, player);
               if (this.machine.is(BaseFinderFsm.State.FOUND)) {
                  return;
               }
            }

            switch ((BaseFinderFsm.State)this.machine.state()) {
               case IDLE:
               case FOUND:
               case PAUSED:
               case ERROR:
               default:
                  break;
               case SERVER_TRANSFER:
                  this.tickServerTransfer(player);
                  break;
               case DESCENDING:
                  this.tickDescend(player);
                  break;
               case SEARCHING:
                  this.tickSearch(player);
                  break;
               case BYPASSING:
                  this.tickBypass(player);
            }
         }
      }
   }

   public BaseFinderFsm.State getState() {
      return this.machine.state();
   }

   public Optional<class_2338> getFoundPosition() {
      return Optional.ofNullable(this.foundPosition);
   }

   private void tickServerTransfer(class_746 player) {
      if (!this.stateActionStarted) {
         this.navigator.cancel();
         this.transferStage = 0;
         this.transferStartedTick = this.tick;
         Optional<String> command = this.adapter.anarchyCommand(this.currentAnarchy);
         if (command.isEmpty()) {
            this.signal(BaseFinderFsm.Signal.TRANSFER_DONE);
         } else {
            sendCommand(player, command.get());
            this.stateActionStarted = true;
         }
      } else if (this.transferStage == 0 && this.tick - this.transferStartedTick >= 80L) {
         Optional<String> rtp = this.adapter.randomTeleportCommand(this.rtpMode.getValue());
         if (rtp.isEmpty()) {
            this.signal(BaseFinderFsm.Signal.TRANSFER_DONE);
         } else {
            sendCommand(player, rtp.get());
            this.transferStage = 1;
            this.transferStartedTick = this.tick;
         }
      } else {
         if (this.transferStage == 1 && this.tick - this.transferStartedTick >= 120L) {
            this.headingYaw = player.method_36454();
            this.currentAnarchy = this.currentAnarchy >= 999 ? 1 : this.currentAnarchy + 1;
            this.signal(BaseFinderFsm.Signal.TRANSFER_DONE);
         }
      }
   }

   private void tickDescend(class_746 player) {
      class_2338 target = this.descentTarget(player);
      if (!this.stateActionStarted) {
         this.navigator.pathTo(target, 1);
         this.segmentGoal = target;
         this.stateActionStarted = true;
         this.resetProgress(player);
      }

      if (player.method_24515().method_10262(target) <= 4.0) {
         this.searchStartedTick = this.tick;
         this.routeIndex = 0;
         this.signal(BaseFinderFsm.Signal.DESCENT_DONE);
      } else {
         if (this.stuck(player)) {
            this.signal(BaseFinderFsm.Signal.PATH_STUCK);
         }
      }
   }

   private void tickSearch(class_746 player) {
      if (!this.stateActionStarted) {
         this.segmentGoal = this.nextSearchGoal(player);
         this.navigator.pathTo(this.segmentGoal, 2);
         this.stateActionStarted = true;
         this.resetProgress(player);
      }

      if (player.method_24515().method_10262(this.segmentGoal) <= 9.0) {
         if (!this.routePoints.isEmpty()) {
            this.routeIndex = (this.routeIndex + 1) % this.routePoints.size();
         }

         if (this.heightMode.is("Smart") && this.routePoints.isEmpty()) {
            this.smartHeight += 15;
            if (this.smartHeight > 30) {
               this.smartHeight = -60;
            }
         }

         this.stateActionStarted = false;
      } else {
         if (this.stuck(player)) {
            this.signal(BaseFinderFsm.Signal.PATH_STUCK);
         }
      }
   }

   private void tickBypass(class_746 player) {
      if (!this.stateActionStarted) {
         class_243 forward = horizontalDirection(this.headingYaw);
         class_243 side = new class_243(-forward.field_1350, 0.0, forward.field_1352).method_1021(16.0 * this.bypassSide);
         class_243 destination = player.method_73189().method_1019(forward.method_1021(24.0)).method_1019(side);
         this.segmentGoal = class_2338.method_49637(destination.field_1352, this.currentSearchHeight(), destination.field_1350);
         this.navigator.pathTo(this.segmentGoal, 2);
         this.stateActionStarted = true;
         this.resetProgress(player);
      }

      if (player.method_24515().method_10262(this.segmentGoal) <= 9.0 || this.machine.ticksInState(this.tick) > 400L) {
         this.bypassSide *= -1;
         this.headingYaw += 90.0F;
         this.signal(BaseFinderFsm.Signal.BYPASS_DONE);
      }
   }

   private void tickScanner(class_310 client, class_746 player) {
      if (!this.scanner.isRunning()
         && this.tick >= this.nextScanTick
         && (
            this.lastScanCenter == null
               || this.lastScanCenter.method_10262(player.method_24515()) >= square(Math.max(4, this.scanRadius.getValue().intValue() / 3))
         )) {
         this.lastScanCenter = player.method_24515();
         this.scanner.begin(this.lastScanCenter, this.scanRadius.getValue().intValue(), this.targetBlocks);
      }

      this.scanner
         .scan(client.field_1687, 2048)
         .ifPresent(
            result -> {
               this.scanner.reset();
               this.nextScanTick = this.tick + 40L;
               if (result.matches().size() >= this.minContainers.getValue().intValue()) {
                  this.foundPosition = result.nearestTo(player.method_24515()).orElse(result.scanCenter());
                  this.navigator.cancel();
                  ChatUtil.info(
                     "Container cluster found near "
                        + this.foundPosition.method_10263()
                        + ", "
                        + this.foundPosition.method_10264()
                        + ", "
                        + this.foundPosition.method_10260()
                  );
                  this.signal(BaseFinderFsm.Signal.CLUSTER_FOUND);
                  client.execute(() -> {
                     if (this.isEnabled()) {
                        this.setEnabled(false);
                     }
                  });
               }
            }
         );
   }

   private class_2338 descentTarget(class_746 player) {
      if (!this.routePoints.isEmpty()) {
         class_2338 first = (class_2338)this.routePoints.getFirst();
         return new class_2338(first.method_10263(), first.method_10264(), first.method_10260());
      } else {
         return new class_2338(player.method_31477(), this.currentSearchHeight(), player.method_31479());
      }
   }

   private class_2338 nextSearchGoal(class_746 player) {
      if (!this.routePoints.isEmpty()) {
         return this.routePoints.get(this.routeIndex);
      }

      class_243 direction = horizontalDirection(this.headingYaw);
      class_243 destination = player.method_73189().method_1019(direction.method_1021(this.segmentLength.getValue()));
      return class_2338.method_49637(destination.field_1352, this.currentSearchHeight(), destination.field_1350);
   }

   private int currentSearchHeight() {
      return this.heightMode.is("Smart") ? this.smartHeight : this.searchHeight.getValue().intValue();
   }

   private boolean stuck(class_746 player) {
      if (this.tick - this.lastProgressTick < 160L) {
         return false;
      }

      class_2338 current = player.method_24515();
      boolean stuck = this.lastProgressPosition != null && current.method_10262(this.lastProgressPosition) < 4.0;
      this.lastProgressPosition = current;
      this.lastProgressTick = this.tick;
      return stuck;
   }

   private void resetProgress(class_746 player) {
      this.lastProgressPosition = player.method_24515();
      this.lastProgressTick = this.tick;
   }

   private String safetyReason(class_310 client, class_746 player) {
      PveManagerFeature manager = PveManagerFeature.INSTANCE;
      if (!this.machine.is(BaseFinderFsm.State.FOUND) && !this.machine.is(BaseFinderFsm.State.ERROR) && !this.machine.is(BaseFinderFsm.State.IDLE)) {
         if (player.method_5805() && !(player.method_6032() + player.method_6067() < manager.minimumHealth.getValue())) {
            if (!player.method_68878()) {
               int slot = MiningInventory.bestPickaxeSlot(player);
               if (slot < 0) {
                  return "pickaxe missing";
               }

               if (MiningInventory.durabilityPercent(player.method_31548().method_5438(slot)) <= manager.minimumToolDurability.getValue()) {
                  return "pickaxe durability";
               }
            }

            double radius = manager.playerRadius.getValue();
            if (manager.pauseNearPlayers.getValue() && radius > 0.0) {
               double squared = radius * radius;

               for (class_1657 other : client.field_1687.method_18456()) {
                  if (other != player && other.method_5805() && !other.method_7325() && other.method_5858(player) <= squared) {
                     return "nearby player";
                  }
               }
            }

            return null;
         } else {
            return "low health";
         }
      } else {
         return null;
      }
   }

   private void pauseForSafety(String reason) {
      if (!this.machine.is(BaseFinderFsm.State.PAUSED)) {
         this.resumeState = this.machine.state();
      }

      this.pauseReason = reason;
      this.safetyPaused = true;
      this.navigator.cancel();
      this.transition(BaseFinderFsm.State.PAUSED);
   }

   private boolean shouldTransferAgain() {
      return this.usesServerTransfer
         && this.machine.is(BaseFinderFsm.State.SEARCHING)
         && this.tick - this.searchStartedTick >= this.transferInterval.getValue().longValue() * 60L * 20L;
   }

   private void signal(BaseFinderFsm.Signal signal) {
      this.transition(BaseFinderFsm.next(this.machine.state(), signal, this.usesServerTransfer));
   }

   private void transition(BaseFinderFsm.State next) {
      if (this.machine.transition(next, this.tick)) {
         this.stateActionStarted = false;
         this.segmentGoal = null;
         if (next != BaseFinderFsm.State.PAUSED) {
            this.safetyPaused = false;
            this.pauseReason = null;
         }

         if (next == BaseFinderFsm.State.SEARCHING) {
            this.searchStartedTick = this.tick;
         }
      }
   }

   private static class_243 horizontalDirection(float yaw) {
      double radians = Math.toRadians(yaw);
      return new class_243(-Math.sin(radians), 0.0, Math.cos(radians)).method_1029();
   }

   private static void sendCommand(class_746 player, String command) {
      if (player != null && command != null && !command.isBlank()) {
         player.field_3944.method_45730(command.charAt(0) == '/' ? command.substring(1) : command);
      }
   }

   private static long square(int value) {
      return (long)value * value;
   }

   private void cleanup() {
      class_310 client = class_310.method_1551();

      try {
         this.navigator.end();
      } catch (LinkageError | RuntimeException var3) {
      }

      if (client.field_1761 != null) {
         client.field_1761.method_2925();
      }

      this.scanner.reset();
      this.machine.reset(this.tick);
      this.stateActionStarted = false;
      this.segmentGoal = null;
      this.snapshot.restore(client);
   }
}
