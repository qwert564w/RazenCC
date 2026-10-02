package org.ryzen.menu.ui.controls;

import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class InputComponent extends Component {
   private static final String EMPTY_PLACEHOLDER = "Empty";
   private final Supplier<String> valueSupplier;
   private final Consumer<String> valueConsumer;
   private String placeholder = "Empty";
   private Predicate<String> filter = val -> true;
   private int mouseX;
   private int mouseY;
   private float alpha = 1.0F;
   private boolean focused;
   private boolean masked;
   private boolean allSelected;

   public InputComponent(Supplier<String> valueSupplier, Consumer<String> valueConsumer) {
      this.valueSupplier = valueSupplier;
      this.valueConsumer = valueConsumer;
   }

   public InputComponent filter(Predicate<String> filter) {
      this.filter = filter;
      return this;
   }

   public InputComponent place(Component owner, int x, int y, int width, int height, int mouseX, int mouseY) {
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(width), owner.px(height));
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      return this;
   }

   public InputComponent alpha(float alpha) {
      this.alpha = alpha;
      return this;
   }

   public InputComponent placeholder(String placeholder) {
      this.placeholder = placeholder;
      return this;
   }

   public InputComponent masked(boolean masked) {
      this.masked = masked;
      return this;
   }

   public void focus() {
      this.focused = true;
      this.allSelected = false;
   }

   public boolean isFocused() {
      return this.focused;
   }

   public void blur() {
      this.focused = false;
      this.allSelected = false;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      this.focused = this.contains(mouseX, mouseY);
      this.allSelected = false;
      return this.focused;
   }

   public boolean handleKey(int key) {
      if (!this.focused) {
         return false;
      }

      if (key != 256 && key != 257 && key != 335) {
         if (MenuClipboard.shortcutDown()) {
            if (key == 65) {
               this.allSelected = !this.value().isEmpty();
               return true;
            }

            if (key == 67) {
               if (this.allSelected) {
                  MenuClipboard.set(this.value());
               }

               return true;
            }

            if (key == 88) {
               if (this.allSelected) {
                  MenuClipboard.set(this.value());
                  this.replaceValue("");
               }

               return true;
            }

            if (key == 86) {
               String base = this.allSelected ? "" : this.value();
               this.replaceValue(base + MenuClipboard.get());
               return true;
            }
         }

         if (key == 259) {
            if (this.allSelected) {
               this.replaceValue("");
            } else {
               this.deleteLastCodePoint();
            }
         }

         return true;
      } else {
         this.blur();
         return true;
      }
   }

   public boolean handleCharacter(int codePoint) {
      if (!this.focused) {
         return false;
      }

      if (MenuClipboard.shortcutDown()) {
         return true;
      }

      if (!Character.isISOControl(codePoint) && Character.isValidCodePoint(codePoint)) {
         String base = this.allSelected ? "" : this.value();
         this.replaceValue(base + new String(Character.toChars(codePoint)));
      }

      return true;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      boolean hovered = this.contains(this.mouseX, this.mouseY);
      String value = this.value();
      boolean active = this.focused || !value.isEmpty();
      int background = !this.focused && !hovered ? Theme.Colors.BACKGROUND_SURFACE_M : Theme.Colors.BACKGROUND_SURFACE_S;
      int textColor = this.focused ? Theme.Colors.TEXT_TEXT : (active ? -1 : Theme.Colors.TEXT_GHOST);
      UiFontStyle style = active ? UiFontStyle.MEDIUM : UiFontStyle.REGULAR_TRACKED;
      String visibleValue = this.masked ? "•".repeat(value.codePointCount(0, value.length())) : value;
      String displayValue = this.focused && !this.allSelected ? visibleValue + "|" : (active ? visibleValue : MenuText.ui(this.placeholder));
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(this.alpha(background, this.alpha))
         .radius(this.px(4.0F))
         .border(Math.max(0.5F, this.px(0.5F)), this.alpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
         .draw();
      float size = this.px(10.0F);
      float letterSpacing = style.letterSpacingEm() * size;
      float textWidth = UiFonts.sfProDisplay().measureWidth(displayValue, size, letterSpacing);
      float textX = this.x() + this.px(8.0F);
      float maxTextWidth = Math.max(1.0F, this.px(112.0F));
      float drawX = textWidth > maxTextWidth ? textX - Math.round(textWidth - maxTextWidth) : textX;
      float textY = UiFonts.sfProDisplay().centeredTextY(this.y() + this.height() / 2.0F, size);
      float scissorHeight = Math.max(1.0F, this.height() - this.px(8.0F));
      Render2DUtil.pushScissor(textX, this.y() + this.px(4.0F), maxTextWidth, scissorHeight);
      if (this.focused && this.allSelected && !visibleValue.isEmpty()) {
         Render2DUtil.rect(drawX, this.y() + this.px(4.0F), UiFonts.sfProDisplay().measureWidth(visibleValue, size, letterSpacing), scissorHeight)
            .color(this.alpha(Theme.getAccent(), this.alpha * 0.55F))
            .radius(this.px(2.0F))
            .draw();
      }

      Render2DUtil.text(drawX, textY, size, displayValue).style(style).color(this.alpha(textColor, this.alpha)).draw();
      Render2DUtil.popScissor();
   }

   private String value() {
      String value = this.valueSupplier.get();
      return value == null ? "" : value;
   }

   private void deleteLastCodePoint() {
      String value = this.value();
      if (!value.isEmpty()) {
         int lastCodePoint = value.codePointBefore(value.length());
         this.replaceValue(value.substring(0, value.length() - Character.charCount(lastCodePoint)));
      }
   }

   private void replaceValue(String value) {
      String resolved = value == null ? "" : value.replaceAll("\\R", " ");
      if (this.filter.test(resolved)) {
         this.valueConsumer.accept(resolved);
         this.allSelected = false;
      }
   }
}
