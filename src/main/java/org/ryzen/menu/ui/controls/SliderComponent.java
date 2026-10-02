package org.ryzen.menu.ui.controls;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.AnimatedValue;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class SliderComponent extends Component {
   private static final long ANIMATION_MS = 140L;
   private final Supplier<Float> valueSupplier;
   private final SliderComponent.ValueConsumer valueConsumer;
   private final AnimatedValue animation = new AnimatedValue(140L, Animation.Easing.EASE_OUT_QUAD);
   private int trackColor;
   private int fillColor;
   private int knobColor;
   private float alpha = 1.0F;
   private boolean enabled = true;
   private boolean dragging;
   private float step;
   private double anchorValue;
   private double anchorMouseX;
   private Runnable releaseAction = () -> {};

   public SliderComponent(Supplier<Float> valueSupplier, SliderComponent.ValueConsumer valueConsumer, int trackColor, int fillColor, int knobColor) {
      this.valueSupplier = valueSupplier;
      this.valueConsumer = valueConsumer;
      this.trackColor = trackColor;
      this.fillColor = fillColor;
      this.knobColor = knobColor;
   }

   public SliderComponent place(Component owner, int x, int y, int width, int height) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      return this;
   }

   public SliderComponent style(int trackColor, int fillColor, int knobColor) {
      this.trackColor = trackColor;
      this.fillColor = fillColor;
      this.knobColor = knobColor;
      return this;
   }

   public SliderComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public SliderComponent enabled(boolean enabled) {
      if (!enabled && this.dragging) {
         this.dragging = false;
         this.releaseAction.run();
      }

      this.enabled = enabled;
      return this;
   }

   public SliderComponent onRelease(Runnable releaseAction) {
      this.releaseAction = releaseAction == null ? () -> {} : releaseAction;
      return this;
   }

   public SliderComponent step(float step) {
      this.step = Math.max(0.0F, step);
      return this;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (this.enabled && this.contains(mouseX, mouseY)) {
         this.dragging = true;
         double mouse = this.preciseMouseX(mouseX);
         double current = MathUtil.clamp01(this.valueSupplier.get());
         double knobCenter = this.innerX() + current * this.innerWidth();
         double knobRadius = this.height() / 2.0 + 1.0;
         if (Math.abs(mouse - knobCenter) > knobRadius) {
            this.updateValue(mouseX);
            current = MathUtil.clamp01(this.valueSupplier.get());
         }

         this.anchorValue = this.snap(current);
         this.anchorMouseX = mouse;
         return true;
      } else {
         return false;
      }
   }

   public boolean drag(int mouseX) {
      if (this.enabled && this.dragging) {
         double mouse = this.preciseMouseX(mouseX);
         double value = MathUtil.clamp01((float)(this.anchorValue + (mouse - this.anchorMouseX) / this.innerWidth()));
         this.valueConsumer.accept((float)this.snap(value));
         return true;
      } else {
         return false;
      }
   }

   public boolean releasePointer() {
      if (!this.dragging) {
         return false;
      }

      this.dragging = false;
      this.releaseAction.run();
      return true;
   }

   public boolean isDragging() {
      return this.dragging;
   }

   public boolean handleScroll(int mouseX, int mouseY, double vertical) {
      if (!(this.step <= 0.0F) && this.contains(mouseX, mouseY)) {
         float current = Math.round(MathUtil.clamp01(this.valueSupplier.get()) / this.step) * this.step;
         float next = MathUtil.clamp01(current + (vertical > 0.0 ? this.step : -this.step));
         this.valueConsumer.accept(Math.round(next / this.step) * this.step);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      float value = this.animation.toward(MathUtil.clamp01(this.valueSupplier.get()));
      float effectiveAlpha = this.alpha * (this.enabled ? 1.0F : 0.32F);
      int effectiveFillColor = this.enabled ? this.fillColor : Theme.Colors.ICON_GHOST;
      int effectiveKnobColor = this.enabled ? this.knobColor : Theme.Colors.ICON_GHOST;
      float currentHeight = this.height();
      float padding = Math.round(currentHeight * 0.125F);
      float trackHeight = Math.round(currentHeight * 0.75F);
      float knob = Math.round(currentHeight * 0.875F);
      if (trackHeight < 2.0F) {
         trackHeight = 2.0F;
      }

      if (knob < 2.0F) {
         knob = 2.0F;
      }

      float trackX = this.x() + padding;
      float trackWidth = Math.max(0.0F, this.width() - padding * 2.0F);
      float trackY = this.y() + (currentHeight - trackHeight) / 2.0F;
      float knobY = this.y() + (currentHeight - knob) / 2.0F;
      float filled = MathUtil.clamp(Math.round(trackWidth * value), 0.0F, trackWidth);
      float knobX = MathUtil.clamp(trackX + filled - knob / 2.0F, this.x(), this.x() + this.width() - knob);
      Render2DUtil.rect(this.x(), this.y(), this.width(), currentHeight).color(ColorUtil.multiplyAlpha(this.trackColor, effectiveAlpha)).radius(999.0F).draw();
      if (filled > 0.0F) {
         Render2DUtil.rect(trackX, trackY, filled, trackHeight)
            .color(ColorUtil.multiplyAlpha(effectiveFillColor, effectiveAlpha))
            .radius(999.0F)
            .shadow(ColorUtil.multiplyAlpha(Theme.Colors.CONTROL_SHADOW, effectiveAlpha), this.px(7.5F))
            .draw();
      }

      Render2DUtil.rect(knobX, knobY, knob, knob)
         .color(ColorUtil.multiplyAlpha(effectiveKnobColor, effectiveAlpha))
         .radius(knob)
         .shadow(ColorUtil.multiplyAlpha(Theme.Colors.CONTROL_SHADOW, effectiveAlpha), this.px(7.5F))
         .draw();
   }

   private void updateValue(int mouseX) {
      double value = MathUtil.clamp01((float)((this.preciseMouseX(mouseX) - this.innerX()) / this.innerWidth()));
      this.valueConsumer.accept((float)this.snap(value));
   }

   private double snap(double value) {
      return this.step <= 0.0F ? value : MathUtil.clamp01((float)((double)Math.round(value / this.step) * this.step));
   }

   private double preciseMouseX(int mouseX) {
      class_310 minecraft = class_310.method_1551();
      return minecraft != null ? minecraft.field_1729.method_68879(minecraft.method_22683()) : mouseX;
   }

   private double innerX() {
      return this.x() + this.px(2.0F);
   }

   private double innerWidth() {
      return Math.max(1.0F, this.width() - this.px(2.0F) * 2.0F);
   }

   @FunctionalInterface
   @Environment(EnvType.CLIENT)
   public interface ValueConsumer {
      void accept(float var1);
   }
}
