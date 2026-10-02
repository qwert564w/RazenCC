package org.ryzen.menu.ui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class PillButton extends Component {
   private final Runnable action;
   private String label;
   private float textSize = 11.0F;
   private int idleBackground = Theme.Colors.CONTROL;
   private int hoverBackground = Theme.Colors.SURFACE_HOVER;
   private int textColor = Theme.Colors.SECONDARY;
   private float alpha = 1.0F;
   private int mouseX;
   private int mouseY;

   public PillButton(String label, Runnable action) {
      this.label = label;
      this.action = action == null ? () -> {} : action;
   }

   public PillButton place(Component owner, float x, float y, float width, float height, int mouseX, int mouseY) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public PillButton label(String label) {
      this.label = label;
      return this;
   }

   public PillButton textSize(float textSize) {
      this.textSize = textSize;
      return this;
   }

   public PillButton colors(int idleBackground, int hoverBackground, int textColor) {
      this.idleBackground = idleBackground;
      this.hoverBackground = hoverBackground;
      this.textColor = textColor;
      return this;
   }

   public PillButton alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         return false;
      }

      this.action.run();
      return true;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      boolean hovered = this.contains(this.mouseX, this.mouseY);
      float designX = this.designX(this.x());
      float designY = this.designY(this.y());
      float designWidth = this.designX(this.x() + this.width()) - designX;
      float designHeight = this.designY(this.y() + this.height()) - designY;
      this.rect(designX, designY, designWidth, designHeight, hovered ? this.hoverBackground : this.idleBackground, 999.0F, this.alpha);
      this.textCentered(
         designX + designWidth / 2.0F,
         this.centeredTextY(designY + designHeight / 2.0F, this.textSize),
         this.textSize,
         MenuText.ui(this.label),
         this.textColor,
         this.alpha,
         UiFontStyle.MEDIUM
      );
   }
}
