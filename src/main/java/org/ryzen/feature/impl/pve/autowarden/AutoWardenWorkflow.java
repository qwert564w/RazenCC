package org.ryzen.feature.impl.pve.autowarden;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.pve.PveStateMachine;

@Environment(EnvType.CLIENT)
public final class AutoWardenWorkflow {
   private static final Map<AutoWardenWorkflow.Phase, Set<AutoWardenWorkflow.Phase>> ALLOWED = allowedTransitions();
   private static final Map<AutoWardenWorkflow.Phase, Long> DEFAULT_TIMEOUT_TICKS = Map.ofEntries(
      Map.entry(AutoWardenWorkflow.Phase.IDLE, Long.MAX_VALUE),
      Map.entry(AutoWardenWorkflow.Phase.ENSURE_ANARCHY, 500L),
      Map.entry(AutoWardenWorkflow.Phase.HOME_TO_WARDEN_CITY, 500L),
      Map.entry(AutoWardenWorkflow.Phase.SEARCH_CHEST, 600L),
      Map.entry(AutoWardenWorkflow.Phase.PATROL_CITY, 3600L),
      Map.entry(AutoWardenWorkflow.Phase.MOVE_TO_CHEST, 1200L),
      Map.entry(AutoWardenWorkflow.Phase.HOME_WAIT, 12000L),
      Map.entry(AutoWardenWorkflow.Phase.RETURN_TO_CITY, 1200L),
      Map.entry(AutoWardenWorkflow.Phase.OPEN_AND_LOOT, 600L),
      Map.entry(AutoWardenWorkflow.Phase.PVP_HIDE, 3600L),
      Map.entry(AutoWardenWorkflow.Phase.GO_TO_STORAGE, 3600L),
      Map.entry(AutoWardenWorkflow.Phase.DEPOSIT_LOOT, 1800L),
      Map.entry(AutoWardenWorkflow.Phase.RESTOCK, 1800L),
      Map.entry(AutoWardenWorkflow.Phase.SELL_ITEMS, 12000L)
   );
   private final PveStateMachine<AutoWardenWorkflow.Phase> machine = new PveStateMachine<>(AutoWardenWorkflow.Phase.IDLE);
   private String lastReason = "Disabled";

   public AutoWardenWorkflow.Phase phase() {
      return this.machine.state();
   }

   public long enteredTick() {
      return this.machine.enteredTick();
   }

   public long ticksInPhase(long tick) {
      return this.machine.ticksInState(tick);
   }

   public long transitionCount() {
      return this.machine.transitionCount();
   }

   public String lastReason() {
      return this.lastReason;
   }

   public boolean start(boolean validConfiguration, boolean needsStorage, long tick) {
      if (!validConfiguration) {
         this.lastReason = "Waiting for valid anarchy settings";
         return false;
      } else {
         return this.move(
            needsStorage ? AutoWardenWorkflow.Phase.GO_TO_STORAGE : AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            needsStorage ? "Supplies or stored loot require home" : "Starting loot rotation",
            tick
         );
      }
   }

   public boolean anarchyReady(long tick) {
      return this.move(AutoWardenWorkflow.Phase.HOME_TO_WARDEN_CITY, "Loot anarchy selected", tick);
   }

   public boolean cityReady(long tick) {
      return this.move(AutoWardenWorkflow.Phase.SEARCH_CHEST, "Warden city reached", tick);
   }

   public boolean chestSelected(int remainingSeconds, int homeWaitThresholdSeconds, long tick) {
      AutoWardenWorkflow.Phase next = remainingSeconds > Math.max(0, homeWaitThresholdSeconds)
         ? AutoWardenWorkflow.Phase.HOME_WAIT
         : AutoWardenWorkflow.Phase.MOVE_TO_CHEST;
      return this.move(next, next == AutoWardenWorkflow.Phase.HOME_WAIT ? "Waiting safely for chest timer" : "Approaching selected chest", tick);
   }

   public boolean noChestFound(long tick) {
      return this.move(AutoWardenWorkflow.Phase.PATROL_CITY, "No suitable chest is currently known", tick);
   }

   public boolean patrolFoundChest(long tick) {
      return this.move(AutoWardenWorkflow.Phase.MOVE_TO_CHEST, "Patrol discovered a chest", tick);
   }

