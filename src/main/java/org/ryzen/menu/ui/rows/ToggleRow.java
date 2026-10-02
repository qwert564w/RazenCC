package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.ToggleComponent;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class ToggleRow extends SettingRow {
   private final ToggleComponent toggle;

   public ToggleRow(BooleanSetting setting) {
      super(setting);
      this.toggle = new ToggleComponent(
         setting::getValue, () -> setting.setValue(!setting.getValue()), ToggleComponent.Style.SWITCH, Theme.Colors.CONTROL_STRONG, Theme.getAccent()
      );
   }

   @Override
   protected void placeControl(Component owner) {
      this.toggle
         .place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight())
         .style(ToggleComponent.Style.SWITCH, Theme.Colors.CONTROL_STRONG, controlAccent(this.setting))
         .alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      return this.toggle.handleClick(mouseX, mouseY);
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.toggle.render(minecraft, guiGraphicsExtractor);
   }

   @Override
   protected int controlWidth() {
      return 36;
   }

   @Override
   protected int controlHeight() {
      return 16;
   }
}
