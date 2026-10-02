package org.ryzen.menu.pages.modules;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.Feature;
import org.ryzen.feature.setting.Setting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.MarqueeText;
import org.ryzen.menu.ui.controls.ToggleComponent;
import org.ryzen.menu.ui.rows.RowHost;
import org.ryzen.menu.ui.rows.SettingRow;
import org.ryzen.menu.ui.rows.SettingRows;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ModuleCard extends Component implements RowHost {
   static final int HEADER_HEIGHT = 64;
   static final int ROW_HEIGHT = 40;
   private final Feature feature;
   private final ToggleComponent featureToggle;
   private final MarqueeText descriptionText;
   private final ModuleBindPopup bindPopup = new ModuleBindPopup();
   private final ModuleBindDetailsPopup bindDetailsPopup = new ModuleBindDetailsPopup();
   private final List<SettingRow> rows = new ArrayList<>();
   private final Animation cardHighlightAnimation = new Animation(0L, Animation.Easing.LINEAR);
   private int designX;
   private int designY;
   private int designWidth;
   private int mouseX;
   private int mouseY;
   private float alpha = 1.0F;
   private float viewportMinY;
   private float viewportMaxY;
   private int popupViewportMaxY;

   public ModuleCard(Feature feature) {
      this.feature = feature;
      this.featureToggle = new ToggleComponent(
         feature::isEnabled, feature::toggle, ToggleComponent.Style.SWITCH, Theme.Colors.CONTROL_STRONG, Theme.getAccent()
      );
      this.descriptionText = new MarqueeText(this::displayDescription);

      for (Setting<?> setting : feature.getSettings()) {
         SettingRow row = SettingRows.create(feature.getName(), setting);
         if (row != null) {
            this.rows.add(row);
         }
      }
   }

   public Feature feature() {
      return this.feature;
   }

   public int designHeight() {
      int visibleRows = this.visibleRowCount();
      return visibleRows == 0 ? 64 : 64 + visibleRows * 40;
   }

   public void place(Component owner, int x, int y, int width, float alpha, int mouseX, int mouseY, int offsetX, int popupViewportMaxY) {
      this.viewportMinY = owner.y() + owner.px(54.0F);
      this.viewportMaxY = owner.y() + owner.height();
      this.popupViewportMaxY = popupViewportMaxY;
      this.designX = x + offsetX;
      this.designY = y;
      this.designWidth = width;
      this.alpha = alpha;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.attach(owner, owner.sx(this.designX), owner.sy(y), owner.px(width), owner.px(this.designHeight()));
      this.bindPopup.place(owner, mouseX, mouseY, alpha);
      this.bindDetailsPopup.place(owner, mouseX, mouseY, alpha);
      if (this.feature.isToggleable()) {
         this.featureToggle
            .place(owner, this.designX + width - 16 - 36, y + 12, 36, 16)
            .style(ToggleComponent.Style.SWITCH, Theme.Colors.CONTROL_STRONG, Theme.getAccent())
            .alpha(alpha);
      }

      int rowY = this.designY + 64;

      for (SettingRow row : this.rows) {
         if (row.visible()) {
            row.place(owner, this, this.designX, rowY, width, alpha, mouseX, mouseY);
            rowY += 40;
         }
      }
   }

   @Override
   public void closeOtherRows(SettingRow except) {
      for (SettingRow row : this.rows) {
         if (row != except) {
            row.closeTransient();
         }
      }
   }

   @Override
   public int popupViewportMaxY() {
      return this.popupViewportMaxY;
   }

   public boolean handleRightClick(int mouseX, int mouseY) {
      return this.hit(mouseX, mouseY, this.designX, this.designY, this.designWidth, 64.0F);
   }

   public boolean handleMiddleClick(int mouseX, int mouseY) {
      if (!this.hit(mouseX, mouseY, this.designX, this.designY, this.designWidth, 64.0F)) {
         return false;
      }

      if (!this.feature.supportsBinds()) {
         return true;
      }

      this.closeOtherRows(null);
      this.bindDetailsPopup.close();
      this.bindPopup.openAt(this.feature, this.designX(mouseX), this.designY(mouseY), 0.0F, 54.0F, 1024.0F, 640.0F);
      return true;
   }

   public void drag(int mouseX) {
      for (SettingRow row : this.rows) {
         row.drag(mouseX);
      }
   }

   @Override
   public boolean contains(float mouseX, float mouseY) {
      return !(mouseY < this.viewportMinY) && !(mouseY > this.viewportMaxY) ? super.contains(mouseX, mouseY) : false;
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contains(mouseX, mouseY)) {
         this.closeOtherRows(null);
         return false;
      }

      if (this.hit(mouseX, mouseY, this.designX, this.designY, this.designWidth, 64.0F)) {
         this.closeOtherRows(null);
         if (this.feature.isToggleable()) {
            this.feature.toggle();
         }

         return true;
      } else {
         int rowY = this.designY + 64;

         for (SettingRow row : this.rows) {
            if (row.visible()) {
               if (this.hit(mouseX, mouseY, this.designX + 12, rowY, this.designWidth - 24, 40.0F)) {
                  return row.click(mouseX, mouseY);
               }

               rowY += 40;
            }
         }

         return true;
      }
   }

   public boolean handleKey(int key) {
      if (this.bindDetailsPopup.captureKey(key)) {
         return true;
      }

      for (SettingRow row : this.rows) {
         if (row.key(key)) {
            return true;
         }
      }

      return false;
   }

   public boolean handleCharacter(int codePoint) {
      for (SettingRow row : this.rows) {
         if (row.character(codePoint)) {
            return true;
         }
      }

      return false;
   }

   public boolean handleScroll(int mouseX, int mouseY, double vertical) {
      for (SettingRow row : this.rows) {
         if (row.visible() && row.scroll(mouseX, mouseY, vertical)) {
            return true;
         }
      }

      return false;
   }

   public boolean handleDropdownPopupClick(int mouseX, int mouseY) {
      boolean handled = false;

      for (SettingRow row : this.rows) {
         if (row.hasOpenPopup()) {
            handled |= row.popupClick(mouseX, mouseY);
         }
      }

      return handled;
   }

   public boolean handleBindPopupClick(int mouseX, int mouseY, int button) {
      if (!this.feature.supportsBinds()) {
         return false;
      }

      if (this.bindDetailsPopup.captureMouseButton(button)) {
         return true;
      }

      for (SettingRow row : this.rows) {
         if (row.captureMouse(button)) {
            return true;
         }
      }

      boolean detailsHovered = this.bindDetailsPopup.containsPopup(mouseX, mouseY);
      if (detailsHovered) {
         return this.bindDetailsPopup.handleMouseButton(mouseX, mouseY, button);
      }

      boolean popupHovered = this.bindPopup.containsPopup(mouseX, mouseY);
      ModuleBindPopup.Action action = this.bindPopup.handleMouseButton(mouseX, mouseY, button);
      if (popupHovered) {
         if (action.type() == ModuleBindPopup.ActionType.EDIT_BIND) {
            this.bindDetailsPopup.openForBind(this.feature, action.bindIndex(), this.bindPopup.popupX(), this.bindPopup.popupY(), 0.0F, 54.0F, 1024.0F, 640.0F);
         } else if (action.type() == ModuleBindPopup.ActionType.CREATE_BIND) {
            this.bindDetailsPopup.openForNewBind(this.feature, this.bindPopup.popupX(), this.bindPopup.popupY(), 0.0F, 54.0F, 1024.0F, 640.0F);
         } else if (action.type() == ModuleBindPopup.ActionType.RELOAD) {
            this.feature.clearBind();
            this.bindDetailsPopup.close();
            this.bindPopup.close();
         }

         return true;
      } else {
         this.bindDetailsPopup.close();
         return false;
      }
   }

   public boolean isCapturingBind() {
      if (!this.feature.supportsBinds()) {
         return false;
      }

      if (this.bindDetailsPopup.isListeningForBind()) {
         return true;
      }

      for (SettingRow row : this.rows) {
         if (row.isCapturingBind()) {
            return true;
         }
      }

      return false;
   }

   public void closeBindPopup() {
      this.bindPopup.close();
      this.bindDetailsPopup.close();
   }

   public void flashHighlight() {
      this.cardHighlightAnimation.animate(1.0F, 0.0F, 2000L, Animation.Easing.LINEAR);
   }

   public void releasePointer() {
      for (SettingRow row : this.rows) {
         row.releasePointer();
      }
   }

   public void closeOverlays() {
      for (SettingRow row : this.rows) {
         row.closeTransientImmediately();
      }

      this.bindPopup.closeImmediately();
      this.bindDetailsPopup.close();
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      boolean hovered = this.contains(this.mouseX, this.mouseY);
      Render2DUtil.rect(this.sx(this.designX), this.sy(this.designY), this.px(this.designWidth), this.px(this.designHeight()))
         .color(this.alpha(hovered ? Theme.Colors.MODULE_CARD_HOVER : Theme.Colors.MODULE_CARD, this.alpha))
         .radius(this.px(8.0F))
         .border(Math.max(0.5F, this.px(0.5F)), this.alpha(Theme.Colors.DIVIDER_HEADER, this.alpha))
         .draw();
      this.renderHighlightFlash();
      this.renderHeader();
      if (this.feature.isToggleable()) {
         this.featureToggle.render(minecraft, guiGraphicsExtractor);
      }

      float contentHeight = Math.round(this.height() - this.px(64.0F));
      if (contentHeight > 0.0F && this.visibleRowCount() > 0) {
         Render2DUtil.pushScissor(this.x(), this.y() + this.px(64.0F), this.width(), contentHeight);
         int rowY = this.designY + 64;

         for (SettingRow row : this.rows) {
            if (row.visible()) {
               this.rect(this.designX + 16, rowY, this.designWidth - 32, 1.0F, Theme.Colors.DIVIDER_HEADER, 0.0F, this.alpha);
               row.render(minecraft, guiGraphicsExtractor);
               rowY += 40;
            }
         }

         Render2DUtil.popScissor();
      }
   }

   public void renderOverlay(class_310 minecraft, class_332 guiGraphicsExtractor) {
      for (SettingRow row : this.rows) {
         if (row.visible()) {
            row.renderPopupOverlay(minecraft, guiGraphicsExtractor);
         }
      }

      this.bindPopup.render(minecraft, guiGraphicsExtractor);
      this.bindDetailsPopup.render(minecraft, guiGraphicsExtractor);
   }

   private void renderHighlightFlash() {
      float highlight = this.cardHighlightAnimation.getValue();
      if (!(highlight <= 0.001F)) {
         float rawProgress = 1.0F - highlight;
         float blink = (float)((1.0 - Math.cos(rawProgress * Math.PI * 6.0)) / 2.0);
         float envelope = Math.max(0.0F, 1.0F - rawProgress * 0.7F);
         float flashAlpha = blink * envelope;
         Render2DUtil.rect(this.sx(this.designX), this.sy(this.designY), this.px(this.designWidth), this.px(this.designHeight()))
            .color(this.alpha(ColorUtil.withAlpha(-1, Math.round(35.0F * flashAlpha)), this.alpha))
            .radius(this.px(8.0F))
            .draw();
      }
   }

   private void renderHeader() {
      float titleHeight = UiFonts.sfProDisplay().textHeight(14.0F);
      float descriptionHeight = UiFonts.sfProDisplay().textHeight(12.0F);
      float headerGap = 5.0F;
      float headerBlockTop = this.designY + (64.0F - (titleHeight + headerGap + descriptionHeight)) / 2.0F;
      int switchX = this.designX + this.designWidth - 16 - 36;
      boolean showActionIcon = !this.rows.isEmpty();
      int actionIconX = this.feature.isToggleable() ? switchX - 22 : this.designX + this.designWidth - 16 - 12;
      float textRight;
      if (!this.feature.isToggleable()) {
         textRight = showActionIcon ? actionIconX - 6.0F : this.designX + this.designWidth - 16;
      } else {
         textRight = !showActionIcon ? switchX - 6.0F : switchX - 22.0F - 6.0F;
      }

      float textX = this.designX + 16;
      float titleTextWidth = Math.max(1.0F, textRight - textX);
      float descriptionTextWidth = this.designWidth - 32.0F;
      String title = UiFonts.sfProDisplay().ellipsize(this.feature.getName(), 14.0F, 14.0F * UiFontStyle.SEMIBOLD.letterSpacingEm(), titleTextWidth);
      this.text(textX, headerBlockTop, 14.0F, title, Theme.Colors.PRIMARY, this.alpha, UiFontStyle.SEMIBOLD);
      this.descriptionText
         .place(this, textX, headerBlockTop + titleHeight + headerGap, descriptionTextWidth, descriptionHeight, this.mouseX, this.mouseY)
         .style(12.0F, UiFontStyle.REGULAR, Theme.Colors.SECONDARY_DARK, this.alpha)
         .render(class_310.method_1551(), null);
      if (showActionIcon) {
         this.texture(
            actionIconX,
            this.designY + 14,
            12.0F,
            !this.feature.isToggleable() && !this.feature.supportsBinds() ? Textures.Icons.OPTION : Textures.Icons.COMMAND,
            Theme.Colors.SECONDARY_DARK,
            this.alpha
         );
      }
   }

   private String displayDescription() {
      String description = MenuText.featureDescription(this.feature.getDescription());
      return description.endsWith(".") ? description : description + ".";
   }

   private int visibleRowCount() {
      int count = 0;

      for (SettingRow row : this.rows) {
         if (row.visible()) {
            count++;
         }
      }

      return count;
   }
}