   public boolean returnWindowReached(long tick) {
      return this.move(AutoWardenWorkflow.Phase.RETURN_TO_CITY, "Chest return window reached", tick);
   }

   public boolean chestReached(long tick) {
      return this.move(AutoWardenWorkflow.Phase.OPEN_AND_LOOT, "Chest is in interaction range", tick);
   }

   public boolean continueSearching(long tick) {
      return this.move(AutoWardenWorkflow.Phase.SEARCH_CHEST, "Searching for another chest", tick);
   }

   public boolean requestStorage(String reason, long tick) {
      return this.move(AutoWardenWorkflow.Phase.GO_TO_STORAGE, reason, tick);
   }

   public boolean storageReady(long tick) {
      return this.move(AutoWardenWorkflow.Phase.DEPOSIT_LOOT, "Storage reached", tick);
   }

   public boolean depositFinished(boolean needsRestock, boolean autoSell, long tick) {
      AutoWardenWorkflow.Phase next = needsRestock
         ? AutoWardenWorkflow.Phase.RESTOCK
         : (autoSell ? AutoWardenWorkflow.Phase.SELL_ITEMS : AutoWardenWorkflow.Phase.IDLE);
      return this.move(next, needsRestock ? "Restocking supplies" : (autoSell ? "Selling deposited loot" : "Cycle complete"), tick);
   }

   public boolean restockFinished(boolean autoSell, long tick) {
      return this.move(
         autoSell ? AutoWardenWorkflow.Phase.SELL_ITEMS : AutoWardenWorkflow.Phase.IDLE, autoSell ? "Supplies restored; selling loot" : "Cycle complete", tick
      );
   }

   public boolean sellFinished(long tick) {
      return this.move(AutoWardenWorkflow.Phase.IDLE, "Selling complete", tick);
   }

   public boolean dangerDetected(long tick) {
      if (this.machine.is(AutoWardenWorkflow.Phase.PVP_HIDE)) {
         return false;
      }

      this.lastReason = "Avoiding nearby danger";
      return this.machine.transition(AutoWardenWorkflow.Phase.PVP_HIDE, tick);
   }

   public boolean dangerCleared(boolean carryLoot, long tick) {
      return this.move(
         carryLoot ? AutoWardenWorkflow.Phase.GO_TO_STORAGE : AutoWardenWorkflow.Phase.SEARCH_CHEST,
         carryLoot ? "Danger ended; preserving loot" : "Danger ended",
         tick
      );
   }

   public boolean rotateAnarchy(long tick) {
      return this.move(AutoWardenWorkflow.Phase.ENSURE_ANARCHY, "Rotating to another loot anarchy", tick);
   }

   public boolean move(AutoWardenWorkflow.Phase next, String reason, long tick) {
      Objects.requireNonNull(next, "next");
      AutoWardenWorkflow.Phase current = this.machine.state();
      if (current == next) {
         return false;
      }

      if (!ALLOWED.getOrDefault(current, Set.of()).contains(next)) {
         return false;
      }

      this.lastReason = reason != null && !reason.isBlank() ? reason : next.name();
      return this.machine.transition(next, tick);
   }

   public boolean timedOut(long tick, long overrideTimeoutTicks) {
      long timeout = overrideTimeoutTicks > 0L ? overrideTimeoutTicks : DEFAULT_TIMEOUT_TICKS.getOrDefault(this.phase(), 1200L);
      return timeout != Long.MAX_VALUE && this.ticksInPhase(tick) >= timeout;
   }

   public boolean timedOut(long tick) {
      return this.timedOut(tick, -1L);
   }

   public void failSafe(String reason, long tick) {
      this.lastReason = reason != null && !reason.isBlank() ? reason : "Stopped safely";
      this.machine.transition(AutoWardenWorkflow.Phase.IDLE, tick);
   }

   public void reset(long tick) {
      this.machine.reset(tick);
      this.lastReason = "Disabled";
   }

