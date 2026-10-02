package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2663;
import net.minecraft.class_2960;
import net.minecraft.class_742;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class PopChamsFeature extends Feature implements MinecraftContext {
   public static final byte USE_TOTEM_STATUS = 35;
   private static final long FALLBACK_ANIMATION_DURATION_NANOS = 1000000000L;
   public final BooleanSetting blending = this.register(new BooleanSetting("Additive Glow", true).configKey("render.popchams.blending"));
   public final BooleanSetting textured = this.register(new BooleanSetting("Textured", false).configKey("render.popchams.textured"));
   public final ColorSetting color = this.register(new ColorSetting("Color", -1).configKey("render.popchams.color"));
   public final NumberSetting glowRadius = this.register(new NumberSetting("Glow Radius", 6.0, 1.0, 12.0, 0.05F, "px").configKey("render.chams.glowRadius"));
   private final List<PopChamsFeature.Snapshot> snapshots = new ArrayList<>();

   public PopChamsFeature() {
      super("PopChams", "Leaves a rising ghost model when a player uses a totem", FeatureCategory.VISUAL, -1);
   }

   public static PopChamsFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(PopChamsFeature.class);
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_2663 packet && packet.method_11470() == 35) {
         mc.execute(() -> this.capture(packet));
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   private void capture(class_2663 packet) {
      if (this.isEnabled() && mc.field_1687 != null) {
         if (packet.method_11469(mc.field_1687) instanceof class_742 player) {
            class_2960 texture = player.method_52814().comp_1626().comp_3627();
            if (texture != null) {
               this.snapshots
                  .add(
                     new PopChamsFeature.Snapshot(
                        texture,
                        this.color.getValue(),
                        this.textured.getValue(),
                        player.method_23317(),
                        player.method_23318(),
                        player.method_23321(),
                        player.field_6283,
                        player.field_6241 - player.field_6259,
                        player.method_36455(),
                        player.field_42108.method_48569(),
                        player.field_42108.method_48566(),
                        System.nanoTime()
                     )
                  );
            }
         }
      }
   }

   public List<PopChamsFeature.Snapshot> activeSnapshots(long nowNanos) {
      this.snapshots.removeIf(snapshot -> snapshot.expired(nowNanos));
      return this.snapshots.isEmpty() ? List.of() : List.copyOf(this.snapshots);
   }

   public int effectiveGlowRadius() {
      float value = this.glowRadius.getValue().floatValue();
      return value <= 0.0F ? 6 : Math.round(value);
   }

   private void clear() {
      this.snapshots.clear();
   }

   @Environment(EnvType.CLIENT)
   public record Snapshot(
      class_2960 texture,
      int baseColor,
      boolean textured,
      double x,
      double y,
      double z,
      float bodyYaw,
      float relativeHeadYaw,
      float pitch,
      float limbProgress,
      float limbSpeed,
      long startedAtNanos
   ) {
      public float animation(long nowNanos) {
         float progress = Math.clamp((float)(nowNanos - this.startedAtNanos) / 1.0E9F, 0.0F, 1.0F);
         return 1.0F - easeOutQuart(progress);
      }

      public boolean expired(long nowNanos) {
         return nowNanos - this.startedAtNanos >= 1000000000L;
      }

      private static float easeOutQuart(float value) {
         float inverse = 1.0F - value;
         return 1.0F - inverse * inverse * inverse * inverse;
      }
   }
}
