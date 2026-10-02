package org.ryzen.feature;

import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_465;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.lifecycle.ShutdownEvent;
import org.ryzen.feature.impl.combat.AiTrainingFeature;
import org.ryzen.feature.impl.combat.AimAssistFeature;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.impl.combat.AutoClickerFeature;
import org.ryzen.feature.impl.combat.AutoCrystalFeature;
import org.ryzen.feature.impl.combat.AutoMaceFeature;
import org.ryzen.feature.impl.combat.AutoMinecartFeature;
import org.ryzen.feature.impl.combat.AutoSwapFeature;
import org.ryzen.feature.impl.combat.AutoTotemFeature;
import org.ryzen.feature.impl.combat.AutoTrapFeature;
import org.ryzen.feature.impl.combat.BowAimbotFeature;
import org.ryzen.feature.impl.combat.CombatExtraFeature;
import org.ryzen.feature.impl.combat.CombatMiscFeature;
import org.ryzen.feature.impl.combat.CriticalsFeature;
import org.ryzen.feature.impl.combat.ElytraTargetFeature;
import org.ryzen.feature.impl.combat.HitBoxesFeature;
import org.ryzen.feature.impl.combat.KbDisplacementFeature;
import org.ryzen.feature.impl.combat.NoEntityTraceFeature;
import org.ryzen.feature.impl.combat.NoFriendDamageFeature;
import org.ryzen.feature.impl.combat.OffhandGappleFeature;
import org.ryzen.feature.impl.combat.TargetStrafeFeature;
import org.ryzen.feature.impl.combat.TriggerBotFeature;
import org.ryzen.feature.impl.combat.VelocityFeature;
import org.ryzen.feature.impl.misc.AntiCrashFeature;
import org.ryzen.feature.impl.misc.AuctionHelperFeature;
import org.ryzen.feature.impl.misc.AutoDuelFeature;
import org.ryzen.feature.impl.misc.AutoEnderChestFeature;
import org.ryzen.feature.impl.misc.AutoExchangeFeature;
import org.ryzen.feature.impl.misc.AutoShulkerFeature;
import org.ryzen.feature.impl.misc.AutoTpaAcceptFeature;
import org.ryzen.feature.impl.misc.BotsFeature;
import org.ryzen.feature.impl.misc.ChatHelperFeature;
import org.ryzen.feature.impl.misc.ChestStealerFeature;
import org.ryzen.feature.impl.misc.ClickFriendFeature;
import org.ryzen.feature.impl.misc.CrystalOptimizerFeature;
import org.ryzen.feature.impl.misc.DeathCoordsFeature;
import org.ryzen.feature.impl.misc.EcSaverFeature;
import org.ryzen.feature.impl.misc.FlagDetectorFeature;
import org.ryzen.feature.impl.misc.FunDeliverFeature;
import org.ryzen.feature.impl.misc.KeyFinderFeature;
import org.ryzen.feature.impl.misc.MineAssistantFeature;
import org.ryzen.feature.impl.misc.NameProtectFeature;
import org.ryzen.feature.impl.misc.NoDelaysFeature;
import org.ryzen.feature.impl.misc.ServerHelperFeature;
import org.ryzen.feature.impl.misc.ToggleSoundsFeature;
import org.ryzen.feature.impl.misc.XCarryFeature;
import org.ryzen.feature.impl.misc.autobuy.AutoBuyFeature;
import org.ryzen.feature.impl.misc.collector.CollectorFeature;
import org.ryzen.feature.impl.movement.AirStuckFeature;
import org.ryzen.feature.impl.movement.AutoJumpFeature;
import org.ryzen.feature.impl.movement.BedrockClipFeature;
import org.ryzen.feature.impl.movement.BlinkFeature;
import org.ryzen.feature.impl.movement.DragonFlyFeature;
import org.ryzen.feature.impl.movement.ElytraBoostFeature;
import org.ryzen.feature.impl.movement.ElytraBounceFeature;
import org.ryzen.feature.impl.movement.ElytraJumpFeature;
import org.ryzen.feature.impl.movement.ElytraMotionFeature;
import org.ryzen.feature.impl.movement.ElytraRecastFeature;
import org.ryzen.feature.impl.movement.ElytraTimerFeature;
import org.ryzen.feature.impl.movement.FlightFeature;
import org.ryzen.feature.impl.movement.GrimGlideFeature;
import org.ryzen.feature.impl.movement.InventoryMoveFeature;
import org.ryzen.feature.impl.movement.NoPushFeature;
import org.ryzen.feature.impl.movement.NoSlowFeature;
import org.ryzen.feature.impl.movement.NoWebFeature;
import org.ryzen.feature.impl.movement.ParkourFeature;
import org.ryzen.feature.impl.movement.SafeWalkFeature;
import org.ryzen.feature.impl.movement.ScaffoldFeature;
import org.ryzen.feature.impl.movement.SpeedFeature;
import org.ryzen.feature.impl.movement.SpiderFeature;
import org.ryzen.feature.impl.movement.SprintFeature;
import org.ryzen.feature.impl.movement.SuperFireworkFeature;
import org.ryzen.feature.impl.movement.TimerFeature;
import org.ryzen.feature.impl.movement.TpLootFeature;
import org.ryzen.feature.impl.movement.WallClimbFeature;
import org.ryzen.feature.impl.movement.WaterSpeedFeature;
import org.ryzen.feature.impl.movement.WindHopFeature;
import org.ryzen.feature.impl.player.AimingItemsFeature;
import org.ryzen.feature.impl.player.AutoRespawnFeature;
import org.ryzen.feature.impl.player.AutoToolFeature;
import org.ryzen.feature.impl.player.CaptchaSolverFeature;
import org.ryzen.feature.impl.player.ClickPearlFeature;
import org.ryzen.feature.impl.player.FullBrightFeature;
import org.ryzen.feature.impl.player.LockSlotFeature;
import org.ryzen.feature.impl.player.MultiActionFeature;
import org.ryzen.feature.impl.player.NoInteractFeature;
import org.ryzen.feature.impl.player.NoJumpBoostFeature;
import org.ryzen.feature.impl.player.NoPearlTeleportFeature;
import org.ryzen.feature.impl.player.PearlTargetFeature;
import org.ryzen.feature.impl.player.PearlTrackerFeature;
import org.ryzen.feature.impl.player.SwapWheelFeature;
import org.ryzen.feature.impl.player.UseTrackerFeature;
import org.ryzen.feature.impl.pve.AntiAfkFeature;
import org.ryzen.feature.impl.pve.AppleFarmerFeature;
import org.ryzen.feature.impl.pve.AuctionRelistFeature;
import org.ryzen.feature.impl.pve.AutoArmorFeature;
import org.ryzen.feature.impl.pve.AutoAuthFeature;
import org.ryzen.feature.impl.pve.AutoCrafterFeature;
import org.ryzen.feature.impl.pve.AutoFarmFeature;
import org.ryzen.feature.impl.pve.AutoFishFeature;
import org.ryzen.feature.impl.pve.AutoGappleFeature;
import org.ryzen.feature.impl.pve.AutoLeaveFeature;
import org.ryzen.feature.impl.pve.AutoPotionFeature;
import org.ryzen.feature.impl.pve.AutoTpLootFeature;
import org.ryzen.feature.impl.pve.AutoTradeFeature;
import org.ryzen.feature.impl.pve.AutoUseFeature;
import org.ryzen.feature.impl.pve.BaseFinderFeature;
import org.ryzen.feature.impl.pve.ClanInvestFeature;
import org.ryzen.feature.impl.pve.ClanUpgradeFeature;
import org.ryzen.feature.impl.pve.CreeperFarmFeature;
import org.ryzen.feature.impl.pve.GriefJoinerFeature;
import org.ryzen.feature.impl.pve.MineHelperFeature;
import org.ryzen.feature.impl.pve.NukerFeature;
import org.ryzen.feature.impl.pve.PveManagerFeature;
import org.ryzen.feature.impl.pve.SynchronizationFeature;
import org.ryzen.feature.impl.pve.autowarden.AutoWardenFeature;
import org.ryzen.feature.impl.visual.AncientXrayFeature;
import org.ryzen.feature.impl.visual.ArrowsFeature;
import org.ryzen.feature.impl.visual.BlockOutlineFeature;
import org.ryzen.feature.impl.visual.BoardSpooferFeature;
import org.ryzen.feature.impl.visual.CapeFeature;
import org.ryzen.feature.impl.visual.ChamsFeature;
import org.ryzen.feature.impl.visual.CosmeticsFeature;
import org.ryzen.feature.impl.visual.CrosshairFeature;
import org.ryzen.feature.impl.visual.CubesFeature;
import org.ryzen.feature.impl.visual.EmotionsFeature;
import org.ryzen.feature.impl.visual.EntityEspFeature;
import org.ryzen.feature.impl.visual.FireworkEspFeature;
import org.ryzen.feature.impl.visual.HitParticlesFeature;
import org.ryzen.feature.impl.visual.HoldMyItemsFeature;
import org.ryzen.feature.impl.visual.HudFeature;
import org.ryzen.feature.impl.visual.ItemPhysicsFeature;
import org.ryzen.feature.impl.visual.JumpCirclesFeature;
import org.ryzen.feature.impl.visual.KillEffectFeature;
import org.ryzen.feature.impl.visual.NameTagsFeature;
import org.ryzen.feature.impl.visual.PopChamsFeature;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.ryzen.feature.impl.visual.ShaderHandsFeature;
import org.ryzen.feature.impl.visual.SkeletonEspFeature;
import org.ryzen.feature.impl.visual.SkyShaderFeature;
import org.ryzen.feature.impl.visual.SwingAnimationFeature;
import org.ryzen.feature.impl.visual.TrajectoriesFeature;
import org.ryzen.feature.impl.visual.ViewModelFeature;
import org.ryzen.feature.impl.visual.WardenEspFeature;
import org.ryzen.feature.impl.visual.WorldParticlesFeature;
import org.ryzen.feature.impl.visual.WorldTweaksFeature;
import org.ryzen.feature.setting.BindSetting;

