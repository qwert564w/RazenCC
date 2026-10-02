package org.ryzen.feature.impl.pve;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1297;
import net.minecraft.class_1542;
import net.minecraft.class_1548;
import net.minecraft.class_1703;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2246;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_638;
import net.minecraft.class_7439;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.navigation.BaritoneNavigator;
import org.ryzen.pve.navigation.NavigationOptions;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class CreeperFarmFeature extends PveFeature implements MinecraftContext {
   private static final Pattern MONEY_NUMBER = Pattern.compile(
      "(?iu)(?:\\$|₽)\\s*([0-9][0-9\\s.,]*)([kкmм]?)|([0-9][0-9\\s.,]*)([kкmм]?)\\s*(?:\\$|₽|coins?|монет(?:а|ы|у)?|валют(?:а|ы)?)"
   );
   private static final int ACTION_INTERVAL_TICKS = 4;
   private static final int ATTACK_TIMEOUT_TICKS = 1200;
   private static final int UNLOAD_RETRY_TICKS = 1200;
   public final BooleanSetting unloadGunpowder = this.register(new BooleanSetting("Unload Gunpowder", false));
   public final ModeSetting unloadTarget = this.register(new ModeSetting("Unload Target", "Chest", "Clan", "Ender Chest", "Chest", "None"));
   public final TextSetting farmPosition = this.register(new TextSetting("Farm Position", "auto"));
   public final TextSetting regionMin = this.register(new TextSetting("Region Min", "auto"));
   public final TextSetting regionMax = this.register(new TextSetting("Region Max", "auto"));
   public final TextSetting storagePosition = this.register(new TextSetting("Storage Position", "auto"));
   public final TextSetting clanUnloadCommand = this.register(new TextSetting("Clan Unload Command", ""));
   public final NumberSetting regionRadius = this.register(new NumberSetting("Region Radius", 32.0, 8.0, 128.0, 4.0, " blocks"));
   public final NumberSetting regionHeight = this.register(new NumberSetting("Region Height", 16.0, 4.0, 64.0, 2.0, " blocks"));
   public final NumberSetting chunkStep = this.register(new NumberSetting("Chunk Step", 1.0, 1.0, 4.0, 1.0, " chunks"));
   public final NumberSetting chunkLoadTimeout = this.register(new NumberSetting("Chunk Load Timeout", 60.0, 10.0, 180.0, 5.0, "s"));
   public final NumberSetting unloadAtStacks = this.register(new NumberSetting("Unload At", 16.0, 1.0, 36.0, 1.0, " stacks"));
   public final NumberSetting attackRange = this.register(new NumberSetting("Attack Range", 3.0, 2.0, 4.0, 0.1, " blocks"));
   public final NumberSetting moneyPerGunpowder = this.register(new NumberSetting("Money Per Gunpowder", 0.0, 0.0, 10000.0, 0.1, ""));
   private final PveStateMachine<CreeperFarmFeature.Phase> state = new PveStateMachine<>(CreeperFarmFeature.Phase.APPROACH);
   private final BaritoneNavigator navigator = BaritoneNavigator.INSTANCE;
   private final List<class_2338> chunkWaypoints = new ArrayList<>();
   private final List<class_2338> patrolWaypoints = new ArrayList<>();
   private long tick;
   private long lastActionTick;
   private long lastNavigationTick;
   private long lastProgressTick;
   private long unloadBackoffUntil;
   private class_2338 farmCenter;
   private class_2338 farmMin;
   private class_2338 farmMax;
   private class_2338 storage;
   private class_238 farmBounds;
   private int chunkWaypointIndex;
   private int patrolWaypointIndex;
   private int previousGunpowder;
   private int gunpowderCollected;
   private int gunpowderUnloaded;
   private int kills;
   private int recoveries;
   private double moneyEarned;
   private class_1548 target;
   private boolean targetAttacked;
   private boolean targetWasSwelling;
   private int targetAttacks;
   private boolean navigationActive;
   private boolean openedContainer;
   private boolean storageOpenRequested;
   private boolean unloadCommandSent;
   private CreeperFarmFeature.UnloadStep unloadStep = CreeperFarmFeature.UnloadStep.FIND;
   private float savedYaw;
   private float savedPitch;
   private boolean rotationSaved;

   public CreeperFarmFeature() {
      super(
         "CreeperFarm",
         "Loads a creeper farm, collects drops and unloads gunpowder",
         -1,
         AutomationPriority.BOT,
         AutomationResource.MOVEMENT,
         AutomationResource.ROTATION,
         AutomationResource.INVENTORY,
         AutomationResource.SCREEN,
         AutomationResource.CHAT,
         AutomationResource.NAVIGATION,
         AutomationResource.COMBAT
      );
   }

   @Override
   protected void onPveEnable() {
      this.tick = 0L;
      this.lastActionTick = -4611686018427387904L;
      this.lastNavigationTick = -4611686018427387904L;
      this.lastProgressTick = 0L;
      this.unloadBackoffUntil = 0L;
      this.farmCenter = null;
      this.farmMin = null;
      this.farmMax = null;
      this.storage = null;
      this.farmBounds = null;
      this.chunkWaypointIndex = 0;
      this.patrolWaypointIndex = 0;
      this.previousGunpowder = mc.field_1724 == null ? 0 : countGunpowder(mc.field_1724);
      this.gunpowderCollected = 0;
      this.gunpowderUnloaded = 0;
      this.kills = 0;
      this.recoveries = 0;
      this.moneyEarned = 0.0;
      this.target = null;
      this.targetAttacked = false;
      this.targetWasSwelling = false;
      this.targetAttacks = 0;
      this.navigationActive = false;
      this.openedContainer = false;
      this.storageOpenRequested = false;
      this.unloadCommandSent = false;
      this.unloadStep = CreeperFarmFeature.UnloadStep.FIND;
      this.rotationSaved = false;
      this.chunkWaypoints.clear();
      this.patrolWaypoints.clear();
      this.state.reset(0L);
      this.beginNavigation();
   }

   @Override
   protected void onPveDisable() {
      class_746 player = mc.field_1724;
      this.closeFeatureContainer(player);
      this.restoreRotation(player);
      this.endNavigation();
      this.clearTarget();
      this.chunkWaypoints.clear();
      this.patrolWaypoints.clear();
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.cancelNavigation();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      class_638 level = event.getClient().field_1687;
      if (player != null && level != null && event.getClient().field_1761 != null && player.method_5805()) {
         this.tick++;
         this.updateInventoryStats(player);
         this.updateTargetStats();
         switch ((CreeperFarmFeature.Phase)this.state.state()) {
            case APPROACH:
               this.tickApproach(player);
               break;
            case LOADING_CHUNKS:
               this.tickLoadingChunks(player);
               break;
            case LOOTING:
               this.tickLooting(player, level);
               break;
            case UNLOADING:
               this.tickUnloading(player, level);
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_7439 packet) {
         OptionalDouble delta = parseMoneyDelta(packet.comp_763().getString());
         if (delta.isPresent()) {
            this.moneyEarned = this.moneyEarned + delta.getAsDouble();
         }
      }
   }

   public CreeperFarmFeature.Phase getPhase() {
      return this.state.state();
   }

   public class_2338 getRegionMin() {
      return this.farmMin;
   }

   public class_2338 getRegionMax() {
      return this.farmMax;
   }

   public CreeperFarmFeature.FarmStats getStats() {
      double hours = this.tick / 72000.0;
      double incomePerHour = hours <= 0.0 ? 0.0 : this.moneyEarned / hours;
      return new CreeperFarmFeature.FarmStats(
         this.tick, this.moneyEarned, this.gunpowderCollected, this.kills, incomePerHour, this.state.state(), this.recoveries
      );
   }

   public int getGunpowderUnloaded() {
      return this.gunpowderUnloaded;
   }

   private void tickApproach(class_746 player) {
      this.resolveRegion(player);
      if (this.farmBounds != null && this.farmCenter != null) {
         boolean inside = this.farmBounds.method_1006(player.method_73189());
         boolean timedOut = this.tick - this.lastProgressTick > 1200L;
         CreeperFarmFeature.Phase next = nextPhase(CreeperFarmFeature.Phase.APPROACH, new CreeperFarmFeature.FarmSignals(inside, false, false, false, timedOut));
         if (next == CreeperFarmFeature.Phase.LOADING_CHUNKS) {
            this.transition(next);
         } else if (timedOut) {
            this.recoverFromStall(player);
         } else {
            this.navigateTo(this.farmCenter, 3);
         }
      } else {
         this.recoverFromStall(player);
      }
   }

   private void tickLoadingChunks(class_746 player) {
      if (this.chunkWaypoints.isEmpty()) {
         this.buildWaypoints();
      }

      long timeout = Math.max(1L, Math.round(this.chunkLoadTimeout.getValue() * 20.0));
      boolean timedOut = this.state.ticksInState(this.tick) >= timeout;
      boolean complete = this.chunkWaypointIndex >= this.chunkWaypoints.size();
      CreeperFarmFeature.Phase next = nextPhase(
         CreeperFarmFeature.Phase.LOADING_CHUNKS, new CreeperFarmFeature.FarmSignals(true, complete, false, false, timedOut)
      );
      if (next != CreeperFarmFeature.Phase.LOADING_CHUNKS) {
         this.transition(next);
      } else {
         class_2338 waypoint = this.chunkWaypoints.get(this.chunkWaypointIndex);
         if (at(player, waypoint, 4.0)) {
            this.chunkWaypointIndex++;
            this.markProgress();
            this.cancelNavigation();
         } else {
            this.navigateTo(waypoint, 3);
         }
      }
   }

   private void tickLooting(class_746 player, class_638 level) {
      if (this.shouldUnload(player)) {
         this.transition(CreeperFarmFeature.Phase.UNLOADING);
      } else if (this.tick - this.lastProgressTick > 1200L) {
         this.recoverFromStall(player);
      } else {
         class_1542 powder = this.nearestGunpowderEntity(level, player);
         if (powder != null) {
            this.clearTarget();
            if (powder.method_5858(player) > 2.25) {
               this.navigateTo(powder.method_24515(), 1);
            } else {
               this.cancelNavigation();
            }
         } else {
            if (!this.validTarget(this.target)) {
               this.target = this.nearestCreeper(level, player);
               this.targetAttacked = false;
               this.targetWasSwelling = false;
               this.targetAttacks = 0;
            }

            if (this.target != null) {
               this.lootCreeper(player);
            } else {
               this.patrol(player);
            }
         }
      }
   }

   private void lootCreeper(class_746 player) {
      if (this.target != null) {
         double distanceSquared = this.target.method_5858(player);
         if (!this.target.method_7000() && this.target.method_7007() <= 0) {
            double range = this.attackRange.getValue();
            if (distanceSquared > range * range) {
               this.navigateTo(this.target.method_24515(), Math.max(1, (int)Math.floor(range - 1.0)));
            } else {
               this.cancelNavigation();
               if (player.method_6057(this.target) && !(player.method_7261(0.0F) < 0.95F) && this.actionReady()) {
                  this.lookAt(player, this.target.method_33571());
                  mc.field_1761.method_2918(player, this.target);
                  player.method_6104(class_1268.field_5808);
                  this.targetAttacked = true;
                  this.targetAttacks++;
                  this.lastActionTick = this.tick;
                  this.markProgress();
                  if (this.targetAttacks >= 20) {
                     this.clearTarget();
                  }
               }
            }
         } else {
            this.targetWasSwelling = true;
            class_2338 retreat = this.farthestPatrolPoint(player.method_73189());
            if (retreat != null) {
               this.navigateTo(retreat, 2);
            }
         }
      }
   }

   private void patrol(class_746 player) {
      if (this.patrolWaypoints.isEmpty()) {
         this.buildWaypoints();
      }

      if (this.patrolWaypoints.isEmpty()) {
         this.recoverFromStall(player);
      } else {
         this.patrolWaypointIndex = this.patrolWaypointIndex % this.patrolWaypoints.size();
         class_2338 waypoint = this.patrolWaypoints.get(this.patrolWaypointIndex);
         if (at(player, waypoint, 4.0)) {
            this.patrolWaypointIndex = (this.patrolWaypointIndex + 1) % this.patrolWaypoints.size();
            this.markProgress();
            this.cancelNavigation();
         } else {
            this.navigateTo(waypoint, 3);
         }
      }
   }

   private void tickUnloading(class_746 player, class_638 level) {
      int powder = countGunpowder(player);
      if (powder <= 0) {
         this.closeFeatureContainer(player);
         this.transition(CreeperFarmFeature.Phase.LOOTING);
      } else if (!this.unloadTarget.is("None") && this.unloadGunpowder.getValue()) {
         if (this.tick - this.lastProgressTick > 800L) {
            this.unloadBackoffUntil = this.tick + 1200L;
            this.recoverFromStall(player);
         } else if (this.unloadTarget.is("Clan")) {
            this.tickClanUnload(player);
         } else {
            this.tickBlockStorageUnload(player, level);
         }
      } else {
         this.unloadBackoffUntil = this.tick + 1200L;
         this.transition(CreeperFarmFeature.Phase.LOOTING);
      }
   }

   private void tickClanUnload(class_746 player) {
      String command = this.clanUnloadCommand.getValue().trim();
      if (command.isEmpty()) {
         this.unloadBackoffUntil = this.tick + 1200L;
         this.transition(CreeperFarmFeature.Phase.LOOTING);
      } else {
         if (!this.unloadCommandSent) {
            ServerAdapters.current().sendCommand(player, command);
            this.unloadCommandSent = true;
            this.storageOpenRequested = true;
            this.lastActionTick = this.tick;
         }

         class_1703 menu = this.currentStorageMenu(player);
         if (menu != null) {
            this.depositGunpowder(player, menu);
         } else {
            if (this.state.ticksInState(this.tick) > 240L) {
               this.unloadBackoffUntil = this.tick + 1200L;
               this.closeFeatureContainer(player);
               this.transition(CreeperFarmFeature.Phase.LOOTING);
            }
         }
      }
   }

   private void tickBlockStorageUnload(class_746 player, class_638 level) {
      switch (this.unloadStep) {
         case FIND:
            this.storage = this.resolveStorage(player, level);
            if (this.storage != null) {
               this.unloadStep = CreeperFarmFeature.UnloadStep.MOVE;
               this.markProgress();
            } else if (this.state.ticksInState(this.tick) > 200L) {
               this.unloadBackoffUntil = this.tick + 1200L;
               this.transition(CreeperFarmFeature.Phase.LOOTING);
            }
            break;
         case MOVE:
            if (this.storage == null) {
               this.unloadStep = CreeperFarmFeature.UnloadStep.FIND;
               return;
            }

            if (at(player, this.storage, 4.0)) {
               this.cancelNavigation();
               this.unloadStep = CreeperFarmFeature.UnloadStep.OPEN;
               this.markProgress();
            } else {
               this.navigateTo(this.storage, 2);
            }
            break;
         case OPEN:
            class_1703 menu = this.currentStorageMenu(player);
            if (menu != null) {
               this.depositGunpowder(player, menu);
               return;
            }

            if (this.storage == null || !at(player, this.storage, 4.0)) {
               this.unloadStep = CreeperFarmFeature.UnloadStep.MOVE;
               return;
            }

            if (this.actionReady()) {
               this.openStorage(player, this.storage);
            }
      }
   }

   private void depositGunpowder(class_746 player, class_1703 menu) {
      int slot = findPlayerContainerSlot(menu, stack -> stack.method_31574(class_1802.field_8054));
      if (slot >= 0 && this.actionReady()) {
         InventoryUtil.quickMoveSlot(slot);
         this.lastActionTick = this.tick;
      } else if (slot < 0) {
         this.closeFeatureContainer(player);
         this.transition(CreeperFarmFeature.Phase.LOOTING);
      }
   }

   private void openStorage(class_746 player, class_2338 position) {
      this.lookAt(player, class_243.method_24953(position));
      class_3965 hit = new class_3965(class_243.method_24953(position), class_2350.field_11036, position, false);
      mc.field_1761.method_2896(player, class_1268.field_5808, hit);
      player.method_6104(class_1268.field_5808);
      this.storageOpenRequested = true;
      this.lastActionTick = this.tick;
   }

   private class_1703 currentStorageMenu(class_746 player) {
      if ((this.storageOpenRequested || this.openedContainer) && InventoryUtil.isContainerScreenOpen()) {
         class_1703 menu = InventoryUtil.getOpenMenu();
         if (menu != null && menu != player.field_7498) {
            this.openedContainer = true;
            return menu;
         } else {
            return null;
         }
      } else {
         return null;
      }
   }

   private class_2338 resolveStorage(class_746 player, class_638 level) {
      Optional<class_2338> configured = PveCoordinateParser.parse(this.storagePosition.getValue());
      if (configured.isPresent()) {
         return configured.get();
      }

      class_2338 origin = this.farmCenter == null ? player.method_24515() : this.farmCenter;
      boolean ender = this.unloadTarget.is("Ender Chest");
      class_2338 signed = this.findSignedStorage(level, origin, ender);
      return signed != null ? signed : this.findNearestStorage(level, player, origin, ender);
   }

   private class_2338 findSignedStorage(class_638 level, class_2338 origin, boolean ender) {
      int radius = Math.min(20, intValue(this.regionRadius));
      int yRadius = Math.min(8, intValue(this.regionHeight));

      for (class_2338 cursor : class_2338.method_10097(origin.method_10069(-radius, -yRadius, -radius), origin.method_10069(radius, yRadius, radius))) {
         if (level.method_8321(cursor) instanceof class_2625 sign) {
            List<String> lines = new ArrayList<>(8);

            for (int line = 0; line < 4; line++) {
               lines.add(sign.method_49853().method_49859(line, false).getString());
               lines.add(sign.method_49854().method_49859(line, false).getString());
            }

            if (matchesGunpowderLabel(lines)) {
               class_2338 storagePos = storageNear(level, cursor, 2, ender);
               if (storagePos != null) {
                  return storagePos;
               }
            }
         }
      }

      return null;
   }

   private class_2338 findNearestStorage(class_638 level, class_746 player, class_2338 origin, boolean ender) {
      int radius = Math.min(20, intValue(this.regionRadius));
      int yRadius = Math.min(8, intValue(this.regionHeight));
      class_2338 best = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (class_2338 cursor : class_2338.method_10097(origin.method_10069(-radius, -yRadius, -radius), origin.method_10069(radius, yRadius, radius))) {
         if (isStorage(level.method_8320(cursor), ender)) {
            double distance = player.method_73189().method_1025(class_243.method_24953(cursor));
            if (distance < bestDistance) {
               bestDistance = distance;
               best = cursor.method_10062();
            }
         }
      }

      return best;
   }

   private static class_2338 storageNear(class_638 level, class_2338 sign, int radius, boolean ender) {
      class_2338 best = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (class_2338 cursor : class_2338.method_10097(sign.method_10069(-radius, -radius, -radius), sign.method_10069(radius, radius, radius))) {
         if (isStorage(level.method_8320(cursor), ender)) {
            double distance = cursor.method_10262(sign);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = cursor.method_10062();
            }
         }
      }

      return best;
   }

   static boolean matchesGunpowderLabel(Iterable<String> lines) {
      for (String line : lines) {
         String normalized = line == null ? "" : line.replaceAll("§.", "").toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
         if (normalized.contains("gunpowder") || normalized.contains("порох")) {
            return true;
         }
      }

      return false;
   }

   private void resolveRegion(class_746 player) {
      class_2338 configuredCenter = PveCoordinateParser.parse(this.farmPosition.getValue())
         .orElseGet(() -> this.farmCenter == null ? player.method_24515().method_10062() : this.farmCenter);
      Optional<class_2338> configuredMin = PveCoordinateParser.parse(this.regionMin.getValue());
      Optional<class_2338> configuredMax = PveCoordinateParser.parse(this.regionMax.getValue());
      this.farmCenter = configuredCenter;
      if (configuredMin.isPresent() && configuredMax.isPresent()) {
         class_2338 first = configuredMin.get();
         class_2338 second = configuredMax.get();
         this.farmMin = new class_2338(
            Math.min(first.method_10263(), second.method_10263()),
            Math.min(first.method_10264(), second.method_10264()),
            Math.min(first.method_10260(), second.method_10260())
         );
         this.farmMax = new class_2338(
            Math.max(first.method_10263(), second.method_10263()),
            Math.max(first.method_10264(), second.method_10264()),
            Math.max(first.method_10260(), second.method_10260())
         );
         this.farmCenter = new class_2338(
            (this.farmMin.method_10263() + this.farmMax.method_10263()) / 2,
            (this.farmMin.method_10264() + this.farmMax.method_10264()) / 2,
            (this.farmMin.method_10260() + this.farmMax.method_10260()) / 2
         );
      } else {
         int radius = intValue(this.regionRadius);
         int height = intValue(this.regionHeight);
         this.farmMin = this.farmCenter.method_10069(-radius, -height, -radius);
         this.farmMax = this.farmCenter.method_10069(radius, height, radius);
      }

      this.farmBounds = new class_238(
         this.farmMin.method_10263(),
         this.farmMin.method_10264(),
         this.farmMin.method_10260(),
         this.farmMax.method_10263() + 1.0,
         this.farmMax.method_10264() + 1.0,
         this.farmMax.method_10260() + 1.0
      );
      if (this.chunkWaypoints.isEmpty()) {
         this.buildWaypoints();
      }
   }

   private void buildWaypoints() {
      this.chunkWaypoints.clear();
      this.patrolWaypoints.clear();
      if (this.farmMin != null && this.farmMax != null && this.farmCenter != null) {
         int step = Math.max(16, intValue(this.chunkStep) * 16);
         int y = this.farmCenter.method_10264();

         for (int x = this.farmMin.method_10263(); x <= this.farmMax.method_10263(); x += step) {
            for (int z = this.farmMin.method_10260(); z <= this.farmMax.method_10260(); z += step) {
               this.chunkWaypoints.add(new class_2338(x, y, z));
               if (this.chunkWaypoints.size() >= 81) {
                  break;
               }
            }

            if (this.chunkWaypoints.size() >= 81) {
               break;
            }
         }

         if (this.chunkWaypoints.stream().noneMatch(this.farmCenter::equals)) {
            this.chunkWaypoints.add(this.farmCenter);
         }

         this.patrolWaypoints.add(new class_2338(this.farmMin.method_10263(), y, this.farmMin.method_10260()));
         this.patrolWaypoints.add(new class_2338(this.farmMax.method_10263(), y, this.farmMin.method_10260()));
         this.patrolWaypoints.add(new class_2338(this.farmMax.method_10263(), y, this.farmMax.method_10260()));
         this.patrolWaypoints.add(new class_2338(this.farmMin.method_10263(), y, this.farmMax.method_10260()));
         this.patrolWaypoints.add(this.farmCenter);
      }
   }

   private class_1542 nearestGunpowderEntity(class_638 level, class_746 player) {
      if (this.farmBounds == null) {
         return null;
      }

      class_1542 best = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (class_1297 entity : level.method_18112()) {
         if (entity instanceof class_1542 item
            && item.method_5805()
            && item.method_6983().method_31574(class_1802.field_8054)
            && this.farmBounds.method_1006(item.method_73189())) {
            double distance = item.method_5858(player);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = item;
            }
         }
      }

      return best;
   }

   private class_1548 nearestCreeper(class_638 level, class_746 player) {
      return this.creepersSortedByDistance(level, player).stream().findFirst().orElse(null);
   }

   public List<class_1548> creepersSortedByDistance(class_638 level, class_746 player) {
      if (level != null && player != null && this.farmBounds != null) {
         List<class_1548> creepers = new ArrayList<>();

         for (class_1297 entity : level.method_18112()) {
            if (entity instanceof class_1548 creeper && this.validTarget(creeper) && this.farmBounds.method_1006(creeper.method_73189())) {
               creepers.add(creeper);
            }
         }

         creepers.sort(Comparator.comparingDouble(creeperx -> creeperx.method_5858(player)));
         return List.copyOf(creepers);
      } else {
         return List.of();
      }
   }

   private boolean validTarget(class_1548 creeper) {
      return creeper != null
         && creeper.method_5805()
         && !creeper.method_31481()
         && this.farmBounds != null
         && this.farmBounds.method_1006(creeper.method_73189());
   }

   private void updateTargetStats() {
      if (this.target != null && (!this.target.method_5805() || this.target.method_31481())) {
         if (this.targetAttacked && !this.targetWasSwelling) {
            this.kills++;
         }

         this.clearTarget();
      }
   }

   private void updateInventoryStats(class_746 player) {
      int current = countGunpowder(player);
      int gained = Math.max(0, current - this.previousGunpowder);
      int unloaded = Math.max(0, this.previousGunpowder - current);
      if (gained > 0 && this.state.state() != CreeperFarmFeature.Phase.UNLOADING) {
         this.gunpowderCollected += gained;
         this.moneyEarned = this.moneyEarned + gained * this.moneyPerGunpowder.getValue();
         this.markProgress();
      }

      if (unloaded > 0 && this.state.state() == CreeperFarmFeature.Phase.UNLOADING) {
         this.gunpowderUnloaded += unloaded;
         this.markProgress();
      }

      this.previousGunpowder = current;
   }

   private boolean shouldUnload(class_746 player) {
      if (this.unloadGunpowder.getValue() && !this.unloadTarget.is("None") && this.tick >= this.unloadBackoffUntil) {
         int powder = countGunpowder(player);
         int threshold = intValue(this.unloadAtStacks) * 64;
         return powder >= threshold || powder > 0 && freeSlots(player) <= 1;
      } else {
         return false;
      }
   }

   private void recoverFromStall(class_746 player) {
      this.cancelNavigation();
      this.closeFeatureContainer(player);
      this.clearTarget();
      this.recoveries++;
      this.lastProgressTick = this.tick;
      this.chunkWaypointIndex = 0;
      this.patrolWaypointIndex = 0;
      this.unloadStep = CreeperFarmFeature.UnloadStep.FIND;
      this.unloadCommandSent = false;
      this.storage = null;
      if (this.state.state() == CreeperFarmFeature.Phase.APPROACH) {
         this.state.reset(this.tick);
      } else {
         this.transition(CreeperFarmFeature.Phase.APPROACH);
      }
   }

   private void transition(CreeperFarmFeature.Phase next) {
      if (this.state.transition(next, this.tick)) {
         this.cancelNavigation();
         this.markProgress();
         if (next == CreeperFarmFeature.Phase.LOADING_CHUNKS) {
            this.chunkWaypointIndex = 0;
         }

         if (next == CreeperFarmFeature.Phase.UNLOADING) {
            this.unloadStep = CreeperFarmFeature.UnloadStep.FIND;
            this.unloadCommandSent = false;
            this.storage = null;
         }

         if (next != CreeperFarmFeature.Phase.UNLOADING) {
            this.closeFeatureContainer(mc.field_1724);
         }

         if (next != CreeperFarmFeature.Phase.LOOTING) {
            this.clearTarget();
         }
      }
   }

   static CreeperFarmFeature.Phase nextPhase(CreeperFarmFeature.Phase phase, CreeperFarmFeature.FarmSignals signals) {
      return switch (phase) {
         case APPROACH -> signals.insideRegion() ? CreeperFarmFeature.Phase.LOADING_CHUNKS : CreeperFarmFeature.Phase.APPROACH;
         case LOADING_CHUNKS -> !signals.chunksLoaded() && !signals.timedOut() ? CreeperFarmFeature.Phase.LOADING_CHUNKS : CreeperFarmFeature.Phase.LOOTING;
         case LOOTING -> signals.shouldUnload() ? CreeperFarmFeature.Phase.UNLOADING : CreeperFarmFeature.Phase.LOOTING;
         case UNLOADING -> signals.unloadComplete()
            ? CreeperFarmFeature.Phase.LOOTING
            : (signals.timedOut() ? CreeperFarmFeature.Phase.APPROACH : CreeperFarmFeature.Phase.UNLOADING);
      };
   }

   static OptionalDouble parseMoneyDelta(String message) {
      if (message == null) {
         return OptionalDouble.empty();
      }

      String normalized = message.replaceAll("§.", "").trim().toLowerCase(Locale.ROOT);
      boolean positiveContext = normalized.contains("+")
         || normalized.contains("earned")
         || normalized.contains("received")
         || normalized.contains("sold")
         || normalized.contains("заработ")
         || normalized.contains("получ")
         || normalized.contains("продан");
      boolean moneyContext = normalized.contains("$")
         || normalized.contains("₽")
         || normalized.contains("coin")
         || normalized.contains("монет")
         || normalized.contains("валют");
      if (positiveContext && moneyContext) {
         Matcher matcher = MONEY_NUMBER.matcher(normalized);
         double best = -1.0;

         while (matcher.find()) {
            String token = matcher.group(1) != null ? matcher.group(1) : matcher.group(3);
            String suffix = matcher.group(1) != null ? matcher.group(2) : matcher.group(4);
            OptionalDouble parsed = parseLocalizedNumber(token, suffix);
            if (parsed.isPresent()) {
               best = Math.max(best, parsed.getAsDouble());
            }
         }

         return best > 0.0 ? OptionalDouble.of(best) : OptionalDouble.empty();
      } else {
         return OptionalDouble.empty();
      }
   }

   private static OptionalDouble parseLocalizedNumber(String token, String suffix) {
      String compact = token.replace(" ", "");
      if (compact.isEmpty()) {
         return OptionalDouble.empty();
      }

      int comma = compact.lastIndexOf(44);
      int dot = compact.lastIndexOf(46);
      int separator = Math.max(comma, dot);
      String normalized;
      if (separator >= 0 && compact.length() - separator - 1 <= 2) {
         String integer = compact.substring(0, separator).replace(",", "").replace(".", "");
         String fraction = compact.substring(separator + 1);
         normalized = integer + "." + fraction;
      } else {
         normalized = compact.replace(",", "").replace(".", "");
      }

      try {
         double value = Double.parseDouble(normalized);
         if (suffix == null || !suffix.equalsIgnoreCase("k") && !suffix.equalsIgnoreCase("к")) {
            if (suffix != null && (suffix.equalsIgnoreCase("m") || suffix.equalsIgnoreCase("м"))) {
               value *= 1000000.0;
            }
         } else {
            value *= 1000.0;
         }

         return value > 0.0 ? OptionalDouble.of(value) : OptionalDouble.empty();
      } catch (NumberFormatException ignored) {
         return OptionalDouble.empty();
      }
   }

   private boolean beginNavigation() {
      if (!this.navigator.isAvailable()) {
         return false;
      }

      try {
         this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(NavigationOptions.walking()));
         this.navigationActive = true;
         return true;
      } catch (RuntimeException | LinkageError ignored) {
         this.navigationActive = false;
         return false;
      }
   }

   private void navigateTo(class_2338 target, int radius) {
      if (target != null && this.beginNavigation()) {
         Optional<class_2338> goal = this.navigator.currentGoal();
         boolean sameGoal = goal.isPresent() && goal.get().equals(target);
         if (!sameGoal || !this.navigator.isPathing() && this.tick - this.lastNavigationTick >= 40L) {
            try {
               this.navigator.pathTo(target, radius);
               this.lastNavigationTick = this.tick;
            } catch (RuntimeException | LinkageError ignored) {
               this.lastNavigationTick = this.tick;
            }
         }
      }
   }

   private void cancelNavigation() {
      if (this.navigationActive) {
         try {
            this.navigator.cancel();
         } catch (RuntimeException | LinkageError ignored) {
            this.navigationActive = false;
         }
      }
   }

   private void endNavigation() {
      if (this.navigationActive) {
         try {
            this.navigator.end();
         } catch (RuntimeException | LinkageError ignored) {
            this.navigator.cancel();
         } finally {
            this.navigationActive = false;
         }
      }
   }

   private void closeFeatureContainer(class_746 player) {
      if (this.openedContainer && player != null && player.field_7512 != player.field_7498) {
         player.method_7346();
      }

      this.openedContainer = false;
      this.storageOpenRequested = false;
   }

   private void clearTarget() {
      this.target = null;
      this.targetAttacked = false;
      this.targetWasSwelling = false;
      this.targetAttacks = 0;
   }

   private class_2338 farthestPatrolPoint(class_243 position) {
      class_2338 farthest = null;
      double distance = Double.NEGATIVE_INFINITY;

      for (class_2338 waypoint : this.patrolWaypoints) {
         double candidate = position.method_1025(class_243.method_24953(waypoint));
         if (candidate > distance) {
            distance = candidate;
            farthest = waypoint;
         }
      }

      return farthest;
   }

   private boolean actionReady() {
      return this.tick - this.lastActionTick >= 4L;
   }

   private void markProgress() {
      this.lastProgressTick = this.tick;
   }

   private void lookAt(class_746 player, class_243 targetPosition) {
      if (PveManagerFeature.INSTANCE.rotate.getValue()) {
         this.saveRotation(player);
         class_243 delta = targetPosition.method_1020(player.method_33571());
         double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         player.method_36456((float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F);
         player.method_36457((float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal))));
      }
   }

   private void saveRotation(class_746 player) {
      if (!this.rotationSaved && player != null) {
         this.savedYaw = player.method_36454();
         this.savedPitch = player.method_36455();
         this.rotationSaved = true;
      }
   }

   private void restoreRotation(class_746 player) {
      if (this.rotationSaved && player != null) {
         player.method_36456(this.savedYaw);
         player.method_36457(this.savedPitch);
         this.rotationSaved = false;
      }
   }

   private static boolean isStorage(class_2680 state, boolean ender) {
      return ender ? state.method_27852(class_2246.field_10443) : state.method_27852(class_2246.field_10034) || state.method_27852(class_2246.field_10380);
   }

   private static int findPlayerContainerSlot(class_1703 menu, Predicate<class_1799> predicate) {
      int start = ContainerLootService.containerSlotCount(menu);

      for (int slot = start; slot < menu.field_7761.size(); slot++) {
         class_1799 stack = menu.method_7611(slot).method_7677();
         if (!stack.method_7960() && predicate.test(stack)) {
            return slot;
         }
      }

      return -1;
   }

   private static int countGunpowder(class_746 player) {
      int count = 0;

      for (int slot = 9; slot < 45; slot++) {
         class_1799 stack = player.field_7498.method_7611(slot).method_7677();
         if (stack.method_31574(class_1802.field_8054)) {
            count += stack.method_7947();
         }
      }

      if (player.method_6079().method_31574(class_1802.field_8054)) {
         count += player.method_6079().method_7947();
      }

      return count;
   }

   private static int freeSlots(class_746 player) {
      int free = 0;

      for (int slot = 9; slot < 45; slot++) {
         if (player.field_7498.method_7611(slot).method_7677().method_7960()) {
            free++;
         }
      }

      return free;
   }

   private static boolean at(class_746 player, class_2338 position, double radius) {
      return position != null && player.method_73189().method_1025(class_243.method_24953(position)) <= radius * radius;
   }

   private static int intValue(NumberSetting setting) {
      return (int)Math.round(setting.getValue());
   }

   @Environment(EnvType.CLIENT)
   record FarmSignals(boolean insideRegion, boolean chunksLoaded, boolean shouldUnload, boolean unloadComplete, boolean timedOut) {
   }

   @Environment(EnvType.CLIENT)
   public record FarmStats(long uptimeTicks, double money, int gunpowder, int kills, double incomePerHour, CreeperFarmFeature.Phase phase, int recoveries) {
   }

   @Environment(EnvType.CLIENT)
   public enum Phase {
      APPROACH,
      LOADING_CHUNKS,
      LOOTING,
      UNLOADING;
   }

   @Environment(EnvType.CLIENT)
   enum UnloadStep {
      FIND,
      MOVE,
      OPEN;
   }
}
