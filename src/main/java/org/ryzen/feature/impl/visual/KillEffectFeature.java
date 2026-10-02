package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4184;
import org.joml.Vector3fc;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.world.WorldMeshRenderer;

@Environment(EnvType.CLIENT)
public final class KillEffectFeature extends Feature {
   private static final String EFFECT_BEAM = "Beam";
   private static final String EFFECT_SOUL = "Soul";
   private static final String EFFECT_RUNES = "Runes";
   private static final long CREDIT_WINDOW_MS = 3000L;
   private static final int RING_SEGMENTS = 48;
   public final ModeSetting effect = this.register(new ModeSetting("Effect", "Beam", "Beam", "Soul", "Runes"));
   public final NumberSetting duration = this.register(new NumberSetting("Duration", 1200.0, 400.0, 3500.0, 50.0, " ms"));
   public final NumberSetting intensity = this.register(new NumberSetting("Intensity", 1.2, 0.1, 3.0, 0.05, ""));
   public final NumberSetting beamHeight = this.register(
      new NumberSetting("Beam Height", 10.0, 3.0, 25.0, 0.5, " blocks").visibleWhen(() -> this.effect.is("Beam"))
   );
   public final NumberSetting soulHeight = this.register(
      new NumberSetting("Soul Height", 2.5, 0.6, 6.0, 0.1, " blocks").visibleWhen(() -> this.effect.is("Soul"))
   );
   public final NumberSetting runeSize = this.register(new NumberSetting("Rune Size", 3.2, 0.8, 8.0, 0.1, " blocks").visibleWhen(() -> this.effect.is("Runes")));
   public final ColorSetting color = this.register(new ColorSetting("Color", 16777215));
   private final List<KillEffectFeature.Burst> bursts = new ArrayList<>();
   private final Map<Integer, Long> recentHits = new HashMap<>();
   private final Map<Integer, class_243> lastKnownPositions = new HashMap<>();

   public KillEffectFeature() {
      super("KillEffect", "Plays an effect where a player you killed died", FeatureCategory.VISUAL, -1);
   }

   public static KillEffectFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(KillEffectFeature.class);
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      if (event.getClient().field_1692 instanceof class_1657 player) {
         this.recentHits.put(player.method_5628(), System.currentTimeMillis());
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      if (client.field_1687 != null) {
         long now = System.currentTimeMillis();
         this.recentHits.entrySet().removeIf(entry -> now - entry.getValue() > 3000L);
         Iterator<Entry<Integer, Long>> iterator = this.recentHits.entrySet().iterator();

         while (iterator.hasNext()) {
            Entry<Integer, Long> entry = iterator.next();
            class_1297 entity = client.field_1687.method_8469(entry.getKey());
            if (entity instanceof class_1309 living && living.method_5805()) {
               this.lastKnownPositions.put(entry.getKey(), living.method_73189());
            } else {
               class_243 position = entity != null ? entity.method_73189() : this.lastKnownPositions.get(entry.getKey());
               if (position != null) {
                  this.bursts.add(new KillEffectFeature.Burst(position, now));
               }

               this.lastKnownPositions.remove(entry.getKey());
               iterator.remove();
            }
         }

         long lifetime = this.duration.getValue().longValue();
         this.bursts.removeIf(burst -> now - burst.startedAt() > lifetime);
      }
   }

