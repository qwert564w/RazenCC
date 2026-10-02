package org.ryzen.menu.pages.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.BindMode;
import org.ryzen.feature.Feature;
import org.ryzen.feature.setting.BindSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.ToggleComponent;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ModuleBindDetailsPopup extends Component {
   public static final int POPUP_WIDTH = 144;
   public static final int POPUP_HEIGHT = 148;
   private static final int POPUP_PADDING = 6;
   private static final int POPUP_GAP = 2;
   private static final int ITEM_HEIGHT = 32;
   private static final int ITEM_RADIUS = 8;
   private static final int ITEM_WIDTH = 132;
   private static final int ICON_SIZE = 12;
   private static final int ICON_TEXT_GAP = 6;
   private static final int POPUP_OFFSET_X = 8;
   private static final int VALUE_PADDING_RIGHT = 12;
   private static final int KEY_ROW_Y = 6;
   private static final int MODE_ROW_Y = 40;
   private static final int VISIBLE_ROW_Y = 74;
   private static final int DELETE_ROW_Y = 108;
   private final ToggleComponent visibleToggle = new ToggleComponent(
      () -> this.feature != null && this.currentBindVisible(),
      this::toggleCurrentBindVisible,
      ToggleComponent.Style.SWITCH,
      Theme.Colors.CONTROL_STRONG,
      Theme.getAccent()
   );
   private Feature feature;
   private int bindIndex = -1;
   private int mouseX;
   private int mouseY;
   private float popupX;
   private float popupY;
   private float minX;
   private float minY;
   private float maxX;
   private float maxY;
   private float alpha = 1.0F;
   private boolean open;
   private final Animation animation = new Animation(150L, Animation.Easing.EASE_OUT_QUAD);
   private boolean listeningForBind;
   private BindMode pendingBindMode = BindMode.TOGGLE;
   private boolean pendingBindVisible = true;

   public void place(Component owner, int mouseX, int mouseY, float alpha) {
      this.attach(owner, owner.x(), owner.y(), owner.width(), owner.height());
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.alpha = alpha;
      if (this.open) {
         this.placeVisibleToggle(owner);
      }
   }

   public void openForBind(Feature feature, int bindIndex, float anchorX, float anchorY, float minX, float minY, float maxX, float maxY) {
      this.openInternal(feature, bindIndex, false, anchorX, anchorY, minX, minY, maxX, maxY);
   }

   public void openForNewBind(Feature feature, float anchorX, float anchorY, float minX, float minY, float maxX, float maxY) {
      this.openInternal(feature, -1, false, anchorX, anchorY, minX, minY, maxX, maxY);
   }

   public void close() {
      this.open = false;
      this.feature = null;
      this.bindIndex = -1;
      this.listeningForBind = false;
      this.pendingBindMode = BindMode.TOGGLE;
      this.pendingBindVisible = true;
   }

   public boolean isOpen() {
      return this.open;
   }

   public boolean isListeningForBind() {
      return this.open && this.listeningForBind;
   }

   public boolean containsPopup(int mouseX, int mouseY) {
      return this.open && this.hit(mouseX, mouseY, this.popupX, this.popupY, 144.0F, 148.0F);
   }

   public boolean captureKey(int key) {
      if (!this.isListeningForBind() || this.feature == null) {
         return false;
      }

      if (key == 256) {
         if (this.bindIndex < 0) {
            this.close();
         } else {
            this.listeningForBind = false;
         }

         return true;
      } else if (key != 259 && key != 261) {
         this.applyBind(BindSetting.key(key));
         return true;
      } else {
         this.deleteCurrentBind();
         return true;
      }
   }

   public boolean captureMouseButton(int button) {
      if (this.isListeningForBind() && this.feature != null) {
         this.applyBind(BindSetting.mouse(button));
         return true;
      } else {
         return false;
      }
   }

   public boolean handleMouseButton(int mouseX, int mouseY, int button) {
      if (!this.open || this.feature == null) {
         return false;
      }

      if (!this.containsPopup(mouseX, mouseY)) {
         this.close();
         return false;
      }

      if (button != 0) {
         return true;
      }

      float contentX = this.popupX + 6.0F;
      if (this.hit(mouseX, mouseY, contentX, this.popupY + 6.0F, 132.0F, 32.0F)) {
         this.listeningForBind = true;
         return true;
      }

      if (this.hit(mouseX, mouseY, contentX, this.popupY + 40.0F, 132.0F, 32.0F)) {
         BindMode nextMode = this.currentBindMode() == BindMode.TOGGLE ? BindMode.HOLD : BindMode.TOGGLE;
         this.setCurrentBindMode(nextMode);
         return true;
      }

      if (this.hit(mouseX, mouseY, contentX, this.popupY + 74.0F, 132.0F, 32.0F)) {
         if (!this.visibleToggle.handleClick(mouseX, mouseY)) {
            this.toggleCurrentBindVisible();
         }

         return true;
      } else if (this.hit(mouseX, mouseY, contentX, this.popupY + 108.0F, 132.0F, 32.0F)) {
         this.deleteCurrentBind();
         return true;
      } else {
         return true;
      }
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.open && this.feature != null) {
         float progress = this.animation.getValue();
         float baseAlpha = this.alpha;
         float baseY = this.popupY;
         this.alpha = baseAlpha * progress;
         this.popupY = baseY - 6.0F * (1.0F - progress);

         try {
            this.renderContent();
         } finally {
            this.alpha = baseAlpha;
            this.popupY = baseY;
         }
      }
   }

   private void renderContent() {
      this.glassPanel(this.sx(this.popupX), this.sy(this.popupY), this.px(144.0F), this.px(148.0F), this.px(12.0F), this.px(20.0F), this.alpha);
      float contentX = this.popupX + 6.0F;
      int keyValueColor = this.listeningForBind ? Theme.getAccent() : Theme.Colors.SECONDARY_DARK;
      this.renderValueRow(
         contentX, this.popupY + 6.0F, Textures.Icons.COMMAND, MenuText.setting("Key"), this.bindLabel(), Theme.Colors.TEXT_TEXT, keyValueColor
      );
      this.renderValueRow(
         contentX,
         this.popupY + 40.0F,
         Textures.Icons.KEYBOARD,
         MenuText.setting("Mode"),
         this.modeLabel(),
         Theme.Colors.TEXT_TEXT,
         Theme.Colors.SECONDARY_DARK
      );
      this.renderVisibleRow(contentX, this.popupY + 74.0F);
      this.renderValueRow(
         contentX, this.popupY + 108.0F, Textures.Icons.DELETE, MenuText.ui("Delete"), null, Theme.Colors.TRAFFIC_CLOSE, Theme.Colors.TRAFFIC_CLOSE
      );
   }

   private void renderValueRow(float x, float y, class_2960 icon, String label, String value, int labelColor, int valueColor) {
      boolean hovered = this.hit(this.mouseX, this.mouseY, x, y, 132.0F, 32.0F);
      if (hovered) {
         Render2DUtil.rect(this.sx(x), this.sy(y), this.px(132.0F), this.px(32.0F))
            .color(this.alpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
            .radius(this.px(8.0F))
            .draw();
      }

      float iconX = x + 6.0F;
      float iconY = y + 10.0F;
      this.texture(iconX, iconY, 12.0F, icon, labelColor, this.alpha);
      this.drawCenteredRowText(iconX + 12.0F + 6.0F, iconY + 6.0F, label, labelColor);
      if (value != null) {
         this.drawRightAlignedRowText(x + 132.0F - 12.0F, iconY + 6.0F, value, valueColor);
      }
   }

   private void renderVisibleRow(float x, float y) {
      boolean hovered = this.hit(this.mouseX, this.mouseY, x, y, 132.0F, 32.0F);
      if (hovered) {
         Render2DUtil.rect(this.sx(x), this.sy(y), this.px(132.0F), this.px(32.0F))
            .color(this.alpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
            .radius(this.px(8.0F))
            .draw();
      }

      float iconX = x + 6.0F;
      float iconY = y + 10.0F;
      this.texture(iconX, iconY, 12.0F, Textures.Icons.EYE, Theme.Colors.TEXT_TEXT, this.alpha);
      this.drawCenteredRowText(iconX + 12.0F + 6.0F, iconY + 6.0F, MenuText.ui("Visible"), Theme.Colors.TEXT_TEXT);
      this.visibleToggle.render(null, null);
   }

   private void drawCenteredRowText(float x, float centerY, String label, int color) {
      this.drawRowText(x, centerY, label, color, false);
   }

   private void drawRightAlignedRowText(float x, float centerY, String label, int color) {
      this.drawRowText(x, centerY, label, color, true);
   }

   private void drawRowText(float x, float centerY, String label, int color, boolean rightAlign) {
      MsdfFont font = UiFonts.sfProDisplay();
      float screenFontSize = this.px(12.0F);
      float screenCenterY = this.sy(0.0F) + this.px(centerY);
      float baselineCenterOffset = this.measureBaselineCenterOffset(font, label, screenFontSize);
      float screenTextY = screenCenterY - baselineCenterOffset - font.ascender(screenFontSize);
      Render2DUtil.TextBuilder text = Render2DUtil.text(this.sx(x), screenTextY, screenFontSize, label)
         .style(UiFontStyle.MEDIUM)
         .color(this.alpha(color, this.alpha));
      if (rightAlign) {
         text.align(TextAlign.RIGHT);
      }

      text.draw();
   }

   private float measureBaselineCenterOffset(MsdfFont font, String text, float size) {
      float minTop = Float.POSITIVE_INFINITY;
      float maxBottom = Float.NEGATIVE_INFINITY;
      int index = 0;

      while (index < text.length()) {
         int codePoint = text.codePointAt(index);
         index += Character.charCount(codePoint);
         MsdfFont.Glyph glyph = font.glyph(codePoint);
         if (glyph != null && glyph.planeBounds() != null) {
            MsdfFont.Bounds plane = glyph.planeBounds();
            minTop = Math.min(minTop, -plane.top() * size);
            maxBottom = Math.max(maxBottom, -plane.bottom() * size);
         }
      }

      return minTop != Float.POSITIVE_INFINITY && maxBottom != Float.NEGATIVE_INFINITY ? (minTop + maxBottom) * 0.5F : 0.0F;
   }

   private String bindLabel() {
      if (this.listeningForBind) {
         return MenuText.ui("Press");
      } else {
         return this.feature != null && this.feature.hasBindAt(this.bindIndex)
            ? MenuText.bind(this.feature.getBind().getDisplayValue(this.bindIndex))
            : MenuText.ui("Unbound");
      }
   }

   private String modeLabel() {
      return MenuText.option(this.currentBindMode() == BindMode.HOLD ? "Hold" : "Toggle");
   }

   private void placeVisibleToggle(Component owner) {
      if (this.feature != null) {
         float progress = this.animation.getValue();
         this.visibleToggle
            .place(owner, Math.round(this.popupX + 144.0F - 6.0F - 36.0F - 6.0F), Math.round(this.popupY + 74.0F + 8.0F - 6.0F * (1.0F - progress)), 36, 16)
            .style(ToggleComponent.Style.SWITCH, Theme.Colors.CONTROL_STRONG, Theme.getAccent())
            .alpha(this.alpha * progress);
      }
   }

   private void openInternal(
      Feature feature, int bindIndex, boolean listenImmediately, float anchorX, float anchorY, float minX, float minY, float maxX, float maxY
   ) {
      this.feature = feature;
      this.bindIndex = bindIndex;
      this.listeningForBind = listenImmediately;
      this.pendingBindMode = feature != null && feature.hasBindAt(bindIndex) ? feature.getBindModeAt(bindIndex) : BindMode.TOGGLE;
      this.pendingBindVisible = feature == null || !feature.hasBindAt(bindIndex) || feature.isBindVisibleAt(bindIndex);
      if (!this.open) {
         this.animation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.open = true;
      this.minX = minX;
      this.minY = minY;
      this.maxX = maxX;
      this.maxY = maxY;
      float preferredRightX = anchorX + 144.0F + 8.0F;
      float preferredLeftX = anchorX - 144.0F - 8.0F;
      this.popupX = preferredRightX + 144.0F <= maxX ? preferredRightX : preferredLeftX;
      this.popupY = anchorY;
      this.clampPosition();
   }

   private void applyBind(int bindCode) {
      if (this.feature != null) {
         BindMode mode = this.currentBindMode();
         boolean visible = this.currentBindVisible();
         if (this.bindIndex >= 0 && this.feature.hasBindAt(this.bindIndex)) {
            this.bindIndex = this.feature.setBindAt(this.bindIndex, bindCode);
         } else {
            this.bindIndex = this.feature.addBind(bindCode);
         }

         if (this.bindIndex >= 0) {
            this.feature.setBindModeAt(this.bindIndex, mode);
            this.feature.setBindVisibleAt(this.bindIndex, visible);
         }

         this.listeningForBind = false;
      }
   }

   private BindMode currentBindMode() {
      return this.feature != null && this.feature.hasBindAt(this.bindIndex) ? this.feature.getBindModeAt(this.bindIndex) : this.pendingBindMode;
   }

   private void setCurrentBindMode(BindMode mode) {
      if (this.feature != null && this.feature.hasBindAt(this.bindIndex)) {
         this.feature.setBindModeAt(this.bindIndex, mode);
      } else {
         this.pendingBindMode = mode;
      }
   }

   private boolean currentBindVisible() {
      return this.feature != null && this.feature.hasBindAt(this.bindIndex) ? this.feature.isBindVisibleAt(this.bindIndex) : this.pendingBindVisible;
   }

   private void toggleCurrentBindVisible() {
      if (this.feature != null && this.feature.hasBindAt(this.bindIndex)) {
         this.feature.setBindVisibleAt(this.bindIndex, !this.feature.isBindVisibleAt(this.bindIndex));
      } else {
         this.pendingBindVisible = !this.pendingBindVisible;
      }
   }

   private void deleteCurrentBind() {
      if (this.feature != null && this.bindIndex >= 0) {
         this.feature.removeBindAt(this.bindIndex);
      }

      this.close();
   }

   private void clampPosition() {
      this.popupX = clamp(this.popupX, this.minX, this.maxX - 144.0F);
      this.popupY = clamp(this.popupY, this.minY, this.maxY - 148.0F);
   }

   private static float clamp(float value, float min, float max) {
      return max < min ? min : Math.max(min, Math.min(max, value));
   }
}
