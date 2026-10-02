package org.ryzen.menu.ui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ButtonComponent extends Component {
   private final String label;
   private final Runnable action;
   private final Animation pressAnimation = new Animation(480L, Animation.Easing.EASE_OUT_QUAD);
   private float alpha = 1.0F;
   private int mouseX;
   private int mouseY;

   public ButtonComponent(String label, Runnable action) {
      this.label = label;
      this.action = action == null ? () -> {} : action;
   }

   public ButtonComponent place(Component owner, int x, int y, int width, int height, int mouseX, int mouseY) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public ButtonComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         return false;
      }

      this.pressAnimation.animate(1.0F, 0.0F, 480L, Animation.Easing.EASE_OUT_QUAD);
      this.action.run();
      return true;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      float pressed = this.pressAnimation.getValue();
      boolean hovered = this.contains(this.mouseX, this.mouseY);
      int baseBackground = hovered ? Theme.Colors.BACKGROUND_SURFACE_S : Theme.Colors.BACKGROUND_SURFACE_M;
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(ColorUtil.multiplyAlpha(ColorUtil.lerp(baseBackground, Theme.getAccent(), pressed), this.alpha))
         .radius(this.px(4.0F))
         .border(Math.max(0.5F, this.px(0.5F)), ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
         .draw();
      float textSize = this.px(12.0F);
      Render2DUtil.text(
            this.x() + this.width() / 2.0F, UiFonts.sfProDisplay().centeredTextY(this.y() + this.height() / 2.0F, textSize), textSize, MenuText.ui(this.label)
         )
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.CENTER)
         .color(ColorUtil.multiplyAlpha(ColorUtil.lerp(Theme.Colors.TEXT_GHOST, -1, pressed), this.alpha))
         .draw();
   }
}
