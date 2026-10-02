package org.ryzen.feature.impl.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12180;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1839;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_638;
import net.minecraft.class_642;
import net.minecraft.class_746;
import net.minecraft.class_12180.class_12181;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.event.events.input.MouseInputEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.world.ZoneGizmos;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class ServerHelperFeature extends Feature {
   private static final int ZONE_BASE_COLOR = 8527888;
   private static final int ZONE_THREAT_COLOR = -11141291;
   private static final float ZONE_LINE_WIDTH = 2.0F;
   private static final float ZONE_FILL_ALPHA = 0.16F;
   private static final float RING_FILL_ALPHA = 0.25F;
   private static final double ZONE_CENTER_HEIGHT = 1.5;
   private static final String SERVER_AUTO = "Auto";
   private static final String SERVER_FUN_TIME = "FunTime";
   private static final String SERVER_HOLY_WORLD = "HolyWorld";
   private static final String SERVER_LONY_GRIEF = "LonyGrief";
   private static final String SERVER_REALLY_WORLD = "ReallyWorld";
   public final ModeSetting server = this.register(new ModeSetting("Server", "Auto", "Auto", "FunTime", "HolyWorld", "LonyGrief", "ReallyWorld"));
   public final BooleanSetting itemZones = this.register(new BooleanSetting("Item Zones", true));
   private final ModeSetting zoneColorMode = this.register(ColorMode.setting().visibleWhen(() -> this.itemZones.getValue()));
   public final ColorSetting zoneColor = this.register(
      new ColorSetting("Zone Color", 8527888).visibleWhen(() -> this.itemZones.getValue() && ColorMode.isCustom(this.zoneColorMode))
   );
   public final InputBindSetting ftDisorientation = this.register(this.funTimeBind("Дезориентация"));
   public final InputBindSetting ftSheerDust = this.register(this.funTimeBind("Явная пыль"));
   public final InputBindSetting ftGodsAura = this.register(this.funTimeBind("Божья аура"));
   public final InputBindSetting ftFreezeSnowball = this.register(this.funTimeBind("Снежок заморозка"));
   public final InputBindSetting ftFieryTornado = this.register(this.funTimeBind("Огненный смерч"));
   public final InputBindSetting ftStratum = this.register(this.funTimeBind("Пласт"));
   public final InputBindSetting ftTrap = this.register(this.funTimeBind("Трапка"));
   public final InputBindSetting ftStrengthPotion = this.register(this.funTimeBind("Зелье силы"));
   public final InputBindSetting ftInvisibilityPotion = this.register(this.funTimeBind("Зелье невидимости"));
   public final InputBindSetting ftSpeedPotion = this.register(this.funTimeBind("Зелье скорости"));
   public final InputBindSetting ftLeapingPotion = this.register(this.funTimeBind("Зелье прыгучести"));
   public final InputBindSetting ftRegenerationPotion = this.register(this.funTimeBind("Зелье регенерации"));
   public final InputBindSetting ftNightVisionPotion = this.register(this.funTimeBind("Зелье ночного зрения"));
   public final InputBindSetting ftFireResistancePotion = this.register(this.funTimeBind("Зелье огнестойкости"));
   public final InputBindSetting ftWaterBreathingPotion = this.register(this.funTimeBind("Зелье водного дыхания"));
   public final InputBindSetting ftPopper = this.register(this.funTimeBind("Хлопушка"));
   public final InputBindSetting ftHolyWater = this.register(this.funTimeBind("Святая вода"));
   public final InputBindSetting ftRagePotion = this.register(this.funTimeBind("Зелье Гнева"));
   public final InputBindSetting ftPaladinPotion = this.register(this.funTimeBind("Зелье Палладина"));
   public final InputBindSetting ftAssassinPotion = this.register(this.funTimeBind("Зелье Ассасина"));
   public final InputBindSetting ftRadiationPotion = this.register(this.funTimeBind("Зелье Радиации"));
   public final InputBindSetting ftDrowsinessPotion = this.register(this.funTimeBind("Снотворное"));
   public final InputBindSetting hwWinnerPotion = this.register(this.holyWorldBind("Зелье победителя"));
   public final InputBindSetting hwStrengthPotion = this.register(this.holyWorldBind("Улучшенное зелье силы"));
   public final InputBindSetting hwSpeedPotion = this.register(this.holyWorldBind("Улучшенное зелье скорости"));
   public final InputBindSetting hwStun = this.register(this.holyWorldBind("Стан"));
   public final InputBindSetting hwExplosiveTrap = this.register(this.holyWorldBind("Взрывная трапка"));
   public final InputBindSetting hwTrap = this.register(this.holyWorldBind("Трапка"));
   public final InputBindSetting hwTntCannon = this.register(this.holyWorldBind("Тнт-Пушка"));
   public final InputBindSetting hwDynamite = this.register(this.holyWorldBind("Динамит"));
   public final InputBindSetting hwDynamiteA = this.register(this.holyWorldBind("Динамит A"));
   public final InputBindSetting hwDynamiteB = this.register(this.holyWorldBind("Динамит B"));
   public final InputBindSetting hwC4 = this.register(this.holyWorldBind("C4"));
   public final InputBindSetting hwBlastWave = this.register(this.holyWorldBind("Разрывная волна"));
   public final InputBindSetting hwDynamiteB2 = this.register(this.holyWorldBind("Динамит Б2"));
   public final InputBindSetting hwStealer = this.register(this.holyWorldBind("Стиллер"));
   public final InputBindSetting hwReliableStealer = this.register(this.holyWorldBind("Надёжный стиллер"));
   public final InputBindSetting hwIceWave = this.register(this.holyWorldBind("Ледяная волна"));
   public final InputBindSetting hwSpecialCompass = this.register(this.holyWorldBind("Особый компас"));
   public final InputBindSetting hwEnchantedApple = this.register(this.holyWorldBind("Зачарованное яблоко"));
   public final InputBindSetting hwUniversalKey = this.register(this.holyWorldBind("Универсальный ключ"));
   public final InputBindSetting hwVexEgg = this.register(this.holyWorldBind("Vex Spawn Egg"));
   public final InputBindSetting hwGoldenSpawner = this.register(this.holyWorldBind("Золотой спавнер"));
   public final InputBindSetting hwUniqueClaim = this.register(this.holyWorldBind("Уникальный приват"));
   public final InputBindSetting hwOpenBackpack = this.register(this.holyWorldBind("Открыть рюкзак"));
   private static final List<DonItems.DonItem> BACKPACK_PRIORITY = List.of(
      DonItems.HolyWorld.INFINITY_BACKPACK,
      DonItems.HolyWorld.BACKPACK_IV,
      DonItems.HolyWorld.BACKPACK_III,
      DonItems.HolyWorld.BACKPACK_II,
      DonItems.HolyWorld.BACKPACK_I
   );
   private static final Map<DonItems.DonItem, ServerHelperFeature.Zone> ZONES = Map.ofEntries(
      Map.entry(DonItems.FunTime.TRAP, ServerHelperFeature.Zone.cube(1.99)),
      Map.entry(DonItems.FunTime.STRATUM, ServerHelperFeature.Zone.stratum()),
      Map.entry(DonItems.FunTime.DISORIENTATION, ServerHelperFeature.Zone.ring(10.0)),
      Map.entry(DonItems.FunTime.SHEER_DUST, ServerHelperFeature.Zone.ring(10.0)),
      Map.entry(DonItems.FunTime.FIERY_TORNADO, ServerHelperFeature.Zone.ring(10.0)),
      Map.entry(DonItems.FunTime.FREEZE_SNOWBALL, ServerHelperFeature.Zone.ring(7.0)),
      Map.entry(DonItems.FunTime.GODS_AURA, ServerHelperFeature.Zone.ring(2.0)),
      Map.entry(DonItems.HolyWorld.TRAP, ServerHelperFeature.Zone.cube(1.99)),
      Map.entry(DonItems.HolyWorld.EXPLOSIVE_TRAP, ServerHelperFeature.Zone.cube(3.99)),
      Map.entry(DonItems.HolyWorld.STAN, ServerHelperFeature.Zone.cube(15.01))
   );
   public final InputBindSetting rwGrinchPotion = this.register(this.reallyWorldBind("Зелье Гринча"));
   public final InputBindSetting rwNewYearHorror = this.register(this.reallyWorldBind("Новогодний ужас"));
   public final InputBindSetting rwDarknessEssence = this.register(this.reallyWorldBind("Эссенция кромешника"));
   public final InputBindSetting rwSnowball = this.register(this.reallyWorldBind("Снежок"));
   public final InputBindSetting rwTrap = this.register(this.reallyWorldBind("Ловушка"));
   private final List<ServerHelperFeature.QuickAction> reallyWorldActions = List.of(
      this.action(this.rwGrinchPotion, DonItems.ReallyWorld.GRINCH_POTION),
      this.action(this.rwNewYearHorror, DonItems.ReallyWorld.NEW_YEAR_HORROR),
      this.action(this.rwDarknessEssence, DonItems.ReallyWorld.DARKNESS_ESSENCE),
      this.action(this.rwSnowball, DonItems.ReallyWorld.SNOWBALL),
      this.action(this.rwTrap, DonItems.ReallyWorld.TRAP)
   );
   private final List<ServerHelperFeature.QuickAction> funTimeActions = List.of(
      this.action(this.ftDisorientation, DonItems.FunTime.DISORIENTATION),
      this.action(this.ftSheerDust, DonItems.FunTime.SHEER_DUST),
      this.action(this.ftGodsAura, DonItems.FunTime.GODS_AURA),
      this.action(this.ftFreezeSnowball, DonItems.FunTime.FREEZE_SNOWBALL),
      this.action(this.ftFieryTornado, DonItems.FunTime.FIERY_TORNADO),
      this.action(this.ftStratum, DonItems.FunTime.STRATUM),
      this.action(this.ftTrap, DonItems.FunTime.TRAP),
      this.action(this.ftStrengthPotion, DonItems.FunTime.ENHANCED_STRENGTH_POTION),
      this.action(this.ftInvisibilityPotion, DonItems.FunTime.ENHANCED_INVISIBILITY_POTION),
      this.action(this.ftSpeedPotion, DonItems.FunTime.ENHANCED_SPEED_POTION),
      this.action(this.ftLeapingPotion, DonItems.FunTime.ENHANCED_LEAPING_POTION),
      this.action(this.ftRegenerationPotion, DonItems.FunTime.ENHANCED_REGENERATION_POTION),
      this.action(this.ftNightVisionPotion, DonItems.FunTime.ENHANCED_NIGHT_VISION_POTION),
      this.action(this.ftFireResistancePotion, DonItems.FunTime.ENHANCED_FIRE_RESISTANCE_POTION),
      this.action(this.ftWaterBreathingPotion, DonItems.FunTime.ENHANCED_WATER_BREATHING_POTION),
      this.action(this.ftPopper, DonItems.FunTime.POPPER),
      this.action(this.ftHolyWater, DonItems.FunTime.HOLY_WATER),
      this.action(this.ftRagePotion, DonItems.FunTime.RAGE_POTION),
      this.action(this.ftPaladinPotion, DonItems.FunTime.PALADIN_POTION),
      this.action(this.ftAssassinPotion, DonItems.FunTime.ASSASSIN_POTION),
      this.action(this.ftRadiationPotion, DonItems.FunTime.RADIATION_POTION),
      this.action(this.ftDrowsinessPotion, DonItems.FunTime.DROWSINESS_POTION)
   );
   private final List<ServerHelperFeature.QuickAction> holyWorldActions = List.of(
      this.action(this.hwWinnerPotion, DonItems.HolyWorld.WINNER_POTION),
      this.action(this.hwStrengthPotion, DonItems.HolyWorld.ENHANCED_STRENGTH_POTION),
      this.action(this.hwSpeedPotion, DonItems.HolyWorld.ENHANCED_SPEED_POTION),
      this.action(this.hwStun, DonItems.HolyWorld.STAN),
      this.action(this.hwExplosiveTrap, DonItems.HolyWorld.EXPLOSIVE_TRAP),
      this.action(this.hwTrap, DonItems.HolyWorld.TRAP),
      this.action(this.hwTntCannon, DonItems.HolyWorld.TNT_CANNON),
      this.action(this.hwDynamite, DonItems.HolyWorld.DYNAMITE),
      this.action(this.hwDynamiteA, DonItems.HolyWorld.DYNAMITE_A),
      this.action(this.hwDynamiteB, DonItems.HolyWorld.DYNAMITE_B),
      this.action(this.hwC4, DonItems.HolyWorld.C4),
      this.action(this.hwBlastWave, DonItems.HolyWorld.BLAST_WAVE),
      this.action(this.hwDynamiteB2, DonItems.HolyWorld.DYNAMITE_B2),
      this.action(this.hwStealer, DonItems.HolyWorld.STEALER),
      this.action(this.hwReliableStealer, DonItems.HolyWorld.RELIABLE_STEALER),
      this.action(this.hwIceWave, DonItems.HolyWorld.ICE_WAVE),
      this.action(this.hwSpecialCompass, DonItems.HolyWorld.SPECIAL_COMPASS),
      this.action(this.hwEnchantedApple, DonItems.HolyWorld.ENCHANTED_APPLE),
      this.action(this.hwUniversalKey, DonItems.HolyWorld.UNIVERSAL_KEY),
      this.action(this.hwVexEgg, DonItems.HolyWorld.VEX_SPAWN_EGG),
      this.action(this.hwGoldenSpawner, DonItems.HolyWorld.GOLDEN_SPAWNER),
      this.action(this.hwUniqueClaim, DonItems.HolyWorld.UNIQUE_CLAIM)
   );
   private final Map<DonItems.DonItem, class_1799> quickUseIconCache = new HashMap<>();
   private ServerHelperFeature.PendingAction pendingAction;

   public ServerHelperFeature() {
      super("ServerHelper", "Quick FunTime/HolyWorld DonItems actions", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onDisable() {
      this.pendingAction = null;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1 && this.queueMatching(bind -> bind.matches(event.getKey()))) {
         event.cancel();
      }
   }

   @EventTarget
   public void onMouseInput(MouseInputEvent event) {
      if (event.getAction() == 1 && this.queueMatching(bind -> bind.matchesMouse(event.getButton()))) {
         event.cancel();
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.pendingAction != null && !InventorySwap.isBusy() && !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.INVENTORY)) {
         ServerHelperFeature.PendingAction action = this.pendingAction;
         this.pendingAction = null;
         class_310 client = event.getClient();
         class_746 player = client.field_1724;
         if (player != null && client.field_1761 != null && client.field_1755 == null) {
            ServerHelperFeature.FoundItem found = this.findItem(player, action.candidates);
            if (found == null) {
               ChatUtil.error("Не найдено: " + action.label);
            } else {
               boolean waitForUse = this.requiresHeldUse(found.stack);
               if (found.selected) {
                  InventorySwap.useSelected(waitForUse);
               } else {
                  InventorySwap.useFromSlot(found.containerSlot, waitForUse, true);
               }
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.itemZones.getValue()) {
         class_310 client = event.getClient();
         class_746 player = client.field_1724;
         class_638 level = client.field_1687;
         if (player != null && level != null && client.field_1755 == null && client.field_1769 != null) {
            ServerHelperFeature.Zone zone = this.heldZone(player);
            if (zone != null) {
               class_243 position = Render3DUtil.interpolatedPosition(player, event.getDeltaTracker().method_60637(false));
               int baseColor = ColorUtil.withAlpha(ColorMode.resolve(this.zoneColorMode, this.zoneColor), 255);
               class_12181 ignored = client.field_1769.method_75414();

               try {
                  class_12180.method_75553(this.zoneGizmo(player, level, zone, position, baseColor)).method_75533();
               } catch (Throwable var12) {
                  if (ignored != null) {
                     try {
                        ignored.close();
                     } catch (Throwable var11) {
                        var12.addSuppressed(var11);
                     }
                  }

                  throw var12;
               }

               if (ignored != null) {
                  ignored.close();
               }
            }
         }
      }
   }

   private class_12175 zoneGizmo(class_746 player, class_638 level, ServerHelperFeature.Zone zone, class_243 position, int baseColor) {
      return switch (zone.shape()) {
         case CUBE -> {
            class_238 box = class_238.method_30048(position.method_1031(0.0, 1.5, 0.0), 1.0, 1.0, 1.0).method_1014(zone.size());
            int stroke = this.threatInside(player, level, box) ? -11141291 : baseColor;
            yield ZoneGizmos.cube(box, stroke, ColorUtil.multiplyAlpha(stroke, 0.16F), 2.0F);
         }
         case RING -> {
            int stroke = this.threatWithin(player, level, zone.size()) ? -11141291 : baseColor;
            yield ZoneGizmos.ring(position, zone.size(), stroke, ColorUtil.multiplyAlpha(stroke, 0.25F), 2.0F);
         }
         case STRATUM -> {
            class_2338 blockPosition = player.method_24515();
            yield ZoneGizmos.stratum(
               blockPosition,
               position.method_1020(class_243.method_24954(blockPosition)),
               player.method_36454(),
               player.method_36455(),
               player.method_58149(),
               baseColor,
               ColorUtil.multiplyAlpha(baseColor, 0.16F)
            );
         }
      };
   }

   private ServerHelperFeature.Zone heldZone(class_746 player) {
      ServerHelperFeature.Zone mainHand = this.zoneOf(player.method_6047());
      return mainHand != null ? mainHand : this.zoneOf(player.method_6079());
   }

   private ServerHelperFeature.Zone zoneOf(class_1799 stack) {
      return stack.method_7960() ? null : DonItems.find(stack, this.selectedServer().orElse(DonItems.Server.FUNTIME)).map(ZONES::get).orElse(null);
   }

   private boolean threatInside(class_746 player, class_638 level, class_238 box) {
      for (class_1657 other : level.method_18456()) {
         if (this.isThreat(player, other) && other.method_5829().method_994(box)) {
            return true;
         }
      }

      return false;
   }

   private boolean threatWithin(class_746 player, class_638 level, double radius) {
      for (class_1657 other : level.method_18456()) {
         if (this.isThreat(player, other) && player.method_5739(other) <= radius) {
            return true;
         }
      }

      return false;
   }

   private boolean isThreat(class_746 player, class_1657 other) {
      return other != player && other.method_5805() && !other.method_7325() && !FriendManager.INSTANCE.isFriend(other.method_7334().name());
   }

   public List<ServerHelperFeature.QuickUseEntry> quickUseEntries(class_746 player) {
      if (this.isEnabled() && player != null) {
         DonItems.Server selected = this.selectedServer().orElse(DonItems.Server.FUNTIME);
         Map<DonItems.DonItem, ServerHelperFeature.ItemSummary> inventory = this.indexQuickUseInventory(player, selected);
         List<ServerHelperFeature.QuickUseEntry> entries = new ArrayList<>();

         for (ServerHelperFeature.QuickAction action : this.actionsFor(selected)) {
            if (action.bind.isBound()) {
               entries.add(this.quickUseEntry(action.bind, List.of(action.item), inventory));
            }
         }

         if (selected == DonItems.Server.HOLYWORLD && this.hwOpenBackpack.isBound()) {
            entries.add(this.quickUseEntry(this.hwOpenBackpack, BACKPACK_PRIORITY, inventory));
         }

         return List.copyOf(entries);
      } else {
         return List.of();
      }
   }

   private Map<DonItems.DonItem, ServerHelperFeature.ItemSummary> indexQuickUseInventory(class_746 player, DonItems.Server selected) {
      Map<DonItems.DonItem, ServerHelperFeature.ItemSummary> inventory = new HashMap<>();

      for (int slot = 9; slot < 45; slot++) {
         class_1799 stack = player.field_7498.method_7611(slot).method_7677();
         if (!stack.method_7960()) {
            DonItems.find(stack, selected).ifPresent(item -> {
               ServerHelperFeature.ItemSummary summary = inventory.computeIfAbsent(item, ignored -> new ServerHelperFeature.ItemSummary());
               summary.add(stack);
               class_1799 icon = stack.method_7972();
               icon.method_7939(1);
               this.quickUseIconCache.put(item, icon);
            });
         }
      }

      return inventory;
   }

   private ServerHelperFeature.QuickUseEntry quickUseEntry(
      InputBindSetting bind, List<DonItems.DonItem> candidates, Map<DonItems.DonItem, ServerHelperFeature.ItemSummary> inventory
   ) {
      int count = 0;
      class_1799 icon = class_1799.field_8037;

      for (DonItems.DonItem candidate : candidates) {
         ServerHelperFeature.ItemSummary summary = inventory.get(candidate);
         if (summary != null) {
            count += summary.count;
            if (icon.method_7960()) {
               icon = summary.icon;
            }
         }
      }

      if (icon.method_7960()) {
         for (DonItems.DonItem candidate : candidates) {
            class_1799 cached = this.quickUseIconCache.get(candidate);
            if (cached != null && !cached.method_7960()) {
               icon = cached;
               break;
            }
         }
      }

      if (icon.method_7960() && !candidates.isEmpty()) {
         icon = DonItems.displayStack((DonItems.DonItem)candidates.getFirst());
      }

      return new ServerHelperFeature.QuickUseEntry(icon, count, bind.getDisplayValue());
   }

   private boolean queueMatching(Predicate<InputBindSetting> matcher) {
      class_310 client = class_310.method_1551();
      if (client.field_1724 != null
         && client.field_1761 != null
         && client.field_1755 == null
         && this.pendingAction == null
         && !InventorySwap.isBusy()
         && !PveAutomationCoordinator.INSTANCE.isClaimed(AutomationResource.INVENTORY)) {
         Optional<DonItems.Server> selected = this.selectedServer();
         if (selected.isEmpty()) {
            return false;
         }

         for (ServerHelperFeature.QuickAction action : this.actionsFor(selected.get())) {
            if (matcher.test(action.bind)) {
               this.pendingAction = new ServerHelperFeature.PendingAction(List.of(action.item), action.item.displayName());
               return true;
            }
         }

         if (selected.get() == DonItems.Server.HOLYWORLD && matcher.test(this.hwOpenBackpack)) {
            this.pendingAction = new ServerHelperFeature.PendingAction(BACKPACK_PRIORITY, "рюкзак");
            return true;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private ServerHelperFeature.FoundItem findItem(class_746 player, List<DonItems.DonItem> candidates) {
      int selectedHotbar = player.method_31548().method_67532();

      for (DonItems.DonItem candidate : candidates) {
         class_1799 selected = player.method_6047();
         if (candidate.matches(selected)) {
            return new ServerHelperFeature.FoundItem(-1, selected.method_7972(), true);
         }

         for (int slot = 36; slot < 45; slot++) {
            if (slot - 36 != selectedHotbar) {
               class_1799 stack = player.field_7498.method_7611(slot).method_7677();
               if (candidate.matches(stack)) {
                  return new ServerHelperFeature.FoundItem(slot, stack.method_7972(), false);
               }
            }
         }

         for (int slot = 9; slot < 36; slot++) {
            class_1799 stack = player.field_7498.method_7611(slot).method_7677();
            if (candidate.matches(stack)) {
               return new ServerHelperFeature.FoundItem(slot, stack.method_7972(), false);
            }
         }
      }

      return null;
   }

   private boolean requiresHeldUse(class_1799 stack) {
      class_1839 animation = stack.method_7976();
      return animation == class_1839.field_8950 || animation == class_1839.field_8946;
   }

   private Optional<DonItems.Server> selectedServer() {
      return Optional.of(switch (this.resolvedServer()) {
         case "HolyWorld" -> DonItems.Server.HOLYWORLD;
         case "ReallyWorld" -> DonItems.Server.REALLYWORLD;
         default -> DonItems.Server.FUNTIME;
      });
   }

   private boolean showFunTimeSettings() {
      String resolved = this.resolvedServer();
      return resolved.equals("FunTime") || resolved.equals("LonyGrief");
   }

   private boolean showHolyWorldSettings() {
      return this.resolvedServer().equals("HolyWorld");
   }

   private boolean showReallyWorldSettings() {
      return this.resolvedServer().equals("ReallyWorld");
   }

   public String resolvedServer() {
      String chosen = this.server.getValue();
      if (!"Auto".equals(chosen)) {
         return chosen;
      } else {
         class_642 data = class_310.method_1551().method_1558();
         String address = data != null && data.field_3761 != null ? data.field_3761.toLowerCase(Locale.ROOT) : "";
         String name = data != null && data.field_3752 != null ? data.field_3752.toLowerCase(Locale.ROOT) : "";
         String identity = address + " " + name;
         if (identity.contains("holyworld") || identity.contains("holy-world")) {
            return "HolyWorld";
         } else if (identity.contains("reallyworld") || identity.contains("really-world")) {
            return "ReallyWorld";
         } else {
            return !identity.contains("lonygrief") && !identity.contains("lony-grief") ? "FunTime" : "LonyGrief";
         }
      }
   }

   private InputBindSetting funTimeBind(String item) {
      return new InputBindSetting(item, -1).configKey("funtime." + item).visibleWhen(this::showFunTimeSettings);
   }

   private InputBindSetting holyWorldBind(String item) {
      return new InputBindSetting(item, -1).configKey("holyworld." + item).visibleWhen(this::showHolyWorldSettings);
   }

   private InputBindSetting reallyWorldBind(String item) {
      return new InputBindSetting(item, -1).configKey("reallyworld." + item).visibleWhen(this::showReallyWorldSettings);
   }

   private ServerHelperFeature.QuickAction action(InputBindSetting bind, DonItems.DonItem item) {
      return new ServerHelperFeature.QuickAction(bind, item);
   }

   private List<ServerHelperFeature.QuickAction> actionsFor(DonItems.Server server) {
      return switch (server) {
         case HOLYWORLD -> this.holyWorldActions;
         case REALLYWORLD -> this.reallyWorldActions;
         default -> this.funTimeActions;
      };
   }

   @Environment(EnvType.CLIENT)
   private record FoundItem(int containerSlot, class_1799 stack, boolean selected) {
   }

   @Environment(EnvType.CLIENT)
   private static final class ItemSummary {
      private class_1799 icon = class_1799.field_8037;
      private int count;

      private void add(class_1799 stack) {
         this.count = this.count + stack.method_7947();
         if (this.icon.method_7960()) {
            this.icon = stack.method_7972();
            this.icon.method_7939(1);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record PendingAction(List<DonItems.DonItem> candidates, String label) {
      private PendingAction {
         candidates = List.copyOf(candidates);
      }
   }

   @Environment(EnvType.CLIENT)
   private record QuickAction(InputBindSetting bind, DonItems.DonItem item) {
   }

   @Environment(EnvType.CLIENT)
   public record QuickUseEntry(class_1799 icon, int count, String bindLabel) {
      public QuickUseEntry {
         icon = icon == null ? class_1799.field_8037 : icon.method_7972();
         count = Math.max(0, count);
         bindLabel = bindLabel == null ? "" : bindLabel;
      }

      public class_1799 icon() {
         return this.icon.method_7972();
      }
   }

   @Environment(EnvType.CLIENT)
   private record Zone(ServerHelperFeature.ZoneShape shape, double size) {
      private static ServerHelperFeature.Zone cube(double inflation) {
         return new ServerHelperFeature.Zone(ServerHelperFeature.ZoneShape.CUBE, inflation);
      }

      private static ServerHelperFeature.Zone ring(double radius) {
         return new ServerHelperFeature.Zone(ServerHelperFeature.ZoneShape.RING, radius);
      }

      private static ServerHelperFeature.Zone stratum() {
         return new ServerHelperFeature.Zone(ServerHelperFeature.ZoneShape.STRATUM, 0.0);
      }
   }

   @Environment(EnvType.CLIENT)
   private enum ZoneShape {
      CUBE,
      RING,
      STRATUM;
   }
}
