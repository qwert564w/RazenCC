package org.ryzen.feature.impl.movement;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12180;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2824;
import net.minecraft.class_2828;
import net.minecraft.class_746;
import net.minecraft.class_12180.class_12181;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureEnableRejectedException;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.world.ZoneGizmos;

@Environment(EnvType.CLIENT)
public final class BlinkFeature extends Feature implements PlayerContext {
   public final BooleanSetting flushOnAttack = this.register(new BooleanSetting("Flush On Attack", true));
   public final BooleanSetting autoFlush = this.register(new BooleanSetting("Auto Flush", true));
   public final NumberSetting autoFlushTicks = this.register(
      new NumberSetting("Flush Interval", 10.0, 1.0, 100.0, 1.0, " ticks").visibleWhen(this.autoFlush::getValue)
   );
   public final BooleanSetting render = this.register(new BooleanSetting("Show Position", true));
   public final ColorSetting color = this.register(new ColorSetting("Color", 6592255));
   private final List<class_2596<?>> heldPackets = new ArrayList<>();
   private class_243 serverPosition;
   private int ticksSinceFlush;
   private boolean flushing;

   public BlinkFeature() {
      super("Blink", "Queues your movement packets and releases them on demand", FeatureCategory.MOVEMENT, -1);
   }

   @Override
   protected void onEnable() {
      class_746 player = this.localPlayer();
      if (player == null) {
         throw new FeatureEnableRejectedException("no player in the world");
      }

      this.serverPosition = player.method_73189();
      this.heldPackets.clear();
      this.ticksSinceFlush = 0;
   }

   @Override
   protected void onDisable() {
      this.flush();
      this.serverPosition = null;
      this.ticksSinceFlush = 0;
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.heldPackets.clear();
      this.serverPosition = null;
      this.ticksSinceFlush = 0;
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (!this.flushing && event.getPhase() == PacketSendEvent.Phase.PRE) {
         class_2596<?> packet = event.getPacket();
         if (packet instanceof class_2828) {
            this.heldPackets.add(packet);
            event.cancel();
         } else {
            if (this.flushOnAttack.getValue() && packet instanceof class_2824 && !this.heldPackets.isEmpty()) {
               this.flush();
            }
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      if (player != null) {
         if (!player.method_5805()) {
            this.setEnabled(false);
         } else {
            if (this.autoFlush.getValue() && ++this.ticksSinceFlush >= this.autoFlushTicks.getValue().intValue()) {
               this.flush();
            }
         }
      }
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.render.getValue() && this.serverPosition != null) {
         class_238 box = new class_238(
            this.serverPosition.field_1352 - 0.3,
            this.serverPosition.field_1351,
            this.serverPosition.field_1350 - 0.3,
            this.serverPosition.field_1352 + 0.3,
            this.serverPosition.field_1351 + 1.8,
            this.serverPosition.field_1350 + 0.3
         );
         int stroke = ColorUtil.withAlpha(this.color.getValue(), 255);
         class_12181 ignored = event.getClient().field_1769.method_75414();

         try {
            class_12180.method_75553(ZoneGizmos.cube(box, stroke, ColorUtil.multiplyAlpha(stroke, 0.25F), 2.0F)).method_75533();
         } catch (Throwable var8) {
            if (ignored != null) {
               try {
                  ignored.close();
               } catch (Throwable var7) {
                  var8.addSuppressed(var7);
               }
            }

            throw var8;
         }

         if (ignored != null) {
            ignored.close();
         }
      }
   }

   private void flush() {
      class_746 player = this.localPlayer();
      this.ticksSinceFlush = 0;
      if (player != null && player.field_3944 != null && !this.heldPackets.isEmpty()) {
         List<class_2596<?>> queued = List.copyOf(this.heldPackets);
         this.heldPackets.clear();
         this.flushing = true;

         try {
            for (class_2596<?> packet : queued) {
               player.field_3944.method_52787(packet);
            }
         } finally {
            this.flushing = false;
         }

         this.serverPosition = player.method_73189();
      } else {
         if (player != null) {
            this.serverPosition = player.method_73189();
         }

         this.heldPackets.clear();
      }
   }
}
