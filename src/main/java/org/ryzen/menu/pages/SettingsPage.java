package org.ryzen.menu.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.core.MenuAppearance;
import org.ryzen.menu.core.MenuBackground;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.i18n.UiLanguage;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.PageComponent;
import org.ryzen.menu.ui.SmoothScroll;
import org.ryzen.menu.ui.controls.DropdownComponent;
import org.ryzen.menu.ui.controls.IconButton;
import org.ryzen.menu.ui.controls.MarqueeText;
import org.ryzen.menu.ui.controls.ModeComponent;
import org.ryzen.menu.ui.controls.SliderComponent;
import org.ryzen.menu.ui.controls.ToggleComponent;
import org.ryzen.menu.ui.popups.DropdownPopup;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class SettingsPage extends PageComponent {
   private static final int ACCENT_SWATCH_COUNT = 10;
   private static final int ACCENT_SWATCH_Y = 256;
   private static final int ACCENT_SWATCH_SIZE = 18;
   private static final int ACCENT_SWATCH_GAP = 4;
   private static final int ACCENT_SWATCH_STEP = 22;
   private static final int ACCENT_SWATCH_WIDTH = 216;
   private static final int ACCENT_SWATCH_RIGHT = 488;
   private static final int ACCENT_SWATCH_X = 272;
   private static final int TOP_DROPDOWN_Y = 104;
   private static final int RESET_BUTTON_SIZE = 24;
   private static final int RESET_ICON_SIZE = 16;
   private static final int RESET_PADDING = 4;
   private static final int RESET_BUTTON_X = 968;
   private static final int RESET_BUTTON_Y = 110;
   private static final int TOP_DROPDOWN_RIGHT = 960;
   private static final String[] FPS_MODES = new String[]{"FPS Boost", "Balanced", "Quality"};
   private static final String[] LANGUAGE_MODES = UiLanguage.canonicalNames();
   private static final String[] BLUR_MODES = new String[]{"No blur", "Soft blur", "Strong blur"};
   private static final String[] CORNER_MODES = new String[]{"Small", "Medium", "Large"};
   private static final String[] BACKGROUND_MODES = MenuBackground.labels();
   private static final int CONTENT_TOP = 200;
   private static final int BACKGROUND_ROW_Y = 405;
   private static final int CONTENT_BOTTOM = 628;
   private final List<Component> children = new ArrayList<>();
   private final Map<String, MarqueeText> descriptionLabels = new HashMap<>();
   private final SliderComponent themeSlider = new SliderComponent(
      () -> this.themeValue, value -> this.themeValue = value, Theme.Colors.OUTLINES_SMALL, Theme.getAccent(), -1
   );
   private final SliderComponent uiScaleSlider = new SliderComponent(
         () -> this.pendingUiScaleValue, value -> this.pendingUiScaleValue = value, Theme.Colors.OUTLINES_SMALL, Theme.getAccent(), -1
      )
      .step(0.006666667F)
      .onRelease(this::applyUiScale);
   private final SliderComponent backgroundDimSlider = new SliderComponent(() -> this.backgroundDimValue, value -> {
      this.backgroundDimValue = value;
      MenuAppearance.setBackgroundDim(value);
   }, Theme.Colors.OUTLINES_SMALL, Theme.getAccent(), -1);
   private final ToggleComponent lowPerformanceToggle = new ToggleComponent(
      () -> this.lowPerformance, () -> this.lowPerformance = !this.lowPerformance, ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, Theme.getAccent()
   );
   private final ToggleComponent reduceShadowsToggle = new ToggleComponent(
      () -> this.reduceShadows, () -> this.reduceShadows = !this.reduceShadows, ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, Theme.getAccent()
   );
   private final ToggleComponent cacheUiToggle = new ToggleComponent(
      () -> this.cacheUi, () -> this.cacheUi = !this.cacheUi, ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, Theme.getAccent()
   );
   private final ToggleComponent soundToggle = new ToggleComponent(
      () -> this.sound, () -> this.sound = !this.sound, ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, Theme.getAccent()
   );
   private final ModeComponent fpsModeDropdown = new ModeComponent(() -> MenuText.option(FPS_MODES[this.fpsMode]));
   private final ModeComponent languageDropdown = new ModeComponent(() -> MenuText.option(LANGUAGE_MODES[this.languageMode]));
   private final ModeComponent blurModeDropdown = new ModeComponent(() -> MenuText.option(BLUR_MODES[this.blurMode]));
   private final ModeComponent roundedModeDropdown = new ModeComponent(() -> MenuText.option(CORNER_MODES[this.roundedMode]));
   private final DropdownPopup fpsPopup = new DropdownPopup(() -> FPS_MODES, () -> this.fpsMode, index -> this.fpsMode = index);
   private final DropdownPopup languagePopup = new DropdownPopup(() -> LANGUAGE_MODES, () -> this.languageMode, index -> {
      this.languageMode = index;
      UiLanguage.set(UiLanguage.byIndex(index));
   });
   private final DropdownPopup blurPopup = new DropdownPopup(() -> BLUR_MODES, () -> this.blurMode, index -> {
      this.blurMode = index;
      MenuAppearance.setBlurMode(index);
   });
   private final DropdownPopup cornersPopup = new DropdownPopup(() -> CORNER_MODES, () -> this.roundedMode, index -> {
      this.roundedMode = index;
      this.applyPanelRadius();
   });
   private final ModeComponent backgroundDropdown = new ModeComponent(() -> MenuText.option(BACKGROUND_MODES[this.backgroundMode]));
   private final DropdownPopup backgroundPopup = new DropdownPopup(() -> BACKGROUND_MODES, () -> this.backgroundMode, index -> {
      this.backgroundMode = index;
      MenuAppearance.setBackground(MenuBackground.byIndex(index));
   });
   private final List<DropdownPopup> popups = List.of(this.fpsPopup, this.languagePopup, this.blurPopup, this.cornersPopup, this.backgroundPopup);
   private final IconButton resetButton = new IconButton(Textures.Icons.REFRESH_CCW, 16, this::resetSettings).tint(Theme.Colors.SECONDARY, -1);
   private float themeValue = MenuConfigStore.getFloat("themeValue", 0.38F);
   private float pendingUiScaleValue = 0.30158734F;
   private float backgroundDimValue = MathUtil.clamp01(MenuConfigStore.getFloat("backgroundDim", MenuConfigStore.getFloat("blurStrength", 0.35F)));
   private boolean lowPerformance = MenuConfigStore.getBoolean("lowPerformance", true);
   private boolean reduceShadows = MenuConfigStore.getBoolean("reduceShadows", false);
   private boolean cacheUi = MenuConfigStore.getBoolean("cacheUi", true);
   private boolean sound = MenuConfigStore.getBoolean("sound", true);
   private int blurMode = MathUtil.clamp(MenuConfigStore.getInt("blurMode", 1), 0, BLUR_MODES.length - 1);
   private int fpsMode = MathUtil.clamp(MenuConfigStore.getInt("fpsMode", 0), 0, FPS_MODES.length - 1);
   private int languageMode = UiLanguage.current().ordinal();
   private int roundedMode = MathUtil.clamp(MenuConfigStore.getInt("roundedMode", 2), 0, Theme.Sizes.PANEL_RADII.length - 1);
   private int backgroundMode = MathUtil.clamp(MenuConfigStore.getInt("backgroundMode", 0), 0, BACKGROUND_MODES.length - 1);
   private final Animation accentAnimation = new Animation(180L, Animation.Easing.EASE_OUT_QUAD);
   private final SmoothScroll scroll = new SmoothScroll();
   private float scrollY;
   private int animatedAccentTarget;

   public SettingsPage() {
      this.children.add(this.themeSlider);
      this.children.add(this.uiScaleSlider);
      this.children.add(this.backgroundDimSlider);
      this.children.add(this.lowPerformanceToggle);
      this.children.add(this.reduceShadowsToggle);
      this.children.add(this.cacheUiToggle);
      this.children.add(this.soundToggle);
      Theme.setAccentIndex(MenuConfigStore.getInt("accentIndex", 0));
      MenuAppearance.setBlurMode(this.blurMode);
      MenuAppearance.setBackground(MenuBackground.byIndex(this.backgroundMode));
      MenuAppearance.setBackgroundDim(this.backgroundDimValue);
      this.animatedAccentTarget = Theme.accentIndex();
      this.accentAnimation.animate(this.animatedAccentTarget, this.animatedAccentTarget, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   @Override
   protected void onLayout() {
      if (!this.uiScaleSlider.isDragging()) {
         this.pendingUiScaleValue = this.state.uiScaleValue();
      }

      this.scrollY = this.scroll.update(this.maxScroll());
      this.placeControls();
   }

   private float maxScroll() {
      float viewportBottom = this.designY(this.y() + this.height());
      return Math.max(0.0F, 628.0F - viewportBottom);
   }

   private int oy(int designY) {
      return designY >= 200 ? designY - Math.round(this.scrollY) : designY;
   }

   public boolean handleMouseButton(int mouseX, int mouseY, int button) {
      boolean consumed = this.processMouseButton(mouseX, mouseY, button);
      if (consumed) {
         this.persistSettings();
      }

      return consumed;
   }

   private boolean processMouseButton(int mouseX, int mouseY, int button) {
      if (!this.contentContains(mouseX, mouseY)) {
         return false;
      }

      if (button == 0) {
         for (DropdownPopup popup : this.popups) {
            if (popup.isOpen() && popup.handleClick(mouseX, mouseY)) {
               return true;
            }
         }
      }

      if (button != 0) {
         return true;
      }

      for (Component child : this.children) {
         if (child.handleClick(mouseX, mouseY)) {
            return true;
         }
      }

      if (this.fpsModeDropdown.handleClick(mouseX, mouseY)) {
         this.openPopup(this.fpsPopup);
         return true;
      }

      if (this.languageDropdown.handleClick(mouseX, mouseY)) {
         this.openPopup(this.languagePopup);
         return true;
      }

      if (this.blurModeDropdown.handleClick(mouseX, mouseY)) {
         this.openPopup(this.blurPopup);
         return true;
      }

      if (this.roundedModeDropdown.handleClick(mouseX, mouseY)) {
         this.openPopup(this.cornersPopup);
         return true;
      }

      if (this.backgroundDropdown.handleClick(mouseX, mouseY)) {
         this.openPopup(this.backgroundPopup);
         return true;
      }

      if (this.resetButton.handleClick(mouseX, mouseY)) {
         return true;
      }

      if (this.hit(mouseX, mouseY, 272.0F, this.oy(256) - 6, 216.0F, 32.0F)) {
         this.setAccentIndex((int)MathUtil.clamp((this.designX(mouseX) - 272.0F) / 22.0F, 0.0F, Theme.accentCount() - 1));
      }

      return true;
   }

   private void openPopup(DropdownPopup popup) {
      for (DropdownPopup other : this.popups) {
         if (other != popup) {
            other.closeImmediately();
         }
      }

      popup.toggle();
   }

   public void drag(int mouseX, int mouseY) {
      this.themeSlider.drag(mouseX);
      this.uiScaleSlider.drag(mouseX);
      this.backgroundDimSlider.drag(mouseX);
   }

   public void releasePointer() {
      boolean wasDragging = this.themeSlider.isDragging() || this.uiScaleSlider.isDragging() || this.backgroundDimSlider.isDragging();
      this.themeSlider.releasePointer();
      this.uiScaleSlider.releasePointer();
      this.backgroundDimSlider.releasePointer();
      if (wasDragging) {
         this.persistSettings();
      }
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
      if (this.hit(mouseX, mouseY, 272.0F, this.oy(256) - 6, 216.0F, 32.0F)) {
         int next = Math.floorMod(Theme.accentIndex() + (vertical > 0.0 ? -1 : 1), Theme.accentCount());
         this.setAccentIndex(next);
         this.persistSettings();
      } else {
         if (this.contentContains(mouseX, mouseY)) {
            this.scroll.scroll(vertical, this.maxScroll());
         }
      }
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (!(this.progress <= 0.001F)) {
         this.pageHeader(MenuText.ui("Settings"), MenuText.ui("Customize your cheat client to meet your needs with just one click."));
         this.resetButton.render(minecraft, guiGraphicsExtractor);
         this.fpsModeDropdown.render(minecraft, guiGraphicsExtractor);
         Render2DUtil.pushScissor(this.x(), this.sy(200.0F), this.width(), this.y() + this.height() - this.sy(200.0F));
         this.renderBody(minecraft, guiGraphicsExtractor);
         Render2DUtil.popScissor();

         for (DropdownPopup popup : this.popups) {
            popup.render(minecraft, guiGraphicsExtractor);
         }
      }
   }

   private void renderBody(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.sectionLabel(32, 214, "Theme Editor");
      this.sectionLabel(536, 214, "Interface");
      this.sectionCard(16, 234, 488, 171);
      this.sectionCard(520, 234, 488, 279);
      this.sectionLabel(32, 423, "Performance");
      this.sectionCard(16, 442, 488, 174);
      this.sectionLabel(536, 532, "Controls");
      this.sectionCard(520, 550, 488, 66);
      this.settingText(32, 246, 39, "Client Color", "Adjust the main accent color of the interface.", 260);

      for (int index = 0; index < Theme.accentCount(); index++) {
         int swatchX = 272 + index * 22;
         this.rect(swatchX, this.oy(256), 18.0F, 18.0F, Theme.accent(index), 4.0F, this.progress);
      }

      this.settingDivider(32, dividerY(246, 39, 297, 42));
      this.settingText(32, 297, 42, "Theme Variable", "Fine-tune specific theme settings or scaling.", 340);
      this.textRight(
         355.0F,
         this.centeredTextY(this.oy(309) + 8.0F, 12.0F),
         12.0F,
         String.format(Locale.ROOT, "%.1f", this.themeValue),
         Theme.Colors.SECONDARY,
         this.progress
      );
      this.settingDivider(32, dividerY(297, 42, 351, 42));
      this.settingText(32, 351, 42, "Languages", "Choose the language that suits you.", 364);
      this.settingText(32, 454, 42, "Low Performance Mode", "Optimize UI rendering to increase FPS.", 458);
      this.settingDivider(32, dividerY(454, 42, 508, 42));
      this.settingText(32, 508, 42, "Reduce Shadows", "Disable interface shadow effects to save resources.", 458);
      this.settingDivider(32, dividerY(508, 42, 562, 42));
      this.settingText(32, 562, 42, "Cache UI", "Keep layout elements in memory to reduce stutters.", 458);
      this.settingText(536, 246, 39, "UI Scale", "Change the overall size of the client menus.", 810);
      this.textRight(
         859.0F,
         this.centeredTextY(this.oy(256) + 8.0F, 12.0F),
         12.0F,
         String.format(Locale.ROOT, "%.0f%%", MathUtil.lerp(50.0F, 200.0F, this.pendingUiScaleValue)),
         Theme.Colors.SECONDARY,
         this.progress
      );
      this.settingDivider(536, dividerY(246, 39, 297, 42));
      this.settingText(536, 297, 42, "Glass Blur", "Controls blur inside frosted glass cards and popups.", this.blurDropdownLeft() - 12);
      this.settingDivider(536, dividerY(297, 42, 351, 42));
      boolean backgroundEnabled = this.backgroundEnabled();
      this.settingText(
         536,
         351,
         42,
         "Background Dimming",
         backgroundEnabled ? "Darken the animated menu background." : "Select a Menu Background to enable.",
         800,
         backgroundEnabled
      );
      this.textRight(
         859.0F,
         this.centeredTextY(this.oy(363) + 8.0F, 12.0F),
         12.0F,
         backgroundEnabled ? String.format(Locale.ROOT, "%.0f%%", this.backgroundDimValue * 100.0F) : MenuText.option("Off"),
         backgroundEnabled ? Theme.Colors.SECONDARY : Theme.Colors.TEXT_GHOST,
         this.progress
      );
      this.settingDivider(536, dividerY(351, 42, 405, 42));
      this.settingText(536, 405, 42, "Menu Background", "Animated shader behind the menu content.", 868);
      this.settingDivider(536, dividerY(405, 42, 459, 42));
      this.settingText(536, 459, 42, "Rounded Corners", "Select the corner smoothness for menu elements.", 868);
      this.settingText(536, 562, 42, "Toggle Sound", "Enable or disable audio feedback for clicks.", 962);

      for (Component child : this.children) {
         child.render(minecraft, guiGraphicsExtractor);
      }

      this.languageDropdown.render(minecraft, guiGraphicsExtractor);
      this.blurModeDropdown.render(minecraft, guiGraphicsExtractor);
      this.roundedModeDropdown.render(minecraft, guiGraphicsExtractor);
      this.backgroundDropdown.render(minecraft, guiGraphicsExtractor);
      float indicatorX = 272.0F + this.accentAnimation.getValue() * 22.0F;
      this.outline(indicatorX, this.oy(256), 18.0F, 18.0F, -1, 4.0F, 2.0F, this.progress);
   }

   private void setAccentIndex(int index) {
      float from = this.accentAnimation.getValue();
      Theme.setAccentIndex(index);
      this.animatedAccentTarget = Theme.accentIndex();
      this.accentAnimation.animate(from, this.animatedAccentTarget, 180L, Animation.Easing.EASE_OUT_QUAD);
   }

   private void placeControls() {
      int accent = Theme.getAccent();
      this.themeSlider.place(this, 360, this.oy(309), 128, 16).style(Theme.Colors.OUTLINES_SMALL, accent, -1).alpha(this.progress);
      this.uiScaleSlider.place(this, 864, this.oy(256), 128, 16).style(Theme.Colors.OUTLINES_SMALL, accent, -1).alpha(this.progress);
      this.backgroundDimSlider
         .place(this, 864, this.oy(363), 128, 16)
         .style(Theme.Colors.OUTLINES_SMALL, accent, -1)
         .enabled(this.backgroundEnabled())
         .alpha(this.progress);
      this.lowPerformanceToggle
         .place(this, 470, this.oy(466), 18, 18)
         .style(ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, accent)
         .alpha(this.progress)
         .circle();
      this.reduceShadowsToggle
         .place(this, 470, this.oy(520), 18, 18)
         .style(ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, accent)
         .alpha(this.progress)
         .circle();
      this.cacheUiToggle.place(this, 470, this.oy(574), 18, 18).style(ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, accent).alpha(this.progress).circle();
      this.soundToggle.place(this, 974, this.oy(574), 18, 18).style(ToggleComponent.Style.CIRCLE, Theme.Colors.CONTROL, accent).alpha(this.progress).circle();
      this.resetButton.place(this, 968.0F, 110.0F, 24, this.mouseX, this.mouseY).alpha(this.progress);
      this.placePillRight(this.fpsModeDropdown, FPS_MODES[this.fpsMode], 960, 104);
      this.placeValueRight(this.languageDropdown, 488, this.oy(centeredControlY(351, 42, 24)));
      this.placePillRight(this.blurModeDropdown, BLUR_MODES[this.blurMode], 992, this.oy(centeredControlY(297, 42, 36)));
      this.placeValueRight(this.roundedModeDropdown, 992, this.oy(centeredControlY(459, 42, 24)));
      this.placeValueRight(this.backgroundDropdown, 992, this.oy(centeredControlY(405, 42, 24)));
      int popupWidth = 144;
      this.fpsPopup.place(this, 960 - popupWidth, 144, this.mouseX, this.mouseY, this.progress);
      this.languagePopup.place(this, 488 - popupWidth, this.oy(centeredControlY(351, 42, 24) + 24 + 4), this.mouseX, this.mouseY, this.progress);
      this.blurPopup.place(this, 992 - popupWidth, this.oy(centeredControlY(297, 42, 36) + 43), this.mouseX, this.mouseY, this.progress);
      this.cornersPopup.place(this, 992 - popupWidth, this.oy(centeredControlY(459, 42, 24) + 24 + 4), this.mouseX, this.mouseY, this.progress);
      this.backgroundPopup.place(this, 992 - popupWidth, this.oy(centeredControlY(405, 42, 24) + 24 + 4), this.mouseX, this.mouseY, this.progress);
   }

   private void placePillRight(DropdownComponent dropdown, String value, int right, int y) {
      int width = DropdownComponent.pillWidth(MenuText.option(value));
      dropdown.place(this, right - width, y, width, 36).style(DropdownComponent.Style.PILL).alpha(this.progress);
   }

   private int blurDropdownLeft() {
      return 992 - DropdownComponent.pillWidth(MenuText.option(BLUR_MODES[this.blurMode]));
   }

   private void placeValueRight(DropdownComponent dropdown, int right, int y) {
      dropdown.place(this, right - 112, y, 112, 24).style(DropdownComponent.Style.VALUE).alpha(this.progress);
   }

   private static int centeredControlY(int rowY, int rowHeight, int controlHeight) {
      return rowY + Math.round((rowHeight - controlHeight) / 2.0F);
   }

   private void sectionLabel(int x, int y, String label) {
      this.text(x, this.oy(y), 12.0F, MenuText.ui(label), Theme.Colors.TEXT_GHOST, this.progress, UiFontStyle.REGULAR);
   }

   private void sectionCard(int x, int y, int width, int height) {
      Render2DUtil.rect(this.sx(x), this.sy(this.oy(y)), this.px(width), this.px(height))
         .color(this.alpha(Theme.Colors.BACKGROUND_SURFACE_S, this.progress))
         .radius(this.px(8.0F))
         .border(Math.max(0.5F, this.px(0.5F)), this.alpha(Theme.Colors.OUTLINES_SMALL, this.progress))
         .draw();
   }

   private void settingDivider(int x, int y) {
      this.rect(x, this.oy(y), 456.0F, 1.0F, Theme.Colors.OUTLINES_SMALL, 0.0F, this.progress);
   }

   private static int dividerY(int previousY, int previousHeight, int nextY, int nextHeight) {
      return Math.round((previousY + previousHeight + nextY) / 2.0F + (nextHeight - previousHeight) / 4.0F);
   }

   private void settingText(int x, int rowY, int rowHeight, String title, String description, int textRight) {
      this.settingText(x, rowY, rowHeight, title, description, textRight, true);
   }

   private void settingText(int x, int rowY, int rowHeight, String title, String description, int textRight, boolean enabled) {
      float titleSize = 14.0F;
      float descriptionSize = 12.0F;
      float gap = 4.0F;
      float stateAlpha = this.progress * (enabled ? 1.0F : 0.45F);
      float titleHeight = UiFonts.sfProDisplay().textHeight(this.px(titleSize));
      float descriptionHeight = UiFonts.sfProDisplay().textHeight(this.px(descriptionSize));
      float blockHeight = titleHeight + this.px(gap) + descriptionHeight;
      float titleY = this.sy(this.oy(rowY)) + (this.px(rowHeight) - blockHeight) / 2.0F;
      Render2DUtil.text(this.sx(x), titleY, this.px(titleSize), MenuText.ui(title))
         .style(UiFontStyle.MEDIUM)
         .color(this.alpha(enabled ? Theme.Colors.TEXT_TEXT : Theme.Colors.TEXT_GHOST, stateAlpha))
         .draw();
      float descriptionY = titleY + titleHeight + this.px(gap);
      MarqueeText marquee = this.descriptionLabels.computeIfAbsent(title + "\u0000" + description, ignored -> new MarqueeText(() -> MenuText.ui(description)));
      marquee.placeAt(this, this.sx(x), descriptionY, this.px(Math.max(0, textRight - x)), descriptionHeight, this.mouseX, this.mouseY)
         .style(descriptionSize, UiFontStyle.REGULAR, enabled ? Theme.Colors.SECONDARY : Theme.Colors.TEXT_GHOST, stateAlpha)
         .render(class_310.method_1551(), null);
   }

   private void resetSettings() {
      this.setAccentIndex(0);
      this.themeValue = 0.38F;
      this.pendingUiScaleValue = 0.30158734F;
      this.applyUiScale();
      this.backgroundDimValue = 0.35F;
      MenuAppearance.setBackgroundDim(this.backgroundDimValue);
      this.lowPerformance = true;
      this.reduceShadows = false;
      this.cacheUi = true;
      this.sound = true;
      this.blurMode = 1;
      MenuAppearance.setBlurMode(this.blurMode);
      this.roundedMode = 2;
      this.applyPanelRadius();
      this.backgroundMode = 0;
      MenuAppearance.setBackground(MenuBackground.NONE);
      this.languageMode = 0;
      UiLanguage.set(UiLanguage.ENGLISH);
      this.fpsMode = 0;
   }

   private void persistSettings() {
      MenuConfigStore.save(data -> {
         data.addProperty("accentIndex", Theme.accentIndex());
         data.addProperty("themeValue", this.themeValue);
         data.remove("blurStrength");
         data.addProperty("backgroundDim", this.backgroundDimValue);
         data.addProperty("blurMode", this.blurMode);
         data.addProperty("fpsMode", this.fpsMode);
         data.remove("languageMode");
         data.addProperty("language", UiLanguage.byIndex(this.languageMode).storageKey());
         data.addProperty("roundedMode", this.roundedMode);
         data.addProperty("backgroundMode", this.backgroundMode);
         data.addProperty("lowPerformance", this.lowPerformance);
         data.addProperty("reduceShadows", this.reduceShadows);
         data.addProperty("cacheUi", this.cacheUi);
         data.addProperty("sound", this.sound);
      });
   }

   private boolean backgroundEnabled() {
      return MenuBackground.byIndex(this.backgroundMode).isAnimated();
   }

   private void applyUiScale() {
      if (this.state != null) {
         this.state.setUiScaleValue(this.pendingUiScaleValue);
      }
   }

   private void applyPanelRadius() {
      if (this.state != null) {
         this.state.setPanelRadius(Theme.Sizes.PANEL_RADII[this.roundedMode]);
      }
   }
}
