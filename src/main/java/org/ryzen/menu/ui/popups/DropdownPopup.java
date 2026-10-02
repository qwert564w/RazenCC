package org.ryzen.menu.ui.popups;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.MarqueeText;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class DropdownPopup extends Component {
   private final Supplier<String[]> options;
   private final IntSupplier selectedIndex;
   private final IntConsumer onPick;
   private final Map<String, MarqueeText> optionTexts = new HashMap<>();
   private final Animation animation = new Animation(150L, Animation.Easing.EASE_OUT_QUAD);
   private boolean open;
   private boolean closing;
   private int designX;
   private int designY;
   private float pageAlpha = 1.0F;
   private int hoverMouseX;
   private int hoverMouseY;

   public DropdownPopup(Supplier<String[]> options, IntSupplier selectedIndex, IntConsumer onPick) {
      this.options = options;
      this.selectedIndex = selectedIndex;
      this.onPick = onPick;
   }

   public DropdownPopup place(Component owner, int x, int y, int mouseX, int mouseY, float pageAlpha) {
      this.designX = x;
      this.designY = y;
      this.hoverMouseX = mouseX;
      this.hoverMouseY = mouseY;
      this.pageAlpha = pageAlpha;
      this.attach(owner, owner.sx(x), owner.sy(y), owner.px(this.popupWidth()), owner.px(this.popupHeight()));
      return this;
   }

   public void toggle() {
      if (this.open) {
         this.close();
      } else {
         this.open = true;
         this.closing = false;
         this.animation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
      }
   }

   public void close() {
      if (this.open) {
         this.closing = true;
         this.animation.animate(this.animation.getValue(), 0.0F, 120L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.open = false;
   }

   public void closeImmediately() {
      this.open = false;
      this.closing = false;
      this.animation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public boolean isOpen() {
      return this.open;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.open) {
         return false;
      }

      String[] values = this.options.get();
      int itemX = this.designX + 6;
      int itemWidth = this.popupWidth() - 12;
      int step = 34;

      for (int index = 0; index < values.length; index++) {
         int itemY = this.designY + 6 + index * step;
         if (this.hit(mouseX, mouseY, itemX, itemY, itemWidth, 32.0F)) {
            this.onPick.accept(index);
            this.close();
            return true;
         }
      }

      this.close();
      return false;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.open || this.closing) {
         float popupProgress = this.animation.getValue();
         if (popupProgress <= 0.001F) {
            if (!this.open && this.animation.isFinished()) {
               this.closing = false;
            }
         } else {
            float alpha = this.pageAlpha * popupProgress;
            float lift = -6.0F * (1.0F - popupProgress);
            String[] values = this.options.get();
            float popupY = this.designY + lift;
            this.glassPanel(
               this.sx(this.designX), this.sy(popupY), this.px(this.popupWidth()), this.px(this.popupHeight()), this.px(12.0F), this.px(20.0F), alpha
            );
            int selected = this.selectedIndex.getAsInt();
            int itemX = this.designX + 6;
            int itemWidth = this.popupWidth() - 12;
            int step = 34;

            for (int index = 0; index < values.length; index++) {
               float itemY = popupY + 6.0F + index * step;
               boolean isSelected = index == selected;
               this.rect(itemX, itemY, itemWidth, 32.0F, isSelected ? Theme.Colors.SURFACE_ACTIVE : 0, 8.0F, alpha);
               String canonicalValue = values[index];
               MarqueeText optionText = this.optionTexts.computeIfAbsent(canonicalValue, ignored -> new MarqueeText(() -> MenuText.option(canonicalValue)));
               optionText.placeAt(this, this.sx(itemX + 10), this.sy(itemY), this.px(itemWidth - 20), this.px(32.0F), this.hoverMouseX, this.hoverMouseY)
                  .style(12.0F, UiFontStyle.MEDIUM, isSelected ? -1 : Theme.Colors.SECONDARY, alpha)
                  .render(minecraft, guiGraphicsExtractor);
            }
         }
      }
   }

   private int popupWidth() {
      return 144;
   }

   private int popupHeight() {
      int count = this.options.get().length;
      return 12 + count * 32 + Math.max(0, count - 1) * 2;
   }
}
