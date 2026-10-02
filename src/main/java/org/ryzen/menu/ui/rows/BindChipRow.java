package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.InputBindComponent;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class BindChipRow extends SettingRow {
   private final InputBindSetting bindSetting;
   private final InputBindComponent chip;

   public BindChipRow(InputBindSetting setting) {
      super(setting);
      this.bindSetting = setting;
      this.chip = new InputBindComponent(setting);
   }

   @Override
   protected void placeControl(Component owner) {
      this.chip.place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight()).alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      this.chip.handleClick(mouseX, mouseY);
      return true;
   }

   @Override
   public boolean key(int key) {
      return this.chip.captureKey(key);
   }

   @Override
   public boolean captureMouse(int button) {
      return this.chip.captureMouse(button);
   }

   @Override
   public boolean isCapturingBind() {
      return this.chip.isListening();
   }

   @Override
   public void closeTransient() {
      this.chip.stopListening();
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.chip.render(minecraft, guiGraphicsExtractor);
   }

   @Override
   protected int labelColor() {
      return Theme.Colors.TEXT_TEXT;
   }

   @Override
   protected int controlWidth() {
      return InputBindComponent.pillWidth(MenuText.bind(this.bindSetting.getDisplayValue()));
   }

   @Override
   protected int controlHeight() {
      return 20;
   }
}