   public void renderWorld() {
      if (!this.bursts.isEmpty()) {
         long now = System.currentTimeMillis();
         double lifetime = Math.max(1.0, this.duration.getValue());
         List<WorldMeshRenderer.Line> lines = new ArrayList<>();
         List<WorldMeshRenderer.Ring> rings = new ArrayList<>();
         List<WorldMeshRenderer.PlaneRect> quads = new ArrayList<>();

         for (KillEffectFeature.Burst burst : List.copyOf(this.bursts)) {
            double progress = class_3532.method_15350((now - burst.startedAt()) / lifetime, 0.0, 1.0);
            float alpha = (float)Math.pow(1.0 - progress, 1.6) * this.intensity.getValue().floatValue();
            if (!(alpha <= 0.01F)) {
               switch ((String)this.effect.getValue()) {
                  case "Soul":
                     this.buildSoul(burst, progress, alpha, quads);
                     break;
                  case "Runes":
                     this.buildRunes(burst, progress, alpha, rings);
                     break;
                  default:
                     this.buildBeam(burst, progress, alpha, lines);
               }
            }
         }

         WorldMeshRenderer.WorldMesh mesh = new WorldMeshRenderer.WorldMesh(lines, rings, quads);
         if (!mesh.isEmpty()) {
            WorldMeshRenderer.render(mesh, true);
         }
      }
   }

   private void buildBeam(KillEffectFeature.Burst burst, double progress, float alpha, List<WorldMeshRenderer.Line> lines) {
      double height = this.beamHeight.getValue() * (0.4 + progress * 0.6);
      class_243 base = burst.position();
      class_243 top = base.method_1031(0.0, height, 0.0);
      int bottomColor = ColorUtil.withAlpha(this.color.getValue(), (int)(alpha * 255.0F));
      int topColor = ColorUtil.withAlpha(this.color.getValue(), 0);
      lines.add(new WorldMeshRenderer.Line(base, top, bottomColor, topColor));

      for (int strand = 0; strand < 3; strand++) {
         double angle = progress * Math.PI * 2.0 + strand * (Math.PI * 2.0 / 3.0);
         double radius = 0.25 + progress * 0.35;
         class_243 offset = new class_243(Math.cos(angle) * radius, 0.0, Math.sin(angle) * radius);
         lines.add(new WorldMeshRenderer.Line(base.method_1019(offset), top.method_1019(offset.method_1021(0.3)), bottomColor, topColor));
      }
   }

   private void buildSoul(KillEffectFeature.Burst burst, double progress, float alpha, List<WorldMeshRenderer.PlaneRect> quads) {
      double rise = this.soulHeight.getValue() * progress;
      double size = 0.45 * (1.0 - progress * 0.55);
      class_243 center = burst.position().method_1031(0.0, 0.9 + rise, 0.0);
      class_4184 camera = class_310.method_1551().field_1773.method_19418();
      Vector3fc left = camera.method_35689();
      Vector3fc up = camera.method_19336();
      class_243 axis = new class_243(left.x(), left.y(), left.z());
      class_243 normal = new class_243(up.x(), up.y(), up.z()).method_1036(axis);
      if (!(axis.method_1027() < 1.0E-6) && !(normal.method_1027() < 1.0E-6)) {
         quads.add(
            new WorldMeshRenderer.PlaneRect(
               center, axis.method_1029(), normal.method_1029(), size, size, ColorUtil.withAlpha(this.color.getValue(), (int)(alpha * 200.0F))
            )
         );
      }
   }

   private void buildRunes(KillEffectFeature.Burst burst, double progress, float alpha, List<WorldMeshRenderer.Ring> rings) {
      class_243 center = burst.position().method_1031(0.0, 0.05, 0.0);
      class_243 u = new class_243(1.0, 0.0, 0.0);
      class_243 v = new class_243(0.0, 0.0, 1.0);
      double base = this.runeSize.getValue();

      for (int ring = 0; ring < 3; ring++) {
         double scale = 0.4 + progress * (0.6 + ring * 0.25);
         rings.add(
            new WorldMeshRenderer.Ring(
               center.method_1031(0.0, ring * 0.15, 0.0),
               u,
               v,
               base * scale * (1.0 - ring * 0.18),
               0.03 + ring * 0.01,
               ColorUtil.withAlpha(this.color.getValue(), (int)(alpha * (200 - ring * 45))),
               48
            )
         );
      }
   }

   private void clear() {
      this.bursts.clear();
      this.recentHits.clear();
      this.lastKnownPositions.clear();
   }

   @Environment(EnvType.CLIENT)
   private record Burst(class_243 position, long startedAt) {
   }
}
