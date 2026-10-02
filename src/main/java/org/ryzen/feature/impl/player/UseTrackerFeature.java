package org.ryzen.feature.impl.player;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1812;
import net.minecraft.class_2663;
import net.minecraft.class_742;
import net.minecraft.class_9334;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class UseTrackerFeature extends Feature implements MinecraftContext {
   private static final String TOTEMS = "Totems";
   private static final String POTIONS = "Potions";
   private static final String ITEMS = "Items";
   private static final byte USE_TOTEM_STATUS = 35;
   public final MultiSelectSetting tracked = this.register(new MultiSelectSetting("Track", List.of("Totems", "Potions", "Items"), "Totems", "Potions", "Items"));
   private final Set<Integer> announcedUses = new HashSet<>();

   public UseTrackerFeature() {
      super("Use Tracker", "Reports totems, potions and consumables used by nearby players", FeatureCategory.PLAYER, -1);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (event.getClient().field_1687 != null && event.getClient().field_1724 != null) {
         Set<Integer> stillUsing = new HashSet<>();

         for (class_1297 entity : event.getClient().field_1687.method_18112()) {
            if (entity instanceof class_1657 player && player != event.getClient().field_1724 && player.method_5805() && player.method_6115()) {
               class_1799 active = player.method_6030();
               if (this.isTracked(active)) {
                  stillUsing.add(player.method_5628());
                  if (player.method_6014() <= 1 && this.announcedUses.add(player.method_5628())) {
                     ChatUtil.info(player.method_5477().getString() + " used " + active.method_7964().getString());
                  }
               }
            }
         }

         this.announcedUses.retainAll(stillUsing);
      } else {
         this.announcedUses.clear();
      }
   }

   @EventTarget
   public void onPacket(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE
         && this.tracked.isSelected("Totems")
         && event.getPacket() instanceof class_2663 packet
         && packet.method_11470() == 35) {
         mc.execute(() -> {
            if (this.isEnabled() && mc.field_1687 != null) {
               if (packet.method_11469(mc.field_1687) instanceof class_742 player) {
                  ChatUtil.info(player == mc.field_1724 ? "You used a totem" : player.method_5477().getString() + " used a totem");
               }
            }
         });
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.announcedUses.clear();
   }

   private boolean isTracked(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return false;
      } else {
         return stack.method_7909() instanceof class_1812
            ? this.tracked.isSelected("Potions")
            : this.tracked.isSelected("Items") && (stack.method_57826(class_9334.field_50075) || stack.method_31574(class_1802.field_8103));
      }
   }
}
