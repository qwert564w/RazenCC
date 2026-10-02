package org.ryzen.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.RenderContext;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class ArmorHudElement extends HudElement {
   private static final float PADDING = 8.0F;
   private static final float CELL_SIZE = 27.5F;
   private static final float CELL_GAP = 2.5F;
   private static final float ITEM_SIZE = 20.0F;
   private static final float ITEM_TOP = 3.0F;
   private static final float BAR_WIDTH = 16.0F;
   private static final float BAR_HEIGHT = 2.5F;
   private static final float BAR_BOTTOM = 3.0F;
   private static final float CELL_RADIUS = 5.0F;
   private static final float CONTENT_RADIUS = 10.0F;
   private static final class_1304[] ARMOR_ORDER = new class_1304[]{class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166};

   public ArmorHudElement() {
      super("armor_hud", "ArmorHud");
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      float innerW = 16.0F * unit + 27.5F * ARMOR_ORDER.length * unit + 2.5F * (ARMOR_ORDER.length - 1) * unit;
      float innerH = 16.0F * unit + 27.5F * unit;
      this.width = 10.0F * unit + innerW;
      this.height = 38.0F * unit + 5.0F * unit + innerH;
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      class_746 player = mc.field_1724;
      if (player != null) {
         float alpha = this.appearAlpha();
         float[] inner = this.drawCard(unit, alpha, 38.0F, Textures.Icons.SCAN_HEART, MenuText.ui("ArmorHud"));
         float innerX = inner[0];
         float innerY = inner[1];
         float innerW = inner[2];
         float innerH = inner[3];
         Render2DUtil.rect(innerX, innerY, innerW, innerH).color(ColorUtil.multiplyAlpha(ColorUtil.rgba(33, 33, 38, 102), alpha)).radius(10.0F * unit).draw();
         float cellSize = 27.5F * unit;
         float cellGap = 2.5F * unit;
         float cellsX = innerX + (innerW - (cellSize * ARMOR_ORDER.length + cellGap * (ARMOR_ORDER.length - 1))) / 2.0F;
         float cellsY = innerY + (innerH - cellSize) / 2.0F;

         for (int index = 0; index < ARMOR_ORDER.length; index++) {
            float cellX = cellsX + index * (cellSize + cellGap);
            Render2DUtil.rect(cellX, cellsY, cellSize, cellSize).color(ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_SMALL, alpha)).radius(5.0F * unit).draw();
            class_1799 stack = player.method_6118(ARMOR_ORDER[index]);
            drawDurability(stack, cellX, cellsY, cellSize, unit, alpha);
         }

         if (!(alpha < 0.75F)) {
            class_332 extractor = RenderContext.currentGuiGraphicsExtractor();
            if (extractor != null) {
               Render2DUtil.flush();
               Matrix3x2fStack pose = extractor.method_51448();
               float guiScale = mc.method_22683().method_4495();
               float itemSize = 20.0F * unit;
               float itemScale = itemSize / 16.0F;

               for (int index = 0; index < ARMOR_ORDER.length; index++) {
                  class_1799 stack = player.method_6118(ARMOR_ORDER[index]);
                  if (!stack.method_7960()) {
                     float cellX = cellsX + index * (cellSize + cellGap);
                     float itemX = cellX + (cellSize - itemSize) / 2.0F;
                     float itemY = cellsY + 3.0F * unit;
                     itemX = Math.round(itemX * guiScale) / guiScale;
                     itemY = Math.round(itemY * guiScale) / guiScale;
                     pose.pushMatrix();
                     pose.translate(itemX, itemY);
                     pose.scale(itemScale);
                     extractor.method_51427(stack, 0, 0);
                     pose.popMatrix();
                  }
               }
            }
         }
      }
   }

   private static void drawDurability(class_1799 stack, float cellX, float cellY, float cellSize, float unit, float alpha) {
      if (!stack.method_7960() && stack.method_7963()) {
         float remaining = Math.clamp((float)(stack.method_7936() - stack.method_7919()) / Math.max(1, stack.method_7936()), 0.0F, 1.0F);
         float barWidth = 16.0F * unit;
         float barHeight = 2.5F * unit;
         float barX = cellX + (cellSize - barWidth) / 2.0F;
         float barY = cellY + cellSize - 3.0F * unit - barHeight;
         Render2DUtil.rect(barX, barY, barWidth, barHeight).color(ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_MEDIUM, alpha)).radius(barHeight).draw();
         float fillWidth = barWidth * remaining;
         if (!(fillWidth <= 0.0F)) {
            int color = remaining > 0.5F ? Theme.Colors.TRAFFIC_MAXIMIZE : (remaining > 0.25F ? Theme.Colors.TRAFFIC_MINIMIZE : Theme.Colors.TRAFFIC_CLOSE);
            Render2DUtil.rect(barX, barY, Math.max(barHeight, fillWidth), barHeight).color(ColorUtil.multiplyAlpha(color, alpha)).radius(barHeight).draw();
         }
      }
   }
}
