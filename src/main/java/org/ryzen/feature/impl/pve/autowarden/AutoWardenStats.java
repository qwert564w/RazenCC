package org.ryzen.feature.impl.pve.autowarden;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class AutoWardenStats {
   private long startedAtMillis;
   private AutoWardenWorkflow.Phase phase = AutoWardenWorkflow.Phase.IDLE;
   private int activeAnarchy = -1;
   private int cycles;
   private int chestsLooted;
   private int valuablesCollected;
   private int itemsStored;
   private int deaths;
   private int invisibility;
   private int food;
   private int speed;
   private int homeWaitSeconds;
   private String target = "-";
   private String lastAction = "Disabled";

   public synchronized void start(long nowMillis) {
      this.startedAtMillis = nowMillis;
      this.phase = AutoWardenWorkflow.Phase.IDLE;
      this.activeAnarchy = -1;
      this.cycles = 0;
      this.chestsLooted = 0;
      this.valuablesCollected = 0;
      this.itemsStored = 0;
      this.deaths = 0;
      this.invisibility = 0;
      this.food = 0;
      this.speed = 0;
      this.homeWaitSeconds = 0;
      this.target = "-";
      this.lastAction = "Starting";
   }

   public synchronized void phase(AutoWardenWorkflow.Phase phase, String action) {
      this.phase = phase;
      this.lastAction = action != null && !action.isBlank() ? action : phase.name();
      if (phase != AutoWardenWorkflow.Phase.HOME_WAIT) {
         this.homeWaitSeconds = 0;
      }
   }

   public synchronized void activeAnarchy(int activeAnarchy) {
      this.activeAnarchy = activeAnarchy;
   }

   public synchronized void target(String target) {
      this.target = target != null && !target.isBlank() ? target : "-";
   }

   public synchronized void homeWaitSeconds(int seconds) {
      this.homeWaitSeconds = Math.max(0, seconds);
   }

   public synchronized void supplies(int invisibility, int food, int speed) {
      this.invisibility = Math.max(0, invisibility);
      this.food = Math.max(0, food);
      this.speed = Math.max(0, speed);
   }

   public synchronized void chestLooted(int itemCount) {
      this.chestsLooted++;
      this.valuablesCollected = this.valuablesCollected + Math.max(0, itemCount);
   }

   public synchronized void stored(int itemCount) {
      this.itemsStored = this.itemsStored + Math.max(0, itemCount);
   }

   public synchronized void cycleComplete() {
      this.cycles++;
   }

   public synchronized void died() {
      this.deaths++;
   }

   public synchronized AutoWardenStats.Snapshot snapshot(long nowMillis) {
      long uptime = this.startedAtMillis == 0L ? 0L : Math.max(0L, nowMillis - this.startedAtMillis);
      return new AutoWardenStats.Snapshot(
         uptime,
         this.phase,
         this.activeAnarchy,
         this.cycles,
         this.chestsLooted,
         this.valuablesCollected,
         this.itemsStored,
         this.deaths,
         this.invisibility,
         this.food,
         this.speed,
         this.homeWaitSeconds,
         this.target,
         this.lastAction
      );
   }

   @Environment(EnvType.CLIENT)
   public record Snapshot(
      long uptimeMillis,
      AutoWardenWorkflow.Phase phase,
      int activeAnarchy,
      int cycles,
      int chestsLooted,
      int valuablesCollected,
      int itemsStored,
      int deaths,
      int invisibility,
      int food,
      int speed,
      int homeWaitSeconds,
      String target,
      String lastAction
   ) {
      public String formattedUptime() {
         Duration duration = Duration.ofMillis(Math.max(0L, this.uptimeMillis));
         long hours = duration.toHours();
         long minutes = duration.toMinutesPart();
         long seconds = duration.toSecondsPart();
         return hours > 0L ? "%02d:%02d:%02d".formatted(hours, minutes, seconds) : "%02d:%02d".formatted(minutes, seconds);
      }

      public Map<String, String> widgetData() {
         LinkedHashMap<String, String> values = new LinkedHashMap<>();
         values.put("Uptime", this.formattedUptime());
         values.put("Phase", this.phase.name());
         values.put("Anarchy", this.activeAnarchy < 0 ? "-" : Integer.toString(this.activeAnarchy));
         values.put("Cycles", Integer.toString(this.cycles));
         values.put("Chests", Integer.toString(this.chestsLooted));
         values.put("Valuables", Integer.toString(this.valuablesCollected));
         values.put("Stored", Integer.toString(this.itemsStored));
         values.put("Deaths", Integer.toString(this.deaths));
         values.put("Target", this.target);
         values.put("Supplies", "invis %d / food %d / speed %d".formatted(this.invisibility, this.food, this.speed));
         if (this.homeWaitSeconds > 0) {
            values.put("Arena wait", this.homeWaitSeconds + "s");
         }

         values.put("Action", this.lastAction);
         return Map.copyOf(values);
      }
   }
}
