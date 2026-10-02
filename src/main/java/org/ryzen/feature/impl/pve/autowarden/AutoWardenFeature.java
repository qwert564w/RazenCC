package org.ryzen.feature.impl.pve.autowarden;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1799;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3965;
import net.minecraft.class_412;
import net.minecraft.class_442;
import net.minecraft.class_638;
import net.minecraft.class_639;
import net.minecraft.class_642;
import net.minecraft.class_7260;
import net.minecraft.class_7439;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_12180.class_12181;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.FeatureEnableRejectedException;
import org.ryzen.feature.impl.pve.PveManagerFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PvpStateTracker;
import org.ryzen.pve.economy.AuctionPriceScanner;
import org.ryzen.pve.economy.EconomyItemText;
import org.ryzen.pve.economy.ServerUiText;
import org.ryzen.pve.navigation.BaritoneNavigator;
import org.ryzen.pve.navigation.NavigationOptions;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AutoWardenFeature extends PveFeature implements MinecraftContext {
   private static final int COMMAND_SETTLE_TICKS = 80;
   private static final int ACTION_INTERVAL_TICKS = 4;
   private static final int OPEN_RETRY_TICKS = 30;
   private static final int INVENTORY_FULL_FREE_SLOTS = 2;
   private static final int ZONE_COLOR = -871038977;
   private static final float ZONE_LINE_WIDTH = 1.4F;
   public final TextSetting homeAnarchy = this.register(new TextSetting("Home Anarchy", "0", 3));
   public final TextSetting lootAnarchies = this.register(new TextSetting("Loot Anarchies", "", 64));
   public final NumberSetting maxChestWait = this.register(new NumberSetting("Max Chest Wait", 900.0, 0.0, 7200.0, 10.0, " s"));
   public final NumberSetting minLootPerCycle = this.register(new NumberSetting("Min Loot", 1.0, 1.0, 36.0, 1.0, " items"));
   public final NumberSetting homeWaitThreshold = this.register(new NumberSetting("Home Wait Threshold", 180.0, 0.0, 3600.0, 10.0, " s"));
   public final NumberSetting returnBuffer = this.register(new NumberSetting("Return Buffer", 12.0, 1.0, 120.0, 1.0, " s"));
   public final NumberSetting preOpenSeconds = this.register(new NumberSetting("Before Opening", 2.0, 0.0, 20.0, 1.0, " s"));
   public final NumberSetting chestGrace = this.register(new NumberSetting("Chest Open Delay", 1.0, 0.0, 10.0, 0.5, " s"));
   public final NumberSetting teleportCooldown = this.register(new NumberSetting("Teleport Cooldown", 8.0, 1.0, 30.0, 1.0, " s"));
   public final BooleanSetting needInvisibility = this.register(new BooleanSetting("Require Invisibility", true));
   public final BooleanSetting needFood = this.register(new BooleanSetting("Require Food", true));
   public final BooleanSetting needSpeed = this.register(new BooleanSetting("Require Speed", false));
   public final NumberSetting minInvisibility = this.register(
      new NumberSetting("Min Invisibility", 2.0, 0.0, 64.0, 1.0, "").visibleWhen(this.needInvisibility::getValue)
   );
   public final NumberSetting minFood = this.register(new NumberSetting("Min Food", 32.0, 0.0, 256.0, 1.0, "").visibleWhen(this.needFood::getValue));
   public final NumberSetting minSpeed = this.register(new NumberSetting("Min Speed", 1.0, 0.0, 64.0, 1.0, "").visibleWhen(this.needSpeed::getValue));
   public final NumberSetting homeScanRadius = this.register(new NumberSetting("Home Scan Radius", 24.0, 4.0, 48.0, 1.0, " blocks"));
   public final TextSetting restockSign = this.register(new TextSetting("Restock Sign", "", 64));
   public final BooleanSetting avoidCrowded = this.register(new BooleanSetting("Avoid Crowded Chests", true));
   public final NumberSetting crowdRadius = this.register(new NumberSetting("Crowd Radius", 12.0, 0.0, 64.0, 1.0, " blocks"));
   public final NumberSetting crowdPenalty = this.register(new NumberSetting("Crowd Penalty", 20.0, 0.0, 500.0, 1.0, ""));
   public final BooleanSetting fleeWarden = this.register(new BooleanSetting("Flee From Warden", true));
   public final BooleanSetting emptyHand = this.register(new BooleanSetting("Keep Hand Empty", false));
   public final BooleanSetting autoReconnect = this.register(new BooleanSetting("Auto Reconnect", true));
   public final NumberSetting autoReconnectDelay = this.register(
      new NumberSetting("Reconnect Delay", 5.0, 1.0, 120.0, 1.0, " s").visibleWhen(this.autoReconnect::getValue)
   );
   public final BooleanSetting autoReporter = this.register(new BooleanSetting("Auto Reporter", false));
   public final BooleanSetting scoutCities = this.register(new BooleanSetting("Scout Warden Cities", true));
   public final ButtonSetting scoutNow = this.register(
      new ButtonSetting("Scout Now", "Scout", this::printScoutRecommendation).visibleWhen(this.scoutCities::getValue)
   );
   public final BooleanSetting telegramNotifications = this.register(new BooleanSetting("Telegram Notifications", false));
   public final ButtonSetting telegramConnect = this.register(
      new ButtonSetting("Connect Telegram", "Connect", this::connectTelegram).visibleWhen(this.telegramNotifications::getValue)
   );
   public final BooleanSetting autoSell = this.register(new BooleanSetting("Auto Sell", false));
   public final TextSetting sellSign = this.register(new TextSetting("Sell Sign", "продажа", 64).visibleWhen(this.autoSell::getValue));
   public final NumberSetting sellMarkup = this.register(new NumberSetting("Sell Markup", 0.0, -50.0, 200.0, 1.0, "%").visibleWhen(this.autoSell::getValue));
   public final NumberSetting fallbackSellPrice = this.register(
      new NumberSetting("Fallback Sell Price", 1000.0, 1.0, 1.0E8, 100.0, "").visibleWhen(this.autoSell::getValue)
   );
   public final NumberSetting investPercent = this.register(
      new NumberSetting("Invest Into Clan", 100.0, 0.0, 100.0, 1.0, "%").visibleWhen(this.autoSell::getValue)
   );
   public final NumberSetting sellMaxPages = this.register(new NumberSetting("Pages To Parse", 3.0, 1.0, 10.0, 1.0, "").visibleWhen(this.autoSell::getValue));
   public final BooleanSetting showZone = this.register(new BooleanSetting("Show Warden Zone", true));
   public final BooleanSetting showStats = this.register(new BooleanSetting("Show Statistics", true));
   private final AutoWardenWorkflow workflow = new AutoWardenWorkflow();
   private final AnarchyRotation rotation = new AnarchyRotation();
   private final AutoWardenStats stats = new AutoWardenStats();
   private final WardenChestScanner scanner = new WardenChestScanner();
   private final BaritoneNavigator navigator = BaritoneNavigator.INSTANCE;
   private final Set<String> reportedAttackers = new HashSet<>();
   private FunTimeWardenContract contract;
   private TelegramNotifier telegram;
   private List<Integer> configuredLootAnarchies = List.of();
   private int configuredHomeAnarchy = -1;
   private int currentAnarchy = -1;
   private int targetAnarchy = -1;
   private long tick;
   private long phaseActionTick;
   private long reconnectAtMillis;
   private long combatHoldUntilMillis;
   private long lastContainerOpenTick;
   private long lastScoutTick;
   private long storageRetryUntil;
   private boolean phaseActionStarted;
   private int phaseSubstep;
   private boolean navigationActive;
   private boolean reconnectAttempted;
   private boolean resourcesHeld;
   private boolean wasDead;
   private String lastAttacker;
   private class_642 reconnectServer;
   private class_2338 cityCenter;
   private class_2338 targetChest;
   private WardenChestScanner.ChestObservation targetObservation;
   private class_2338 storageCenter;
   private class_2338 activeStorageChest;
   private int patrolIndex;
   private int cycleLoot;
   private int depositedThisCycle;
   private int sellStep;
   private int sellSourceSlot = -1;
   private class_1799 sellStack = class_1799.field_8037;
   private long sellPrice;
   private long bestAuctionPrice;
   private int sellPagesScanned;

   public AutoWardenFeature() {
      super(
         "AutoWarden",
         "Automates Warden-city chest routes, storage, supplies and selling",
         -1,
         AutomationPriority.BOT,
         AutomationResource.MOVEMENT,
         AutomationResource.ROTATION,
         AutomationResource.INVENTORY,
         AutomationResource.SCREEN,
         AutomationResource.CHAT,
         AutomationResource.NAVIGATION
      );
   }

   @Override
   protected void validatePveEnable() {
      ServerAdapter adapter = ServerAdapters.current();
      this.contract = new FunTimeWardenContract(adapter);
      if (!this.contract.supported()) {
         throw new FeatureEnableRejectedException("AutoWarden requires a FunTime server profile");
      }

      this.configuredHomeAnarchy = AutoWardenParsers.parseHomeAnarchy(this.homeAnarchy.getValue()).orElse(-1);
      this.configuredLootAnarchies = AutoWardenParsers.parseLootAnarchies(this.lootAnarchies.getValue(), this.configuredHomeAnarchy);
      if (this.configuredHomeAnarchy < 0 || this.configuredLootAnarchies.isEmpty()) {
         throw new FeatureEnableRejectedException("configure one home anarchy and at least one loot anarchy");
      }

      if (!this.navigator.isAvailable()) {
         throw new FeatureEnableRejectedException("Baritone is unavailable");
      }
   }

   @Override
   protected void onPveEnable() {
      class_310 client = class_310.method_1551();
      this.resetRuntime();
      this.resourcesHeld = true;
      this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(NavigationOptions.walking()));
      this.navigationActive = true;
      this.stats.start(System.currentTimeMillis());
      this.rememberServer(client);
      if (this.telegramNotifications.getValue()) {
         this.ensureTelegram().reloadAndVerify();
      }
   }

   @Override
   protected void onPveDisable() {
      this.cleanupRuntime(true);
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.resourcesHeld = false;
      this.endNavigation();
      this.closeOwnedContainer(mc.field_1724);
      if ((reason == PveAutomationCoordinator.RevocationReason.DISCONNECT || reason == PveAutomationCoordinator.RevocationReason.WORLD_CHANGE)
         && this.autoReconnect.getValue()
         && this.reconnectServer != null) {
         this.scheduleReconnect();
      }
   }

   @Override
   protected boolean disableAfterRevocation(PveAutomationCoordinator.RevocationReason reason) {
      boolean reconnectTransition = reason == PveAutomationCoordinator.RevocationReason.DISCONNECT
         || reason == PveAutomationCoordinator.RevocationReason.WORLD_CHANGE;
      return !reconnectTransition || !this.autoReconnect.getValue() || this.reconnectServer == null;
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      this.rememberServer(client);
      if (client.field_1724 == null || client.field_1687 == null || client.field_1761 == null) {
         this.tickReconnect(client);
      } else if (this.ensureResources()) {
         class_746 player = client.field_1724;
         this.tick++;
         this.updateCurrentAnarchy();
         this.updateSupplies(player);
         if (this.scoutCities.getValue() && this.tick - this.lastScoutTick >= 6000L) {
            this.printScoutRecommendation();
         }

         this.handleDeath(player);
         if (!player.method_29504()) {
            if (this.emptyHand.getValue()) {
               this.keepSafeHand(player);
            }

            if (this.dangerNearby(player, client.field_1687)) {
               this.workflow.dangerDetected(this.tick);
            } else if (this.workflow.phase() == AutoWardenWorkflow.Phase.PVP_HIDE
               && System.currentTimeMillis() >= this.combatHoldUntilMillis
               && !PvpStateTracker.INSTANCE.isActive()) {
               this.workflow.dangerCleared(AutoWardenInventory.carryingValuables(player), this.tick);
               this.phaseChanged();
            }

            long timeoutOverride = this.workflow.phase() == AutoWardenWorkflow.Phase.HOME_WAIT
               ? Math.round((this.maxChestWait.getValue() + this.teleportCooldown.getValue() * 3.0) * 20.0)
               : -1L;
            if (this.workflow.timedOut(this.tick, timeoutOverride)) {
               this.failCurrentPhase("Phase timed out");
            }

            this.dispatchPhase(player, client.field_1687);
            this.updateStatsPhase();
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_7439 packet) {
         String text = packet.comp_763().getString();
         AutoWardenParsers.parseAnarchy(text).ifPresent(value -> this.currentAnarchy = value);
         AutoWardenParsers.parseCombatHoldMillis(text).ifPresent(value -> {
            this.combatHoldUntilMillis = Math.max(this.combatHoldUntilMillis, System.currentTimeMillis() + value);
            PvpStateTracker.INSTANCE.markCombatFor(value);
         });
         AutoWardenParsers.parseAttacker(text).ifPresent(attacker -> this.lastAttacker = attacker);
         if (AutoWardenParsers.indicatesFullAnarchy(text) && this.targetAnarchy >= 0) {
            this.rotation.avoidFor(this.targetAnarchy, 120000L, System.currentTimeMillis());
            this.workflow.rotateAnarchy(this.tick);
            this.phaseChanged();
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.rememberServer(event.getClient());
      if (this.autoReconnect.getValue() && this.reconnectServer != null) {
         this.scheduleReconnect();
      }
   }

   @EventTarget
   public void onWorldJoin(WorldJoinEvent event) {
      this.reconnectAtMillis = 0L;
      this.reconnectAttempted = false;
      if (this.isEnabled()) {
         if (this.ensureResources()) {
            this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(NavigationOptions.walking()));
            this.navigationActive = true;
            this.workflow.move(AutoWardenWorkflow.Phase.ENSURE_ANARCHY, "Connection restored", this.tick);
            this.phaseChanged();
         }
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.showStats.getValue()) {
         AutoWardenStats.Snapshot snapshot = this.stats.snapshot(System.currentTimeMillis());
         float x = 12.0F;
         float y = 92.0F;

         for (Entry<String, String> entry : snapshot.widgetData().entrySet()) {
            Render2DUtil.text(x, y, 10.0F, entry.getKey() + ": " + entry.getValue())
               .style(UiFontStyle.MEDIUM)
               .color(Theme.Colors.TEXT_TEXT)
               .outline(-1342177280, 0.7F)
               .draw();
            y += 12.0F;
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.showZone.getValue() && mc.field_1687 != null && mc.field_1769 != null) {
         class_12181 ignored = event.getClient().field_1769.method_75414();

         try {
            for (class_7260 warden : mc.field_1687
               .method_18467(class_7260.class, mc.field_1724 == null ? new class_238(class_2338.field_10980) : mc.field_1724.method_5829().method_1014(96.0))) {
               class_12180.method_75553(new AutoWardenFeature.ZoneGizmo(warden.method_5829().method_1009(16.0, 4.0, 16.0))).method_75533();
            }
         } catch (Throwable var6) {
            if (ignored != null) {
               try {
                  ignored.close();
               } catch (Throwable var5) {
                  var6.addSuppressed(var5);
               }
            }

            throw var6;
         }

         if (ignored != null) {
            ignored.close();
         }
      }
   }

   public AutoWardenWorkflow.Phase getPhase() {
      return this.workflow.phase();
   }

   public AutoWardenStats.Snapshot getStats() {
      return this.stats.snapshot(System.currentTimeMillis());
   }

   public int activeLootAnarchy() {
      return this.targetAnarchy;
   }

   public boolean isOnLootAnarchy() {
      return this.configuredLootAnarchies.contains(this.currentAnarchy);
   }

   public boolean managesHotbar() {
      return this.workflow.phase() != AutoWardenWorkflow.Phase.IDLE;
   }

   public boolean stocksSpeed() {
      return this.needSpeed.getValue();
   }

   private void dispatchPhase(class_746 player, class_638 level) {
      switch (this.workflow.phase()) {
         case IDLE:
            this.tickIdle(player);
            break;
         case ENSURE_ANARCHY:
            this.tickEnsureAnarchy(player);
            break;
         case HOME_TO_WARDEN_CITY:
            this.tickHomeToCity(player);
            break;
         case SEARCH_CHEST:
            this.tickSearchChest(player, level);
            break;
         case PATROL_CITY:
            this.tickPatrol(player, level);
            break;
         case MOVE_TO_CHEST:
            this.tickMoveToChest(player);
            break;
         case HOME_WAIT:
            this.tickHomeWait(player);
            break;
         case RETURN_TO_CITY:
            this.tickReturnToCity(player);
            break;
         case OPEN_AND_LOOT:
            this.tickOpenAndLoot(player);
            break;
         case PVP_HIDE:
            this.tickHide(player, level);
            break;
         case GO_TO_STORAGE:
            this.tickGoStorage(player);
            break;
         case DEPOSIT_LOOT:
            this.tickDeposit(player, level);
            break;
         case RESTOCK:
            this.tickRestock(player, level);
            break;
         case SELL_ITEMS:
            this.tickSell(player, level);
      }
   }

   private void tickIdle(class_746 player) {
      if (this.tick >= this.storageRetryUntil) {
         boolean needsStorage = AutoWardenInventory.carryingValuables(player) || this.suppliesMissing(player);
         if (this.workflow.start(true, needsStorage, this.tick)) {
            this.phaseChanged();
         }
      }
   }

   private void tickEnsureAnarchy(class_746 player) {
      if (!this.phaseActionStarted) {
         this.targetAnarchy = this.rotation.pickNext(this.configuredLootAnarchies, this.crowdPenalty.getValue(), System.currentTimeMillis());
         if (this.targetAnarchy < 0) {
            this.failCurrentPhase("No loot anarchy is available");
         } else {
            this.stats.activeAnarchy(this.targetAnarchy);
            if (this.currentAnarchy != this.targetAnarchy) {
               this.contract.switchAnarchy(this.targetAnarchy).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            }

            this.startPhaseAction();
         }
      } else {
         if (this.currentAnarchy == this.targetAnarchy || this.phaseActionElapsed(this.commandSettleTicks())) {
            this.currentAnarchy = this.targetAnarchy;
            this.workflow.anarchyReady(this.tick);
            this.phaseChanged();
         }
      }
   }

   private void tickHomeToCity(class_746 player) {
      if (this.suppliesMissing(player)) {
         this.workflow.requestStorage("Supplies are below the configured minimum", this.tick);
         this.phaseChanged();
      } else if (!this.phaseActionStarted) {
         this.configuredHomeCommand().ifPresent(command -> this.contractAdapter().sendCommand(player, command));
         this.startPhaseAction();
      } else {
         if (this.phaseActionElapsed(this.commandSettleTicks())) {
            this.cityCenter = player.method_24515();
            this.workflow.cityReady(this.tick);
            this.phaseChanged();
         }
      }
   }

   private void tickSearchChest(class_746 player, class_638 level) {
      long now = System.currentTimeMillis();
      List<WardenChestScanner.ChestObservation> candidates = this.scanner.scanLootChests(level, player, 64, this.crowdRadius.getValue().intValue(), now);
      Optional<WardenChestScanner.ChestObservation> selected = this.scanner
         .selectBest(
            candidates, player.method_73189(), this.maxChestWait.getValue().intValue(), this.avoidCrowded.getValue(), this.crowdPenalty.getValue(), now
         );
      if (selected.isEmpty()) {
         this.workflow.noChestFound(this.tick);
         this.phaseChanged();
      } else {
         this.selectChest(selected.get());
         int remaining = selected.get().remainingSeconds(now);
         this.workflow.chestSelected(remaining, this.homeWaitThreshold.getValue().intValue(), this.tick);
         this.phaseChanged();
      }
   }

   private void tickPatrol(class_746 player, class_638 level) {
      long now = System.currentTimeMillis();
      Optional<WardenChestScanner.ChestObservation> selected = this.scanner
         .selectBest(
            this.scanner.scanLootChests(level, player, 64, this.crowdRadius.getValue().intValue(), now),
            player.method_73189(),
            this.maxChestWait.getValue().intValue(),
            this.avoidCrowded.getValue(),
            this.crowdPenalty.getValue(),
            now
         );
      if (selected.isPresent()) {
         this.selectChest(selected.get());
         this.workflow.patrolFoundChest(this.tick);
         this.phaseChanged();
      } else {
         if (!this.navigator.isPathing() || this.phaseActionElapsed(200L)) {
            class_2338 center = this.cityCenter == null ? player.method_24515() : this.cityCenter;
            int[][] offsets = new int[][]{{24, 0}, {0, 24}, {-24, 0}, {0, -24}, {16, 16}, {-16, 16}, {-16, -16}, {16, -16}};
            int[] offset = offsets[Math.floorMod(this.patrolIndex++, offsets.length)];
            this.pathTo(center.method_10069(offset[0], 0, offset[1]), 3);
            this.startPhaseAction();
         }
      }
   }

   private void tickMoveToChest(class_746 player) {
      if (this.targetChest == null) {
         this.workflow.continueSearching(this.tick);
         this.phaseChanged();
      } else if (player.method_5707(class_243.method_24953(this.targetChest)) <= 16.0) {
         this.cancelNavigation();
         this.workflow.chestReached(this.tick);
         this.phaseChanged();
      } else {
         if (!this.navigator.isPathing()) {
            this.pathTo(this.targetChest, 2);
         }
      }
   }

   private void tickHomeWait(class_746 player) {
      if (!this.phaseActionStarted) {
         if (this.currentAnarchy != this.configuredHomeAnarchy) {
            this.contract.switchAnarchy(this.configuredHomeAnarchy).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            this.phaseSubstep = 0;
         } else {
            this.configuredHomeCommand().ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            this.phaseSubstep = 1;
         }

         this.startPhaseAction();
      } else if (this.phaseSubstep == 0 && this.phaseActionElapsed(this.commandSettleTicks())) {
         this.currentAnarchy = this.configuredHomeAnarchy;
         this.configuredHomeCommand().ifPresent(command -> this.contractAdapter().sendCommand(player, command));
         this.phaseSubstep = 1;
         this.phaseActionTick = this.tick;
      } else if (this.phaseSubstep != 1 || this.phaseActionElapsed(this.commandSettleTicks())) {
         int remaining = this.targetObservation == null ? 0 : this.targetObservation.remainingSeconds(System.currentTimeMillis());
         int returnAt = this.returnBuffer.getValue().intValue() + this.preOpenSeconds.getValue().intValue();
         this.stats.homeWaitSeconds(remaining);
         if (remaining <= returnAt) {
            this.workflow.returnWindowReached(this.tick);
            this.phaseChanged();
         }
      }
   }

   private void tickReturnToCity(class_746 player) {
      if (!this.phaseActionStarted) {
         this.contract.switchAnarchy(this.targetAnarchy).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
         this.startPhaseAction();
      } else if (this.phaseSubstep == 0 && this.phaseActionElapsed(this.commandSettleTicks())) {
         this.configuredHomeCommand().ifPresent(command -> this.contractAdapter().sendCommand(player, command));
         this.currentAnarchy = this.targetAnarchy;
         this.phaseSubstep = 1;
         this.phaseActionTick = this.tick;
      } else {
         if (this.phaseSubstep == 1 && this.phaseActionElapsed(this.commandSettleTicks())) {
            this.workflow.move(AutoWardenWorkflow.Phase.MOVE_TO_CHEST, "Returned for chest opening", this.tick);
            this.phaseChanged();
         }
      }
   }

   private void tickOpenAndLoot(class_746 player) {
      class_1703 menu = this.currentForeignMenu(player);
      if (menu == null) {
         if (this.phaseActionElapsed(Math.max(30, Math.round(this.chestGrace.getValue().floatValue() * 20.0F)))) {
            if (this.targetChest != null && this.openContainer(player, this.targetChest)) {
               this.startPhaseAction();
            } else {
               this.scanner.markFailed(this.targetChest, System.currentTimeMillis());
               this.workflow.continueSearching(this.tick);
               this.phaseChanged();
            }
         }
      } else if (this.actionReady() && AutoWardenInventory.quickMoveFirstContainerItem(menu, AutoWardenInventory::isValuable)) {
         this.cycleLoot++;
         this.phaseActionTick = this.tick;
      } else {
         this.closeOwnedContainer(player);
         this.scanner.markLooted(this.targetChest);
         this.stats.chestLooted(this.cycleLoot);
         this.rotation.onChestLooted(this.targetAnarchy);
         this.notifyTelegram("Looted chest on anarchy " + this.targetAnarchy + ": " + this.cycleLoot + " items");
         boolean shouldStore = this.cycleLoot >= this.minLootPerCycle.getValue().intValue()
            || AutoWardenInventory.freeSlots(player) <= 2
            || this.suppliesMissing(player);
         if (shouldStore) {
            this.workflow.requestStorage("Loot or supplies require storage", this.tick);
         } else {
            this.workflow.continueSearching(this.tick);
         }

         this.phaseChanged();
      }
   }

   private void tickHide(class_746 player, class_638 level) {
      Optional<class_243> danger = this.nearestDanger(player, level);
      if (!danger.isEmpty()) {
         class_243 away = player.method_73189().method_1020(danger.get()).method_1029();
         if (away.method_1027() < 1.0E-6) {
            away = new class_243(1.0, 0.0, 0.0);
         }

         class_2338 escape = class_2338.method_49638(player.method_73189().method_1019(away.method_1021(24.0)));
         if (!this.navigator.isPathing() || this.phaseActionElapsed(100L)) {
            this.pathTo(escape, 3);
            this.startPhaseAction();
         }
      }
   }

   private void tickGoStorage(class_746 player) {
      if (!this.phaseActionStarted) {
         if (this.currentAnarchy != this.configuredHomeAnarchy) {
            this.contract.switchAnarchy(this.configuredHomeAnarchy).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            this.phaseSubstep = 0;
         } else {
            this.configuredHomeCommand().ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            this.phaseSubstep = 1;
         }

         this.startPhaseAction();
      } else if (this.phaseSubstep == 0 && this.phaseActionElapsed(this.commandSettleTicks())) {
         this.currentAnarchy = this.configuredHomeAnarchy;
         this.configuredHomeCommand().ifPresent(command -> this.contractAdapter().sendCommand(player, command));
         this.phaseSubstep = 1;
         this.phaseActionTick = this.tick;
      } else {
         if (this.phaseSubstep == 1 && this.phaseActionElapsed(this.commandSettleTicks())) {
            this.storageCenter = player.method_24515();
            this.workflow.storageReady(this.tick);
            this.phaseChanged();
         }
      }
   }

   private void tickDeposit(class_746 player, class_638 level) {
      List<WardenChestScanner.StorageChest> storage = this.scanStorage(level);
      WardenChestScanner.StorageChest deposit = storage.stream()
         .filter(chest -> chest.kind() == WardenChestScanner.StorageKind.DEPOSIT)
         .findFirst()
         .orElse(null);
      if (deposit == null) {
         this.delayStorageRetry("No unsigned deposit chest was found");
      } else if (this.ensureStorageOpen(player, deposit.position())) {
         class_1703 menu = this.currentForeignMenu(player);
         if (menu != null) {
            if (this.actionReady() && AutoWardenInventory.quickMoveFirstPlayerItem(menu, AutoWardenInventory::isValuable)) {
               this.depositedThisCycle++;
               this.phaseActionTick = this.tick;
            } else {
               this.closeOwnedContainer(player);
               this.stats.stored(this.depositedThisCycle);
               this.rotation.onDeposited(this.targetAnarchy, this.depositedThisCycle);
               boolean needsRestock = this.suppliesMissing(player);
               this.workflow.depositFinished(needsRestock, this.autoSell.getValue(), this.tick);
               if (!needsRestock && !this.autoSell.getValue()) {
                  this.recordCycleComplete();
               }

               this.phaseChanged();
            }
         }
      }
   }

   private void tickRestock(class_746 player, class_638 level) {
      if (!this.suppliesMissing(player)) {
         this.closeOwnedContainer(player);
         this.workflow.restockFinished(this.autoSell.getValue(), this.tick);
         if (!this.autoSell.getValue()) {
            this.recordCycleComplete();
         }

         this.phaseChanged();
      } else {
         WardenChestScanner.StorageChest chest = this.scanStorage(level)
            .stream()
            .filter(value -> value.kind() == WardenChestScanner.StorageKind.RESTOCK)
            .findFirst()
            .orElse(null);
         if (chest == null) {
            this.delayStorageRetry("No signed supply chest was found");
         } else if (this.ensureStorageOpen(player, chest.position())) {
            class_1703 menu = this.currentForeignMenu(player);
            if (menu != null && this.actionReady()) {
               if (this.needInvisibility.getValue()
                  && AutoWardenInventory.countInvisibility(player) < this.minInvisibility.getValue().intValue()
                  && AutoWardenInventory.quickMoveFirstContainerItem(menu, AutoWardenInventory::isInvisibilityPotion)) {
                  this.phaseActionTick = this.tick;
               } else if (this.needFood.getValue()
                  && AutoWardenInventory.countFood(player) < this.minFood.getValue().intValue()
                  && AutoWardenInventory.quickMoveFirstContainerItem(menu, AutoWardenInventory::isRestockableFood)) {
                  this.phaseActionTick = this.tick;
               } else if (this.needSpeed.getValue()
                  && AutoWardenInventory.countSpeed(player) < this.minSpeed.getValue().intValue()
                  && AutoWardenInventory.quickMoveFirstContainerItem(menu, AutoWardenInventory::isSpeedPotion)) {
                  this.phaseActionTick = this.tick;
               } else {
                  this.delayStorageRetry("Supply chest does not contain the required stock");
               }
            }
         }
      }
   }

   private void tickSell(class_746 player, class_638 level) {
      WardenChestScanner.StorageChest chest = this.scanStorage(level)
         .stream()
         .filter(value -> value.kind() == WardenChestScanner.StorageKind.SELL)
         .findFirst()
         .orElse(null);
      if (chest == null) {
         this.completeCycle();
      } else if (this.sellStep == 0) {
         if (this.ensureStorageOpen(player, chest.position())) {
            class_1703 menu = this.currentForeignMenu(player);
            if (menu != null && this.actionReady()) {
               int slot = ContainerLootService.findFirst(menu, AutoWardenInventory::isValuable);
               if (slot < 0) {
                  this.closeOwnedContainer(player);
                  this.completeCycle();
               } else {
                  this.sellStack = menu.method_7611(slot).method_7677().method_7972();
                  ContainerLootService.quickMoveFirst(menu, stack -> class_1799.method_31577(stack, this.sellStack));
                  this.phaseActionTick = this.tick;
                  this.sellStep = 1;
               }
            }
         }
      } else if (this.sellStep == 1) {
         this.closeOwnedContainer(player);
         this.sellSourceSlot = InventoryUtil.findPlayerMenuSlot(player, stack -> class_1799.method_31577(stack, this.sellStack));
         if (this.sellSourceSlot < 0) {
            this.resetSellOperation();
         } else {
            this.contract.auction().ifPresent(command -> this.contractAdapter().sendCommand(player, command + " " + itemQuery(this.sellStack)));
            this.phaseActionTick = this.tick;
            this.sellStep = 2;
         }
      } else if (this.sellStep == 2) {
         class_1703 menu = this.currentForeignMenu(player);
         if (menu != null && this.actionReady()) {
            AuctionPriceScanner.competitivePrice(menu, this.sellStack.method_7909(), this.sellStack.method_7964().getString(), this.sellStack.method_7947())
               .ifPresent(value -> this.bestAuctionPrice = this.bestAuctionPrice == 0L ? value : Math.min(this.bestAuctionPrice, value));
            this.sellPagesScanned++;
            if (this.sellPagesScanned < this.sellMaxPages.getValue().intValue()) {
               int nextPage = ContainerLootService.findFirst(
                  menu, stack -> EconomyItemText.containsAny(stack, "next page", "следующая страница", "вперед", "далее")
               );
               if (nextPage >= 0) {
                  InventoryUtil.clickSlot(nextPage, 0, class_1713.field_7790);
                  this.phaseActionTick = this.tick;
                  return;
               }
            }

            long base = this.bestAuctionPrice > 0L ? this.bestAuctionPrice : this.fallbackSellPrice.getValue().longValue();
            this.sellPrice = Math.max(1L, Math.round(base * (1.0 + this.sellMarkup.getValue() / 100.0)));
            this.closeOwnedContainer(player);
            this.selectSellStack(player);
            this.contract.sell(this.sellPrice).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            long invest = Math.round(this.sellPrice * this.investPercent.getValue() / 100.0);
            this.contract.invest(invest).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
            this.phaseActionTick = this.tick;
            this.sellStep = 3;
         }
      } else {
         if (this.phaseActionElapsed(40L)) {
            this.resetSellOperation();
         }
      }
   }

   private void completeCycle() {
      this.workflow.sellFinished(this.tick);
      this.recordCycleComplete();
      this.phaseChanged();
   }

   private void recordCycleComplete() {
      this.stats.cycleComplete();
      this.notifyTelegram("AutoWarden cycle complete: " + this.stats.snapshot(System.currentTimeMillis()).widgetData());
      this.scanner.clearLootedCycle();
      this.cycleLoot = 0;
      this.depositedThisCycle = 0;
   }

   private void selectChest(WardenChestScanner.ChestObservation observation) {
      this.targetObservation = observation;
      this.targetChest = observation.position();
      this.stats.target(observation.position().method_10263() + ", " + observation.position().method_10264() + ", " + observation.position().method_10260());
   }

   private List<WardenChestScanner.StorageChest> scanStorage(class_638 level) {
      class_2338 center = this.storageCenter == null ? mc.field_1724.method_24515() : this.storageCenter;
      return this.scanner.scanStorage(level, center, this.homeScanRadius.getValue().intValue(), this.restockSign.getValue(), this.sellSign.getValue());
   }

   private boolean ensureStorageOpen(class_746 player, class_2338 position) {
      class_1703 menu = this.currentForeignMenu(player);
      if (menu != null && position.equals(this.activeStorageChest)) {
         return true;
      }

      if (player.method_5707(class_243.method_24953(position)) > 16.0) {
         if (!this.navigator.isPathing()) {
            this.pathTo(position, 2);
         }

         return false;
      } else {
         this.cancelNavigation();
         if (this.tick - this.lastContainerOpenTick < 30L) {
            return false;
         }

         this.activeStorageChest = position;
         this.lastContainerOpenTick = this.tick;
         return this.openContainer(player, position);
      }
   }

   private boolean openContainer(class_746 player, class_2338 position) {
      if (mc.field_1761 != null && position != null) {
         class_243 directionVector = player.method_33571().method_1020(class_243.method_24953(position));
         class_2350 direction = class_2350.method_10147((float)directionVector.field_1352, (float)directionVector.field_1351, (float)directionVector.field_1350);
         class_3965 hit = new class_3965(class_243.method_24953(position), direction, position, false);
         mc.field_1761.method_2896(player, class_1268.field_5808, hit);
         player.method_6104(class_1268.field_5808);
         return true;
      } else {
         return false;
      }
   }

   private class_1703 currentForeignMenu(class_746 player) {
      return player != null && player.field_7512 != null && player.field_7512 != player.field_7498 ? player.field_7512 : null;
   }

   private void closeOwnedContainer(class_746 player) {
      if (player != null && this.currentForeignMenu(player) != null) {
         player.method_7346();
      }

      this.activeStorageChest = null;
   }

   private boolean dangerNearby(class_746 player, class_638 level) {
      Optional<class_243> danger = this.nearestDanger(player, level);
      if (danger.isPresent()) {
         this.combatHoldUntilMillis = Math.max(this.combatHoldUntilMillis, System.currentTimeMillis() + 10000L);
         return true;
      } else {
         return PvpStateTracker.INSTANCE.isActive() || System.currentTimeMillis() < this.combatHoldUntilMillis;
      }
   }

   private Optional<class_243> nearestDanger(class_746 player, class_638 level) {
      List<class_243> positions = new ArrayList<>();
      if (this.fleeWarden.getValue()) {
         for (class_7260 warden : level.method_18467(class_7260.class, player.method_5829().method_1014(32.0))) {
            positions.add(warden.method_73189());
         }
      }

      for (class_1657 other : level.method_18456()) {
         if (other != player
            && other.method_5805()
            && !other.method_68878()
            && !other.method_7325()
            && !FriendManager.INSTANCE.isFriend(other.method_7334().name())
            && player.method_5858(other) <= 256.0) {
            positions.add(other.method_73189());
         }
      }

      return positions.stream().min(Comparator.comparingDouble(player.method_73189()::method_1025));
   }

   private boolean suppliesMissing(class_746 player) {
      return this.needInvisibility.getValue() && AutoWardenInventory.countInvisibility(player) < this.minInvisibility.getValue().intValue()
         || this.needFood.getValue() && AutoWardenInventory.countFood(player) < this.minFood.getValue().intValue()
         || this.needSpeed.getValue() && AutoWardenInventory.countSpeed(player) < this.minSpeed.getValue().intValue();
   }

   private void updateSupplies(class_746 player) {
      this.stats.supplies(AutoWardenInventory.countInvisibility(player), AutoWardenInventory.countFood(player), AutoWardenInventory.countSpeed(player));
   }

   private void handleDeath(class_746 player) {
      boolean dead = player.method_29504();
      if (dead && !this.wasDead) {
         this.wasDead = true;
         this.stats.died();
         this.rotation.onDeath(this.targetAnarchy);
         this.notifyTelegram("AutoWarden died on anarchy " + this.targetAnarchy);
         if (this.autoReporter.getValue() && this.lastAttacker != null && this.reportedAttackers.add(this.lastAttacker)) {
            this.contract.report(this.lastAttacker).ifPresent(command -> this.contractAdapter().sendCommand(player, command));
         }
      } else if (!dead) {
         this.wasDead = false;
      }
   }

   private void keepSafeHand(class_746 player) {
      if (!player.method_6115() && this.currentForeignMenu(player) == null) {
         int selected = player.method_31548().method_67532();
         if (!player.method_6047().method_7960()) {
            int safe = AutoWardenInventory.findEmptyHotbarSlot(player, selected);
            if (safe >= 0) {
               player.method_31548().method_61496(safe);
            }
         }
      }
   }

   private void selectSellStack(class_746 player) {
      if (this.sellSourceSlot >= 0) {
         int selected = player.method_31548().method_67532();
         int selectedMenu = 36 + selected;
         if (this.sellSourceSlot >= 36 && this.sellSourceSlot <= 44) {
            player.method_31548().method_61496(this.sellSourceSlot - 36);
         } else if (this.sellSourceSlot != selectedMenu) {
            InventoryUtil.swapWithHotbar(this.sellSourceSlot, selected);
         }
      }
   }

   private void resetSellOperation() {
      this.sellStep = 0;
      this.sellSourceSlot = -1;
      this.sellStack = class_1799.field_8037;
      this.sellPrice = 0L;
      this.bestAuctionPrice = 0L;
      this.sellPagesScanned = 0;
   }

   private static String itemQuery(class_1799 stack) {
      return class_7923.field_41178.method_10221(stack.method_7909()).method_12832();
   }

   private void failCurrentPhase(String reason) {
      this.rotation.onFailedCycle(this.targetAnarchy);
      this.scanner.markFailed(this.targetChest, System.currentTimeMillis());
      this.cancelNavigation();
      this.closeOwnedContainer(mc.field_1724);
      this.resetSellOperation();
      this.workflow.rotateAnarchy(this.tick);
      if (this.workflow.phase() != AutoWardenWorkflow.Phase.ENSURE_ANARCHY) {
         this.workflow.failSafe(reason, this.tick);
      }

      this.phaseChanged();
   }

   private void delayStorageRetry(String reason) {
      this.closeOwnedContainer(mc.field_1724);
      this.storageRetryUntil = this.tick + 1200L;
      this.workflow.failSafe(reason, this.tick);
      ChatUtil.error(reason);
      this.notifyTelegram(reason);
      this.phaseChanged();
   }

   private void pathTo(class_2338 position, int radius) {
      if (!this.navigationActive) {
         this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(NavigationOptions.walking()));
         this.navigationActive = true;
      }

      this.navigator.pathTo(position, radius);
   }

   private void cancelNavigation() {
      if (this.navigationActive) {
         this.navigator.cancel();
      }
   }

   private void endNavigation() {
      if (this.navigationActive) {
         this.navigator.end();
         this.navigationActive = false;
      }
   }

   private void startPhaseAction() {
      this.phaseActionStarted = true;
      this.phaseActionTick = this.tick;
   }

   private boolean phaseActionElapsed(long ticks) {
      return this.phaseActionStarted && this.tick - this.phaseActionTick >= Math.max(0L, ticks);
   }

   private boolean actionReady() {
      return this.tick - this.phaseActionTick >= 4L;
   }

   private long commandSettleTicks() {
      return Math.max(80, Math.round(this.teleportCooldown.getValue().floatValue() * 20.0F));
   }

   private void phaseChanged() {
      this.phaseActionStarted = false;
      this.phaseSubstep = 0;
      this.phaseActionTick = this.tick;
      this.lastContainerOpenTick = -4611686018427387904L;
      this.activeStorageChest = null;
      this.stats.phase(this.workflow.phase(), this.workflow.lastReason());
   }

   private void updateStatsPhase() {
      this.stats.phase(this.workflow.phase(), this.workflow.lastReason());
   }

   private void updateCurrentAnarchy() {
      OptionalInt parsed = AutoWardenParsers.parseAnarchy(ServerUiText.tabHeader(mc));
      parsed.ifPresent(value -> this.currentAnarchy = value);
   }

   private void rememberServer(class_310 client) {
      class_642 server = client == null ? null : client.method_1558();
      if (server != null && !server.method_2994() && !server.method_52811()) {
         this.reconnectServer = server;
      }
   }

   private void scheduleReconnect() {
      this.reconnectAtMillis = System.currentTimeMillis() + Math.round(this.autoReconnectDelay.getValue() * 1000.0);
      this.reconnectAttempted = false;
   }

   private void tickReconnect(class_310 client) {
      if (this.isEnabled()
         && this.autoReconnect.getValue()
         && this.reconnectServer != null
         && this.reconnectAtMillis != 0L
         && !this.reconnectAttempted
         && System.currentTimeMillis() >= this.reconnectAtMillis) {
         this.reconnectAttempted = true;
         class_412.method_36877(new class_442(), client, class_639.method_2950(this.reconnectServer.field_3761), this.reconnectServer, false, null);
      }
   }

   private boolean ensureResources() {
      if (this.resourcesHeld) {
         return true;
      }

      this.resourcesHeld = PveAutomationCoordinator.INSTANCE
         .acquire(
            this,
            AutomationPriority.BOT,
            Set.of(
               AutomationResource.MOVEMENT,
               AutomationResource.ROTATION,
               AutomationResource.INVENTORY,
               AutomationResource.SCREEN,
               AutomationResource.CHAT,
               AutomationResource.NAVIGATION
            )
         );
      return this.resourcesHeld;
   }

   private void connectTelegram() {
      this.ensureTelegram()
         .reloadAndVerify()
         .thenAccept(status -> class_310.method_1551().execute(() -> ChatUtil.info("Telegram: " + status.name().toLowerCase(Locale.ROOT))));
   }

   private void notifyTelegram(String message) {
      if (this.telegramNotifications.getValue()) {
         this.ensureTelegram().send(message);
      }
   }

   private TelegramNotifier ensureTelegram() {
      if (this.telegram == null) {
         this.telegram = new TelegramNotifier(class_310.method_1551().field_1697.toPath());
      }

      return this.telegram;
   }

   private void printScoutRecommendation() {
      if (this.configuredLootAnarchies.isEmpty()) {
         ChatUtil.error("Configure loot anarchies first");
      } else {
         long now = System.currentTimeMillis();
         int best = this.configuredLootAnarchies
            .stream()
            .max(Comparator.comparingDouble(value -> this.rotation.score(value, this.crowdPenalty.getValue(), now)))
            .orElse(-1);
         ChatUtil.info(best < 0 ? "No Warden city is currently available" : "Recommended Warden city: anarchy " + best);
         this.lastScoutTick = this.tick;
      }
   }

   private Optional<String> configuredHomeCommand() {
      return this.contract.home(PveManagerFeature.INSTANCE.resolvedHomeName());
   }

   private ServerAdapter contractAdapter() {
      return ServerAdapters.current();
   }

   private void resetRuntime() {
      this.tick = 0L;
      this.phaseActionTick = 0L;
      this.reconnectAtMillis = 0L;
      this.combatHoldUntilMillis = 0L;
      this.lastContainerOpenTick = -4611686018427387904L;
      this.lastScoutTick = 0L;
      this.storageRetryUntil = 0L;
      this.phaseActionStarted = false;
      this.phaseSubstep = 0;
      this.reconnectAttempted = false;
      this.currentAnarchy = -1;
      this.targetAnarchy = -1;
      this.lastAttacker = null;
      this.cityCenter = null;
      this.targetChest = null;
      this.targetObservation = null;
      this.storageCenter = null;
      this.activeStorageChest = null;
      this.patrolIndex = 0;
      this.cycleLoot = 0;
      this.depositedThisCycle = 0;
      this.wasDead = false;
      this.reportedAttackers.clear();
      this.scanner.clearWorld();
      this.rotation.reset();
      this.workflow.reset(0L);
      this.resetSellOperation();
   }

   private void cleanupRuntime(boolean closeTelegram) {
      this.endNavigation();
      this.closeOwnedContainer(mc.field_1724);
      this.resourcesHeld = false;
      this.workflow.reset(this.tick);
      this.scanner.clearWorld();
      this.resetSellOperation();
      if (closeTelegram && this.telegram != null) {
         this.telegram.close();
         this.telegram = null;
      }
   }

   @Environment(EnvType.CLIENT)
   private record ZoneGizmo(class_238 box) implements class_12175 {
      public void method_75531(class_12177 primitives, float alpha) {
         class_243 a = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1321);
         class_243 b = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1324);
         class_243 c = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1321);
         class_243 d = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1324);
         class_243 e = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1321);
         class_243 f = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1324);
         class_243 g = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1321);
         class_243 h = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1324);
         primitives.method_75473(a, b, -871038977, 1.4F);
         primitives.method_75473(a, c, -871038977, 1.4F);
         primitives.method_75473(a, e, -871038977, 1.4F);
         primitives.method_75473(b, d, -871038977, 1.4F);
         primitives.method_75473(b, f, -871038977, 1.4F);
         primitives.method_75473(c, d, -871038977, 1.4F);
         primitives.method_75473(c, g, -871038977, 1.4F);
         primitives.method_75473(e, f, -871038977, 1.4F);
         primitives.method_75473(e, g, -871038977, 1.4F);
         primitives.method_75473(f, h, -871038977, 1.4F);
         primitives.method_75473(d, h, -871038977, 1.4F);
         primitives.method_75473(g, h, -871038977, 1.4F);
      }
   }
}
