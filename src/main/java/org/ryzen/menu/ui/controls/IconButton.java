package org.ryzen.menu.ui.controls;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class IconButton extends Component {
   private final Runnable action;
   private class_2960 icon;
   private int iconSize;
   private int backgroundRadius = 6;
   private int idleTint = Theme.Colors.ICON;
   private int hoverTint = -1;
   private int activeTint = -1;
   private int disabledTint = Theme.Colors.ICON_GHOST;
   private Integer hoverBackground;
   private Integer activeBackground;
   private Integer activeHoverBackground;
   private boolean active;
   private boolean enabled = true;
   private float alpha = 1.0F;
   private int mouseX;
   private int mouseY;

   public IconButton(class_2960 icon, int iconSize, Runnable action) {
      this.icon = icon;
      this.iconSize = iconSize;
      this.action = action == null ? () -> {} : action;
   }

   public IconButton place(Component owner, float x, float y, int box, int mouseX, int mouseY) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(box), owner.px(box));
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public IconButton placeAt(Component owner, float screenX, float screenY, float boxPx, int mouseX, int mouseY) {
      this.attach(owner, screenX, screenY, boxPx, boxPx);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public IconButton icon(class_2960 icon) {
      this.icon = icon;
      return this;
   }

   public IconButton iconSize(int iconSize) {
      this.iconSize = iconSize;
      return this;
   }

   public IconButton radius(int backgroundRadius) {
      this.backgroundRadius = backgroundRadius;
      return this;
   }

   public IconButton tint(int idleTint, int hoverTint) {
      this.idleTint = idleTint;
      this.hoverTint = hoverTint;
      return this;
   }

   public IconButton activeTint(int activeTint) {
      this.activeTint = activeTint;
      return this;
   }

   public IconButton disabledTint(int disabledTint) {
      this.disabledTint = disabledTint;
      return this;
   }

   public IconButton hoverBackground(Integer color) {
      this.hoverBackground = color;
      return this;
   }

   public IconButton activeBackground(Integer color, Integer hoveredColor) {
      this.activeBackground = color;
      this.activeHoverBackground = hoveredColor;
      return this;
   }

   public IconButton active(boolean active) {
      this.active = active;
      return this;
   }

   public IconButton enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
   }

   public IconButton alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public boolean hovered() {
      return this.enabled && this.contains(this.mouseX, this.mouseY);
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (this.enabled && this.contains(mouseX, mouseY)) {
         this.action.run();
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      boolean hovered = this.hovered();
      Integer background = this.resolveBackground(hovered);
      if (background != null) {
         Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
            .color(ColorUtil.multiplyAlpha(background, this.alpha))
            .radius(this.px(this.backgroundRadius))
            .draw();
      }

      float iconPx = this.px(this.iconSize);
      float inset = (this.width() - iconPx) / 2.0F;
      Render2DUtil.texture(this.x() + inset, this.y() + inset, iconPx, iconPx, this.icon)
         .color(ColorUtil.multiplyAlpha(this.resolveTint(hovered), this.alpha))
         .draw();
   }

   private Integer resolveBackground(boolean hovered) {
      if (!this.active) {
         return hovered ? this.hoverBackground : null;
      } else {
         return hovered && this.activeHoverBackground != null ? this.activeHoverBackground : this.activeBackground;
      }
   }

   private int resolveTint(boolean hovered) {
      if (!this.enabled) {
         return this.disabledTint;
      } else if (this.active) {
         return this.activeTint;
      } else {
         return hovered ? this.hoverTint : this.idleTint;
      }
   }
}
