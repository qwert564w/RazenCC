package org.ryzen.feature.impl.pve;

import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1887;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2625;
import net.minecraft.class_2680;
import net.minecraft.class_3965;
import net.minecraft.class_5321;
import net.minecraft.class_638;
import net.minecraft.class_6880;
import net.minecraft.class_746;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.navigation.BaritoneNavigator;
import org.ryzen.pve.navigation.NavigationOptions;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class AppleFarmerFeature extends PveFeature implements MinecraftContext {
   private static final int OFFHAND_SWAP_BUTTON = 40;
   private static final int CRAFT_RESULT_SLOT = 0;
   private static final int CRAFT_INPUT_SLOT = 1;
   private static final int HOME_SETTLE_TICKS = 60;
   private static final int ACTION_INTERVAL_TICKS = 4;
   private static final int STORAGE_RETRY_TICKS = 1200;
   private static final double INTERACT_DISTANCE_SQUARED = 20.25;
   public final BooleanSetting takeBones = this.register(new BooleanSetting("Take Bones", false));
   public final BooleanSetting dropJunk = this.register(new BooleanSetting("Drop Junk", false));
   public final TextSetting farmPosition = this.register(new TextSetting("Farm Position", "auto"));
   public final TextSetting appleStorage = this.register(new TextSetting("Apple Storage", "auto"));
   public final TextSetting boneStorage = this.register(new TextSetting("Bone Storage", "auto"));
   public final BooleanSetting useHomeCommand = this.register(new BooleanSetting("Use Home Command", false));
   public final NumberSetting farmRadius = this.register(new NumberSetting("Farm Radius", 8.0, 3.0, 24.0, 1.0, " blocks"));
   public final NumberSetting verticalScan = this.register(new NumberSetting("Vertical Scan", 10.0, 3.0, 24.0, 1.0, " blocks"));
   public final NumberSetting treesPerCycle = this.register(new NumberSetting("Trees Per Cycle", 4.0, 1.0, 32.0, 1.0, ""));
   public final NumberSetting maxBlocksPerCycle = this.register(new NumberSetting("Max Blocks Per Cycle", 192.0, 16.0, 1024.0, 16.0, ""));
   public final NumberSetting boneMealPerCycle = this.register(new NumberSetting("Bone Meal Per Cycle", 48.0, 1.0, 256.0, 1.0, ""));
   public final NumberSetting boneMealReserve = this.register(new NumberSetting("Bone Meal Reserve", 16.0, 0.0, 256.0, 1.0, ""));
   public final NumberSetting saplingReserve = this.register(new NumberSetting("Sapling Reserve", 16.0, 1.0, 128.0, 1.0, ""));
   public final NumberSetting depositAppleStacks = this.register(new NumberSetting("Deposit Apple Stacks", 4.0, 1.0, 27.0, 1.0, " stacks"));
   public final NumberSetting repairBelow = this.register(new NumberSetting("Repair Below", 20.0, 1.0, 95.0, 1.0, "%"));
   public final NumberSetting repairTo = this.register(new NumberSetting("Repair To", 90.0, 5.0, 100.0, 1.0, "%"));
   public final BooleanSetting buyXpBottles = this.register(new BooleanSetting("Buy XP Bottles", false));
   public final TextSetting xpBottleBuyCommand = this.register(new TextSetting("XP Bottle Buy Command", ""));
   private final PveStateMachine<AppleFarmerFeature.Phase> state = new PveStateMachine<>(AppleFarmerFeature.Phase.RETURN_HOME);
   private final BaritoneNavigator navigator = BaritoneNavigator.INSTANCE;
   private long tick;
   private long lastActionTick;
   private long lastNavigationTick;
   private long skipAppleStorageUntil;
   private long skipBoneStorageUntil;
   private class_2338 farmCenter;
   private class_2338 appleChest;
   private class_2338 boneChest;
   private class_2338 activeWorkPos;
   private boolean navigationActive;
   private boolean miningIssued;
   private boolean homeCommandSent;
   private boolean buyCommandSent;
   private boolean openedContainer;
   private boolean storageOpenRequested;
   private int plantedThisCycle;
   private int boneMealUsedThisCycle;
   private int blocksBrokenThisCycle;
   private int completedCycles;
   private int craftStep;
   private int craftSourceSlot = -1;
   private long craftStepTick;
   private int repairToolSourceSlot = -1;
   private float savedYaw;
   private float savedPitch;
   private boolean rotationSaved;

   public AppleFarmerFeature() {
      super(
         "AppleFarmer",
         "Plants, grows and harvests apple trees with storage and repair cycles",
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
   protected void onPveEnable() {
      this.tick = 0L;
      this.lastActionTick = -4611686018427387904L;
      this.lastNavigationTick = -4611686018427387904L;
      this.skipAppleStorageUntil = 0L;
      this.skipBoneStorageUntil = 0L;
      this.farmCenter = null;
      this.appleChest = null;
      this.boneChest = null;
      this.activeWorkPos = null;
      this.navigationActive = false;
      this.miningIssued = false;
      this.homeCommandSent = false;
      this.buyCommandSent = false;
      this.openedContainer = false;
      this.storageOpenRequested = false;
      this.plantedThisCycle = 0;
      this.boneMealUsedThisCycle = 0;
      this.blocksBrokenThisCycle = 0;
      this.completedCycles = 0;
      this.craftStep = 0;
      this.craftSourceSlot = -1;
      this.repairToolSourceSlot = -1;
      this.rotationSaved = false;
      this.state.reset(0L);
      this.beginNavigation(NavigationOptions.walking());
   }

   @Override
   protected void onPveDisable() {
      class_746 player = mc.field_1724;
      this.restoreCraftingInventory(player);
      this.restoreRepairTool(player);
      this.closeFeatureContainer(player);
      this.restoreRotation(player);
      this.endNavigation();
      this.activeWorkPos = null;
      this.miningIssued = false;
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
         switch ((AppleFarmerFeature.Phase)this.state.state()) {
            case RETURN_HOME:
               this.tickReturnHome(player);
               break;
            case FIND_FARM:
               this.tickFindFarm(player);
               break;
            case APPROACH_FARM:
               this.tickApproachFarm(player);
               break;
            case PLANT:
               this.tickPlant(player, level);
               break;
            case GROW:
               this.tickGrow(player, level);
               break;
            case BREAK_LEAVES:
               this.tickBreakBlocks(player, level, true);
               break;
            case BREAK_LOGS:
               this.tickBreakBlocks(player, level, false);
               break;
            case DROP_JUNK:
               this.tickDropJunk(player);
               break;
            case DEPOSIT_APPLES_FIND:
               this.tickFindStorage(player, level, AppleFarmerFeature.StorageKind.APPLES);
               break;
            case DEPOSIT_APPLES_MOVE:
               this.tickMoveToStorage(player, this.appleChest, AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN);
               break;
            case DEPOSIT_APPLES_OPEN:
               this.tickDepositApples(player);
               break;
            case BONES_FIND:
               this.tickFindStorage(player, level, AppleFarmerFeature.StorageKind.BONES);
               break;
            case BONES_MOVE:
               this.tickMoveToStorage(player, this.boneChest, AppleFarmerFeature.Phase.BONES_OPEN);
               break;
            case BONES_OPEN:
               this.tickTakeBones(player);
               break;
            case CRAFT_BONE_MEAL:
               this.tickCraftBoneMeal(player);
               break;
            case BUY_BOTTLES:
               this.tickBuyBottles(player);
               break;
            case REPAIR:
               this.tickRepair(player);
         }
      }
   }

   public AppleFarmerFeature.Phase getPhase() {
      return this.state.state();
   }

   public int getCompletedCycles() {
      return this.completedCycles;
   }

   private void tickReturnHome(class_746 player) {
      if (!this.useHomeCommand.getValue()) {
         this.transition(AppleFarmerFeature.Phase.FIND_FARM);
      } else {
         if (!this.homeCommandSent) {
            ServerAdapter adapter = ServerAdapters.current();
            Optional<String> command = adapter.homeCommand(PveManagerFeature.INSTANCE.resolvedHomeName());
            if (command.isEmpty()) {
               this.transition(AppleFarmerFeature.Phase.FIND_FARM);
               return;
            }

            adapter.sendCommand(player, command.get());
            this.homeCommandSent = true;
            this.lastActionTick = this.tick;
         }

         if (this.state.ticksInState(this.tick) >= 60L) {
            if (PveCoordinateParser.parse(this.farmPosition.getValue()).isEmpty()) {
               this.farmCenter = player.method_24515().method_10062();
            }

            this.transition(AppleFarmerFeature.Phase.FIND_FARM);
         }
      }
   }

   private void tickFindFarm(class_746 player) {
      this.farmCenter = PveCoordinateParser.parse(this.farmPosition.getValue())
         .orElseGet(() -> this.farmCenter == null ? player.method_24515().method_10062() : this.farmCenter);
      AppleFarmerFeature.FarmerSignals signals = signals(
         at(player, this.farmCenter, 3.0), false, false, false, false, false, false, false, false, false, false, false, false, false
      );
      this.transition(nextPhase(AppleFarmerFeature.Phase.FIND_FARM, signals));
   }

   private void tickApproachFarm(class_746 player) {
      if (this.farmCenter == null) {
         this.transition(AppleFarmerFeature.Phase.FIND_FARM);
      } else {
         boolean atFarm = at(player, this.farmCenter, 3.0);
         boolean timedOut = this.state.ticksInState(this.tick) > 1200L;
         if (!atFarm && !timedOut) {
            this.navigateTo(this.farmCenter, 2, NavigationOptions.walking());
         }

         this.transition(
            nextPhase(
               AppleFarmerFeature.Phase.APPROACH_FARM,
               signals(atFarm, false, false, false, false, false, false, false, false, false, false, false, false, timedOut)
            )
         );
      }
   }

   private void tickPlant(class_746 player, class_638 level) {
      if (this.farmCenter == null) {
         this.transition(AppleFarmerFeature.Phase.FIND_FARM);
      } else {
         int limit = intValue(this.treesPerCycle);
         if (this.plantedThisCycle >= limit) {
            this.transition(AppleFarmerFeature.Phase.GROW);
         } else {
            class_2338 target = this.findPlantingTarget(level);
            boolean hasSapling = countItem(player, class_1802.field_17535) > 0;
            boolean timedOut = this.state.ticksInState(this.tick) > 600L;
            AppleFarmerFeature.Phase next = nextPhase(
               AppleFarmerFeature.Phase.PLANT,
               signals(true, target != null, hasSapling, false, false, false, false, false, false, false, false, false, false, timedOut)
            );
            if (next != AppleFarmerFeature.Phase.PLANT) {
               this.transition(next);
            } else if (!at(player, target, 4.0)) {
               this.navigateTo(target, 2, NavigationOptions.walking());
            } else if (this.actionReady() && this.selectHotbarItem(player, class_1802.field_17535)) {
               this.cancelNavigation();
               this.lookAt(player, class_243.method_24953(target.method_10084()));
               class_3965 hit = new class_3965(class_243.method_24953(target).method_1031(0.0, 0.5, 0.0), class_2350.field_11036, target, false);
               mc.field_1761.method_2896(player, class_1268.field_5808, hit);
               player.method_6104(class_1268.field_5808);
               this.lastActionTick = this.tick;
               this.plantedThisCycle++;
            }
         }
      }
   }

   private void tickGrow(class_746 player, class_638 level) {
      if (this.farmCenter == null) {
         this.transition(AppleFarmerFeature.Phase.FIND_FARM);
      } else {
         class_2338 sapling = this.findNearestBlock(level, player, true, class_2246.field_10394);
         boolean hasBoneMeal = countItem(player, class_1802.field_8324) > 0;
         boolean underLimit = this.boneMealUsedThisCycle < intValue(this.boneMealPerCycle);
         boolean timedOut = this.state.ticksInState(this.tick) > 900L;
         AppleFarmerFeature.Phase next = nextPhase(
            AppleFarmerFeature.Phase.GROW,
            signals(true, sapling != null && underLimit, hasBoneMeal, false, false, false, false, false, false, false, false, false, false, timedOut)
         );
         if (next != AppleFarmerFeature.Phase.GROW) {
            this.transition(next);
         } else if (!at(player, sapling, 4.0)) {
            this.navigateTo(sapling, 2, NavigationOptions.walking());
         } else if (this.actionReady() && this.selectHotbarItem(player, class_1802.field_8324)) {
            this.cancelNavigation();
            this.lookAt(player, class_243.method_24953(sapling));
            class_3965 hit = new class_3965(class_243.method_24953(sapling), class_2350.field_11036, sapling, false);
            mc.field_1761.method_2896(player, class_1268.field_5808, hit);
            player.method_6104(class_1268.field_5808);
            this.lastActionTick = this.tick;
            this.boneMealUsedThisCycle++;
         }
      }
   }

   private void tickBreakBlocks(class_746 player, class_638 level, boolean leaves) {
      class_2248[] targets = leaves ? new class_2248[]{class_2246.field_10503} : new class_2248[]{class_2246.field_10431, class_2246.field_10126};
      if (this.blocksBrokenThisCycle >= intValue(this.maxBlocksPerCycle)) {
         this.transition(leaves ? AppleFarmerFeature.Phase.BREAK_LOGS : AppleFarmerFeature.Phase.DROP_JUNK);
      } else {
         if (this.activeWorkPos != null && !matchesAny(level.method_8320(this.activeWorkPos), targets)) {
            this.blocksBrokenThisCycle++;
            this.activeWorkPos = null;
            this.miningIssued = false;
            this.lastActionTick = this.tick;
         }

         if (this.activeWorkPos == null) {
            this.activeWorkPos = this.findNearestBlock(level, player, false, targets);
         }

         boolean timedOut = this.state.ticksInState(this.tick) > 1800L;
         AppleFarmerFeature.Phase current = leaves ? AppleFarmerFeature.Phase.BREAK_LEAVES : AppleFarmerFeature.Phase.BREAK_LOGS;
         AppleFarmerFeature.Phase next = nextPhase(
            current, signals(true, this.activeWorkPos != null, true, false, false, false, false, false, false, false, false, false, false, timedOut)
         );
         if (next != current) {
            this.transition(next);
         } else {
            if (!this.miningIssued || !this.navigator.isPathing() && this.tick - this.lastNavigationTick >= 40L) {
               if (!this.beginNavigation(NavigationOptions.mining())) {
                  this.transition(leaves ? AppleFarmerFeature.Phase.BREAK_LOGS : AppleFarmerFeature.Phase.DROP_JUNK);
                  return;
               }

               try {
                  this.navigator.mine(0, level.method_8320(this.activeWorkPos).method_26204());
                  this.miningIssued = true;
                  this.lastNavigationTick = this.tick;
               } catch (RuntimeException | LinkageError ignored) {
                  this.miningIssued = false;
               }
            }
         }
      }
   }

   private void tickDropJunk(class_746 player) {
      int junkSlot = this.dropJunk.getValue() ? this.findJunkSlot(player) : -1;
      boolean timedOut = this.state.ticksInState(this.tick) > 400L;
      if (junkSlot >= 0 && !timedOut) {
         if (this.actionReady()) {
            class_1799 stack = player.field_7498.method_7611(junkSlot).method_7677();
            boolean oneSapling = stack.method_31574(class_1802.field_17535);
            this.clickPlayerMenu(player, junkSlot, oneSapling ? 0 : 1, class_1713.field_7795);
            this.lastActionTick = this.tick;
         }
      } else {
         this.transition(nextPhase(AppleFarmerFeature.Phase.DROP_JUNK, this.maintenanceSignals(player, false, false, timedOut)));
      }
   }

   private void tickFindStorage(class_746 player, class_638 level, AppleFarmerFeature.StorageKind kind) {
      class_2338 found = this.resolveStorage(player, level, kind);
      if (kind == AppleFarmerFeature.StorageKind.APPLES) {
         this.appleChest = found;
      } else {
         this.boneChest = found;
      }

      boolean timedOut = this.state.ticksInState(this.tick) > 200L;
      AppleFarmerFeature.Phase current = kind == AppleFarmerFeature.StorageKind.APPLES
         ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_FIND
         : AppleFarmerFeature.Phase.BONES_FIND;
      if (found != null) {
         this.transition(kind == AppleFarmerFeature.StorageKind.APPLES ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_MOVE : AppleFarmerFeature.Phase.BONES_MOVE);
      } else if (timedOut) {
         if (kind == AppleFarmerFeature.StorageKind.APPLES) {
            this.skipAppleStorageUntil = this.tick + 1200L;
            this.transition(nextPhase(current, this.maintenanceSignals(player, true, false, true)));
         } else {
            this.skipBoneStorageUntil = this.tick + 1200L;
            this.transition(nextPhase(current, this.maintenanceSignals(player, false, true, true)));
         }
      }
   }

   private void tickMoveToStorage(class_746 player, class_2338 target, AppleFarmerFeature.Phase openPhase) {
      AppleFarmerFeature.Phase current = openPhase == AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN
         ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_MOVE
         : AppleFarmerFeature.Phase.BONES_MOVE;
      if (target == null) {
         this.transition(
            openPhase == AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_FIND : AppleFarmerFeature.Phase.BONES_FIND
         );
      } else {
         boolean atDestination = at(player, target, 4.0);
         boolean timedOut = this.state.ticksInState(this.tick) > 800L;
         if (!atDestination && !timedOut) {
            this.navigateTo(target, 2, NavigationOptions.walking());
         } else if (timedOut) {
            if (current == AppleFarmerFeature.Phase.DEPOSIT_APPLES_MOVE) {
               this.skipAppleStorageUntil = this.tick + 1200L;
               this.transition(nextPhase(current, this.maintenanceSignals(player, true, false, true)));
            } else {
               this.skipBoneStorageUntil = this.tick + 1200L;
               this.transition(nextPhase(current, this.maintenanceSignals(player, false, true, true)));
            }
         } else {
            this.transition(openPhase);
         }
      }
   }

   private void tickDepositApples(class_746 player) {
      class_1703 menu = this.openStorageMenu(player, this.appleChest);
      if (menu == null) {
         if (this.state.ticksInState(this.tick) > 200L) {
            this.skipAppleStorageUntil = this.tick + 1200L;
            this.transition(nextPhase(AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN, this.maintenanceSignals(player, true, false, true)));
         }
      } else if (this.state.ticksInState(this.tick) > 400L) {
         this.skipAppleStorageUntil = this.tick + 1200L;
         this.closeFeatureContainer(player);
         this.transition(nextPhase(AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN, this.maintenanceSignals(player, true, false, true)));
      } else {
         int slot = findPlayerContainerSlot(menu, stack -> stack.method_31574(class_1802.field_8279));
         if (slot >= 0 && this.actionReady()) {
            InventoryUtil.quickMoveSlot(slot);
            this.lastActionTick = this.tick;
         } else if (slot < 0) {
            this.closeFeatureContainer(player);
            this.transition(nextPhase(AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN, this.maintenanceSignals(player, true, false, false)));
         }
      }
   }

   private void tickTakeBones(class_746 player) {
      class_1703 menu = this.openStorageMenu(player, this.boneChest);
      if (menu == null) {
         if (this.state.ticksInState(this.tick) > 200L) {
            this.skipBoneStorageUntil = this.tick + 1200L;
            this.transition(AppleFarmerFeature.Phase.CRAFT_BONE_MEAL);
         }
      } else if (this.state.ticksInState(this.tick) > 400L) {
         this.skipBoneStorageUntil = this.tick + 1200L;
         this.closeFeatureContainer(player);
         this.transition(AppleFarmerFeature.Phase.CRAFT_BONE_MEAL);
      } else {
         int targetBones = Math.max(1, intValue(this.boneMealReserve) / 3);
         int boneCount = countItem(player, class_1802.field_8606);
         int slot = ContainerLootService.findFirst(menu, stack -> stack.method_31574(class_1802.field_8606));
         if (slot >= 0 && boneCount < targetBones && this.actionReady()) {
            ContainerLootService.quickMoveFirst(menu, stack -> stack.method_31574(class_1802.field_8606));
            this.lastActionTick = this.tick;
         } else {
            if (slot < 0 && boneCount < targetBones) {
               this.skipBoneStorageUntil = this.tick + 1200L;
            }

            this.closeFeatureContainer(player);
            this.transition(AppleFarmerFeature.Phase.CRAFT_BONE_MEAL);
         }
      }
   }

   private void tickCraftBoneMeal(class_746 player) {
      if (this.state.ticksInState(this.tick) > 400L) {
         this.restoreCraftingInventory(player);
         this.skipBoneStorageUntil = this.tick + 1200L;
         this.transition(AppleFarmerFeature.Phase.PLANT);
      } else {
         int reserve = intValue(this.boneMealReserve);
         if (countItem(player, class_1802.field_8324) >= reserve || countItem(player, class_1802.field_8606) <= 0) {
            this.restoreCraftingInventory(player);
            this.transition(nextPhase(AppleFarmerFeature.Phase.CRAFT_BONE_MEAL, this.maintenanceSignals(player, false, false, false)));
         } else if (player.field_7512 == player.field_7498 && !this.openedContainer) {
            switch (this.craftStep) {
               case 0:
                  if (!player.field_7498.method_34255().method_7960()) {
                     this.restoreCraftingInventory(player);
                     return;
                  }

                  this.craftSourceSlot = findPlayerSlot(player, class_1802.field_8606);
                  if (this.craftSourceSlot < 0) {
                     this.transition(nextPhase(AppleFarmerFeature.Phase.CRAFT_BONE_MEAL, this.maintenanceSignals(player, false, true, false)));
                     return;
                  }

                  this.clickPlayerMenu(player, this.craftSourceSlot, 0, class_1713.field_7790);
                  this.clickPlayerMenu(player, 1, 0, class_1713.field_7790);
                  this.craftStep = 1;
                  this.craftStepTick = this.tick;
                  break;
               case 1:
                  class_1799 result = player.field_7498.method_7611(0).method_7677();
                  if (result.method_31574(class_1802.field_8324)) {
                     this.clickPlayerMenu(player, 0, 0, class_1713.field_7794);
                     this.craftStep = 2;
                     this.craftStepTick = this.tick;
                  } else if (this.tick - this.craftStepTick > 20L) {
                     this.craftStep = 2;
                     this.craftStepTick = this.tick;
                  }
                  break;
               case 2:
                  if (this.tick - this.craftStepTick < 2L) {
                     return;
                  }

                  this.restoreCraftingInventory(player);
                  this.craftStep = 0;
                  this.craftSourceSlot = -1;
                  break;
               default:
                  this.restoreCraftingInventory(player);
                  this.craftStep = 0;
            }
         } else {
            this.closeFeatureContainer(player);
         }
      }
   }

   private void tickBuyBottles(class_746 player) {
      if (countItem(player, class_1802.field_8287) > 0) {
         this.transition(AppleFarmerFeature.Phase.REPAIR);
      } else {
         String command = this.xpBottleBuyCommand.getValue().trim();
         if (this.buyXpBottles.getValue() && !command.isEmpty()) {
            if (!this.buyCommandSent) {
               ServerAdapters.current().sendCommand(player, command);
               this.buyCommandSent = true;
               this.lastActionTick = this.tick;
            }

            if (player.field_7512 != player.field_7498) {
               this.openedContainer = true;
            }

            if (this.state.ticksInState(this.tick) > 160L) {
               this.closeFeatureContainer(player);
               this.transition(countItem(player, class_1802.field_8287) > 0 ? AppleFarmerFeature.Phase.REPAIR : AppleFarmerFeature.Phase.PLANT);
            }
         } else {
            this.transition(AppleFarmerFeature.Phase.PLANT);
         }
      }
   }

   private void tickRepair(class_746 player) {
      if (this.repairToolSourceSlot < 0) {
         int toolSlot = findRepairToolSlot(player, this.repairBelow.getValue());
         if (toolSlot < 0) {
            this.transition(AppleFarmerFeature.Phase.PLANT);
         } else {
            this.saveRotation(player);
            this.swapWithOffhand(player, toolSlot);
            this.repairToolSourceSlot = toolSlot;
            this.lastActionTick = this.tick;
         }
      } else {
         class_1799 tool = player.method_6079();
         boolean repaired = !needsRepair(tool, this.repairTo.getValue());
         int bottleSlot = findPlayerSlot(player, class_1802.field_8287);
         boolean timedOut = this.state.ticksInState(this.tick) > 800L;
         if (repaired || bottleSlot < 0 || timedOut) {
            this.restoreRepairTool(player);
            this.transition(AppleFarmerFeature.Phase.PLANT);
         } else if (this.actionReady()) {
            this.lookAt(player, player.method_73189().method_1031(0.0, -1.0, 0.0));
            this.useFromPlayerSlot(player, bottleSlot);
            this.lastActionTick = this.tick;
         }
      }
   }

   private class_1703 openStorageMenu(class_746 player, class_2338 storage) {
      if ((this.storageOpenRequested || this.openedContainer) && InventoryUtil.isContainerScreenOpen()) {
         class_1703 menu = InventoryUtil.getOpenMenu();
         if (menu != null && menu != player.field_7498) {
            this.openedContainer = true;
            return menu;
         }
      }

      if (storage != null && at(player, storage, 4.0) && this.actionReady()) {
         this.cancelNavigation();
         this.lookAt(player, class_243.method_24953(storage));
         class_3965 hit = new class_3965(class_243.method_24953(storage), class_2350.field_11036, storage, false);
         mc.field_1761.method_2896(player, class_1268.field_5808, hit);
         player.method_6104(class_1268.field_5808);
         this.storageOpenRequested = true;
         this.lastActionTick = this.tick;
         return null;
      } else {
         return null;
      }
   }

   private void closeFeatureContainer(class_746 player) {
      if (this.openedContainer && player != null && player.field_7512 != player.field_7498) {
         player.method_7346();
      }

      this.openedContainer = false;
      this.storageOpenRequested = false;
   }

   private class_2338 resolveStorage(class_746 player, class_638 level, AppleFarmerFeature.StorageKind kind) {
      String setting = kind == AppleFarmerFeature.StorageKind.APPLES ? this.appleStorage.getValue() : this.boneStorage.getValue();
      Optional<class_2338> configured = PveCoordinateParser.parse(setting);
      if (configured.isPresent()) {
         return configured.get();
      }

      class_2338 origin = this.farmCenter == null ? player.method_24515() : this.farmCenter;
      class_2338 signed = this.findSignedChest(level, origin, kind);
      return signed != null ? signed : this.findNearestChest(level, player, origin);
   }

   private class_2338 findSignedChest(class_638 level, class_2338 origin, AppleFarmerFeature.StorageKind kind) {
      int radius = Math.min(16, intValue(this.farmRadius) + 4);
      int yRadius = Math.min(8, intValue(this.verticalScan));

      for (class_2338 cursor : class_2338.method_10097(origin.method_10069(-radius, -yRadius, -radius), origin.method_10069(radius, yRadius, radius))) {
         if (level.method_8321(cursor) instanceof class_2625 sign) {
            List<String> lines = new ArrayList<>(4);

            for (int line = 0; line < 4; line++) {
               lines.add(sign.method_49853().method_49859(line, false).getString());
               lines.add(sign.method_49854().method_49859(line, false).getString());
            }

            if (matchesStorageLabel(lines, kind)) {
               class_2338 chest = findChestNear(level, cursor, 2);
               if (chest != null) {
                  return chest;
               }
            }
         }
      }

      return null;
   }

   private class_2338 findNearestChest(class_638 level, class_746 player, class_2338 origin) {
      int radius = Math.min(16, intValue(this.farmRadius) + 4);
      int yRadius = Math.min(8, intValue(this.verticalScan));
      class_2338 best = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (class_2338 cursor : class_2338.method_10097(origin.method_10069(-radius, -yRadius, -radius), origin.method_10069(radius, yRadius, radius))) {
         if (isNormalChest(level.method_8320(cursor))) {
            double distance = player.method_73189().method_1025(class_243.method_24953(cursor));
            if (distance < bestDistance) {
               bestDistance = distance;
               best = cursor.method_10062();
            }
         }
      }

      return best;
   }

   private static class_2338 findChestNear(class_638 level, class_2338 sign, int radius) {
      class_2338 best = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (class_2338 cursor : class_2338.method_10097(sign.method_10069(-radius, -radius, -radius), sign.method_10069(radius, radius, radius))) {
         if (isNormalChest(level.method_8320(cursor))) {
            double distance = cursor.method_10262(sign);
            if (distance < bestDistance) {
               bestDistance = distance;
               best = cursor.method_10062();
            }
         }
      }

      return best;
   }

   static boolean matchesStorageLabel(Iterable<String> lines, AppleFarmerFeature.StorageKind kind) {
      for (String line : lines) {
         String normalized = normalizeLabel(line);
         if (kind != AppleFarmerFeature.StorageKind.APPLES || !normalized.contains("apple") && !normalized.contains("яблок")) {
            if (kind != AppleFarmerFeature.StorageKind.BONES || !normalized.contains("bone") && !normalized.contains("кост")) {
               continue;
            }

            return true;
         }

         return true;
      }

      return false;
   }

   static String normalizeLabel(String value) {
      return value == null ? "" : value.replaceAll("§.", "").toLowerCase(Locale.ROOT).replaceAll("[^\\p{L}\\p{N}]+", " ").trim();
   }

   private class_2338 findPlantingTarget(class_638 level) {
      int radius = intValue(this.farmRadius);
      int yRadius = Math.min(3, intValue(this.verticalScan));

      for (class_2338 base : class_2338.method_10097(
         this.farmCenter.method_10069(-radius, -yRadius, -radius), this.farmCenter.method_10069(radius, yRadius, radius)
      )) {
         class_2680 state = level.method_8320(base);
         if ((
               state.method_27852(class_2246.field_10566)
                  || state.method_27852(class_2246.field_10219)
                  || state.method_27852(class_2246.field_10253)
                  || state.method_27852(class_2246.field_10520)
            )
            && level.method_22347(base.method_10084())
            && !treeNearby(level, base.method_10084(), 2)) {
            return base.method_10062();
         }
      }

      return null;
   }

   private static boolean treeNearby(class_638 level, class_2338 center, int radius) {
      for (class_2338 cursor : class_2338.method_10097(center.method_10069(-radius, -1, -radius), center.method_10069(radius, 3, radius))) {
         class_2680 state = level.method_8320(cursor);
         if (state.method_27852(class_2246.field_10394) || state.method_27852(class_2246.field_10431) || state.method_27852(class_2246.field_10126)) {
            return true;
         }
      }

      return false;
   }

   private class_2338 findNearestBlock(class_638 level, class_746 player, boolean saplingOnly, class_2248... blocks) {
      if (this.farmCenter == null) {
         return null;
      }

      int radius = intValue(this.farmRadius);
      int yRadius = intValue(this.verticalScan);
      class_2338 best = null;
      double bestDistance = Double.POSITIVE_INFINITY;

      for (class_2338 cursor : class_2338.method_10097(
         this.farmCenter.method_10069(-radius, -yRadius, -radius), this.farmCenter.method_10069(radius, yRadius, radius)
      )) {
         class_2680 state = level.method_8320(cursor);
         if (matchesAny(state, blocks) && (!saplingOnly || state.method_27852(class_2246.field_10394))) {
            double distance = player.method_73189().method_1025(class_243.method_24953(cursor));
            if (distance < bestDistance) {
               bestDistance = distance;
               best = cursor.method_10062();
            }
         }
      }

      return best;
   }

   private int findJunkSlot(class_746 player) {
      int totalSaplings = countItem(player, class_1802.field_17535);
      int reserve = intValue(this.saplingReserve);

      for (int slot = 9; slot < 45; slot++) {
         class_1799 stack = player.field_7498.method_7611(slot).method_7677();
         if (!stack.method_7960()) {
            if (stack.method_31574(class_1802.field_17535) && totalSaplings > reserve) {
               return slot;
            }

            if (stack.method_31574(class_1802.field_8600)
               || stack.method_31574(class_1802.field_17503)
               || stack.method_31574(class_1802.field_8317)
               || stack.method_31574(class_1802.field_8309)
               || stack.method_31574(class_1802.field_46250)
               || stack.method_31574(class_1802.field_46249)) {
               return slot;
            }
         }
      }

      return -1;
   }

   private AppleFarmerFeature.FarmerSignals maintenanceSignals(class_746 player, boolean ignoreAppleStorage, boolean ignoreBoneStorage, boolean timedOut) {
      int apples = countItem(player, class_1802.field_8279);
      boolean deposit = !ignoreAppleStorage
         && this.tick >= this.skipAppleStorageUntil
         && apples > 0
         && (apples >= intValue(this.depositAppleStacks) * 64 || freeSlots(player) <= 2);
      boolean fetchBones = !ignoreBoneStorage
         && this.takeBones.getValue()
         && this.tick >= this.skipBoneStorageUntil
         && countItem(player, class_1802.field_8324) < intValue(this.boneMealReserve);
      class_1799 repairTool = findRepairTool(player, this.repairBelow.getValue());
      return signals(
         true,
         false,
         false,
         this.dropJunk.getValue() && this.findJunkSlot(player) >= 0,
         deposit,
         false,
         false,
         false,
         fetchBones,
         countItem(player, class_1802.field_8606) > 0,
         !repairTool.method_7960(),
         countItem(player, class_1802.field_8287) > 0,
         this.buyXpBottles.getValue() && !this.xpBottleBuyCommand.getValue().isBlank(),
         timedOut
      );
   }

   private static AppleFarmerFeature.FarmerSignals signals(
      boolean atFarm,
      boolean workRemaining,
      boolean hasResource,
      boolean hasJunk,
      boolean shouldDepositApples,
      boolean destinationKnown,
      boolean atDestination,
      boolean inventoryWorkRemaining,
      boolean shouldFetchBones,
      boolean hasBones,
      boolean needsRepair,
      boolean hasBottles,
      boolean canBuyBottles,
      boolean timedOut
   ) {
      return new AppleFarmerFeature.FarmerSignals(
         atFarm,
         workRemaining,
         hasResource,
         hasJunk,
         shouldDepositApples,
         destinationKnown,
         atDestination,
         inventoryWorkRemaining,
         shouldFetchBones,
         hasBones,
         needsRepair,
         hasBottles,
         canBuyBottles,
         timedOut
      );
   }

   static AppleFarmerFeature.Phase nextPhase(AppleFarmerFeature.Phase phase, AppleFarmerFeature.FarmerSignals signals) {
      return switch (phase) {
         case RETURN_HOME -> !signals.atFarm() && !signals.timedOut() ? AppleFarmerFeature.Phase.RETURN_HOME : AppleFarmerFeature.Phase.FIND_FARM;
         case FIND_FARM -> signals.atFarm() ? AppleFarmerFeature.Phase.PLANT : AppleFarmerFeature.Phase.APPROACH_FARM;
         case APPROACH_FARM -> signals.atFarm()
            ? AppleFarmerFeature.Phase.PLANT
            : (signals.timedOut() ? AppleFarmerFeature.Phase.RETURN_HOME : AppleFarmerFeature.Phase.APPROACH_FARM);
         case PLANT -> signals.workRemaining() && signals.hasResource() && !signals.timedOut() ? AppleFarmerFeature.Phase.PLANT : AppleFarmerFeature.Phase.GROW;
         case GROW -> signals.workRemaining() && signals.hasResource() && !signals.timedOut()
            ? AppleFarmerFeature.Phase.GROW
            : AppleFarmerFeature.Phase.BREAK_LEAVES;
         case BREAK_LEAVES -> signals.workRemaining() && !signals.timedOut() ? AppleFarmerFeature.Phase.BREAK_LEAVES : AppleFarmerFeature.Phase.BREAK_LOGS;
         case BREAK_LOGS -> signals.workRemaining() && !signals.timedOut() ? AppleFarmerFeature.Phase.BREAK_LOGS : AppleFarmerFeature.Phase.DROP_JUNK;
         case DROP_JUNK -> signals.hasJunk() && !signals.timedOut() ? AppleFarmerFeature.Phase.DROP_JUNK : maintenancePhase(signals);
         case DEPOSIT_APPLES_FIND -> signals.destinationKnown()
            ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_MOVE
            : (signals.timedOut() ? maintenanceAfterApples(signals) : AppleFarmerFeature.Phase.DEPOSIT_APPLES_FIND);
         case DEPOSIT_APPLES_MOVE -> signals.atDestination()
            ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN
            : (signals.timedOut() ? maintenanceAfterApples(signals) : AppleFarmerFeature.Phase.DEPOSIT_APPLES_MOVE);
         case DEPOSIT_APPLES_OPEN -> signals.inventoryWorkRemaining() && !signals.timedOut()
            ? AppleFarmerFeature.Phase.DEPOSIT_APPLES_OPEN
            : maintenanceAfterApples(signals);
         case BONES_FIND -> signals.destinationKnown()
            ? AppleFarmerFeature.Phase.BONES_MOVE
            : (signals.timedOut() ? maintenanceAfterBones(signals) : AppleFarmerFeature.Phase.BONES_FIND);
         case BONES_MOVE -> signals.atDestination()
            ? AppleFarmerFeature.Phase.BONES_OPEN
            : (signals.timedOut() ? maintenanceAfterBones(signals) : AppleFarmerFeature.Phase.BONES_MOVE);
         case BONES_OPEN -> signals.inventoryWorkRemaining() && !signals.timedOut()
            ? AppleFarmerFeature.Phase.BONES_OPEN
            : AppleFarmerFeature.Phase.CRAFT_BONE_MEAL;
         case CRAFT_BONE_MEAL -> signals.hasBones() && signals.workRemaining() && !signals.timedOut()
            ? AppleFarmerFeature.Phase.CRAFT_BONE_MEAL
            : repairPhase(signals);
         case BUY_BOTTLES -> signals.hasBottles()
            ? AppleFarmerFeature.Phase.REPAIR
            : (!signals.timedOut() && signals.canBuyBottles() ? AppleFarmerFeature.Phase.BUY_BOTTLES : AppleFarmerFeature.Phase.PLANT);
         case REPAIR -> signals.needsRepair() && signals.hasBottles() && !signals.timedOut() ? AppleFarmerFeature.Phase.REPAIR : AppleFarmerFeature.Phase.PLANT;
      };
   }

   private static AppleFarmerFeature.Phase maintenancePhase(AppleFarmerFeature.FarmerSignals signals) {
      if (signals.shouldDepositApples()) {
         return AppleFarmerFeature.Phase.DEPOSIT_APPLES_FIND;
      } else if (signals.hasBones()) {
         return AppleFarmerFeature.Phase.CRAFT_BONE_MEAL;
      } else {
         return signals.shouldFetchBones() ? AppleFarmerFeature.Phase.BONES_FIND : repairPhase(signals);
      }
   }

   private static AppleFarmerFeature.Phase maintenanceAfterApples(AppleFarmerFeature.FarmerSignals signals) {
      if (signals.hasBones()) {
         return AppleFarmerFeature.Phase.CRAFT_BONE_MEAL;
      } else {
         return signals.shouldFetchBones() ? AppleFarmerFeature.Phase.BONES_FIND : repairPhase(signals);
      }
   }

   private static AppleFarmerFeature.Phase maintenanceAfterBones(AppleFarmerFeature.FarmerSignals signals) {
      return signals.hasBones() ? AppleFarmerFeature.Phase.CRAFT_BONE_MEAL : repairPhase(signals);
   }

   private static AppleFarmerFeature.Phase repairPhase(AppleFarmerFeature.FarmerSignals signals) {
      if (!signals.needsRepair()) {
         return AppleFarmerFeature.Phase.PLANT;
      } else if (signals.hasBottles()) {
         return AppleFarmerFeature.Phase.REPAIR;
      } else {
         return signals.canBuyBottles() ? AppleFarmerFeature.Phase.BUY_BOTTLES : AppleFarmerFeature.Phase.PLANT;
      }
   }

   private void transition(AppleFarmerFeature.Phase next) {
      AppleFarmerFeature.Phase previous = this.state.state();
      if (this.state.transition(next, this.tick)) {
         this.cancelNavigation();
         this.activeWorkPos = null;
         this.miningIssued = false;
         this.lastNavigationTick = -4611686018427387904L;
         this.homeCommandSent = false;
         this.buyCommandSent = false;
         if (previous == AppleFarmerFeature.Phase.REPAIR && next != AppleFarmerFeature.Phase.REPAIR) {
            this.restoreRepairTool(mc.field_1724);
            this.restoreRotation(mc.field_1724);
         }

         if (next == AppleFarmerFeature.Phase.PLANT && previous != AppleFarmerFeature.Phase.FIND_FARM && previous != AppleFarmerFeature.Phase.APPROACH_FARM) {
            this.completedCycles++;
            this.plantedThisCycle = 0;
            this.boneMealUsedThisCycle = 0;
            this.blocksBrokenThisCycle = 0;
         }

         if (next != AppleFarmerFeature.Phase.CRAFT_BONE_MEAL) {
            this.craftStep = 0;
            this.craftSourceSlot = -1;
         }
      }
   }

   private boolean beginNavigation(NavigationOptions options) {
      if (!this.navigator.isAvailable()) {
         return false;
      }

      try {
         this.navigator.begin(PveManagerFeature.INSTANCE.configureNavigation(options));
         this.navigationActive = true;
         return true;
      } catch (RuntimeException | LinkageError ignored) {
         this.navigationActive = false;
         return false;
      }
   }

   private void navigateTo(class_2338 target, int radius, NavigationOptions options) {
      if (target != null && this.beginNavigation(options)) {
         Optional<class_2338> currentGoal = this.navigator.currentGoal();
         boolean sameGoal = currentGoal.isPresent() && currentGoal.get().equals(target);
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

   private boolean actionReady() {
      return this.tick - this.lastActionTick >= 4L;
   }

   private boolean selectHotbarItem(class_746 player, class_1792 item) {
      int slot = findPlayerSlot(player, item);
      if (slot < 0) {
         return false;
      } else {
         int selected = player.method_31548().method_67532();
         if (slot >= 36 && slot < 45) {
            player.method_31548().method_61496(slot - 36);
            return true;
         } else {
            this.clickPlayerMenu(player, slot, selected, class_1713.field_7791);
            return player.method_6047().method_31574(item);
         }
      }
   }

   private void useFromPlayerSlot(class_746 player, int slot) {
      int selected = player.method_31548().method_67532();
      if (slot >= 36 && slot < 45) {
         int hotbar = slot - 36;
         int old = selected;
         player.method_31548().method_61496(hotbar);
         mc.field_1761.method_2919(player, class_1268.field_5808);
         player.method_6104(class_1268.field_5808);
         player.method_31548().method_61496(old);
      } else {
         this.clickPlayerMenu(player, slot, selected, class_1713.field_7791);
         mc.field_1761.method_2919(player, class_1268.field_5808);
         player.method_6104(class_1268.field_5808);
         this.clickPlayerMenu(player, slot, selected, class_1713.field_7791);
      }
   }

   private void swapWithOffhand(class_746 player, int slot) {
      this.clickPlayerMenu(player, slot, 40, class_1713.field_7791);
   }

   private void restoreRepairTool(class_746 player) {
      if (this.repairToolSourceSlot >= 0) {
         if (player != null && player.field_7512 == player.field_7498 && player.field_7498.method_40442(this.repairToolSourceSlot)) {
            this.swapWithOffhand(player, this.repairToolSourceSlot);
         }

         this.repairToolSourceSlot = -1;
      }
   }

   private void restoreCraftingInventory(class_746 player) {
      if (player != null && mc.field_1761 != null && player.field_7512 == player.field_7498) {
         if (!player.field_7498.method_34255().method_7960()) {
            int destination = validEmptySlot(player, this.craftSourceSlot) ? this.craftSourceSlot : findEmptyPlayerSlot(player);
            if (destination >= 0) {
               this.clickPlayerMenu(player, destination, 0, class_1713.field_7790);
            }
         }

         class_1799 input = player.field_7498.method_7611(1).method_7677();
         if (!input.method_7960() && player.field_7498.method_34255().method_7960()) {
            int destination = validEmptySlot(player, this.craftSourceSlot) ? this.craftSourceSlot : findEmptyPlayerSlot(player);
            if (destination >= 0) {
               this.clickPlayerMenu(player, 1, 0, class_1713.field_7790);
               this.clickPlayerMenu(player, destination, 0, class_1713.field_7790);
            }
         }
      }
   }

   private static boolean validEmptySlot(class_746 player, int slot) {
      return slot >= 0 && player.field_7498.method_40442(slot) && player.field_7498.method_7611(slot).method_7677().method_7960();
   }

   private static int findEmptyPlayerSlot(class_746 player) {
      for (int slot = 9; slot < 45; slot++) {
         if (player.field_7498.method_7611(slot).method_7677().method_7960()) {
            return slot;
         }
      }

      return -1;
   }

   private void clickPlayerMenu(class_746 player, int slot, int button, class_1713 input) {
      if (mc.field_1761 != null && player.field_7498.method_40442(slot)) {
         mc.field_1761.method_2906(player.field_7498.field_7763, slot, button, input, player);
      }
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

   private static int findPlayerSlot(class_746 player, class_1792 item) {
      for (int slot = 36; slot < 45; slot++) {
         if (player.field_7498.method_7611(slot).method_7677().method_31574(item)) {
            return slot;
         }
      }

      for (int slot = 9; slot < 36; slot++) {
         if (player.field_7498.method_7611(slot).method_7677().method_31574(item)) {
            return slot;
         }
      }

      return -1;
   }

   private static int countItem(class_746 player, class_1792 item) {
      int count = 0;

      for (int slot = 9; slot < 45; slot++) {
         class_1799 stack = player.field_7498.method_7611(slot).method_7677();
         if (stack.method_31574(item)) {
            count += stack.method_7947();
         }
      }

      if (player.method_6079().method_31574(item)) {
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

   private static class_1799 findRepairTool(class_746 player, double thresholdPercent) {
      int slot = findRepairToolSlot(player, thresholdPercent);
      return slot < 0 ? class_1799.field_8037 : player.field_7498.method_7611(slot).method_7677();
   }

   private static int findRepairToolSlot(class_746 player, double thresholdPercent) {
      int bestSlot = -1;
      double worstRemaining = Double.POSITIVE_INFINITY;

      for (int slot = 9; slot < 45; slot++) {
         class_1799 stack = player.field_7498.method_7611(slot).method_7677();
         if (needsRepair(stack, thresholdPercent) && enchantmentLevel(stack, "mending") > 0) {
            double remaining = remainingDurability(stack);
            if (remaining < worstRemaining) {
               worstRemaining = remaining;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   private static boolean needsRepair(class_1799 stack, double thresholdPercent) {
      return !stack.method_7960() && stack.method_7963() && enchantmentLevel(stack, "mending") > 0 && remainingDurability(stack) < thresholdPercent;
   }

   private static double remainingDurability(class_1799 stack) {
      return !stack.method_7963() ? 100.0 : (stack.method_7936() - stack.method_7919()) * 100.0 / Math.max(1, stack.method_7936());
   }

   private static int enchantmentLevel(class_1799 stack, String path) {
      if (stack.method_7960()) {
         return 0;
      }

      for (Entry<class_6880<class_1887>> entry : stack.method_58657().method_57539()) {
         Optional<class_5321<class_1887>> key = ((class_6880)entry.getKey()).method_40230();
         if (key.isPresent() && key.get().method_29177().method_12832().equals(path)) {
            return entry.getIntValue();
         }
      }

      return 0;
   }

   private void lookAt(class_746 player, class_243 target) {
      if (PveManagerFeature.INSTANCE.rotate.getValue()) {
         this.saveRotation(player);
         class_243 delta = target.method_1020(player.method_33571());
         double horizontal = Math.sqrt(delta.field_1352 * delta.field_1352 + delta.field_1350 * delta.field_1350);
         float yaw = (float)Math.toDegrees(Math.atan2(delta.field_1350, delta.field_1352)) - 90.0F;
         float pitch = (float)(-Math.toDegrees(Math.atan2(delta.field_1351, horizontal)));
         player.method_36456(yaw);
         player.method_36457(pitch);
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

   private static boolean at(class_746 player, class_2338 position, double radius) {
      return position != null && player.method_73189().method_1025(class_243.method_24953(position)) <= radius * radius;
   }

   private static boolean matchesAny(class_2680 state, class_2248... blocks) {
      for (class_2248 block : blocks) {
         if (state.method_27852(block)) {
            return true;
         }
      }

      return false;
   }

   private static boolean isNormalChest(class_2680 state) {
      return state.method_27852(class_2246.field_10034) || state.method_27852(class_2246.field_10380);
   }

   private static int intValue(NumberSetting setting) {
      return (int)Math.round(setting.getValue());
   }

   @Environment(EnvType.CLIENT)
   record FarmerSignals(
      boolean atFarm,
      boolean workRemaining,
      boolean hasResource,
      boolean hasJunk,
      boolean shouldDepositApples,
      boolean destinationKnown,
      boolean atDestination,
      boolean inventoryWorkRemaining,
      boolean shouldFetchBones,
      boolean hasBones,
      boolean needsRepair,
      boolean hasBottles,
      boolean canBuyBottles,
      boolean timedOut
   ) {
   }

   @Environment(EnvType.CLIENT)
   public enum Phase {
      RETURN_HOME,
      FIND_FARM,
      APPROACH_FARM,
      PLANT,
      GROW,
      BREAK_LEAVES,
      BREAK_LOGS,
      DROP_JUNK,
      DEPOSIT_APPLES_FIND,
      DEPOSIT_APPLES_MOVE,
      DEPOSIT_APPLES_OPEN,
      BONES_FIND,
      BONES_MOVE,
      BONES_OPEN,
      CRAFT_BONE_MEAL,
      BUY_BOTTLES,
      REPAIR;
   }

   @Environment(EnvType.CLIENT)
   enum StorageKind {
      APPLES,
      BONES;
   }
}
