package org.ryzen.menu.pages.modules;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.feature.Feature;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ModuleBindPopup extends Component {
   public static final int POPUP_WIDTH = 144;
   private static final int POPUP_PADDING = 6;
   private static final int POPUP_GAP = 2;
   private static final int ITEM_HEIGHT = 32;
   private static final int ITEM_RADIUS = 8;
   private static final int ITEM_WIDTH = 132;
   private static final int DIVIDER_TOP_MARGIN = 8;
   private static final int DIVIDER_BOTTOM_MARGIN = 3;
   private static final int ICON_SIZE = 12;
   private static final int ICON_TEXT_GAP = 6;
   private Feature feature;
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

   public void place(Component owner, int mouseX, int mouseY, float alpha) {
      this.attach(owner, owner.x(), owner.y(), owner.width(), owner.height());
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.alpha = alpha;
      this.clampPosition();
   }

   public void openAt(Feature feature, float x, float y, float minX, float minY, float maxX, float maxY) {
      if (feature != null && feature.supportsBinds()) {
         this.feature = feature;
         this.open = true;
         this.popupX = x;
         this.popupY = y;
         this.minX = minX;
         this.minY = minY;
         this.maxX = maxX;
         this.maxY = maxY;
         this.animation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
         this.clampPosition();
      } else {
         this.closeImmediately();
      }
   }

   public void close() {
      if (this.open) {
         this.animation.animate(this.animation.getValue(), 0.0F, 120L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.open = false;
   }

   public void closeImmediately() {
      this.open = false;
      this.feature = null;
      this.animation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public boolean isOpen() {
      return this.open;
   }

   public ModuleBindPopup.Action handleMouseButton(int mouseX, int mouseY, int button) {
      if (this.open && this.feature != null) {
         if (!this.containsPopup(mouseX, mouseY)) {
            this.close();
            return ModuleBindPopup.Action.NONE;
         }

         if (button != 0) {
            return ModuleBindPopup.Action.NONE;
         }

         int bindRows = this.bindRowCount();

         for (int index = 0; index < bindRows; index++) {
            if (this.hit(mouseX, mouseY, this.popupX + 6.0F, this.bindRowY(index), 132.0F, 32.0F)) {
               if (this.feature.getBind().isEmpty()) {
                  return new ModuleBindPopup.Action(ModuleBindPopup.ActionType.CREATE_BIND, -1);
               }

               return new ModuleBindPopup.Action(ModuleBindPopup.ActionType.EDIT_BIND, index);
            }
         }

         if (this.hit(mouseX, mouseY, this.popupX + 6.0F, this.createRowY(), 132.0F, 32.0F)) {
            return new ModuleBindPopup.Action(ModuleBindPopup.ActionType.CREATE_BIND, -1);
         } else {
            return this.hit(mouseX, mouseY, this.popupX + 6.0F, this.reloadRowY(), 132.0F, 32.0F)
               ? new ModuleBindPopup.Action(ModuleBindPopup.ActionType.RELOAD, -1)
               : ModuleBindPopup.Action.NONE;
         }
      } else {
         return ModuleBindPopup.Action.NONE;
      }
   }

   public boolean containsPopup(int mouseX, int mouseY) {
      return this.open && this.hit(mouseX, mouseY, this.popupX, this.popupY, 144.0F, this.popupHeight());
   }

   public float popupX() {
      return this.popupX;
   }

   public float popupY() {
      return this.popupY;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      float progress = this.animation.getValue();
      if (this.feature != null && !(progress <= 0.001F)) {
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
      } else {
         if (!this.open) {
            this.feature = null;
         }
      }
   }

   private void renderContent() {
      this.glassPanel(this.sx(this.popupX), this.sy(this.popupY), this.px(144.0F), this.px(this.popupHeight()), this.px(12.0F), this.px(20.0F), this.alpha);
      float contentX = this.popupX + 6.0F;
      if (this.feature.getBind().isEmpty()) {
         this.renderRow(contentX, this.bindRowY(0), Textures.Icons.COMMAND, MenuText.ui("Unbound"), Theme.Colors.SECONDARY_DARK);
      } else {
         for (int index = 0; index < this.feature.getBind().size(); index++) {
            this.renderRow(
               contentX, this.bindRowY(index), Textures.Icons.COMMAND, MenuText.bind(this.feature.getBind().getDisplayValue(index)), Theme.Colors.TEXT_TEXT
            );
         }
      }

      float dividerY = this.dividerY();
      Render2DUtil.rect(this.sx(contentX), this.sy(dividerY), this.px(132.0F), this.px(1.0F))
         .color(0)
         .border(Math.max(0.5F, this.px(0.5F)), this.alpha(Theme.Colors.OUTLINES_MEDIUM, this.alpha))
         .draw();
      this.renderRow(contentX, this.createRowY(), Textures.Icons.CIRCLE_PLUS, MenuText.ui("New Hotkey"), Theme.Colors.TEXT_TEXT);
      this.renderRow(contentX, this.reloadRowY(), Textures.Icons.REFRESH_CCW, MenuText.ui("Reset"), Theme.Colors.TRAFFIC_CLOSE);
   }

   private void renderRow(float x, float y, class_2960 icon, String label, int color) {
      boolean hovered = this.hit(this.mouseX, this.mouseY, x, y, 132.0F, 32.0F);
      if (hovered) {
         Render2DUtil.rect(this.sx(x), this.sy(y), this.px(132.0F), this.px(32.0F))
            .color(this.alpha(Theme.Colors.OUTLINES_SMALL, this.alpha))
            .radius(this.px(8.0F))
            .draw();
      }

      float iconX = x + 6.0F;
      float iconY = y + 10.0F;
      this.texture(iconX, iconY, 12.0F, icon, color, this.alpha);
      float textX = iconX + 12.0F + 6.0F;
      float textWidth = Math.max(1.0F, x + 132.0F - 6.0F - textX);
      this.drawCenteredRowText(textX, iconY + 6.0F, textWidth, label, color);
   }

   private void drawCenteredRowText(float x, float centerY, float maxWidth, String label, int color) {
      MsdfFont font = UiFonts.sfPro(UiFontStyle.MEDIUM.weight());
      float screenFontSize = this.px(12.0F);
      float letterSpacing = screenFontSize * UiFontStyle.MEDIUM.letterSpacingEm();
      String displayLabel = font.ellipsize(label, screenFontSize, letterSpacing, this.px(maxWidth));
      float screenCenterY = this.sy(0.0F) + this.px(centerY);
      float baselineCenterOffset = this.measureBaselineCenterOffset(font, displayLabel, screenFontSize);
      float screenTextY = screenCenterY - baselineCenterOffset - font.ascender(screenFontSize);
      Render2DUtil.pushScissor(this.sx(x), this.sy(centerY - 16.0F), this.px(maxWidth), this.px(32.0F));
      Render2DUtil.text(this.sx(x), screenTextY, screenFontSize, displayLabel).style(UiFontStyle.MEDIUM).color(this.alpha(color, this.alpha)).draw();
      Render2DUtil.popScissor();
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

   private int bindRowCount() {
      return Math.max(1, this.feature == null ? 0 : this.feature.getBind().size());
   }

   private float bindRowY(int index) {
      return this.popupY + 6.0F + index * 34;
   }

   private float dividerY() {
      float bindRowsBottom = this.bindRowY(this.bindRowCount() - 1) + 32.0F;
      return bindRowsBottom + 8.0F;
   }

   private float createRowY() {
      return this.dividerY() + 3.0F;
   }

   private float reloadRowY() {
      return this.createRowY() + 32.0F + 2.0F;
   }

   private float popupHeight() {
      return this.reloadRowY() + 32.0F + 6.0F - this.popupY;
   }

   private void clampPosition() {
      this.popupX = clamp(this.popupX, this.minX, this.maxX);
      this.popupY = clamp(this.popupY, this.minY, this.maxPopupY());
   }

   private float maxPopupY() {
      float maxAllowedY = this.maxY - this.popupHeight();
      return Math.max(this.minY, maxAllowedY);
   }

   private static float clamp(float value, float min, float max) {
      return max < min ? min : Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   public static final class Action {
      public static final ModuleBindPopup.Action NONE = new ModuleBindPopup.Action(ModuleBindPopup.ActionType.NONE, -1);
      private final ModuleBindPopup.ActionType type;
      private final int bindIndex;

      private Action(ModuleBindPopup.ActionType type, int bindIndex) {
         this.type = type;
         this.bindIndex = bindIndex;
      }

      public ModuleBindPopup.ActionType type() {
         return this.type;
      }

      public int bindIndex() {
         return this.bindIndex;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum ActionType {
      NONE,
      EDIT_BIND,
      CREATE_BIND,
      RELOAD;
   }
}
