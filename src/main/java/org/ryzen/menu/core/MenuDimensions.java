package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.gui.ScaleUtil;

@Environment(EnvType.CLIENT)
public record MenuDimensions(float panelWidth, float panelHeight, float panelRadius, float panelBorder) {
   private static final float DESIGN_WIDTH = 928.0F;
   private static final float DESIGN_HEIGHT = 649.0F;
   private static final float DESIGN_BORDER = 0.5F;
   private static final float SCREEN_MARGIN = 12.0F;

   public MenuDimensions(float panelWidth, float panelHeight, float panelRadius, float panelBorder) {
      if (!(panelWidth <= 0.0F) && !(panelHeight <= 0.0F) && !(panelRadius < 0.0F) && !(panelBorder < 0.0F)) {
         this.panelWidth = panelWidth;
         this.panelHeight = panelHeight;
         this.panelRadius = panelRadius;
         this.panelBorder = panelBorder;
      } else {
         throw new IllegalArgumentException("Invalid menu dimensions");
      }
   }

   public static MenuDimensions resolve(class_310 minecraft, MenuOverlayState state) {
      double guiScale = minecraft.method_22683().method_4495();
      float menuScale = MathUtil.lerp(0.525F, 2.1F, state.uiScaleValue());
      float naturalWidth = Math.max(1.0F, ScaleUtil.toGuiPixels(928.0F * menuScale, guiScale));
      float naturalHeight = Math.max(1.0F, ScaleUtil.toGuiPixels(649.0F * menuScale, guiScale));
      float availableWidth = Math.max(1.0F, minecraft.method_22683().method_4486() - 24.0F);
      float availableHeight = Math.max(1.0F, minecraft.method_22683().method_4502() - 24.0F);
      float fit = Math.min(1.0F, Math.min(availableWidth / naturalWidth, availableHeight / naturalHeight));
      return new MenuDimensions(
         Math.max(1.0F, naturalWidth * fit),
         Math.max(1.0F, naturalHeight * fit),
         Math.max(0.0F, ScaleUtil.toGuiPixels(20.0F * menuScale, guiScale) * fit),
         Math.max(0.1F, ScaleUtil.toGuiPixels(0.5F * menuScale, guiScale) * fit)
      );
   }
}
