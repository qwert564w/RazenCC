package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_2664;
import net.minecraft.class_2675;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AntiCrashFeature extends Feature implements MinecraftContext {
   private static final double DEFAULT_LIMIT = 1.0E9;
   private static final long NOTICE_INTERVAL_MS = 1000L;
   public final NumberSetting limit = this.register(new NumberSetting("Limit", 1.0E9, 1000000.0, 1.0E12, 1000000.0, ""));
   public final BooleanSetting explosions = this.register(new BooleanSetting("Explosions", true));
   public final BooleanSetting particles = this.register(new BooleanSetting("Particles", true));
   public final BooleanSetting notify = this.register(new BooleanSetting("Notify", true));
   private long lastNoticeAt;

   public AntiCrashFeature() {
      super("AntiCrash", "Blocks explosion and particle packets built to crash the client", FeatureCategory.MISC, -1);
   }

   public static AntiCrashFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(AntiCrashFeature.class);
   }

   @Override
   protected void onDisable() {
      this.lastNoticeAt = 0L;
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         if (event.getPacket() instanceof class_2664 packet) {
            if (this.explosions.getValue() && this.isAbsurdExplosion(packet)) {
               event.cancel();
               this.notice("blocked an explosion packet");
            }
         } else {
            if (event.getPacket() instanceof class_2675 packet && this.particles.getValue() && this.isAbsurdParticle(packet)) {
               event.cancel();
               this.notice("blocked a particle packet");
            }
         }
      }
   }

   private boolean isAbsurdExplosion(class_2664 packet) {
      double bound = this.limit.getValue();
      class_243 center = packet.comp_2883();
      return isFinite(center, bound) && !exceeds(packet.comp_4594(), bound)
         ? packet.comp_2884().map(knockback -> !isFinite(knockback, bound)).orElse(false)
         : true;
   }

   private boolean isAbsurdParticle(class_2675 packet) {
      double bound = this.limit.getValue();
      return exceeds(packet.method_11544(), bound)
         || exceeds(packet.method_11547(), bound)
         || exceeds(packet.method_11546(), bound)
         || exceeds(packet.method_11548(), bound)
         || exceeds(packet.method_11549(), bound)
         || exceeds(packet.method_11550(), bound)
         || exceeds(packet.method_11543(), bound);
   }

   private static boolean isFinite(class_243 vector, double bound) {
      return !exceeds(vector.field_1352, bound) && !exceeds(vector.field_1351, bound) && !exceeds(vector.field_1350, bound);
   }

   private static boolean exceeds(double value, double bound) {
      return Double.isNaN(value) || Math.abs(value) > bound;
   }

   private void notice(String what) {
      if (this.notify.getValue()) {
         long now = System.currentTimeMillis();
         if (now - this.lastNoticeAt >= 1000L) {
            this.lastNoticeAt = now;
            mc.execute(() -> ChatUtil.info("AntiCrash: " + what));
         }
      }
   }
}
