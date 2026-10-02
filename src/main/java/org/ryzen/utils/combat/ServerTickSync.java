package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2761;
import net.minecraft.class_8913;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;

@Environment(EnvType.CLIENT)
public final class ServerTickSync {
   public static final ServerTickSync INSTANCE = new ServerTickSync();
   private static final float DEFAULT_TPS = 20.0F;
   private static final float MIN_TPS = 1.0F;
   private static final float SAMPLE_WEIGHT = 0.25F;
   private static final long SAMPLE_STALE_NANOS = 10000000000L;
   private static final long NANOS_PER_SECOND = 1000000000L;
   private volatile float configuredTps = 20.0F;
   private volatile float observedTps = 20.0F;
   private volatile boolean frozen;
   private volatile int validSamples;
   private volatile long lastGameTime = Long.MIN_VALUE;
   private volatile long lastTimePacketNanos;
   private volatile long lastValidSampleNanos;

   private ServerTickSync() {
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         if (event.getPacket() instanceof class_8913 packet) {
            this.configuredTps = Math.max(1.0F, packet.comp_2014());
            this.frozen = packet.comp_2015();
            if (this.validSamples == 0) {
               this.observedTps = this.configuredTps;
            }
         } else {
            if (event.getPacket() instanceof class_2761 packet) {
               this.observeTimePacket(packet.comp_3219(), System.nanoTime());
            }
         }
      }
   }

   @EventTarget
   public void onWorldJoin(WorldJoinEvent event) {
      this.reset();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.reset();
   }

   public float effectiveTps() {
      if (this.frozen) {
         return 0.0F;
      }

      long now = System.nanoTime();
      return this.validSamples != 0 && now - this.lastValidSampleNanos <= 10000000000L
         ? Math.clamp(this.observedTps, 1.0F, this.configuredTps)
         : this.configuredTps;
   }

   public long tickNanos() {
      float tps = this.effectiveTps();
      return tps <= 0.0F ? Long.MAX_VALUE : Math.max(1L, Math.round(1.0E9 / tps));
   }

   public double ticksUntilNextServerTick() {
      long tickNanos = this.tickNanos();
      if (tickNanos != Long.MAX_VALUE && this.lastTimePacketNanos != 0L) {
         long elapsed = Math.max(0L, System.nanoTime() - this.lastTimePacketNanos);
         long phase = elapsed % tickNanos;
         return (double)(tickNanos - phase) / tickNanos;
      } else {
         return 0.0;
      }
   }

   public boolean isFrozen() {
      return this.frozen;
   }

   public void reset() {
      this.configuredTps = 20.0F;
      this.observedTps = 20.0F;
      this.frozen = false;
      this.validSamples = 0;
      this.lastGameTime = Long.MIN_VALUE;
      this.lastTimePacketNanos = 0L;
      this.lastValidSampleNanos = 0L;
   }

   private void observeTimePacket(long gameTime, long now) {
      if (this.lastGameTime != Long.MIN_VALUE && gameTime > this.lastGameTime) {
         long gameTicks = gameTime - this.lastGameTime;
         long elapsedNanos = now - this.lastTimePacketNanos;
         if (gameTicks <= 2000L && elapsedNanos > 0L) {
            float sample = (float)(gameTicks * 1.0E9 / elapsedNanos);
            float upperBound = Math.max(1.0F, this.configuredTps);
            if (sample >= 0.5F && sample <= upperBound * 1.25F) {
               sample = Math.clamp(sample, 1.0F, upperBound);
               this.observedTps = this.validSamples == 0 ? sample : this.observedTps + (sample - this.observedTps) * 0.25F;
               this.validSamples++;
               this.lastValidSampleNanos = now;
            }
         }
      }

      this.lastGameTime = gameTime;
      this.lastTimePacketNanos = now;
   }
}
