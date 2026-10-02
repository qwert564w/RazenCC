package org.ryzen.menu.clickgui;

import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.lwjgl.glfw.GLFW;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.menu.core.MenuOverlayState;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.menu.core.Subscription;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.i18n.UiLanguage;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class FigmaClickGui {
   private static final float WINDOW_HEIGHT = 627.0F;
   private static final float HEADER_HEIGHT = 54.0F;
   private static final float CONTENT_X = 94.0F;
   private static final float CONTENT_WIDTH = 821.0F;
   private static final float MODULE_CONTENT_Y = 65.0F;
   private static final float PAGE_CONTENT_Y = 67.0F;
   private static final float CONTENT_RADIUS = 17.0F;
   private static final float SIDEBAR_X = 19.0F;
   private static final float SIDEBAR_Y = 67.0F;
   private static final float SIDEBAR_WIDTH = 43.276F;
   private static final float SIDEBAR_ACTIVE_SIZE = 32.0F;
   private static final float SIDEBAR_HEIGHT = 456.981F;
   private static final float SIDEBAR_MASK_HEIGHT = 491.602F;
   private static final float CALLOUT_HEIGHT = 29.0F;
   private static final float CALLOUT_PILL_HEIGHT = 22.0F;
   private static final float BREADCRUMB_X = 82.0F;
   private static final float BREADCRUMB_GAP = 21.0F;
   private static final float BREADCRUMB_LIMIT = 786.0F;
   private static final String PROFILE_NAME = "https://t.me/crackdelay";
   private static final float PROFILE_RIGHT = 916.0F;
   private static final float PROFILE_MIN_WIDTH = 120.0F;
   private static final float PROFILE_TEXT_SIZE = 11.0F;
   private static final float PROFILE_TEXT_INSET = 27.119F;
   private static final float PROFILE_TEXT_AVATAR_GAP = 8.0F;
   private static final float PROFILE_AVATAR_SIZE = 25.133F;
   private static final float PROFILE_AVATAR_RIGHT_PADDING = 2.934F;
   private static final float PROFILE_CHROME_WIDTH = 63.185997F;
   private static final float PROFILE_MAX_TEXT_WIDTH = 150.0F;
   private static final float PROFILE_SUMMARY_GAP = 10.0F;
   private static final float SEARCH_BAR_X = 302.0F;
   private static final float SEARCH_BAR_Y = 660.0F;
   private static final float SEARCH_LIST_Y = 118.0F;
   private static final float SEARCH_LIST_HEIGHT = 486.0F;
   private static final float SEARCH_ROW_HEIGHT = 66.0F;
   private static final float SEARCH_ROW_STEP = 80.0F;
   private static final float LANGUAGE_X = -7.0F;
   private static final float LANGUAGE_Y = 581.0F;
   private static final float LANGUAGE_WIDTH = 97.0F;
   private static final float LANGUAGE_ROW_HEIGHT = 16.0F;
   private static final float LANGUAGE_PADDING = 6.0F;
   private static final float[][] SIDEBAR_ICON_RECTS = new float[][]{
      {33.065F, 84.31F, 15.147F, 14.512F},
      {33.065F, 123.259F, 15.147F, 16.661F},
      {33.425F, 170.862F, 14.858F, 12.479F},
      {32.464F, 209.33F, 15.387F, 15.387F},
      {34.387F, 255.49F, 13.464F, 13.464F},
      {34.387F, 295.881F, 14.342F, 12.646F},
      {34.387F, 335.31F, 14.425F, 10.946F},
      {34.387F, 372.816F, 11.004F, 14.426F},
      {34.387F, 414.169F, 12.451F, 12.358F},
      {33.838F, 453.348F, 13.6F, 14.0F},
      {33.638F, 493.348F, 14.0F, 14.0F}
   };
   private static final float[] SIDEBAR_CENTERS = new float[]{
      91.566F, 131.59F, 177.102F, 217.024F, 262.222F, 302.204F, 340.783F, 380.029F, 420.348F, 460.348F, 500.348F
   };
   private static final class_2960[] SIDEBAR_ICONS = new class_2960[]{
      Textures.Icons.SWORDS,
      Textures.Icons.PERSON_STANDING,
      Textures.Icons.EYE,
      Textures.Icons.GAMEPAD,
      Textures.Icons.BOXES,
      Textures.Icons.BRAIN,
      Textures.Icons.HARD_DRIVE,
      Textures.Icons.PALETTE,
      Textures.Icons.KEYBOARD,
      Textures.Icons.SHIRT,
      Textures.Icons.AUTOBUY
   };
   private static final FeatureCategory[] CATEGORIES = FeatureCategory.values();
   private final ClickGuiCanvas canvas = new ClickGuiCanvas();
   private final ClickGuiModulesPage modules = new ClickGuiModulesPage();
   private final ClickGuiBindsPage binds = new ClickGuiBindsPage();
   private final ClickGuiConfigsPage configs = new ClickGuiConfigsPage();
   private final ClickGuiStylePage style = new ClickGuiStylePage();
   private final ClickGuiHudPage hud = new ClickGuiHudPage();
   private final ClickGuiCosmeticsPage cosmetics = new ClickGuiCosmeticsPage();
   private final ClickGuiRedactorPage autoBuy = new ClickGuiRedactorPage();
   private final StringBuilder searchQuery = new StringBuilder();
   private MenuOverlayState state;
   private MenuPage page = MenuPage.NONE;
   private FeatureCategory category = FeatureCategory.COMBAT;
   private boolean searchFocused;
   private boolean languageOpen;
   private float searchScroll;

   public FigmaClickGui() {
      Theme.setAccentIndex(MenuConfigStore.getInt("accentIndex", 5));
   }

   public void layout(float x, float y, float width, MenuOverlayState state, int mouseX, int mouseY) {
      this.state = state;
      this.page = state.displayPage();
      if (this.page == MenuPage.AUTOBUY) {
         float progress = state.contentProgress();
         float entranceScale = 0.85F + progress * 0.15F;
         float stationToGui = 2.1835294F;
         float centeredWidth = width * stationToGui * entranceScale;
         float scale = centeredWidth / 928.0F;
         float screenWidth = class_310.method_1551().method_22683().method_4486();
         float screenHeight = class_310.method_1551().method_22683().method_4502();
         this.canvas
            .configure(
               (screenWidth - centeredWidth) / 2.0F,
               (screenHeight - 649.0F * scale) / 2.0F + (1.0F - progress) * 14.0F * (width / 425.0F),
               centeredWidth,
               mouseX,
               mouseY
            );
      } else {
         this.canvas.configure(x, y, width, mouseX, mouseY);
      }

      switch (this.page) {
         case CONFIGURATIONS:
            this.configs.layout(this.canvas);
            break;
         case SETTINGS:
            this.style.layout(this.canvas);
            break;
         case BINDS:
            this.binds.layout(this.canvas);
            break;
         case HUD:
            this.hud.layout(this.canvas);
            break;
         case COSMETICS:
            this.cosmetics.layout(this.canvas);
            break;
         case AUTOBUY:
            this.autoBuy.layout(this.canvas);
            break;
         case NONE:
            this.modules.layout(this.canvas, this.category);
      }
   }

   public void render(class_310 minecraft, class_332 graphics) {
      if (this.page == MenuPage.AUTOBUY) {
         this.autoBuy.render(this.canvas, graphics);
      } else {
         this.renderWindow();
         switch (this.page) {
            case CONFIGURATIONS:
               this.configs.render(this.canvas);
               break;
            case SETTINGS:
               this.style.render(this.canvas);
               break;
            case BINDS:
               this.binds.render(this.canvas);
               break;
            case HUD:
               this.hud.render(this.canvas);
               break;
            case COSMETICS:
               this.cosmetics.render(this.canvas, graphics);
               break;
            case AUTOBUY:
               this.autoBuy.render(this.canvas, graphics);
               break;
            case NONE:
               this.modules.render(this.canvas);
               break;
            case SEARCH:
               this.renderSearch();
               break;
            default:
               this.renderEmptyContent();
         }

         this.renderSidebar();
         this.renderHeader(minecraft);
         this.renderScrollbar();
         this.renderLanguageMenu();
         this.renderCallouts();
      }
   }

   private void renderLanguageMenu() {
      if (this.languageOpen) {
         UiLanguage[] languages = UiLanguage.values();
         float height = 12.0F + languages.length * 16.0F;
         this.canvas.texture(38.688F, 570.0F, 6.688F, 3.802F, Textures.Icons.CHEVRON_DOWN, ClickGuiPalette.accent());
         this.canvas.rect(-7.0F, 581.0F, 97.0F, height, ClickGuiPalette.accent(), 8.0F);

         for (int index = 0; index < languages.length; index++) {
            UiLanguage language = languages[index];
            boolean active = language == UiLanguage.current();
            float rowY = 587.0F + index * 16.0F;
            if (!active && this.canvas.hit(-7.0F, rowY - 2.0F, 97.0F, 16.0F)) {
               this.canvas.rect(-3.0F, rowY - 2.5F, 89.0F, 16.0F, ColorUtil.rgba(255, 255, 255, 30), 5.0F);
            }

            this.canvas.text(1.0F, rowY, 9.0F, language.canonicalName(), active ? -1 : ColorUtil.rgba(255, 255, 255, 128), UiFontStyle.MEDIUM);
            if (active) {
               this.canvas.texture(76.0F, rowY + 1.5F, 6.343F, 4.75F, Textures.Icons.CHECK, -1);
            }
         }
      }
   }

   private boolean pressLanguageMenu(int mouseX, int mouseY) {
      if (!this.languageOpen) {
         return false;
      }

      if (this.canvas.hit(mouseX, mouseY, 25.0F, 540.0F, 32.0F, 32.0F)) {
         return false;
      }

      UiLanguage[] languages = UiLanguage.values();
      float height = 12.0F + languages.length * 16.0F;
      if (!this.canvas.hit(mouseX, mouseY, -7.0F, 581.0F, 97.0F, height)) {
         this.languageOpen = false;
         return false;
      }

      for (int index = 0; index < languages.length; index++) {
         float rowY = 587.0F + index * 16.0F;
         if (this.canvas.hit(mouseX, mouseY, -7.0F, rowY - 2.0F, 97.0F, 16.0F)) {
            UiLanguage.select(languages[index]);
            this.languageOpen = false;
            return true;
         }
      }

      return true;
   }

   private void renderCallouts() {
      int hovered = this.sidebarAt(this.canvas.mouseX(), this.canvas.mouseY());
      if (hovered >= 0 && hovered < CATEGORIES.length) {
         this.drawCallout(40.638F, SIDEBAR_CENTERS[hovered], categoryTitle(CATEGORIES[hovered]));
      } else if (hovered >= CATEGORIES.length && hovered < SIDEBAR_ICONS.length) {
         this.drawCallout(40.638F, SIDEBAR_CENTERS[hovered], utilityTitle(hovered));
      }

      if (this.canvas.hit(796.0F, 13.0F, 120.0F, 30.0F)) {
         this.drawCallout(900.5F, 27.5F, subscriptionLabel());
      }
   }

   private static String subscriptionLabel() {
      if (Subscription.isExpired()) {
         return MenuText.ui("Expired");
      }

      int days = Subscription.daysLeft();
      return days + " " + MenuText.days(days);
   }

   private void drawCallout(float centerX, float anchorY, String label) {
      float width = Math.max(53.0F, this.canvas.textWidth(label, 9.0F, UiFontStyle.MEDIUM) + 19.0F);
      float left = centerX - width / 2.0F;
      float top = anchorY - 29.0F;
      this.canvas.rect(left, top, width, 22.0F, ClickGuiPalette.accent(), 8.0F);
      this.canvas.texture(centerX - 6.5F, top + 22.0F - 0.5F, 13.0F, 7.0F, Textures.Icons.CALLOUT_POINTER, ClickGuiPalette.accent());
      this.canvas.text(centerX, top + 6.0F, 9.0F, label, -1, UiFontStyle.MEDIUM, TextAlign.CENTER);
   }

   private String fitHeader(String value, float maxWidth) {
      return this.fitHeader(value, maxWidth, 13.0F);
   }

   private String fitHeader(String value, float maxWidth, float size) {
      if (value != null && !value.isEmpty() && !(this.canvas.textWidth(value, size, UiFontStyle.REGULAR) <= maxWidth)) {
         int end = value.length();

         while (end > 0 && this.canvas.textWidth(value.substring(0, end) + "...", size, UiFontStyle.REGULAR) > maxWidth) {
            end--;
         }

         return value.substring(0, end).stripTrailing() + "...";
      } else {
         return value;
      }
   }

   private static String utilityTitle(int index) {
      return switch (index) {
         case 6 -> MenuText.ui("Configs");
         case 7 -> MenuText.ui("Themes");
         default -> MenuText.ui("Binds");
         case 9 -> MenuText.ui("Cosmetics");
         case 10 -> "Redactor";
      };
   }

   public boolean mousePressed(int mouseX, int mouseY, int button) {
      if (this.page == MenuPage.AUTOBUY) {
         return this.autoBuy.mousePressed(this.canvas, button);
      }

      if (this.pressLanguageMenu(mouseX, mouseY)) {
         return true;
      }

      int sidebar = this.sidebarAt(mouseX, mouseY);
      if (sidebar >= 0) {
         if (button == 0) {
            if (sidebar < CATEGORIES.length) {
               this.category = CATEGORIES[sidebar];
               this.state.openPage(MenuPage.NONE);
               this.page = MenuPage.NONE;
               this.modules.layout(this.canvas, this.category);
            } else if (sidebar == 6) {
               this.openPage(MenuPage.CONFIGURATIONS);
            } else if (sidebar == 7) {
               this.openPage(MenuPage.SETTINGS);
            } else if (sidebar == 8) {
               this.openPage(MenuPage.BINDS);
            } else if (sidebar == 9) {
               this.openPage(MenuPage.COSMETICS);
            } else if (sidebar == 10) {
               this.openPage(MenuPage.AUTOBUY);
            }
         }

         return true;
      } else if (this.canvas.hit(mouseX, mouseY, 25.0F, 540.0F, 32.0F, 32.0F)) {
         if (button == 0) {
            this.languageOpen = !this.languageOpen;
         }

         return true;
      } else if (this.canvas.hit(mouseX, mouseY, 25.0F, 578.0F, 32.0F, 32.0F)) {
         if (button == 0) {
            this.openPage(this.page == MenuPage.SEARCH ? MenuPage.NONE : MenuPage.SEARCH);
            this.searchFocused = this.page == MenuPage.SEARCH;
         }

         return true;
      } else {
         return switch (this.page) {
            case CONFIGURATIONS -> this.configs.mousePressed(this.canvas, button);
            case SETTINGS -> this.style.mousePressed(this.canvas, button);
            case BINDS -> this.binds.mousePressed(this.canvas, button);
            case HUD -> this.hud.mousePressed(this.canvas, button, this.state);
            case COSMETICS -> this.cosmetics.mousePressed(this.canvas, button);
            case AUTOBUY -> this.autoBuy.mousePressed(this.canvas, button);
            case NONE -> this.modules.mousePressed(this.canvas, button);
            case SEARCH -> this.mousePressedSearch(button);
            default -> false;
         };
      }
   }

   public void drag(int mouseX, int mouseY) {
      switch (this.page) {
         case HUD:
            this.hud.drag(this.canvas);
            break;
         case COSMETICS:
            this.cosmetics.drag(this.canvas);
            break;
         case AUTOBUY:
            this.autoBuy.drag(this.canvas);
            break;
         case NONE:
            this.modules.drag(this.canvas);
      }
   }

   public void release() {
      this.modules.release();
      this.hud.release();
      this.autoBuy.release();
      this.cosmetics.release();
   }

   public void scroll(double vertical) {
      switch (this.page) {
         case CONFIGURATIONS:
            this.configs.scroll(vertical);
            break;
         case SETTINGS:
            this.style.scroll(vertical);
            break;
         case BINDS:
            this.binds.scroll(vertical);
            break;
         case HUD:
            this.hud.scroll(vertical);
            break;
         case COSMETICS:
            this.cosmetics.scroll(this.canvas, vertical);
            break;
         case AUTOBUY:
            this.autoBuy.scroll(this.canvas, vertical);
            break;
         case NONE:
            this.modules.scroll(vertical);
            break;
         case SEARCH:
            this.scrollSearch(vertical);
      }
   }

   public boolean keyPressed(int key) {
      if (key == 75 && this.page != MenuPage.SEARCH && !this.isCapturingBind() && shiftDown()) {
         this.openPage(MenuPage.SEARCH);
         return true;
      }

      return switch (this.page) {
         case BINDS -> this.binds.keyPressed(key);
         default -> false;
         case AUTOBUY -> this.autoBuy.keyPressed(key);
         case NONE -> this.modules.keyPressed(key);
         case SEARCH -> this.keyPressedSearch(key);
      };
   }

   public boolean charTyped(int codePoint) {
      return switch (this.page) {
         case AUTOBUY -> this.autoBuy.charTyped(codePoint);
         case NONE -> this.modules.charTyped(codePoint);
         case SEARCH -> this.appendSearch(codePoint);
         default -> false;
      };
   }

   public boolean isCapturingBind() {
      return this.page == MenuPage.NONE && this.modules.isCapturingBind()
         || this.page == MenuPage.BINDS && this.binds.isCapturingBind()
         || this.page == MenuPage.AUTOBUY && this.autoBuy.isEditing();
   }

   public boolean isSearchOpen() {
      return this.page == MenuPage.SEARCH;
   }

   public boolean isSearchFocused() {
      return this.isSearchOpen() && this.searchFocused;
   }

   public void backspaceSearch() {
      if (this.isSearchOpen() && !this.searchQuery.isEmpty()) {
         int codePoint = this.searchQuery.codePointBefore(this.searchQuery.length());
         this.searchQuery.delete(this.searchQuery.length() - Character.charCount(codePoint), this.searchQuery.length());
      }
   }

   public boolean isDragHandle(float mouseX, float mouseY) {
      return this.page != MenuPage.AUTOBUY
         && this.canvas.hit(mouseX, mouseY, 0.0F, 0.0F, 928.0F, 54.0F)
         && !this.canvas.hit(mouseX, mouseY, 790.0F, 7.0F, 132.0F, 40.0F);
   }

   public boolean contains(float mouseX, float mouseY) {
      return this.canvas.hit(mouseX, mouseY, 0.0F, 0.0F, 928.0F, 627.0F);
   }

   private void renderWindow() {
      this.canvas.shadowedRect(0.0F, 0.0F, 928.0F, 627.0F, ClickGuiPalette.WINDOW, 20.0F, ClickGuiPalette.SHADOW, 30.0F);
      this.canvas.outlinedRect(0.0F, 0.0F, 928.0F, 627.0F, 0, 20.0F, 0.5F, ColorUtil.rgba(255, 255, 255, 7));
      this.canvas.rect(0.0F, 53.0F, 928.0F, 1.0F, ClickGuiPalette.DIVIDER, 0.0F);
      this.canvas.rect(81.0F, 54.0F, 1.0F, 573.0F, ClickGuiPalette.DIVIDER, 0.0F);
      this.canvas.rect(390.0F, 643.0F, 148.0F, 6.0F, ClickGuiPalette.HOME_BAR, 3.0F);
   }

   private void renderHeader(class_310 minecraft) {
      class_2960 icon = this.activeIcon();
      String title = this.headerTitle();
      String description = this.headerDescription();
      this.canvas.texture(33.0F, 19.0F, 16.0F, 16.194F, Textures.Logos.BOOT, ClickGuiPalette.accent());
      String home = MenuText.ui("Home");
      float cursor = 82.0F;
      this.canvas.text(cursor, 20.0F, 13.0F, home, -1, UiFontStyle.MEDIUM);
      cursor += this.canvas.textWidth(home, 13.0F, UiFontStyle.MEDIUM) + 5.0F;
      this.canvas.texture(cursor, 25.0F, 4.0F, 7.036F, Textures.Header.CHEVRON_RIGHT, -1);
      cursor += 9.0F;
      this.canvas.texture(cursor, 22.0F, 12.044F, 11.539F, icon, ClickGuiPalette.accent());
      cursor += 16.043999F;
      this.canvas.text(cursor, 20.0F, 13.0F, title, ClickGuiPalette.accent(), UiFontStyle.MEDIUM);
      cursor += this.canvas.textWidth(title, 13.0F, UiFontStyle.MEDIUM) + 21.0F;
      String name = this.fitHeader("https://t.me/crackdelay", 150.0F, 11.0F);
      float nameWidth = this.canvas.textWidth(name, 11.0F, UiFontStyle.REGULAR);
      float pillWidth = Math.max(120.0F, 63.185997F + nameWidth);
      float pillX = 916.0F - pillWidth;
      float available = Math.min(786.0F, pillX - 10.0F) - cursor;
      if (available > 40.0F) {
         this.canvas.text(cursor, 20.0F, 13.0F, this.fitHeader(description, available), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      }

      this.canvas.rect(pillX, 13.0F, pillWidth, 30.0F, ClickGuiPalette.accent(), 17.0F);
      this.canvas.texture(pillX + 11.0F, 21.5F, 12.0F, 12.0F, Textures.Icons.USER_ROUND, -1);
      this.canvas.text(pillX + 27.119F, 20.0F, 11.0F, name, -1, UiFontStyle.REGULAR);
      this.canvas.rect(pillX + 16.0F, 42.0F, 57.0F, 1.0F, ClickGuiPalette.RULE, 0.0F);
      this.canvas.texture(887.933F, 14.933F, 25.133F, 25.133F, Textures.ClickGui.AVATAR, -1, 12.5665F);
   }

   private void renderSidebar() {
      int active = this.activeSidebarIndex();
      boolean hasActive = active >= 0 && active < SIDEBAR_ICONS.length;
      this.canvas.outlinedRect(19.0F, 67.0F, 43.276F, 456.981F, ClickGuiPalette.SIDEBAR, 24.0F, 0.5F, ClickGuiPalette.SIDEBAR_STROKE);
      if (hasActive) {
         this.canvas.pushScissor(19.0F, 67.0F, 43.276F, 491.602F);
         float markerX = 24.638F;
         float markerY = SIDEBAR_CENTERS[active] - 16.0F;
         int accent = ClickGuiPalette.accent();
         this.canvas.shadowedRect(markerX, markerY, 32.0F, 32.0F, ColorUtil.multiplyAlpha(accent, 0.12F), 16.0F, ColorUtil.multiplyAlpha(accent, 0.35F), 8.0F);
         this.canvas.popScissor();
      }

      for (int index = 0; index < SIDEBAR_ICONS.length; index++) {
         float centerY = SIDEBAR_CENTERS[index];
         boolean selected = active == index;
         if (!selected && this.canvas.hit(19.0F, centerY - 19.0F, 43.276F, 38.0F)) {
            this.canvas.rect(25.0F, centerY - 16.0F, 31.0F, 32.0F, ClickGuiPalette.CONTROL_HOVER, 15.0F);
         }

         float[] rect = SIDEBAR_ICON_RECTS[index];
         this.canvas.texture(rect[0], rect[1], rect[2], rect[3], SIDEBAR_ICONS[index], selected ? ClickGuiPalette.accent() : ClickGuiPalette.ICON_INACTIVE);
      }

      if (this.languageOpen || this.canvas.hit(25.0F, 540.0F, 32.0F, 32.0F)) {
         this.canvas
            .rect(
               25.0F, 540.0F, 32.0F, 32.0F, this.languageOpen ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.1F) : ClickGuiPalette.CONTROL_HOVER, 16.0F
            );
      }

      boolean searchOpen = this.page == MenuPage.SEARCH;
      if (searchOpen || this.canvas.hit(25.0F, 578.0F, 32.0F, 32.0F)) {
         this.canvas
            .rect(25.0F, 578.0F, 32.0F, 32.0F, searchOpen ? ColorUtil.multiplyAlpha(ClickGuiPalette.accent(), 0.1F) : ClickGuiPalette.CONTROL_HOVER, 16.0F);
      }

      this.canvas.texture(34.0F, 550.0F, 12.502F, 12.502F, Textures.Icons.GLOBE, this.languageOpen ? ClickGuiPalette.accent() : ClickGuiPalette.ICON_INACTIVE);
      this.canvas.texture(34.0F, 587.506F, 13.447F, 13.464F, Textures.Header.SEARCH, searchOpen ? ClickGuiPalette.accent() : ClickGuiPalette.ICON_INACTIVE);
   }

   private void renderScrollbar() {
      float top = this.page == MenuPage.NONE ? 65.0F : 77.0F;
      this.canvas.rect(921.0F, top, 1.0F, 162.0F, ClickGuiPalette.accent(), 1.0F);
   }

   private void renderEmptyContent() {
      this.canvas.outlinedRect(94.0F, 67.0F, 821.0F, 546.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
   }

   private void renderSearch() {
      this.canvas.outlinedRect(94.0F, 67.0F, 821.0F, 546.0F, ClickGuiPalette.SURFACE, 17.0F, 0.5F, ClickGuiPalette.STROKE);
      this.canvas.texture(115.0F, 87.0F, 13.447F, 13.464F, Textures.Header.SEARCH, ClickGuiPalette.accent());
      this.canvas.text(140.0F, 85.0F, 15.0F, MenuText.ui("Search"), -1, UiFontStyle.MEDIUM);
      List<Feature> results = this.searchResults();
      if (results.isEmpty()) {
         this.canvas.text(504.5F, 286.0F, 14.0F, MenuText.ui("Nothing found"), ClickGuiPalette.TEXT_SECONDARY, UiFontStyle.MEDIUM, TextAlign.CENTER);
      } else {
         this.canvas.pushScissor(94.0F, 112.0F, 821.0F, 492.0F);
         float rowY = 118.0F - this.searchScroll;

         for (Feature feature : results) {
            if (rowY + 66.0F >= 112.0F && rowY <= 604.0F) {
               this.renderSearchRow(feature, rowY);
            }

            rowY += 80.0F;
         }

         this.canvas.popScissor();
      }

      this.renderSearchBar();
   }

   private void renderSearchRow(Feature feature, float rowY) {
      boolean hover = this.canvas.hit(115.0F, rowY, 778.0F, 66.0F);
      this.canvas
         .outlinedRect(115.0F, rowY, 778.0F, 66.0F, hover ? ClickGuiPalette.CONTROL_HOVER : ClickGuiPalette.SURFACE, 15.0F, 0.5F, ClickGuiPalette.CARD_STROKE);
      this.canvas.rect(130.0F, rowY + 13.0F, 39.0F, 39.0F, ClickGuiPalette.CONTROL, 19.5F);
      this.canvas.texture(144.0F, rowY + 27.0F, 11.0F, 12.0F, SIDEBAR_ICONS[feature.getCategory().ordinal()], ClickGuiPalette.accent());
      this.canvas.text(179.0F, rowY + 14.0F, 15.0F, this.fitHeader(feature.getName(), 640.0F, 15.0F), -1, UiFontStyle.MEDIUM);
      this.canvas.text(179.0F, rowY + 37.0F, 12.0F, categoryTitle(feature.getCategory()), ClickGuiPalette.TEXT_MUTED, UiFontStyle.REGULAR);
      this.drawToggle(840.0F, rowY + 23.0F, feature.isEnabled());
   }

   private void renderSearchBar() {
      this.canvas.rect(302.0F, 660.0F, 324.0F, 54.0F, ColorUtil.rgba(19, 20, 22, 247), 20.0F);
      String query = this.searchQuery.toString();
      boolean empty = query.isEmpty();
      this.canvas.text(322.0F, 678.0F, 14.0F, empty ? MenuText.ui("Search") + ".." : query, empty ? ClickGuiPalette.TEXT_FAINT : -1, UiFontStyle.REGULAR);
      if (!empty && System.currentTimeMillis() / 500L % 2L == 0L) {
         float caret = 323.0F + this.canvas.textWidth(query, 14.0F, UiFontStyle.REGULAR);
         this.canvas.rect(caret, 677.0F, 1.0F, 18.0F, ClickGuiPalette.accent(), 0.0F);
      }

      this.canvas.texture(547.0F, 683.0F, 8.0F, 8.0F, Textures.Icons.COMMAND, ClickGuiPalette.TEXT_FAINT);
      this.canvas.text(560.0F, 681.0F, 10.0F, "SHIFT+K", ClickGuiPalette.TEXT_FAINT, UiFontStyle.REGULAR);
   }

   private boolean mousePressedSearch(int button) {
      if (button != 0) {
         return this.canvas.hit(94.0F, 67.0F, 821.0F, 546.0F);
      }

      float rowY = 118.0F - this.searchScroll;

      for (Feature feature : this.searchResults()) {
         if (this.canvas.hit(115.0F, rowY, 778.0F, 66.0F)) {
            if (this.canvas.hit(840.0F, rowY + 23.0F, 33.0F, 20.0F)) {
               feature.toggle();
            } else {
               this.openFeature(feature);
            }

            return true;
         }

         rowY += 80.0F;
      }

      return this.canvas.hit(94.0F, 67.0F, 821.0F, 546.0F) || this.canvas.hit(302.0F, 660.0F, 324.0F, 54.0F);
   }

   private void openFeature(Feature feature) {
      this.category = feature.getCategory();
      this.searchScroll = 0.0F;
      this.state.openPage(MenuPage.NONE);
      this.page = MenuPage.NONE;
      this.modules.layout(this.canvas, this.category);
      this.modules.select(feature);
      this.modules.layout(this.canvas, this.category);
   }

   private static boolean shiftDown() {
      long window = class_310.method_1551().method_22683().method_4490();
      return GLFW.glfwGetKey(window, 340) == 1 || GLFW.glfwGetKey(window, 344) == 1;
   }

   private void scrollSearch(double vertical) {
      int count = this.searchResults().size();
      float content = count == 0 ? 0.0F : (count - 1) * 80.0F + 66.0F;
      float max = Math.max(0.0F, content - 486.0F);
      this.searchScroll = Math.max(0.0F, Math.min(max, this.searchScroll - (float)vertical * 42.0F));
   }

   private boolean keyPressedSearch(int key) {
      if (key == 259) {
         this.backspaceSearch();
         return true;
      } else if (key == 256) {
         this.searchQuery.setLength(0);
         this.searchScroll = 0.0F;
         this.openPage(MenuPage.NONE);
         return true;
      } else {
         return false;
      }
   }

   private boolean appendSearch(int codePoint) {
      if (!Character.isISOControl(codePoint) && this.searchQuery.codePointCount(0, this.searchQuery.length()) < 48) {
         this.searchQuery.appendCodePoint(codePoint);
         this.searchScroll = 0.0F;
         return true;
      } else {
         return false;
      }
   }

   private List<Feature> searchResults() {
      String needle = this.searchQuery.toString().strip().toLowerCase(Locale.ROOT);
      return needle.isEmpty()
         ? List.copyOf(FeatureManager.INSTANCE.getFeatures())
         : FeatureManager.INSTANCE.getFeatures().stream().filter(feature -> matchesSearch(feature, needle)).toList();
   }

   private static boolean matchesSearch(Feature feature, String needle) {
      String name = feature.getName().toLowerCase(Locale.ROOT);
      if (name.contains(needle)) {
         return true;
      }

      String description = feature.getDescription();
      return description != null && description.toLowerCase(Locale.ROOT).contains(needle);
   }

   private void drawToggle(float left, float top, boolean enabled) {
      this.canvas.rect(left, top, 33.0F, 20.0F, enabled ? ClickGuiPalette.accent() : ClickGuiPalette.OFF_TRACK, 10.0F);
      this.canvas.rect(left + (enabled ? 16.0F : 2.5F), top + 2.5F, 15.0F, 15.0F, enabled ? -1 : ClickGuiPalette.OFF_KNOB, 8.0F);
   }

   private int sidebarAt(float mouseX, float mouseY) {
      for (int index = 0; index < SIDEBAR_CENTERS.length; index++) {
         if (this.canvas.hit(mouseX, mouseY, 19.0F, SIDEBAR_CENTERS[index] - 19.0F, 43.0F, 38.0F)) {
            return index;
         }
      }

      return -1;
   }

   private int activeSidebarIndex() {
      return switch (this.page) {
         case CONFIGURATIONS -> 6;
         case SETTINGS -> 7;
         case BINDS -> 8;
         default -> -1;
         case COSMETICS -> 9;
         case AUTOBUY -> 10;
         case NONE -> this.category.ordinal();
      };
   }

   private class_2960 activeIcon() {
      int index = this.activeSidebarIndex();
      if (index >= 0 && index < SIDEBAR_ICONS.length) {
         return SIDEBAR_ICONS[index];
      } else {
         return this.page == MenuPage.SEARCH ? Textures.Header.SEARCH : Textures.Icons.SWORDS;
      }
   }

   private String headerTitle() {
      return switch (this.page) {
         case CONFIGURATIONS -> this.configs.headerTitle();
         case SETTINGS -> this.style.headerTitle();
         case BINDS -> this.binds.headerTitle();
         case HUD -> this.hud.headerTitle();
         case COSMETICS -> this.cosmetics.headerTitle();
         case AUTOBUY -> this.autoBuy.headerTitle();
         default -> categoryTitle(this.category);
         case SEARCH -> MenuText.ui("Search");
      };
   }

   private String headerDescription() {
      return switch (this.page) {
         case CONFIGURATIONS -> this.configs.headerDescription();
         case SETTINGS -> this.style.headerDescription();
         case BINDS -> this.binds.headerDescription();
         case HUD -> this.hud.headerDescription();
         case COSMETICS -> this.cosmetics.headerDescription();
         case AUTOBUY -> this.autoBuy.headerDescription();
         default -> categoryDescription(this.category);
         case SEARCH -> MenuText.ui("Quick search for features and settings.");
      };
   }

   private void openPage(MenuPage next) {
      this.state.openPage(next);
      this.page = next;
      this.searchFocused = next == MenuPage.SEARCH;
      switch (next) {
         case CONFIGURATIONS:
            this.configs.layout(this.canvas);
            break;
         case SETTINGS:
            this.style.layout(this.canvas);
            break;
         case BINDS:
            this.binds.layout(this.canvas);
            break;
         case HUD:
            this.hud.layout(this.canvas);
            break;
         case COSMETICS:
            this.cosmetics.layout(this.canvas);
            break;
         case AUTOBUY:
            this.autoBuy.layout(this.canvas);
            break;
         case NONE:
            this.modules.layout(this.canvas, this.category);
      }
   }

   private static String categoryTitle(FeatureCategory category) {
      return MenuText.ui(switch (category) {
         case COMBAT -> "Combat";
         case MOVEMENT -> "Movement";
         case VISUAL -> "Visual";
         case PLAYER -> "Player";
         case MISC -> "Misc";
         case PVE -> "PVE";
      });
   }

   private static String categoryDescription(FeatureCategory category) {
      return MenuText.ui(switch (category) {
         case COMBAT -> "Combat features and attack settings.";
         case MOVEMENT -> "Movement and speed features.";
         case VISUAL -> "Visual effects and world rendering.";
         case PLAYER -> "Player action automation.";
         case MISC -> "Additional client features.";
         case PVE -> "Survival and server task automation.";
      });
   }
}
