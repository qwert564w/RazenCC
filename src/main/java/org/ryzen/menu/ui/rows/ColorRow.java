package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.ColorComponent;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class ColorRow extends SettingRow {
   private final ColorComponent color;

   public ColorRow(ColorSetting setting) {
      super(setting);
      this.color = new ColorComponent(setting::getValue, setting::setValue);
   }

   @Override
   protected void placeControl(Component owner) {
      this.color.place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight()).mouse(this.mouseX, this.mouseY).alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      return this.color.handleClick(mouseX, mouseY);
   }

   @Override
   public void closeTransient() {
      this.color.close();
   }

   @Override
   public void closeTransientImmediately() {
      this.color.closeImmediately();
   }

   @Override
   public boolean hasOpenPopup() {
      return this.color.isOpen();
   }

   @Override
   public boolean popupClick(int mouseX, int mouseY) {
      return this.color.handlePopupClick(mouseX, mouseY);
   }

   @Override
   public void renderPopupOverlay(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.color.renderOverlay();
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.color.render(minecraft, guiGraphicsExtractor);
   }

   @Override
   protected int labelColor() {
      return Theme.Colors.TEXT_TEXT;
   }

   @Override
   protected int controlWidth() {
      return 24;
   }

   @Override
   protected int controlHeight() {
      return 16;
   }
}
