package org.ryzen.menu.ui.controls;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class MarqueeText extends Component {
   private static final int FADE_STEPS = 6;
   private static final long HOVER_DELAY_NANOS = 350000000L;
   private static final float SPEED_DESIGN_PX = 28.0F;
   private static final float STATIC_FADE_DESIGN_PX = 24.0F;
   private static final float SCROLL_FADE_DESIGN_PX = 12.0F;
   private static final float LOOP_GAP_DESIGN_PX = 28.0F;
   private final Supplier<String> textSupplier;
   private float textSize = 12.0F;
   private UiFontStyle style = UiFontStyle.MEDIUM;
   private TextAlign align = TextAlign.LEFT;
   private int color = -1;
   private float alpha = 1.0F;
   private int mouseX;
   private int mouseY;
   private boolean hoveredLastFrame;
   private long hoverStartNanos;

   public MarqueeText(Supplier<String> textSupplier) {
      this.textSupplier = textSupplier;
   }

   public MarqueeText place(Component owner, float x, float y, float width, float height, int mouseX, int mouseY) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public MarqueeText placeAt(Component owner, float x, float y, float width, float height, int mouseX, int mouseY) {
      this.attach(owner, x, y, width, height);
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public MarqueeText style(float textSize, UiFontStyle style, int color, float alpha) {
      this.textSize = textSize;
      this.style = style;
      this.color = color;
      this.alpha = alpha;
      return this;
   }

   public MarqueeText align(TextAlign align) {
      this.align = align == null ? TextAlign.LEFT : align;
      return this;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      String text = this.textSupplier.get();
      if (text != null && !text.isEmpty() && !(this.width() <= 0.5F)) {
         MsdfFont font = UiFonts.sfProDisplay();
         float size = this.px(this.textSize);
         float spacing = size * this.style.letterSpacingEm();
         float textWidth = font.measureWidth(text, size, spacing);
         float textY = font.centeredTextY(this.y() + this.height() / 2.0F, size);
         if (textWidth <= this.width()) {
            this.resetHover();

            float textX = switch (this.align) {
               case CENTER -> this.x() + (this.width() - textWidth) / 2.0F;
               case RIGHT -> this.x() + this.width() - textWidth;
               default -> this.x();
            };
            this.drawText(text, textX, textY, size, this.alpha);
         } else {
            boolean hovered = this.mouseX >= this.x()
               && this.mouseX <= this.x() + this.width()
               && this.mouseY >= this.y()
               && this.mouseY <= this.y() + this.height();
            long now = System.nanoTime();
            if (hovered && !this.hoveredLastFrame) {
               this.hoverStartNanos = now;
            } else if (!hovered) {
               this.hoverStartNanos = 0L;
            }

            this.hoveredLastFrame = hovered;
            long scrollingNanos = hovered ? now - this.hoverStartNanos - 350000000L : -1L;
            if (scrollingNanos <= 0L) {
               this.drawWithEdgeFades(text, this.x(), textY, size, textWidth, false);
            } else {
               float gap = this.px(28.0F);
               float cycle = textWidth + gap;
               float offset = (float)(scrollingNanos / 1.0E9 * this.px(28.0F) % cycle);
               this.drawWithEdgeFades(text, this.x() - offset, textY, size, textWidth, true);
            }
         }
      } else {
         this.resetHover();
      }
   }

   private void drawWithEdgeFades(String text, float textX, float textY, float size, float textWidth, boolean scrolling) {
      float fade = Math.min(this.px(scrolling ? 12.0F : 24.0F), this.width() / 3.0F);
      float leftFade = scrolling ? fade : 0.0F;
      float rightFade = fade;
      this.drawStrip(text, textX, textY, size, this.x() + leftFade, this.x() + this.width() - rightFade, this.alpha, textWidth, scrolling);
      if (scrolling) {
         for (int step = 0; step < 6; step++) {
            float x0 = this.x() + leftFade * step / 6.0F;
            float x1 = this.x() + leftFade * (step + 1) / 6.0F;
            float stripAlpha = this.alpha * (step + 0.5F) / 6.0F;
            this.drawStrip(text, textX, textY, size, x0, x1, stripAlpha, textWidth, true);
         }
      }

      for (int step = 0; step < 6; step++) {
         float x0 = this.x() + this.width() - rightFade + rightFade * step / 6.0F;
         float x1 = this.x() + this.width() - rightFade + rightFade * (step + 1) / 6.0F;
         float stripAlpha = this.alpha * (1.0F - (step + 0.5F) / 6.0F);
         this.drawStrip(text, textX, textY, size, x0, x1, stripAlpha, textWidth, scrolling);
      }
   }

   private void drawStrip(
      String text, float textX, float textY, float size, float clipLeft, float clipRight, float stripAlpha, float textWidth, boolean duplicate
   ) {
      float clipWidth = clipRight - clipLeft;
      if (!(clipWidth <= 0.0F) && !(stripAlpha <= 0.001F)) {
         Render2DUtil.pushScissor(clipLeft, this.y(), clipWidth, this.height());
         this.drawText(text, textX, textY, size, stripAlpha);
         if (duplicate) {
            this.drawText(text, textX + textWidth + this.px(28.0F), textY, size, stripAlpha);
         }

         Render2DUtil.popScissor();
      }
   }

   private void drawText(String text, float textX, float textY, float size, float drawAlpha) {
      Render2DUtil.text(textX, textY, size, text).style(this.style).color(ColorUtil.multiplyAlpha(this.color, drawAlpha)).draw();
   }

   private void resetHover() {
      this.hoveredLastFrame = false;
      this.hoverStartNanos = 0L;
   }
}
