package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.particles.WorldParticleRenderer;
import org.ryzen.utils.render.world.WorldMeshRenderer;

@Environment(EnvType.CLIENT)
public final class CubesFeature extends Feature implements MinecraftContext {
   private static final double SPAWN_RADIUS = 20.0;
   private static final double SPAWN_HEIGHT = 5.0;
   private static final long MIN_LIFETIME_MILLIS = 1500L;
   private static final long MAX_LIFETIME_MILLIS = 4500L;
   private static final float BILLBOARD_SCALE = 2.0F;
   private static final float BILLBOARD_ALPHA = 0.4F;
   private static final float EDGE_ALPHA = 0.8F;
   private static final float DIAGONAL_ALPHA = 0.4F;
   private static final double DRIFT_PER_SECOND = 0.6;
   private static final double SPIN_PER_SECOND = 1.2;
   private static final Vector3f[] CORNERS = new Vector3f[]{
      new Vector3f(-0.5F, -0.5F, -0.5F),
      new Vector3f(-0.5F, -0.5F, 0.5F),
      new Vector3f(-0.5F, 0.5F, -0.5F),
      new Vector3f(-0.5F, 0.5F, 0.5F),
      new Vector3f(0.5F, -0.5F, -0.5F),
      new Vector3f(0.5F, -0.5F, 0.5F),
      new Vector3f(0.5F, 0.5F, -0.5F),
      new Vector3f(0.5F, 0.5F, 0.5F)
   };
   private static final int[][] EDGES = new int[][]{{0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3}, {2, 6}, {3, 7}, {4, 5}, {4, 6}, {5, 7}, {6, 7}};
   private static final int[][] DIAGONALS = new int[][]{{0, 7}, {1, 6}, {2, 5}, {3, 4}};
   public final NumberSetting count = this.register(new NumberSetting("Count", 100.0, 10.0, 500.0, 10.0, ""));
   public final NumberSetting size = this.register(new NumberSetting("Size", 0.2, 0.05, 1.0, 0.05, " blocks"));
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1, "x"));
   public final ModeSetting colorMode = this.register(ColorMode.setting());
   public final ColorSetting color = this.register(new ColorSetting("Color", -14188801).visibleWhen(() -> ColorMode.isCustom(this.colorMode)));
   private final List<CubesFeature.Cube> cubes = new ArrayList<>();
   private final List<WorldParticleRenderer.Sprite> sprites = new ArrayList<>();
   private final WorldParticleRenderer particleRenderer = new WorldParticleRenderer();
   private final Random random = new Random();
   private long lastUpdateNanos;

   public CubesFeature() {
      super("Cubes", "Drifting wireframe cubes around the player", FeatureCategory.VISUAL, -1);
   }

   public static CubesFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(CubesFeature.class);
   }

   @Override
   protected void onEnable() {
      this.lastUpdateNanos = System.nanoTime();
   }

   @Override
   protected void onDisable() {
      this.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clear();
   }

   private void clear() {
      this.cubes.clear();
      this.sprites.clear();
   }

   public void renderWorld() {
      if (!this.inGame()) {
         this.clear();
      } else {
         this.update();
         this.drawBillboards();
         this.drawWireframes();
      }
   }

   private void update() {
      long now = System.nanoTime();
      float elapsed = this.lastUpdateNanos == 0L ? 0.0F : (float)Math.min((now - this.lastUpdateNanos) / 1.0E9, 0.1);
      this.lastUpdateNanos = now;
      float speedFactor = this.speed.getValue().floatValue();
      Iterator<CubesFeature.Cube> iterator = this.cubes.iterator();

      while (iterator.hasNext()) {
         if (!iterator.next().update(elapsed, speedFactor, now)) {
            iterator.remove();
         }
      }

      int target = this.count.getValue().intValue();

      while (this.cubes.size() < target) {
         this.cubes.add(this.spawn(now));
      }
   }

   private CubesFeature.Cube spawn(long nowNanos) {
      class_243 position = this.player().method_73189().method_1031(this.random(-20.0, 20.0), this.random(0.0, 5.0), this.random(-20.0, 20.0));
      class_243 drift = new class_243(this.random(-1.0, 1.0), this.random(0.0, 2.0), this.random(-1.0, 1.0));
      class_243 spin = new class_243(this.random(-1.0, 1.0), this.random(-1.0, 1.0), this.random(-1.0, 1.0));
      long lifetime = (long)this.random(1500.0, 4500.0);
      return new CubesFeature.Cube(position, drift, spin, lifetime, nowNanos);
   }

   private void drawBillboards() {
      this.sprites.clear();
      float halfSize = this.size.getValue().floatValue() * 2.0F;

      for (int index = 0; index < this.cubes.size(); index++) {
         CubesFeature.Cube cube = this.cubes.get(index);
         this.sprites.add(new WorldParticleRenderer.Sprite(cube.position, halfSize, ColorUtil.withAlpha(this.gradient(index), cube.alpha * 0.4F)));
      }

      if (!this.sprites.isEmpty()) {
         this.particleRenderer.render(this.sprites, 0.0F);
      }
   }

   private void drawWireframes() {
      if (!this.cubes.isEmpty()) {
         float scale = this.size.getValue().floatValue();
         List<WorldMeshRenderer.Line> lines = new ArrayList<>(this.cubes.size() * (EDGES.length + DIAGONALS.length));
         Vector3f[] corners = new Vector3f[CORNERS.length];

         for (int index = 0; index < this.cubes.size(); index++) {
            CubesFeature.Cube cube = this.cubes.get(index);
            int base = this.gradient(index);
            int edgeColor = ColorUtil.withAlpha(base, cube.alpha * 0.8F);
            int diagonalColor = ColorUtil.withAlpha(base, cube.alpha * 0.4F);
            Quaternionf rotation = new Quaternionf()
               .rotationXYZ((float)cube.rotation.field_1352, (float)cube.rotation.field_1351, (float)cube.rotation.field_1350);

            for (int corner = 0; corner < CORNERS.length; corner++) {
               corners[corner] = rotation.transform(new Vector3f(CORNERS[corner]).mul(scale));
            }

            for (int[] edge : EDGES) {
               lines.add(line(cube.position, corners[edge[0]], corners[edge[1]], edgeColor));
            }

            for (int[] diagonal : DIAGONALS) {
               lines.add(line(cube.position, corners[diagonal[0]], corners[diagonal[1]], diagonalColor));
            }
         }

         WorldMeshRenderer.render(new WorldMeshRenderer.WorldMesh(lines, List.of(), List.of()), false);
      }
   }

   private static WorldMeshRenderer.Line line(class_243 center, Vector3f from, Vector3f to, int color) {
      return new WorldMeshRenderer.Line(center.method_1031(from.x, from.y, from.z), center.method_1031(to.x, to.y, to.z), color);
   }

   private int gradient(int index) {
      if (ColorMode.isCustom(this.colorMode)) {
         return this.color.getValue();
      }

      float progress = (float)index / Math.max(1, this.cubes.size());
      return ColorUtil.lerp(Theme.accent(0), Theme.accent(1), progress);
   }

   private double random(double min, double max) {
      return min + (max - min) * this.random.nextDouble();
   }

   @Environment(EnvType.CLIENT)
   private static final class Cube {
      private final class_243 drift;
      private final class_243 spin;
      private final long lifetimeMillis;
      private final long spawnedAtNanos;
      private class_243 position;
      private class_243 rotation = class_243.field_1353;
      private float alpha = 1.0F;

      private Cube(class_243 position, class_243 drift, class_243 spin, long lifetimeMillis, long spawnedAtNanos) {
         this.position = position;
         this.drift = drift;
         this.spin = spin;
         this.lifetimeMillis = lifetimeMillis;
         this.spawnedAtNanos = spawnedAtNanos;
      }

      private boolean update(float elapsedSeconds, float speedFactor, long nowNanos) {
         this.position = this.position.method_1019(this.drift.method_1021(0.6 * speedFactor * elapsedSeconds));
         this.rotation = this.rotation.method_1019(this.spin.method_1021(1.2 * speedFactor * elapsedSeconds));
         long ageMillis = (nowNanos - this.spawnedAtNanos) / 1000000L;
         this.alpha = Math.max(0.0F, 1.0F - (float)ageMillis / (float)this.lifetimeMillis);
         return this.alpha > 0.0F;
      }
   }
}
