package org.ryzen.menu.ui.rows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.DropdownComponent;
import org.ryzen.menu.ui.controls.MarqueeText;
import org.ryzen.menu.ui.controls.ModeComponent;
import org.ryzen.menu.ui.controls.MultiSelectComponent;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class DropdownRow extends SettingRow {
   private final DropdownComponent dropdown;
   private final Map<String, MarqueeText> optionTexts = new HashMap<>();
   private final Animation popupAnimation = new Animation(150L, Animation.Easing.EASE_OUT_QUAD);
   private final Animation optionFlashAnimation = new Animation(0L, Animation.Easing.EASE_OUT_QUAD);
   private String flashedOption;
   private boolean open;
   private boolean closing;

   public DropdownRow(ModeSetting setting) {
      super(setting);
      this.dropdown = new ModeComponent(() -> MenuText.option(setting.getValue()));
   }

   public DropdownRow(MultiSelectSetting setting) {
      super(setting);
      this.dropdown = new MultiSelectComponent(setting);
   }

   @Override
   protected void placeControl(Component owner) {
      this.dropdown
         .place(owner, this.controlX(), this.controlY(), this.controlWidth(), this.controlHeight())
         .color(this.dropdownColor())
         .warning(warningColor(this.setting))
         .alpha(this.rowAlpha);
   }

   @Override
   public boolean click(int mouseX, int mouseY) {
      this.host.closeOtherRows(this);
      if (this.dropdown.handleClick(mouseX, mouseY)) {
         this.toggle();
      }

      return true;
   }

   @Override
   public void closeTransient() {
      this.close();
   }

   @Override
   public void closeTransientImmediately() {
      this.open = false;
      this.closing = false;
      this.popupAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   @Override
   public boolean hasOpenPopup() {
      return this.open;
   }

   @Override
   public boolean popupClick(int mouseX, int mouseY) {
      if (this.optionClick(mouseX, mouseY)) {
         return true;
      }

      if (this.dropdown.contains(mouseX, mouseY)) {
         this.close();
         return true;
      }

      if (this.hit(mouseX, mouseY, this.popupX(), this.popupY(), 144.0F, this.popupHeight())) {
         return true;
      }

      this.close();
      return false;
   }

   @Override
   public void renderPopupOverlay(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.open || this.closing) {
         this.renderPopup();
         if (!this.open && this.popupAnimation.isFinished()) {
            this.closing = false;
         }
      }
   }

   @Override
   protected void renderControl(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.dropdown.render(minecraft, guiGraphicsExtractor);
   }

   @Override
   protected boolean controlRendersWarning() {
      return true;
   }

   @Override
   protected int labelColor() {
      return Theme.Colors.TEXT_TEXT;
   }

   @Override
   protected int controlWidth() {
      return 112;
   }

   @Override
   protected int controlHeight() {
      return 24;
   }

   private void toggle() {
      if (this.open) {
         this.close();
      } else {
         this.open = true;
         this.closing = false;
         this.popupAnimation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
      }
   }

   private void close() {
      if (this.open) {
         this.closing = true;
         this.popupAnimation.animate(this.popupAnimation.getValue(), 0.0F, 120L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.open = false;
   }

   private List<String> options() {
      if (this.setting instanceof ModeSetting mode) {
         return mode.getModes();
      } else {
         return this.setting instanceof MultiSelectSetting multi ? multi.getOptions() : List.of();
      }
   }

   private boolean optionClick(int mouseX, int mouseY) {
      List<String> options = this.options();
      int itemX = this.popupX() + 6;
      int itemY = this.popupY() + 6;
      int itemWidth = 132;
      int step = 34;

      for (int index = 0; index < options.size(); index++) {
         if (this.hit(mouseX, mouseY, itemX, itemY + index * step, itemWidth, 32.0F)) {
            String option = options.get(index);
            if (this.setting instanceof ModeSetting mode) {
               mode.setValue(option);
               this.close();
            } else if (this.setting instanceof MultiSelectSetting multi) {
               multi.toggle(option);
               this.flashedOption = option;
               this.optionFlashAnimation.animate(1.0F, 0.0F, 250L, Animation.Easing.EASE_OUT_QUAD);
            }

            return true;
         }
      }

      return false;
   }

   private int popupX() {
      return this.rowX + this.rowWidth - 16 - 144;
   }

   private int popupY() {
      return this.opensUp() ? this.controlY() - 4 - this.popupHeight() : this.controlY() + 24 + 4;
   }

   private boolean opensUp() {
      int popupHeight = this.popupHeight();
      int belowY = this.controlY() + 24 + 4;
      if (belowY + popupHeight <= this.host.popupViewportMaxY()) {
         return false;
      }

      int aboveY = this.controlY() - 4 - popupHeight;
      if (aboveY >= 54) {
         return true;
      }

      int spaceAbove = this.controlY() - 4 - 54;
      int spaceBelow = this.host.popupViewportMaxY() - belowY;
      return spaceAbove > spaceBelow;
   }

   private int popupHeight() {
      int count = this.options().size();
      return 12 + count * 32 + Math.max(0, count - 1) * 2;
   }

   private void renderPopup() {
      List<String> options = this.options();
      float progress = this.popupAnimation.getValue();
      if (!(progress <= 0.001F) && !options.isEmpty()) {
         float slide = (this.opensUp() ? 6.0F : -6.0F) * (1.0F - progress);
         int popupX = this.popupX();
         float popupY = this.popupY() + slide;
         int popupHeight = this.popupHeight();
         int itemX = popupX + 6;
         float itemY = popupY + 6.0F;
         int itemWidth = 132;
         int step = 34;
         this.glassPanel(this.sx(popupX), this.sy(popupY), this.px(144.0F), this.px(popupHeight), this.px(12.0F), this.px(20.0F), this.rowAlpha * progress);

         for (int index = 0; index < options.size(); index++) {
            float optionY = itemY + index * step;
            String option = options.get(index);
            boolean selected = this.setting instanceof ModeSetting mode && mode.is(option)
               || this.setting instanceof MultiSelectSetting multi && multi.isSelected(option);
            boolean hovered = this.hit(this.mouseX, this.mouseY, itemX, optionY, itemWidth, 32.0F);
            if (selected || hovered) {
               int background = selected ? (hovered ? Theme.Colors.OUTLINES_MEDIUM : Theme.Colors.OUTLINES_LARGE) : Theme.Colors.OUTLINES_SMALL;
               Render2DUtil.rect(this.sx(itemX), this.sy(optionY), this.px(itemWidth), this.px(32.0F))
                  .color(this.alpha(background, this.rowAlpha * progress))
                  .radius(this.px(8.0F))
                  .draw();
            }

            float flash = option.equals(this.flashedOption) ? this.optionFlashAnimation.getValue() : 0.0F;
            if (flash > 0.001F) {
               Render2DUtil.rect(this.sx(itemX), this.sy(optionY), this.px(itemWidth), this.px(32.0F))
                  .color(this.alpha(ColorUtil.withAlpha(16777215, Math.round(45.0F * flash)), this.rowAlpha * progress))
                  .radius(this.px(8.0F))
                  .draw();
            }

            Integer optionWarning = warningColor(this.setting.warningLevel(option));
            int textX = itemX + 12;
            int textWidth = 108;
            if (optionWarning != null) {
               int iconX = itemX + 10;
               float iconY = optionY + 10.0F;
               this.texture(iconX, iconY, 12.0F, Textures.Icons.TRIANGLE_ALERT, optionWarning, this.rowAlpha * progress);
               int shiftedTextX = iconX + 12 + 5;
               textWidth = Math.max(20, textWidth - (shiftedTextX - textX));
               textX = shiftedTextX;
            }

            int selectedColor = optionWarning == null ? Theme.Colors.TEXT_TEXT : optionWarning;
            MarqueeText optionText = this.optionTexts.computeIfAbsent(option, ignored -> new MarqueeText(() -> MenuText.option(option)));
            optionText.placeAt(this, this.sx(textX), this.sy(optionY), this.px(textWidth), this.px(32.0F), this.mouseX, this.mouseY)
               .style(12.0F, UiFontStyle.MEDIUM, selected ? selectedColor : Theme.Colors.TEXT_TEXT, this.rowAlpha * progress)
               .render(class_310.method_1551(), null);
         }
      }
   }

   private int dropdownColor() {
      Integer warning = warningColor(this.setting);
      return warning == null ? Theme.Colors.ICON : warning;
   }
}
