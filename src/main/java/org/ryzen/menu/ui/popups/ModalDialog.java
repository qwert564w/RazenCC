package org.ryzen.menu.ui.popups;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.InputComponent;
import org.ryzen.menu.ui.controls.PillButton;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ModalDialog extends Component {
   public static final int WIDTH = 185;
   public static final int HEIGHT = 136;
   private static final int RADIUS = 16;
   private static final int PADDING = 12;
   private static final int GAP = 8;
   private static final int INPUT_HEIGHT = 28;
   private static final int BUTTON_HEIGHT = 24;
   private static final int MODAL_X = 419;
   private static final int MODAL_Y = 252;
   private final class_2960 titleIcon;
   private final String title;
   private final String[] descriptionLines;
   private final InputComponent input;
   private final Runnable onSave;
   private final ModalDialog.Extras extras;
   private final PillButton cancelButton;
   private final PillButton saveButton;
   private boolean open;
   private float pageAlpha = 1.0F;
   private int mouseX;
   private int mouseY;

   public ModalDialog(class_2960 titleIcon, String title, String[] descriptionLines, InputComponent input, Runnable onSave, ModalDialog.Extras extras) {
      this.titleIcon = titleIcon;
      this.title = title;
      this.descriptionLines = descriptionLines;
      this.input = input;
      this.onSave = onSave;
      this.extras = extras == null ? new ModalDialog.Extras() {} : extras;
      this.cancelButton = new PillButton("Cancel", this::close);
      this.saveButton = new PillButton("Save", onSave)
         .colors(ColorUtil.multiplyAlpha(Theme.Colors.SYSTEM_INFORMATION, 0.9F), Theme.Colors.SYSTEM_INFORMATION, -1);
   }

   public void place(Component owner, int mouseX, int mouseY, float pageAlpha) {
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.pageAlpha = pageAlpha;
      this.attach(owner, owner.sx(419.0F), owner.sy(252.0F), owner.px(185.0F), owner.px(136.0F));
      if (this.open) {
         this.input.place(owner, this.contentX(), this.inputY(), this.contentWidth(), 28, mouseX, mouseY).alpha(pageAlpha);
         float buttonWidth = (this.contentWidth() - 8) / 2.0F;
         this.cancelButton.place(owner, this.contentX(), this.buttonsY(), buttonWidth, 24.0F, mouseX, mouseY).alpha(pageAlpha);
         this.saveButton.place(owner, this.contentX() + buttonWidth + 8.0F, this.buttonsY(), buttonWidth, 24.0F, mouseX, mouseY).alpha(pageAlpha);
      }
   }

   public void open() {
      this.open = true;
      this.input.focus();
   }

   public void close() {
      this.open = false;
      this.input.blur();
   }

   public boolean isOpen() {
      return this.open;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         this.close();
         return true;
      }

      if (this.extras.click(this, mouseX, mouseY)) {
         return true;
      }

      if (this.input.handleClick(mouseX, mouseY)) {
         return true;
      }

      if (this.cancelButton.handleClick(mouseX, mouseY)) {
         return true;
      }

      this.saveButton.handleClick(mouseX, mouseY);
      return true;
   }

   public boolean handleKey(int key) {
      if (!this.open) {
         return false;
      } else if (key == 256) {
         this.close();
         return true;
      } else if (key != 257 && key != 335) {
         this.input.handleKey(key);
         return true;
      } else {
         this.onSave.run();
         return true;
      }
   }

   public boolean handleCharacter(int codePoint) {
      return this.open && this.input.handleCharacter(codePoint);
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.open) {
         Component owner = this.frame();
         Render2DUtil.rect(owner.x(), owner.y() + owner.px(54.0F), owner.width(), owner.height() - owner.px(54.0F))
            .color(ColorUtil.withAlpha(Theme.Colors.OVERLAY, Math.round(218.0F * this.pageAlpha)))
            .radius(0.0F, 0.0F, owner.px(12.0F), owner.px(12.0F))
            .draw();
         this.glassPanel(this.x(), this.y(), this.width(), this.height(), this.px(16.0F), this.px(20.0F), this.pageAlpha);
         float cursorY = 264.0F;
         this.texture(this.contentX(), cursorY + 1.0F, 12.0F, this.titleIcon, Theme.Colors.SECONDARY, this.pageAlpha);
         this.text(this.contentX() + 18, this.centeredTextY(cursorY + 7.0F, 12.0F), 12.0F, MenuText.ui(this.title), -1, this.pageAlpha, UiFontStyle.SEMIBOLD);
         float lineStep = UiFonts.sfProDisplay().textHeight(10.0F) + 2.0F;

         for (int index = 0; index < this.descriptionLines.length; index++) {
            this.text(
               this.contentX(),
               cursorY + 22.0F + index * lineStep,
               10.0F,
               MenuText.ui(this.descriptionLines[index]),
               Theme.Colors.SECONDARY,
               this.pageAlpha,
               UiFontStyle.REGULAR
            );
         }

         this.input.render(minecraft, guiGraphicsExtractor);
         this.cancelButton.render(minecraft, guiGraphicsExtractor);
         this.saveButton.render(minecraft, guiGraphicsExtractor);
         this.extras.render(this, this.pageAlpha);
      }
   }

   public int contentX() {
      return 431;
   }

   public int contentWidth() {
      return 161;
   }

   public int headerY() {
      return 264;
   }

   public int inputY() {
      return 316;
   }

   public int inputHeight() {
      return 28;
   }

   private int buttonsY() {
      return 352;
   }

   @Environment(EnvType.CLIENT)
   public interface Extras {
      default void render(ModalDialog modal, float alpha) {
      }

      default boolean click(ModalDialog modal, int mouseX, int mouseY) {
         return false;
      }
   }
}
