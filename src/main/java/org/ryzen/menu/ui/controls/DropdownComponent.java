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
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public abstract class DropdownComponent extends Component {
   private static final int WARNING_ICON_SIZE = 12;
   private static final int WARNING_ICON_GAP = 5;
   private final Supplier<String> valueSupplier;
   private final MarqueeText valueText;
   private DropdownComponent.Style style = DropdownComponent.Style.VALUE;
   private int valueColor = Theme.Colors.ICON;
   private Integer warningColor;
   private float alpha = 1.0F;

   protected DropdownComponent(Supplier<String> valueSupplier) {
      this.valueSupplier = valueSupplier;
      this.valueText = new MarqueeText(this::displayValue);
   }

   public DropdownComponent place(Component owner, int x, int y, int width, int height) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      return this;
   }

   public DropdownComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public DropdownComponent style(DropdownComponent.Style style) {
      this.style = style == null ? DropdownComponent.Style.VALUE : style;
      return this;
   }

   public DropdownComponent color(int color) {
      this.valueColor = color;
      return this;
   }

   public DropdownComponent warning(Integer color) {
      this.warningColor = color;
      return this;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      return this.contains(mouseX, mouseY);
   }

   protected String displayValue() {
      return this.valueSupplier.get();
   }

   protected String value() {
      return this.valueSupplier.get();
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.style == DropdownComponent.Style.PILL) {
         this.renderPill();
      } else {
         this.renderValue(minecraft);
      }
   }

   private void renderValue(class_310 minecraft) {
      int color = ColorUtil.multiplyAlpha(this.valueColor, this.alpha);
      float gap = this.px(4.0F);
      float iconSize = this.px(24.0F);
      float iconPadding = this.px(4.0F);
      float iconX = this.x() + this.width() - iconSize + iconPadding;
      float textRight = this.x() + this.width() - iconSize - gap;
      float warningReserve = this.warningColor == null ? 0.0F : this.px(17.0F);
      float textLeft = this.x() + warningReserve;
      float maxTextWidth = Math.max(1.0F, textRight - textLeft);
      if (this.warningColor != null) {
         float warningSize = this.px(12.0F);
         float warningX = this.x();
         float warningY = this.y() + (this.height() - warningSize) / 2.0F + 1.0F;
         Render2DUtil.texture(warningX, warningY, warningSize, warningSize, Textures.Icons.TRIANGLE_ALERT)
            .color(ColorUtil.multiplyAlpha(this.warningColor, this.alpha))
            .draw();
      }

      class_310 client = minecraft != null ? minecraft : class_310.method_1551();
      int mouseX = (int)Math.round(client.field_1729.method_68879(client.method_22683()));
      int mouseY = (int)Math.round(client.field_1729.method_68883(client.method_22683()));
      this.valueText
         .placeAt(this, textLeft, this.y(), maxTextWidth, this.height(), mouseX, mouseY)
         .style(12.0F, UiFontStyle.MEDIUM, this.valueColor, this.alpha)
         .align(TextAlign.RIGHT)
         .render(client, null);
      Render2DUtil.texture(
            iconX,
            this.y() + iconPadding,
            Math.max(0.0F, iconSize - iconPadding * 2.0F),
            Math.max(0.0F, iconSize - iconPadding * 2.0F),
            Textures.Icons.CHEVRON_DOWN
         )
         .color(color)
         .draw();
   }

   private void renderPill() {
      float iconSize = this.px(12.0F);
      float iconX = this.x() + this.width() - this.px(8.0F) - iconSize;
      float textX = this.x() + this.px(16.0F);
      float textSize = this.px(14.0F);
      float textY = UiFonts.sfProDisplay().centeredTextY(this.y() + this.height() / 2.0F, textSize);
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_MEDIUM, this.alpha))
         .radius(999.0F)
         .draw();
      Render2DUtil.text(textX, textY, textSize, this.value()).style(UiFontStyle.MEDIUM).color(ColorUtil.multiplyAlpha(-1, this.alpha)).draw();
      Render2DUtil.texture(iconX, this.y() + (this.height() - iconSize) / 2.0F, iconSize, iconSize, Textures.Icons.CHEVRONS_LEFT_RIGHT)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.ICON, this.alpha))
         .draw();
   }

   public static int pillWidth(String value) {
      float textWidth = UiFonts.sfProDisplay().measureWidth(value == null ? "" : value, 14.0F, UiFontStyle.MEDIUM.letterSpacingEm() * 14.0F);
      return Math.max(48, Math.round(textWidth) + 44);
   }

   @Environment(EnvType.CLIENT)
   public enum Style {
      VALUE,
      PILL;
   }
}
