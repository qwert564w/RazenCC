package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_2960;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_7923;
import net.minecraft.class_2338.class_2339;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.world.WorldMeshRenderer;

@Environment(EnvType.CLIENT)
public final class AncientXrayFeature extends Feature implements MinecraftContext {
   private static final int SCAN_BUDGET_PER_TICK = 65536;
   public final NumberSetting radius = this.register(new NumberSetting("Radius", 12.0, 4.0, 400.0, 1.0, " blocks"));
   public final BooleanSetting throughWalls = this.register(new BooleanSetting("Through Walls", true));
   private class_2248 targetBlock;
   private final List<class_2338> found = new ArrayList<>();
   private class_2338 scanCenter;
   private int scanRadius;
   private int scanCursor;
   private int scanRadiusSq;
   private class_2339 cursor = new class_2339();
   private boolean scanComplete;
   private class_2338 lastCompletedCenter;
   private int lastCompletedRadius;

   public AncientXrayFeature() {
      super("AncientXray", "Highlights ancient debris through walls", FeatureCategory.VISUAL, -1);
   }

   public static AncientXrayFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(AncientXrayFeature.class);
   }

   @Override
   protected void onEnable() {
      this.targetBlock = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60654("minecraft:ancient_debris"));
      if (this.targetBlock == class_2246.field_10124) {
         this.targetBlock = null;
      }

      this.resetScan();
   }

   @Override
   protected void onDisable() {
      this.found.clear();
      this.resetScan();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.found.clear();
      this.resetScan();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_746 player = event.getClient().field_1724;
      class_638 level = event.getClient().field_1687;
      if (player != null && level != null && this.targetBlock != null) {
         class_2338 center = player.method_24515();
         int r = (int)Math.round(this.radius.getValue());
         if (this.needsRescan(center, r)) {
            this.resetScan();
            this.scanCenter = center;
            this.scanRadius = r;
            this.scanRadiusSq = r * r;
            this.scanCursor = 0;
            this.found.clear();
            this.scanComplete = false;
         }

         if (this.scanCenter != null && !this.scanComplete) {
            this.scanSlice(level);
         }
      }
   }

   private boolean needsRescan(class_2338 center, int r) {
      if (this.scanCenter != null && r == this.scanRadius) {
         double rescanDist = Math.max(3.0, r * 0.25);
         double dx = center.method_10263() - this.scanCenter.method_10263();
         double dz = center.method_10260() - this.scanCenter.method_10260();
         return dx * dx + dz * dz > rescanDist * rescanDist;
      } else {
         return true;
      }
   }

   private void scanSlice(class_638 level) {
      int budget = 65536;
      int originX = this.scanCenter.method_10263();
      int originY = this.scanCenter.method_10264();
      int originZ = this.scanCenter.method_10260();
      int r = this.scanRadius;
      int rSq = this.scanRadiusSq;
      int side = r * 2 + 1;
      int plane = side * side;
      int total = plane * side;

      while (this.scanCursor < total && budget > 0) {
         int index = this.scanCursor++;
         int dy = index / plane - r;
         int remainder = index % plane;
         int dz = remainder / side - r;
         int dx = remainder % side - r;
         if (dx * dx + dy * dy + dz * dz <= rSq) {
            budget--;
            this.cursor.method_10103(originX + dx, originY + dy, originZ + dz);
            if (level.method_22340(this.cursor) && level.method_8320(this.cursor).method_27852(this.targetBlock)) {
               this.found.add(new class_2338(this.cursor));
            }
         }
      }

      if (this.scanCursor >= total) {
         this.scanComplete = true;
         this.lastCompletedCenter = this.scanCenter;
         this.lastCompletedRadius = this.scanRadius;
      }
   }

   private void resetScan() {
      this.scanCenter = null;
      this.scanRadius = 0;
      this.scanRadiusSq = 0;
      this.scanCursor = 0;
      this.scanComplete = false;
   }

   public void renderWorld() {
      if (this.isEnabled() && this.targetBlock != null && mc.field_1687 != null && mc.field_1724 != null) {
         if (this.lastCompletedCenter != null) {
            int accent = Theme.getAccent();
            int edgeColor = ColorUtil.withAlpha(accent, 0.95F);
            int whiteCore = ColorUtil.rgba(255, 255, 255, 70);
            List<WorldMeshRenderer.Line> lines = new ArrayList<>(this.found.size() * 24);

            for (class_2338 pos : this.found) {
               addBoxEdges(lines, pos, edgeColor);
               addBoxEdges(lines, pos, whiteCore, 0.3);
            }

            WorldMeshRenderer.render(new WorldMeshRenderer.WorldMesh(lines, List.of(), List.of()), this.throughWalls.getValue());
         }
      }
   }

   private static void addBoxEdges(List<WorldMeshRenderer.Line> lines, class_2338 pos, int color) {
      addBoxEdges(lines, pos, color, 0.0);
   }

   private static void addBoxEdges(List<WorldMeshRenderer.Line> lines, class_2338 pos, int color, double inset) {
      double minX = pos.method_10263() + inset;
      double minY = pos.method_10264() + inset;
      double minZ = pos.method_10260() + inset;
      double maxX = pos.method_10263() + 1.0 - inset;
      double maxY = pos.method_10264() + 1.0 - inset;
      double maxZ = pos.method_10260() + 1.0 - inset;
      class_243 a = new class_243(minX, minY, minZ);
      class_243 b = new class_243(maxX, minY, minZ);
      class_243 c = new class_243(maxX, minY, maxZ);
      class_243 d = new class_243(minX, minY, maxZ);
      class_243 e = new class_243(minX, maxY, minZ);
      class_243 f = new class_243(maxX, maxY, minZ);
      class_243 g = new class_243(maxX, maxY, maxZ);
      class_243 h = new class_243(minX, maxY, maxZ);
      lines.add(new WorldMeshRenderer.Line(a, b, color));
      lines.add(new WorldMeshRenderer.Line(b, c, color));
      lines.add(new WorldMeshRenderer.Line(c, d, color));
      lines.add(new WorldMeshRenderer.Line(d, a, color));
      lines.add(new WorldMeshRenderer.Line(e, f, color));
      lines.add(new WorldMeshRenderer.Line(f, g, color));
      lines.add(new WorldMeshRenderer.Line(g, h, color));
      lines.add(new WorldMeshRenderer.Line(h, e, color));
      lines.add(new WorldMeshRenderer.Line(a, e, color));
      lines.add(new WorldMeshRenderer.Line(b, f, color));
      lines.add(new WorldMeshRenderer.Line(c, g, color));
      lines.add(new WorldMeshRenderer.Line(d, h, color));
   }
}
