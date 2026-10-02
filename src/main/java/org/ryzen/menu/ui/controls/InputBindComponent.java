package org.ryzen.menu.ui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class InputBindComponent extends Component {
   private static final String LISTENING_LABEL = "...";
   private final InputBindSetting setting;
   private float alpha = 1.0F;
   private boolean listening;

   public InputBindComponent(InputBindSetting setting) {
      this.setting = setting;
   }

   public InputBindComponent place(Component owner, int x, int y, int width, int height) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      return this;
   }

   public InputBindComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public boolean isListening() {
      return this.listening;
   }

   public void stopListening() {
      this.listening = false;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         this.listening = false;
         return false;
      } else {
         this.listening = !this.listening;
         return true;
      }
   }

   public boolean captureKey(int key) {
      if (!this.listening) {
         return false;
      }

      if (key == 259 || key == 261) {
         this.setting.clear();
      } else if (key != 256) {
         this.setting.setKey(key);
      }

      this.listening = false;
      return true;
   }

   public boolean captureMouse(int button) {
      if (this.listening && button != 0) {
         this.setting.setMouse(button);
         this.listening = false;
         return true;
      } else {
         return false;
      }
   }

   public static int pillWidth(String value) {
      float textSize = 10.0F;
      float textWidth = UiFonts.sfProDisplay().measureWidth(value == null ? "" : value, textSize, UiFontStyle.MEDIUM.letterSpacingEm() * textSize);
      return Math.max(44, Math.round(textWidth) + 16);
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(this.alpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
         .radius(this.px(4.0F))
         .border(Math.max(0.5F, this.px(0.5F)), this.alpha(this.listening ? Theme.getAccent() : Theme.Colors.OUTLINES_SMALL, this.alpha))
         .draw();
      String label = this.listening ? "..." : MenuText.bind(this.setting.getDisplayValue());
      float textSize = this.px(10.0F);
      float textY = UiFonts.sfProDisplay().centeredTextY(this.y() + this.height() / 2.0F, textSize);
      int textColor = this.listening ? Theme.getAccent() : Theme.Colors.TEXT_GHOST;
      Render2DUtil.text(this.x() + this.width() / 2.0F, textY, textSize, label)
         .style(UiFontStyle.MEDIUM)
         .color(this.alpha(textColor, this.alpha))
         .align(TextAlign.CENTER)
         .draw();
   }
}
