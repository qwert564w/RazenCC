package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.ButtonComponent;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class ButtonRow extends SettingRow {
   private final ButtonComponent button;

   public ButtonRow(ButtonSetting setting) {
      super(setting);
      this.button = new ButtonComponent(setting.getButtonLabel(), setting::press);
   }

   @Override
   protected void placeControl(Component owner) {
      this.button.place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight(), this.mouseX, this.mouseY).alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      return this.button.handleClick(mouseX, mouseY);
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.button.render(minecraft, guiGraphicsExtractor);
   }

   @Override
   protected int labelColor() {
      return Theme.Colors.TEXT_TEXT;
   }

   @Override
   protected int controlWidth() {
      return 128;
   }

   @Override
   protected int controlHeight() {
      return 24;
   }
}
