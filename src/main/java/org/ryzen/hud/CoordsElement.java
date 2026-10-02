package org.ryzen.hud;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class CoordsElement extends HudElement {
   private static final float LABEL_VALUE_GAP = 8.0F;
   private static final float TEXT_SIZE = 11.0F;
   private static final float STRIP_HEIGHT = 28.0F;
   private static final float CELL_PADDING_X = 10.0F;
   private static final float DIVIDER_HEIGHT = 16.0F;
   private static final float HEADER_ICON_LEFT = 12.0F;
   private static final float HEADER_ICON_SIZE = 16.0F;
   private static final float HEADER_TEXT_GAP = 8.0F;
   private static final float HEADER_RIGHT_PADDING = 12.0F;
   private static final float HEADER_TEXT_SIZE = 12.0F;
   private static final String[] AXES = new String[]{"X", "Y", "Z"};
   private final String[] values = new String[3];
   private final float[] cellWidths = new float[3];

   public CoordsElement() {
      super("coords", "Coordinates");
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.values[0] = coordinate(mc.field_1724.method_23317());
      this.values[1] = coordinate(mc.field_1724.method_23318());
      this.values[2] = coordinate(mc.field_1724.method_23321());
      MsdfFont font = UiFonts.sfProDisplay();
      float textSize = 11.0F * unit;
      float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
      float stripWidth = 0.0F;

      for (int i = 0; i < AXES.length; i++) {
         float contentWidth = font.measureWidth(AXES[i], textSize, letterSpacing) + 8.0F * unit + font.measureWidth(this.values[i], textSize, letterSpacing);
         this.cellWidths[i] = contentWidth + 20.0F * unit;
         stripWidth += this.cellWidths[i];
      }

      float contentCardWidth = 10.0F * unit + stripWidth;
      float headerTextSize = 12.0F * unit;
      float headerLetterSpacing = headerTextSize * UiFontStyle.MEDIUM.letterSpacingEm();
      float headerWidth = 48.0F * unit + font.measureWidth(MenuText.ui("Coordinates"), headerTextSize, headerLetterSpacing);
      this.width = Math.max(contentCardWidth, headerWidth);
      this.height = 71.0F * unit;
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      float alpha = this.appearAlpha();
      float[] inner = this.drawCard(unit, alpha, 38.0F, Textures.Icons.MOVE_3D, MenuText.ui("Coordinates"));
      float innerX = inner[0];
      float innerY = inner[1];
      float innerW = inner[2];
      MsdfFont font = UiFonts.sfProDisplay();
      float textSize = 11.0F * unit;
      float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
      float naturalStripWidth = 0.0F;

      for (float cellWidth : this.cellWidths) {
         naturalStripWidth += cellWidth;
      }

      float extraPerCell = Math.max(0.0F, innerW - naturalStripWidth) / AXES.length;
      float cursorX = innerX;
      float centerY = innerY + inner[3] / 2.0F;
      float textY = font.centeredTextY(centerY, textSize);

      for (int i = 0; i < AXES.length; i++) {
         float cellWidth = this.cellWidths[i] + extraPerCell;
         float contentX = cursorX + 10.0F * unit + extraPerCell / 2.0F;
         Render2DUtil.text(contentX, textY, textSize, AXES[i]).style(UiFontStyle.MEDIUM).color(ColorUtil.multiplyAlpha(Theme.Colors.ICON, alpha)).draw();
         float labelW = font.measureWidth(AXES[i], textSize, letterSpacing);
         float valueX = contentX + labelW + 8.0F * unit;
         Render2DUtil.text(valueX, textY, textSize, this.values[i])
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(Theme.Colors.TEXT_TEXT, alpha))
            .draw();
         if (i < AXES.length - 1) {
            float dividerHeight = 16.0F * unit;
            Render2DUtil.rect(cursorX + cellWidth, centerY - dividerHeight / 2.0F, Math.max(0.5F, unit), dividerHeight)
               .color(ColorUtil.multiplyAlpha(DIVIDER_COLOR, alpha))
               .draw();
         }

         cursorX += cellWidth;
      }
   }

   private static String coordinate(double value) {
      return Integer.toString((int)Math.floor(value));
   }
}
