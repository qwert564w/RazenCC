package org.ryzen.menu.ui.controls;

import java.util.function.BooleanSupplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.AnimatedValue;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class ToggleComponent extends Component {
   private static final long ANIMATION_MS = 140L;
   private final BooleanSupplier enabledSupplier;
   private final Runnable toggleAction;
   private final AnimatedValue animation = new AnimatedValue(140L, Animation.Easing.EASE_OUT_QUAD);
   private ToggleComponent.Style style;
   private int offColor;
   private int onColor;
   private float alpha = 1.0F;

   public ToggleComponent(BooleanSupplier enabledSupplier, Runnable toggleAction, ToggleComponent.Style style, int offColor, int onColor) {
      this.enabledSupplier = enabledSupplier;
      this.toggleAction = toggleAction;
      this.style = style;
      this.offColor = offColor;
      this.onColor = onColor;
   }

   public ToggleComponent place(Component owner, int x, int y, int width, int height) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      return this;
   }

   public ToggleComponent style(ToggleComponent.Style style, int offColor, int onColor) {
      this.style = style;
      this.offColor = offColor;
      this.onColor = onColor;
      return this;
   }

   public ToggleComponent circle() {
      this.style = ToggleComponent.Style.CIRCLE;
      return this;
   }

   public ToggleComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         return false;
      }

      this.toggleAction.run();
      return true;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      float progress = this.animation.toward(this.enabledSupplier.getAsBoolean() ? 1.0F : 0.0F);
      if (this.style != ToggleComponent.Style.CIRCLE && this.style != ToggleComponent.Style.DOT) {
         this.drawSwitch(progress);
      } else {
         this.drawDot(progress);
      }
   }

   private void drawSwitch(float progress) {
      float padding = Math.max(1.0F, this.height() / 8.0F);
      float knobHeight = Math.max(1.0F, this.height() - padding * 2.0F);
      float knobWidth = Math.max(knobHeight, this.width() - this.height());
      float travel = Math.max(0.0F, this.width() - knobWidth - padding * 2.0F);
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(ColorUtil.multiplyAlpha(ColorUtil.lerp(this.offColor, this.onColor, progress), this.alpha))
         .radius(Math.max(1.0F, this.height() / 2.0F))
         .draw();
      Render2DUtil.rect(this.x() + padding + Math.round(travel * progress), this.y() + (this.height() - knobHeight) / 2.0F, knobWidth, knobHeight)
         .color(ColorUtil.multiplyAlpha(-1, this.alpha))
         .radius(Math.max(1.0F, knobHeight / 2.0F))
         .draw();
   }

   private void drawDot(float progress) {
      float diameter = Math.min(this.width(), this.height());
      float left = this.x() + (this.width() - diameter) / 2.0F;
      float top = this.y() + (this.height() - diameter) / 2.0F;
      float dot = Math.max(4.0F, diameter / 4.0F);
      if ((Math.round(dot) & 1) != (Math.round(diameter) & 1)) {
         dot++;
      }

      dot = Math.min(dot, diameter - 2.0F);
      float offset = (diameter - dot) / 2.0F;
      Render2DUtil.rect(left, top, diameter, diameter)
         .color(ColorUtil.multiplyAlpha(ColorUtil.lerp(this.offColor, this.onColor, progress), this.alpha))
         .radius(diameter)
         .draw();
      if (progress > 0.01F) {
         Render2DUtil.rect(left + offset, top + offset, dot, dot).color(ColorUtil.multiplyAlpha(-1, progress * this.alpha)).radius(dot).draw();
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Style {
      SWITCH,
      CIRCLE,
      DOT;
   }
}
