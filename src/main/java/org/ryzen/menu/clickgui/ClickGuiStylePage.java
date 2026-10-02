package org.ryzen.menu.clickgui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ClickGuiStylePage {
   private static final float PANEL_X = 94.0F;
   private static final float PANEL_Y = 67.0F;
   private static final float PANEL_WIDTH = 821.0F;
   private static final float PANEL_HEIGHT = 560.0F;
   private static final float CARD_X = 115.0F;
   private static final float CARD_WIDTH = 778.0F;
   private static final float CARD_HEIGHT = 56.0F;
   private static final float FIRST_CARD_Y = 118.0F;
   private static final float CARD_STEP = 66.0F;
   private static final float SWATCH_SIZE = 14.0F;
   private static final float SWATCH_STEP = 20.0F;
   private static final float LIST_BOTTOM_PADDING = 16.0F;
   private static final int CUSTOM_ACCENT_INDEX = 5;
   private static final int DEFAULT_ACCENT = ColorUtil.rgb(255, 125, 32);
   private static final int BACKDROP_DARK = ColorUtil.rgba(18, 18, 18, 61);
   private static final ClickGuiStylePage.ThemePreset[] PRESETS = new ClickGuiStylePage.ThemePreset[]{
      new ClickGuiStylePage.ThemePreset("Azure", Theme.accent(0), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Indigo", Theme.accent(1), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Violet", Theme.accent(2), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Magenta", Theme.accent(3), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Rose", Theme.accent(4), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Coral", Theme.accent(6), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Amber", Theme.accent(7), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Gold", Theme.accent(8), BACKDROP_DARK),
      new ClickGuiStylePage.ThemePreset("Lime", Theme.accent(9), BACKDROP_DARK)
   };
   private float scrollOffset;
   private int activeCard;

   public ClickGuiStylePage() {
      int storedColor = MenuConfigStore.getInt("accentColor", DEFAULT_ACCENT);
      int storedIndex = MenuConfigStore.getInt("accentIndex", 5);
      Theme.saveAccentPreset(5, storedColor);
      Theme.setAccentIndex(storedIndex);
      this.activeCard = presetIndexFor(MenuConfigStore.getString("themePreset", ""), storedColor);
   }

   private static int presetIndexFor(String storedName, int storedColor) {
      for (int index = 0; index < PRESETS.length; index++) {
         if (PRESETS[index].title().equalsIgnoreCase(storedName)) {
            return index;
         }
      }

      for (int index = 0; index < PRESETS.length; index++) {
         if (ColorUtil.withAlpha(PRESETS[index].accent(), 255) == ColorUtil.withAlpha(storedColor, 255)) {
            return index;
         }
      }

      return -1;
   }

   public void layout(ClickGuiCanvas canvas) {
      this.scrollOffset = clamp(this.scrollOffset, 0.0F, maxScroll());
   }

   public void render(ClickGuiCanvas canvas) {
      canvas.outlinedRect(94.0F, 67.0F, 821.0F, 560.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      canvas.pushScissor(94.0F, 67.0F, 821.0F, 560.0F);
      this.renderThemeList(canvas);
      canvas.popScissor();
   }

   public boolean mousePressed(ClickGuiCanvas canvas, int button) {
      if (button != 0) {
         return canvas.hit(94.0F, 67.0F, 821.0F, 560.0F);
      }

      float offset = this.scrollOffset;

      for (int index = 0; index < PRESETS.length; index++) {
         if (canvas.hit(115.0F, cardY(index) - offset, 778.0F, 56.0F)) {
            this.applyPreset(index);
            return true;
         }
      }

      return canvas.hit(94.0F, 67.0F, 821.0F, 560.0F);
   }

   private void applyPreset(int index) {
      ClickGuiStylePage.ThemePreset preset = PRESETS[index];
      this.activeCard = index;
      Theme.saveAccentPreset(5, preset.accent());
      MenuConfigStore.save(data -> {
         data.addProperty("accentColor", preset.accent());
         data.addProperty("accentIndex", 5);
         data.addProperty("themePreset", preset.title());
      });
   }

   public void scroll(double amount) {
      this.scrollOffset = clamp(this.scrollOffset - (float)amount * 28.0F, 0.0F, maxScroll());
   }

   public String headerTitle() {
      return MenuText.ui("Themes");
   }

   public String headerDescription() {
      return MenuText.ui("Client colours and theme presets.");
   }

   private void renderThemeList(ClickGuiCanvas canvas) {
      float offset = this.scrollOffset;
      canvas.texture(115.0F, 86.0F - offset, 11.0F, 14.426F, Textures.Icons.PALETTE, ClickGuiPalette.accent());
      canvas.text(137.0F, 85.0F - offset, 15.0F, MenuText.ui("Themes"), -1, UiFontStyle.MEDIUM);

      for (int index = 0; index < PRESETS.length; index++) {
         this.renderTile(canvas, index, cardY(index) - offset);
      }
   }

   private void renderTile(ClickGuiCanvas canvas, int index, float y) {
      ClickGuiStylePage.ThemePreset preset = PRESETS[index];
      boolean active = this.activeCard == index;
      boolean hovered = canvas.hit(115.0F, y, 778.0F, 56.0F);
      int fill = active ? preset.accent() : (hovered ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE);
      canvas.outlinedRect(115.0F, y, 778.0F, 56.0F, fill, 12.0F, 0.5F, active ? 0 : ClickGuiPalette.CARD_STROKE);
      int label = active ? -1 : -1;
      canvas.texture(131.0F, y + 12.0F, 11.004F, 14.426F, Textures.Icons.PALETTE, active ? -1 : preset.accent());
      canvas.text(151.0F, y + 12.0F, 14.0F, preset.title(), label, UiFontStyle.MEDIUM);
      canvas.text(
         151.0F,
         y + 33.0F,
         11.0F,
         MenuText.ui(active ? "Active" : "Inactive"),
         active ? ColorUtil.rgba(255, 255, 255, 179) : ClickGuiPalette.TEXT_MUTED,
         UiFontStyle.REGULAR
      );
      int[] swatches = preset.swatches();

      for (int dot = 0; dot < swatches.length; dot++) {
         float dotX = 873.0F - (swatches.length - dot) * 20.0F;
         canvas.rect(dotX, y + 28.0F - 7.0F, 14.0F, 14.0F, swatches[dot], 7.0F);
      }
   }

   private static float cardY(int index) {
      return 118.0F + index * 66.0F;
   }

   private static float maxScroll() {
      float contentBottom = cardY(PRESETS.length - 1) + 56.0F + 16.0F;
      return Math.max(0.0F, contentBottom - 627.0F);
   }

   private static float clamp(float value, float min, float max) {
      return Math.max(min, Math.min(max, value));
   }

   @Environment(EnvType.CLIENT)
   private record ThemePreset(String title, int accent, int backdrop) {
      int[] swatches() {
         return new int[]{this.accent, this.backdrop};
      }
   }
}
