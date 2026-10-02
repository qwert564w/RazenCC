package org.ryzen.feature.impl.misc;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_7439;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.pve.PvpStateTracker;
import org.ryzen.utils.FriendManager;

@Environment(EnvType.CLIENT)
public final class AutoTpaAcceptFeature extends Feature implements MinecraftContext {
   private static final List<String> REQUEST_MARKERS = List.of("has requested teleport", "телепортироваться");
   private static final long ACCEPT_COOLDOWN_MILLIS = 1000L;
   public final BooleanSetting friendsOnly = this.register(new BooleanSetting("Friends Only", false));
   public final BooleanSetting avoidCombat = this.register(new BooleanSetting("Avoid Combat", true));
   private long lastAcceptAt;

   public AutoTpaAcceptFeature() {
      super("AutoTpaAccept", "Automatically accepts teleport requests", FeatureCategory.MISC, -1);
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && this.player() != null) {
         if (event.getPacket() instanceof class_7439 packet) {
            String text = packet.comp_763().getString();
            String normalized = text.toLowerCase(Locale.ROOT);
            if (!REQUEST_MARKERS.stream().noneMatch(normalized::contains)) {
               if (!this.avoidCombat.getValue() || !this.isInCombat()) {
                  if (!this.friendsOnly.getValue()
                     || !FriendManager.INSTANCE.getFriends().stream().map(name -> name.toLowerCase(Locale.ROOT)).noneMatch(normalized::contains)) {
                     long now = System.currentTimeMillis();
                     if (now - this.lastAcceptAt >= 1000L) {
                        this.lastAcceptAt = now;
                        this.player().field_3944.method_45730("tpaccept");
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isInCombat() {
      if (!PvpStateTracker.INSTANCE.isActive() && this.player().field_6235 <= 0) {
         AuraFeature aura = FeatureManager.INSTANCE.getFeature(AuraFeature.class);
         return aura != null && aura.getCurrentTarget() != null;
      } else {
         return true;
      }
   }
}