   private static Map<AutoWardenWorkflow.Phase, Set<AutoWardenWorkflow.Phase>> allowedTransitions() {
      EnumMap<AutoWardenWorkflow.Phase, Set<AutoWardenWorkflow.Phase>> result = new EnumMap<>(AutoWardenWorkflow.Phase.class);
      result.put(AutoWardenWorkflow.Phase.IDLE, EnumSet.of(AutoWardenWorkflow.Phase.ENSURE_ANARCHY, AutoWardenWorkflow.Phase.GO_TO_STORAGE));
      result.put(
         AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
         EnumSet.of(
            AutoWardenWorkflow.Phase.HOME_TO_WARDEN_CITY,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.IDLE,
            AutoWardenWorkflow.Phase.PVP_HIDE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.HOME_TO_WARDEN_CITY,
         EnumSet.of(
            AutoWardenWorkflow.Phase.SEARCH_CHEST,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.IDLE,
            AutoWardenWorkflow.Phase.PVP_HIDE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.SEARCH_CHEST,
         EnumSet.of(
            AutoWardenWorkflow.Phase.PATROL_CITY,
            AutoWardenWorkflow.Phase.MOVE_TO_CHEST,
            AutoWardenWorkflow.Phase.HOME_WAIT,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.PVP_HIDE,
            AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.PATROL_CITY,
         EnumSet.of(
            AutoWardenWorkflow.Phase.SEARCH_CHEST,
            AutoWardenWorkflow.Phase.MOVE_TO_CHEST,
            AutoWardenWorkflow.Phase.HOME_WAIT,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.PVP_HIDE,
            AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.MOVE_TO_CHEST,
         EnumSet.of(
            AutoWardenWorkflow.Phase.OPEN_AND_LOOT,
            AutoWardenWorkflow.Phase.SEARCH_CHEST,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.PVP_HIDE,
            AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.HOME_WAIT,
         EnumSet.of(
            AutoWardenWorkflow.Phase.RETURN_TO_CITY,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.PVP_HIDE,
            AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.RETURN_TO_CITY,
         EnumSet.of(
            AutoWardenWorkflow.Phase.MOVE_TO_CHEST,
            AutoWardenWorkflow.Phase.SEARCH_CHEST,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.PVP_HIDE,
            AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.OPEN_AND_LOOT,
         EnumSet.of(
            AutoWardenWorkflow.Phase.SEARCH_CHEST, AutoWardenWorkflow.Phase.GO_TO_STORAGE, AutoWardenWorkflow.Phase.PVP_HIDE, AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.PVP_HIDE,
         EnumSet.of(
            AutoWardenWorkflow.Phase.SEARCH_CHEST,
            AutoWardenWorkflow.Phase.GO_TO_STORAGE,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.GO_TO_STORAGE,
         EnumSet.of(
            AutoWardenWorkflow.Phase.DEPOSIT_LOOT, AutoWardenWorkflow.Phase.ENSURE_ANARCHY, AutoWardenWorkflow.Phase.PVP_HIDE, AutoWardenWorkflow.Phase.IDLE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.DEPOSIT_LOOT,
         EnumSet.of(
            AutoWardenWorkflow.Phase.RESTOCK,
            AutoWardenWorkflow.Phase.SELL_ITEMS,
            AutoWardenWorkflow.Phase.ENSURE_ANARCHY,
            AutoWardenWorkflow.Phase.IDLE,
            AutoWardenWorkflow.Phase.PVP_HIDE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.RESTOCK,
         EnumSet.of(
            AutoWardenWorkflow.Phase.SELL_ITEMS, AutoWardenWorkflow.Phase.ENSURE_ANARCHY, AutoWardenWorkflow.Phase.IDLE, AutoWardenWorkflow.Phase.PVP_HIDE
         )
      );
      result.put(
         AutoWardenWorkflow.Phase.SELL_ITEMS,
         EnumSet.of(AutoWardenWorkflow.Phase.ENSURE_ANARCHY, AutoWardenWorkflow.Phase.IDLE, AutoWardenWorkflow.Phase.PVP_HIDE)
      );
      return Map.copyOf(result);
   }

   @Environment(EnvType.CLIENT)
   public enum Phase {
      IDLE,
      ENSURE_ANARCHY,
      HOME_TO_WARDEN_CITY,
      SEARCH_CHEST,
      PATROL_CITY,
      MOVE_TO_CHEST,
      HOME_WAIT,
      RETURN_TO_CITY,
      OPEN_AND_LOOT,
      PVP_HIDE,
      GO_TO_STORAGE,
      DEPOSIT_LOOT,
      RESTOCK,
      SELL_ITEMS;
   }
}
