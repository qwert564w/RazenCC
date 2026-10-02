package org.ryzen.hud;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.pve.PveManagerFeature;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class PveStatusElement extends HudElement {
   private static final float PADDING = 8.0F;
   private static final float ICON_SIZE = 14.0F;
   private static final float TITLE_SIZE = 11.0F;
   private static final float ROW_SIZE = 9.0F;
   private static final float ROW_HEIGHT = 13.0F;
   private static final float MIN_WIDTH = 168.0F;
   private final List<PveStatusElement.Row> rows = new ArrayList<>();

   public PveStatusElement() {
      super("pve_status", "PvE State");
   }

   @Override
   protected float defaultY(float unit) {
      return 170.0F * unit;
   }

   @Override
   protected boolean preservePositionOnContentResize() {
      return true;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      this.rebuildRows();
      MsdfFont font = UiFonts.sfProDisplay();
      float rowSize = 9.0F * unit;
      float spacing = rowSize * UiFontStyle.MEDIUM.letterSpacingEm();
      float contentWidth = 168.0F * unit;

      for (PveStatusElement.Row row : this.rows) {
         float measured = font.measureWidth(row.label() + "  " + row.value(), rowSize, spacing);
         contentWidth = Math.max(contentWidth, measured + 16.0F * unit);
      }

      this.width = contentWidth;
      this.height = (34.0F + this.rows.size() * 13.0F) * unit;
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      float alpha = this.appearAlpha();
      this.drawPanel(13.0F * unit, unit, alpha);
      MsdfFont font = UiFonts.sfProDisplay();
      float iconSize = 14.0F * unit;
      float titleSize = 11.0F * unit;
      float headerCenterY = this.y + 15.0F * unit;
      Render2DUtil.texture(this.x + 8.0F * unit, headerCenterY - iconSize / 2.0F, iconSize, iconSize, Textures.Icons.OPTION)
         .color(ColorUtil.multiplyAlpha(Theme.getAccent(), alpha))
         .draw();
      Render2DUtil.text(this.x + 28.0F * unit, font.centeredTextY(headerCenterY, titleSize), titleSize, MenuText.ui("PvE State"))
         .style(UiFontStyle.SEMIBOLD)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
      float cursorY = this.y + 26.0F * unit;
      float rowSize = 9.0F * unit;

      for (PveStatusElement.Row row : this.rows) {
         float centerY = cursorY + 13.0F * unit / 2.0F;
         Render2DUtil.text(this.x + 8.0F * unit, font.centeredTextY(centerY, rowSize), rowSize, row.label())
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(row.accent() ? Theme.getAccent() : Theme.Colors.TEXT_TEXT, alpha))
            .draw();
         float valueWidth = font.measureWidth(row.value(), rowSize, rowSize * UiFontStyle.MEDIUM.letterSpacingEm());
         Render2DUtil.text(this.x + this.width - 8.0F * unit - valueWidth, font.centeredTextY(centerY, rowSize), rowSize, row.value())
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(Theme.Colors.SECONDARY_DARK, alpha))
            .draw();
         cursorY += 13.0F * unit;
      }
   }

   private void rebuildRows() {
      this.rows.clear();

      for (Feature feature : FeatureManager.INSTANCE.getFeatures(FeatureCategory.PVE)) {
         if (feature != PveManagerFeature.INSTANCE && feature.isEnabled()) {
            this.rows.add(new PveStatusElement.Row(feature.getName(), MenuText.ui("In development"), false));
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record Row(String label, String value, boolean accent) {
   }
}
