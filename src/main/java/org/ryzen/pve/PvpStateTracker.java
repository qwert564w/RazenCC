package org.ryzen.pve;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_7439;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldJoinEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;

@Environment(EnvType.CLIENT)
public final class PvpStateTracker {
   public static final PvpStateTracker INSTANCE = new PvpStateTracker();
   private static final long LOCAL_COMBAT_MILLIS = 10000L;
   private static final List<String> ENABLE_MARKERS = List.of(
      "pvp mode enabled", "combat mode enabled", "режим pvp активирован", "режим боя активирован", "до конца pvp", "до конца режима боя"
   );
   private static final List<String> DISABLE_MARKERS = List.of(
      "pvp mode disabled", "combat mode disabled", "режим pvp деактивирован", "режим боя деактивирован", "you are no longer in combat"
   );
   private volatile long activeUntil;
   private volatile boolean serverTagged;

   private PvpStateTracker() {
   }

   public boolean isActive() {
      return this.serverTagged || System.currentTimeMillis() < this.activeUntil;
   }

   public void markCombatFor(long millis) {
      this.activeUntil = Math.max(this.activeUntil, System.currentTimeMillis() + Math.max(0L, millis));
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_7439 packet) {
         String var4 = packet.comp_763().getString().toLowerCase(Locale.ROOT);
         if (DISABLE_MARKERS.stream().anyMatch(var4::contains)) {
            this.serverTagged = false;
            this.activeUntil = 0L;
         } else if (ENABLE_MARKERS.stream().anyMatch(var4::contains)) {
            this.serverTagged = true;
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null) {
         if (player.field_6235 > 0 && player.method_6065() instanceof class_1657) {
            this.markCombatFor(10000L);
         }

         AuraFeature aura = FeatureManager.INSTANCE.getEnabled(AuraFeature.class);
         if (aura != null && aura.getCurrentTarget() instanceof class_1657) {
            this.markCombatFor(10000L);
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

   private void reset() {
      this.activeUntil = 0L;
      this.serverTagged = false;
   }
}
