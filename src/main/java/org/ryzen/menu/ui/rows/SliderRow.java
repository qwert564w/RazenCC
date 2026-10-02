package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.SliderComponent;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class SliderRow extends SettingRow {
   private final NumberSetting number;
   private final SliderComponent slider;

   public SliderRow(NumberSetting setting) {
      super(setting);
      this.number = setting;
      this.slider = new SliderComponent(
            () -> (float)setting.getProgress(),
            progress -> setting.setValue(setting.getMin() + progress * (setting.getMax() - setting.getMin())),
            Theme.Colors.OUTLINES_SMALL,
            Theme.getAccent(),
            -1
         )
         .step((float)(setting.getStep() / (setting.getMax() - setting.getMin())));
   }

   @Override
   protected void placeControl(Component owner) {
      this.slider
         .place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight())
         .style(Theme.Colors.OUTLINES_SMALL, controlAccent(this.setting), -1)
         .alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      return this.slider.handleClick(mouseX, mouseY);
   }

   @Override
   public void drag(int mouseX) {
      this.slider.drag(mouseX);
   }

   @Override
   public boolean scroll(int mouseX, int mouseY, double vertical) {
      return this.slider.handleScroll(mouseX, mouseY, vertical);
   }

   @Override
   public void releasePointer() {
      this.slider.releasePointer();
   }

   @Override
   protected void renderExtras(class_310 minecraft, class_332 guiGraphicsExtractor) {
      float fontSize = 12.0F;
      float textY = this.centeredTextY(this.rowY + 20.0F, fontSize);
      Integer warning = warningColor(this.setting);
      int valueRight = warning == null ? this.controlX() - 5 : this.warningIconX() - 5;
      int color = warning == null ? Theme.Colors.SECONDARY_DARK : warning;
      this.textRight(valueRight, textY, fontSize, MenuText.numberValue(this.number), color, this.rowAlpha, UiFontStyle.REGULAR_TRACKED);
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.slider.render(minecraft, guiGraphicsExtractor);
   }

   @Override
   protected int controlWidth() {
      return 128;
   }

   @Override
   protected int controlHeight() {
      return 16;
   }

   @Override
   protected float labelRight() {
      float fontSize = 12.0F;
      String value = MenuText.numberValue(this.number);
      float valueWidth = UiFonts.sfProDisplay().measureWidth(value, fontSize, fontSize * UiFontStyle.REGULAR_TRACKED.letterSpacingEm());
      Integer warning = warningColor(this.setting);
      int valueRight = warning == null ? this.controlX() - 5 : this.warningIconX() - 5;
      return valueRight - valueWidth - 8.0F;
   }
}
