package org.ryzen.menu.clickgui;

import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.visual.HudFeature;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.menu.core.MenuOverlayState;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiHudPage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 67.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 546.0F;
   private static final float OPEN_WIDTH = 160.0F;
   private static final float OPEN_HEIGHT = 30.0F;
   private static final float OPEN_X = 733.0F;
   private static final float OPEN_Y = 80.0F;
   private static final float SIZE_Y = 118.0F;
   private static final float SIZE_TRACK_Y = 144.0F;
   private static final float ROW_X = 115.0F;
   private static final float ROW_WIDTH = 779.0F;
   private static final float LIST_TOP = 172.0F;
   private static final float LIST_Y = 180.0F;
   private static final float CARD_WIDTH = 381.0F;
   private static final float CARD_HEIGHT = 56.0F;
   private static final float CARD_STEP = 66.0F;
   private static final float LEFT_X = 115.0F;
   private static final float RIGHT_X = 515.0F;
   private static final float TOGGLE_OFFSET_X = 333.0F;
   private static final float TOGGLE_OFFSET_Y = 18.0F;
   private static final Map<String, class_2960> ICONS = Map.ofEntries(
      Map.entry("Watermark", Textures.Logos.BOLT),
      Map.entry("Stafflist", Textures.Icons.USER_ROUND_CHECK),
      Map.entry("Cooldowns", Textures.Icons.REFRESH_CCW),
      Map.entry("Effects", Textures.Icons.SPARKLES),
      Map.entry("Keybinds", Textures.Icons.KEYBOARD),
      Map.entry("Anarchy", Textures.Icons.BOXES),
      Map.entry("Target", Textures.Icons.USER_ROUND),
      Map.entry("Notifications", Textures.Icons.TRIANGLE_ALERT),
      Map.entry("Hotbar", Textures.Icons.GAMEPAD),
      Map.entry("QuickUse", Textures.Icons.DICES),
      Map.entry("Coordinates", Textures.Icons.MOVE_3D),
      Map.entry("ArmorHud", Textures.Icons.SCAN_HEART),
      Map.entry("Potions", Textures.Icons.SPARKLES)
   );
   private float scrollOffset;
   private boolean draggingSize;

   private static HudFeature hud() {
      return FeatureManager.INSTANCE.getFeature(HudFeature.class);
   }

   private static MultiSelectSetting elements() {
      HudFeature hud = hud();
      return hud == null ? null : hud.elements;
   }

   private static NumberSetting size() {
      HudFeature hud = hud();
      return hud == null ? null : hud.size;
   }

   private static List<String> options() {
      MultiSelectSetting elements = elements();
      return elements == null ? List.of() : elements.getOptions();
   }

   public void layout(ClickGuiCanvas canvas) {
      this.scrollOffset = clamp(this.scrollOffset, 0.0F, maxScroll());
   }

   public void render(ClickGuiCanvas canvas) {
      canvas.outlinedRect(94.0F, 67.0F, 821.0F, 546.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      canvas.texture(115.0F, 87.0F, 13.0F, 13.0F, Textures.Header.HUD, ClickGuiPalette.accent());
      canvas.text(140.0F, 85.0F, 15.0F, MenuText.ui("HUD"), -1, UiFontStyle.MEDIUM);
      boolean openHovered = canvas.hit(733.0F, 80.0F, 160.0F, 30.0F);
      canvas.rect(733.0F, 80.0F, 160.0F, 30.0F, openHovered ? ColorUtil.multiplyRgb(ClickGuiPalette.accent(), 1.08F) : ClickGuiPalette.accent(), 10.0F);
      canvas.texture(749.0F, 89.0F, 12.0F, 12.0F, Textures.Header.HUD, -1);
      canvas.text(829.0F, 88.0F, 12.0F, MenuText.ui("HUD Redactor"), -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
      this.renderSize(canvas);
      MultiSelectSetting elements = elements();
      List<String> options = options();
      if (elements != null && !options.isEmpty()) {
         canvas.pushScissor(94.0F, 172.0F, 821.0F, 441.0F);

         for (int index = 0; index < options.size(); index++) {
            String option = options.get(index);
            this.renderCard(canvas, option, elements.isSelected(option), columnX(index), rowY(index) - this.scrollOffset);
         }

         canvas.popScissor();
      }
   }

   private void renderSize(ClickGuiCanvas canvas) {
      NumberSetting size = size();
      if (size != null) {
         canvas.texture(115.0F, 121.0F, 13.412F, 12.0F, Textures.Icons.OPTION, ClickGuiPalette.accent());
         canvas.text(136.0F, 118.0F, 15.0F, MenuText.ui("Size"), -1, UiFontStyle.MEDIUM);
         canvas.text(894.0F, 122.0F, 12.0F, size.getDisplayValue(), ClickGuiPalette.accent(), UiFontStyle.MEDIUM, TextAlign.RIGHT);
         float progress = clamp((float)size.getProgress(), 0.0F, 1.0F);
         canvas.rect(115.0F, 148.0F, 779.0F, 3.0F, ClickGuiPalette.CONTROL, 1.5F);
         canvas.rect(115.0F, 148.0F, 779.0F * progress, 3.0F, ClickGuiPalette.accent(), 1.5F);
         float knob = 121.5F + 766.0F * progress;
         canvas.rect(knob - 6.5F, 143.0F, 13.0F, 13.0F, -1, 6.5F);
      }
   }

   private void updateSize(ClickGuiCanvas canvas) {
      NumberSetting size = size();
      if (size != null) {
         float fraction = clamp((canvas.mouseDesignX() - 115.0F - 6.5F) / 766.0F, 0.0F, 1.0F);
         size.setValue(size.getMin() + (size.getMax() - size.getMin()) * fraction);
      }
   }

   private void renderCard(ClickGuiCanvas canvas, String option, boolean enabled, float x, float y) {
      if (!(y + 56.0F < 172.0F) && !(y > 613.0F)) {
         boolean hovered = canvas.hit(x, y, 381.0F, 56.0F);
         canvas.outlinedRect(x, y, 381.0F, 56.0F, hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE, 15.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
         int tint = enabled ? ClickGuiPalette.accent() : ClickGuiPalette.TEXT_MUTED;
         canvas.rect(x + 12.0F, y + 8.5F, 39.0F, 39.0F, ClickGuiPalette.CONTROL, 19.5F);
         canvas.texture(x + 24.0F, y + 20.5F, 15.0F, 15.0F, ICONS.getOrDefault(option, Textures.Icons.BOXES), tint);
         canvas.text(
            x + 64.0F, y + 20.0F, 14.0F, fit(canvas, MenuText.ui(option), 261.0F, 14.0F), enabled ? -1 : ClickGuiPalette.TEXT_MUTED, UiFontStyle.MEDIUM
         );
         renderToggle(canvas, x + 333.0F, y + 18.0F, enabled);
      }
   }

   private static void renderToggle(ClickGuiCanvas canvas, float x, float y, boolean enabled) {
      canvas.rect(x, y, 33.0F, 20.0F, enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK, 10.0F);
      float knobX = x + (enabled ? 16.0F : 3.0F);
      canvas.rect(knobX, y + 2.5F, 15.0F, 15.0F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 7.5F);
      if (enabled) {
         canvas.texture(knobX + 5.0F, y + 5.5F, 6.0F, 4.493F, Textures.Icons.CHECK, ClickGuiPalette.accent());
      } else {
         canvas.texture(knobX + 4.5F, y + 4.5F, 6.0F, 6.0F, Textures.Icons.X, ClickGuiPalette.OFF_TRACK);
      }
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button, MenuOverlayState state) {
      if (button != 0) {
         return canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
      }

      if (canvas.hit(733.0F, 80.0F, 160.0F, 30.0F)) {
         state.setHudLayoutMode(true);
         return true;
      }

      if (canvas.hit(115.0F, 140.0F, 779.0F, 20.0F)) {
         this.draggingSize = true;
         this.updateSize(canvas);
         return true;
      }

      MultiSelectSetting elements = elements();
      List<String> options = options();
      if (elements != null) {
         for (int index = 0; index < options.size(); index++) {
            float y = rowY(index) - this.scrollOffset;
            if (y + 56.0F >= 172.0F && canvas.hit(columnX(index), y, 381.0F, 56.0F)) {
               elements.toggle(options.get(index));
               return true;
            }
         }
      }

      return canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
   }

   public void drag(ClickGuiCanvas canvas) {
      if (this.draggingSize) {
         this.updateSize(canvas);
      }
   }

   public void release() {
      this.draggingSize = false;
   }

   public void scroll(double amount) {
      this.scrollOffset = clamp(this.scrollOffset - (float)amount * 32.0F, 0.0F, maxScroll());
   }

   private static float columnX(int index) {
      return index % 2 == 0 ? 115.0F : 515.0F;
   }

   private static float rowY(int index) {
      return 180.0F + index / 2 * 66.0F;
   }

   private static float maxScroll() {
      int rows = (options().size() + 1) / 2;
      if (rows == 0) {
         return 0.0F;
      }

      float contentBottom = 180.0F + (rows - 1) * 66.0F + 56.0F + 16.0F;
      return Math.max(0.0F, contentBottom - 613.0F);
   }

   private static String fit(ClickGuiCanvas canvas, String raw, float maxWidth, float size) {
      String value = raw == null ? "" : raw;
      if (canvas.textWidth(value, size, UiFontStyle.MEDIUM) <= maxWidth) {
         return value;
      }

      int end = value.length();

      while (end > 0 && canvas.textWidth(value.substring(0, end) + "...", size, UiFontStyle.MEDIUM) > maxWidth) {
         end--;
      }

      return value.substring(0, end).stripTrailing() + "...";
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   public String headerTitle() {
      return MenuText.ui("HUD");
   }

   public String headerDescription() {
      return MenuText.ui("Game overlay elements.");
   }
}
