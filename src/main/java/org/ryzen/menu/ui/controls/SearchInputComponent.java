package org.ryzen.menu.ui.controls;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class SearchInputComponent extends Component {
   private static final String PLACEHOLDER = "Start typing to search...";
   private static final int PADDING_X = 16;
   private static final int ICON_SIZE = 16;
   private static final int GAP = 10;
   private static final int TEXT_SIZE = 12;
   private final Supplier<String> querySupplier;
   private final Supplier<String> suggestionSupplier;
   private int mouseX;
   private int mouseY;
   private boolean focused;
   private boolean selected;
   private float alpha = 1.0F;

   public SearchInputComponent(Supplier<String> querySupplier, Supplier<String> suggestionSupplier) {
      this.querySupplier = querySupplier;
      this.suggestionSupplier = suggestionSupplier;
   }

   public SearchInputComponent place(Component owner, int x, int y, int width, int height, int mouseX, int mouseY) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public SearchInputComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public SearchInputComponent focused(boolean focused) {
      this.focused = focused;
      return this;
   }

   public SearchInputComponent selected(boolean selected) {
      this.selected = selected;
      return this;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      String query = this.querySupplier.get();
      if (query == null) {
         query = "";
      }

      String suggestion = this.suggestionSupplier.get();
      if (suggestion == null || query.isEmpty()) {
         suggestion = "";
      }

      boolean hasInput = !query.isEmpty();
      boolean active = this.focused || !hasInput && this.contains(this.mouseX, this.mouseY);
      int background = active ? Theme.Colors.OUTLINES_LARGE : Theme.Colors.OUTLINES_MEDIUM;
      int border = active ? Theme.Colors.OUTLINES_LARGE : Theme.Colors.OUTLINES_SMALL;
      UiFontStyle textStyle = hasInput ? UiFontStyle.MEDIUM : UiFontStyle.REGULAR;
      String displayValue = hasInput ? query : (this.focused ? "" : "Start typing to search...");
      float textSize = this.px(12.0F);
      float letterSpacing = textStyle.letterSpacingEm() * textSize;
      float textX = this.x() + this.px(42.0F);
      float maxTextWidth = Math.max(1.0F, this.width() - this.px(58.0F));
      float queryWidth = UiFonts.sfProDisplay().measureWidth(displayValue, textSize, letterSpacing);
      float suggestionWidth = suggestion.isEmpty() ? 0.0F : UiFonts.sfProDisplay().measureWidth(suggestion, textSize, letterSpacing);
      float joinSpacing = hasInput && !suggestion.isEmpty() ? letterSpacing : 0.0F;
      float visualTextWidth = queryWidth + joinSpacing + suggestionWidth;
      float drawX = visualTextWidth > maxTextWidth ? textX - Math.round(visualTextWidth - maxTextWidth) : textX;
      float iconY = this.y() + (this.height() - this.px(16.0F)) / 2.0F;
      float textY = UiFonts.sfProDisplay().centeredTextY(this.y() + this.height() / 2.0F, textSize);
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(ColorUtil.multiplyAlpha(background, this.alpha))
         .radius(Math.max(1.0F, this.height() / 2.0F))
         .border(Math.max(0.5F, this.px(0.5F)), ColorUtil.multiplyAlpha(border, this.alpha))
         .draw();
      Render2DUtil.texture(this.x() + this.px(16.0F), iconY, this.px(16.0F), this.px(16.0F), Textures.Header.SEARCH)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.ICON, this.alpha))
         .draw();
      Render2DUtil.pushScissor(textX, this.y() + this.px(12.0F), maxTextWidth, Math.max(1.0F, this.height() - this.px(24.0F)));
      if (this.focused && this.selected && hasInput) {
         Render2DUtil.rect(drawX, this.y() + this.px(12.0F), queryWidth, Math.max(1.0F, this.height() - this.px(24.0F)))
            .color(ColorUtil.multiplyAlpha(Theme.getAccent(), this.alpha * 0.55F))
            .radius(this.px(2.0F))
            .draw();
      }

      Render2DUtil.text(drawX, textY, textSize, displayValue)
         .style(textStyle)
         .color(ColorUtil.multiplyAlpha(hasInput ? -1 : Theme.Colors.ICON, this.alpha))
         .draw();
      if (!suggestion.isEmpty()) {
         Render2DUtil.text(drawX + Math.round(queryWidth + joinSpacing), textY, textSize, suggestion)
            .style(textStyle)
            .color(ColorUtil.multiplyAlpha(Theme.Colors.ICON, this.alpha))
            .draw();
      }

      if (this.focused && !this.selected && System.currentTimeMillis() / 500L % 2L == 0L) {
         float caretX = drawX + Math.round(queryWidth) + this.px(2.0F);
         Render2DUtil.rect(caretX, this.y() + this.px(16.0F), Math.max(1.0F, this.px(1.0F)), this.px(16.0F))
            .color(ColorUtil.multiplyAlpha(-1, this.alpha))
            .draw();
      }

      Render2DUtil.popScissor();
   }
}