@Environment(EnvType.CLIENT)
public final class FeatureManager {
   public static final FeatureManager INSTANCE = new FeatureManager();
   private static final Comparator<Feature> FEATURE_NAME_ORDER = Comparator.comparing(Feature::getName, String.CASE_INSENSITIVE_ORDER)
      .thenComparing(Feature::getName);
   private final Map<String, Feature> features = new LinkedHashMap<>();
   private final Map<Class<? extends Feature>, Feature> featuresByType = new ConcurrentHashMap<>();
   private List<Feature> sortedFeatures = List.of();
   private final FeatureConfigStore configStore = new FeatureConfigStore();
   private final ScheduledExecutorService configWriter = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "Ryzen Config Writer");
      thread.setDaemon(true);
      return thread;
   });
   private final Object saveLock = new Object();
   private final Map<Feature, Set<Integer>> activeHoldBinds = new IdentityHashMap<>();
   private ScheduledFuture<?> pendingSave;
   private boolean initialized;

   private FeatureManager() {
   }

   public void initialize() {
      if (!this.initialized) {
         List.of(
               new SprintFeature(),
               new AutoJumpFeature(),
               new InventoryMoveFeature(),
               new NoPushFeature(),
               new NoSlowFeature(),
               new DragonFlyFeature(),
               new NoWebFeature(),
               new ElytraBoostFeature(),
               new ElytraJumpFeature(),
               new ElytraBounceFeature(),
               new ElytraMotionFeature(),
               new ElytraTimerFeature(),
               new TpLootFeature(),
               new SafeWalkFeature(),
               new WallClimbFeature(),
               new SpeedFeature(),
               new WaterSpeedFeature(),
               new ParkourFeature(),
               new AirStuckFeature(),
               new BedrockClipFeature(),
               new BlinkFeature(),
               new ElytraRecastFeature(),
               new FlightFeature(),
               new GrimGlideFeature(),
               new SpiderFeature(),
               new SuperFireworkFeature(),
               new TimerFeature(),
               new WindHopFeature(),
               new ScaffoldFeature(),
               new AuraFeature(),
               new AutoCrystalFeature(),
               new CriticalsFeature(),
               new CombatMiscFeature(),
               new CombatExtraFeature(),
               new ElytraTargetFeature(),
               new AiTrainingFeature(),
               new TriggerBotFeature(),
               new AimAssistFeature(),
               new AutoTotemFeature(),
               new AutoClickerFeature(),
               new HitBoxesFeature(),
               new AutoSwapFeature(),
               new VelocityFeature(),
               new BowAimbotFeature(),
               new AutoMaceFeature(),
               new AutoMinecartFeature(),
               new AutoTrapFeature(),
               new KbDisplacementFeature(),
               new NoEntityTraceFeature(),
               new NoFriendDamageFeature(),
               new OffhandGappleFeature(),
               new TargetStrafeFeature(),
               new HudFeature(),
               new ArrowsFeature(),
               new EntityEspFeature(),
               new NameTagsFeature(),
               new HitParticlesFeature(),
               new JumpCirclesFeature(),
               new TrajectoriesFeature(),
               new WorldParticlesFeature(),
               new CubesFeature(),
               new SkeletonEspFeature(),
               new SkyShaderFeature(),
               new PopChamsFeature(),
               new RemovalsFeature(),
               new CrosshairFeature(),
               new SwingAnimationFeature(),
               new ViewModelFeature(),
               new ShaderHandsFeature(),
               new BlockOutlineFeature(),
               new AncientXrayFeature(),
               new CapeFeature(),
               new ChamsFeature(),
               new HoldMyItemsFeature(),
               new ItemPhysicsFeature(),
               new WorldTweaksFeature(),
               new CosmeticsFeature(),
               new EmotionsFeature(),
               new FireworkEspFeature(),
               new KillEffectFeature(),
               new BoardSpooferFeature(),
               new WardenEspFeature(),
               new NameProtectFeature(),
               new NoDelaysFeature(),
               new DeathCoordsFeature(),
               new XCarryFeature(),
               new AuctionHelperFeature(),
               new AutoBuyFeature(),
               new CollectorFeature(),
               new ServerHelperFeature(),
               new AutoTpaAcceptFeature(),
               new AutoDuelFeature(),
               new AutoEnderChestFeature(),
               new BotsFeature(),
               new ChestStealerFeature(),
               new AutoExchangeFeature(),
               new AutoShulkerFeature(),
               new ChatHelperFeature(),
               new ClickFriendFeature(),
               new CrystalOptimizerFeature(),
               new EcSaverFeature(),
               new AntiCrashFeature(),
               new FlagDetectorFeature(),
               new FunDeliverFeature(),
               new KeyFinderFeature(),
               new MineAssistantFeature(),
               new ToggleSoundsFeature(),
               new SwapWheelFeature(),
               new AutoRespawnFeature(),
               new FullBrightFeature(),
               new NoJumpBoostFeature(),
               new NoInteractFeature(),
               new AutoToolFeature(),
               new ClickPearlFeature(),
               new PearlTargetFeature(),
               new MultiActionFeature(),
               new AimingItemsFeature(),
               new LockSlotFeature(),
               new NoPearlTeleportFeature(),
               new PearlTrackerFeature(),
               new CaptchaSolverFeature(),
               new UseTrackerFeature(),
               PveManagerFeature.INSTANCE,
               new AutoFishFeature(),
               new AutoFarmFeature(),
               new AutoArmorFeature(),
               new AutoPotionFeature(),
               new AutoUseFeature(),
               new AutoGappleFeature(),
               new AntiAfkFeature(),
               new AutoLeaveFeature(),
               new AutoAuthFeature(),
               new NukerFeature(),
               new MineHelperFeature(),
               new AutoTpLootFeature(),
               new BaseFinderFeature(),
               new AppleFarmerFeature(),
               new CreeperFarmFeature(),
               new AutoCrafterFeature(),
               new AutoTradeFeature(),
               new AuctionRelistFeature(),
               new ClanInvestFeature(),
               new ClanUpgradeFeature(),
               new GriefJoinerFeature(),
               new SynchronizationFeature(),
               new AutoWardenFeature()
            )
            .forEach(this::register);
         this.configStore.load(this);
         this.initialized = true;
         HudFeature hud = this.getFeature(HudFeature.class);
         if (hud != null) {
            hud.enableByDefaultOnce();
         }

         Runtime.getRuntime().addShutdownHook(new Thread(this::flushPendingSave, "Ryzen Config Flush"));
      }
   }

   public void save() {
      if (this.initialized) {
         JsonObject snapshot = this.configStore.snapshot(this);
         synchronized (this.saveLock) {
            if (this.pendingSave != null) {
               this.pendingSave.cancel(false);
            }

            this.pendingSave = this.configWriter.schedule(() -> this.configStore.save(snapshot), 2L, TimeUnit.SECONDS);
         }
      }
   }

   private void flushPendingSave() {
      synchronized (this.saveLock) {
         if (this.pendingSave == null) {
            return;
         }

         this.pendingSave.cancel(false);
         this.pendingSave = null;
      }

      this.configStore.save(this.configStore.snapshot(this));
   }

   public boolean saveConfigAs(String name) {
      return this.initialized && this.configStore.saveNamed(name, this.configStore.snapshot(this));
   }

   public boolean loadConfig(String name) {
      if (this.initialized && this.configStore.loadNamed(this, name)) {
         this.save();
         return true;
      } else {
         return false;
      }
   }

   public boolean deleteConfig(String name) {
      return this.configStore.deleteNamed(name);
   }

   public List<String> configNames() {
      return this.configStore.listNamed();
   }

   public void register(Feature feature) {
      String key = this.normalize(feature.getName());
      if (this.features.containsKey(key)) {
         throw new IllegalArgumentException("Duplicate feature name: " + feature.getName());
      }

      feature.setStateListener(this::save);
      this.features.put(key, feature);
      this.featuresByType.put((Class<? extends Feature>)feature.getClass(), feature);
      ArrayList<Feature> sorted = new ArrayList<>(this.features.values());
      sorted.sort(FEATURE_NAME_ORDER);
      this.sortedFeatures = List.copyOf(sorted);
   }

   public Collection<Feature> getFeatures() {
      return this.sortedFeatures;
   }

   public List<Feature> getFeatures(FeatureCategory category) {
      List<Feature> result = new ArrayList<>();

      for (Feature feature : this.sortedFeatures) {
         if (feature.getCategory() == category) {
            result.add(feature);
         }
      }

      return result;
   }

   public Feature getFeature(String name) {
      return this.features.get(this.normalize(name));
   }

   public <T extends Feature> T getFeature(Class<T> type) {
      return type.cast(this.featuresByType.get(type));
   }

   public <T extends Feature> T getEnabled(Class<T> type) {
      T feature = this.getFeature(type);
      return feature != null && feature.isEnabled() ? feature : null;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1755 == null || mc.field_1755 instanceof class_465) {
         this.dispatchBind(BindSetting.key(event.getKey()), event.getAction());
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1755 == null || mc.field_1755 instanceof class_465) {
         this.dispatchBind(BindSetting.mouse(event.getButton()), event.getAction());
      }
   }

   @EventTarget
   public void onShutdown(ShutdownEvent event) {
      synchronized (this.saveLock) {
         if (this.pendingSave != null) {
            this.pendingSave.cancel(false);
            this.pendingSave = null;
         }
      }

      this.configStore.save(this.configStore.snapshot(this));
      this.configWriter.shutdown();
      this.initialized = false;
   }

   private String normalize(String value) {
      return value.toLowerCase(Locale.ROOT);
   }

   private void dispatchBind(int bindCode, int action) {
      if (action == 1 || action == 0) {
         for (Feature feature : this.features.values()) {
            if (feature.supportsBinds() && feature.isToggleable() && feature.getBind().matchesCode(bindCode)) {
               this.handleBindInput(feature, bindCode, action);
            }
         }
      }
   }

   private void handleBindInput(Feature feature, int bindCode, int action) {
      if (feature.getBindModeForCode(bindCode) == BindMode.HOLD) {
         Set<Integer> activeCodes = this.activeHoldBinds.computeIfAbsent(feature, ignored -> new LinkedHashSet<>());
         if (action == 1) {
            if (activeCodes.add(bindCode)) {
               feature.setEnabled(true);
            }
         } else {
            activeCodes.remove(bindCode);
            if (activeCodes.isEmpty()) {
               this.activeHoldBinds.remove(feature);
               feature.setEnabled(false);
            }
         }
      } else {
         if (action == 1) {
            feature.toggle();
         }
      }
   }
}
