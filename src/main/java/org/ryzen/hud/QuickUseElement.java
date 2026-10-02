package org.ryzen.hud;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_746;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.RenderContext;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.misc.ServerHelperFeature;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class QuickUseElement extends HudElement {
   private static final float DESIGN_WIDTH = 178.691F;
   private static final float DESIGN_HEIGHT = 91.0F;
   private static final float ROW_STEP = 25.5F;
   private static final float FIRST_ROW_Y = 15.0F;
   private static final float SEPARATOR_OFFSET = 18.091F;
   private static final float LAST_ROW_BOTTOM = 25.0F;
   private static final float INNER_X = 4.964F;
   private static final float INNER_Y = 4.964F;
   private static final float INNER_WIDTH = 168.764F;
   private static final float INNER_HEIGHT = 81.073F;
   private static final float TEXT_X = 15.345F;
   private static final float ITEM_X = 119.345F;
   private static final float COUNT_RIGHT = 163.063F;
   private List<ServerHelperFeature.QuickUseEntry> entries = List.of();

   private static float rowY(int index) {
      return 15.0F + index * 25.5F;
   }

   private static float panelHeight(int rows) {
      return rows <= 0 ? 0.0F : rowY(rows - 1) + 25.0F;
   }

   public QuickUseElement() {
      super("quick_use", "QuickUse");
   }

   @Override
   protected float defaultX(float unit) {
      return 98.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 750.0F * unit;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      class_746 player = mc.field_1724;
      ServerHelperFeature helper = FeatureManager.INSTANCE.getEnabled(ServerHelperFeature.class);
      this.entries = helper != null && player != null ? helper.quickUseEntries(player) : List.of();
      if (this.entries.isEmpty() && showcase(mc)) {
         class_1799 snowballs = new class_1799(class_1802.field_8543, 14);
         this.entries = List.of(
            new ServerHelperFeature.QuickUseEntry(snowballs, 14, "CAPS"),
            new ServerHelperFeature.QuickUseEntry(snowballs, 14, "CAPS"),
            new ServerHelperFeature.QuickUseEntry(snowballs, 14, "CAPS")
         );
      }

      if (this.entries.isEmpty()) {
         this.width = 0.0F;
         this.height = 0.0F;
      } else {
         this.width = 178.691F * unit;
         this.height = panelHeight(this.entries.size()) * unit;
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      if (!this.entries.isEmpty()) {
         float alpha = this.appearAlpha();
         this.drawPanel(13.0F * unit, unit, alpha);
         Render2DUtil.rect(this.x + 4.964F * unit, this.y + 4.964F * unit, 168.764F * unit, (panelHeight(this.entries.size()) - 9.928F) * unit)
            .color(ColorUtil.multiplyAlpha(HudPalette.SURFACE, alpha))
            .radius(13.0F * unit)
            .border(0.5F * unit, ColorUtil.multiplyAlpha(HudPalette.SURFACE_BORDER, alpha))
            .blur(50.0F * unit, alpha)
            .draw();
         MsdfFont font = UiFonts.sfProDisplay();
         float textSize = 10.0F * unit;

         for (int index = 0; index < this.entries.size(); index++) {
            ServerHelperFeature.QuickUseEntry entry = this.entries.get(index);
            float rowTop = this.y + rowY(index) * unit;
            float textY = rowTop;
            String bind = compactBind(entry.bindLabel());
            Render2DUtil.text(this.x + 15.345F * unit, textY, textSize, bind).style(UiFontStyle.MEDIUM).color(ColorUtil.multiplyAlpha(-1, alpha)).draw();
            Render2DUtil.text(this.x + 163.063F * unit, textY, textSize, "x" + entry.count())
               .style(UiFontStyle.MEDIUM)
               .align(TextAlign.RIGHT)
               .color(ColorUtil.multiplyAlpha(-1, alpha))
               .draw();
            float markY = this.y + (rowY(index) + 2.655F) * unit;
            Render2DUtil.rect(this.x + 141.582F * unit, markY, 0.827F * unit, 7.445F * unit)
               .color(ColorUtil.multiplyAlpha(ColorUtil.rgba(255, 255, 255, 27), alpha))
               .radius(2.0F * unit)
               .draw();
            if (index < this.entries.size() - 1) {
               float separatorY = this.y + (rowY(index) + 18.091F) * unit;
               Render2DUtil.rect(this.x + 14.891F * unit, separatorY, 148.082F * unit, 0.827F * unit)
                  .color(ColorUtil.multiplyAlpha(HudPalette.DIVIDER, alpha))
                  .radius(2.0F * unit)
                  .draw();
            }
         }

         if (!(alpha < 0.75F)) {
            class_332 graphics = RenderContext.currentGuiGraphicsExtractor();
            if (graphics != null) {
               Render2DUtil.flush();
               Matrix3x2fStack pose = graphics.method_51448();
               float guiScale = mc.method_22683().method_4495();

               for (int index = 0; index < this.entries.size(); index++) {
                  class_1799 stack = this.entries.get(index).icon();
                  if (!stack.method_7960()) {
                     float itemSize = 11.0F * unit;
                     float itemX = this.x + 119.345F * unit;
                     float itemY = this.y + (rowY(index) + 1.0F) * unit;
                     itemX = Math.round(itemX * guiScale) / guiScale;
                     itemY = Math.round(itemY * guiScale) / guiScale;
                     pose.pushMatrix();
                     pose.translate(itemX, itemY);
                     pose.scale(itemSize / 16.0F);
                     graphics.method_51427(stack, 0, 0);
                     pose.popMatrix();
                  }
               }
            }
         }
      }
   }

   private static String compactBind(String label) {
      if (label != null && !label.isBlank()) {
         return switch (label) {
            case "Mouse Left" -> "M1";
            case "Mouse Right" -> "M2";
            case "Mouse Middle" -> "M3";
            default -> label.startsWith("Mouse ") ? "M" + label.substring(6) : label.toUpperCase();
         };
      } else {
         return "CAPS";
      }
   }
}
