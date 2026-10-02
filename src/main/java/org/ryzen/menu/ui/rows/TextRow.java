package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.InputComponent;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class TextRow extends SettingRow {
   private final InputComponent input;

   public TextRow(TextSetting setting) {
      super(setting);
      this.input = new InputComponent(setting::getValue, setting::setValue).masked(setting.isSecret());
   }

   @Override
   protected void placeControl(Component owner) {
      this.input.place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight(), this.mouseX, this.mouseY).alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      this.input.handleClick(mouseX, mouseY);
      return true;
   }

   @Override
   public boolean key(int key) {
      return this.input.handleKey(key);
   }

   @Override
   public boolean character(int codePoint) {
      return this.input.handleCharacter(codePoint);
   }

   @Override
   public void closeTransient() {
      this.input.blur();
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.input.render(minecraft, guiGraphicsExtractor);
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
