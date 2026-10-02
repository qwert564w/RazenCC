package org.ryzen.menu.pages.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.Setting;
import org.ryzen.menu.core.MenuOverlayState;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.SmoothScroll;
import org.ryzen.menu.ui.controls.MenuClipboard;
import org.ryzen.menu.ui.controls.SearchInputComponent;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.math.MathUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class ModulePage extends Component {
   private static final int PAGE_PADDING = 16;
   private static final int COLUMN_GAP = 16;
   private static final int CARD_GAP = 12;
   private static final int CATEGORY_COUNT = FeatureCategory.values().length;
   private static final int CATEGORY_DOCK_PADDING = 12;
   private static final int CATEGORY_DOCK_GAP = 12;
   private static final int CATEGORY_DOCK_BUTTON_SIZE = 32;
   private static final int CATEGORY_DOCK_WIDTH = 24 + CATEGORY_COUNT * 32 + (CATEGORY_COUNT - 1) * 12;
   private static final int CATEGORY_DOCK_HEIGHT = 56;
   private static final int CATEGORY_DOCK_Y = 612;
   private static final int CATEGORY_DOCK_RADIUS = 12;
   private static final int CATEGORY_DOCK_BUTTON_RADIUS = 8;
   private static final float CATEGORY_DOCK_BORDER_WIDTH = 0.5F;
   private static final int CATEGORY_ICON_SIZE = 16;
   private static final int SEARCH_RESULTS_X = 256;
   private static final int SEARCH_RESULTS_Y = 370;
   private static final int SEARCH_RESULTS_WIDTH = 512;
   private static final int SEARCH_RESULT_HEIGHT = 34;
   private static final int SEARCH_RESULT_GAP = 8;
   private static final int SEARCH_RESULT_RADIUS = 10;
   private static final int SEARCH_RESULT_PADDING_X = 14;
   private static final int SEARCH_RESULT_ICON_SIZE = 12;
   private static final int SEARCH_RESULT_ICON_TEXT_GAP = 8;
   private static final float SEARCH_RESULT_TEXT_SIZE = 12.0F;
   private static final class_2960[] CATEGORY_ICONS = new class_2960[]{
      Textures.Icons.SWORDS, Textures.Icons.PERSON_STANDING, Textures.Icons.EYE, Textures.Icons.USER_ROUND, Textures.Icons.BOXES, Textures.Icons.BRAIN
   };
   private final List<Component> children = new ArrayList<>();
   private final List<ModuleCard> cards = new ArrayList<>();
   private final StringBuilder searchQuery = new StringBuilder();
   private final SearchInputComponent searchInput = new SearchInputComponent(this.searchQuery::toString, this::searchSuggestionSuffix);
   private boolean searchAllSelected;
   private final Animation categoryAnimation = new Animation(150L, Animation.Easing.EASE_OUT_QUAD);
   private final Animation categoryIndicatorAnimation = new Animation(180L, Animation.Easing.EASE_OUT_QUAD);
   private MenuOverlayState state;
   private MenuPage displayedPage = MenuPage.NONE;
   private FeatureCategory selectedCategory = FeatureCategory.COMBAT;
   private FeatureCategory cardsCategory;
   private boolean searchFocused;
   private int mouseX;
   private int mouseY;
   private float transitionProgress;
   private int visualOffsetX;
   private float visualAlpha = 1.0F;
   private float dockProgress = 1.0F;
   private float categoryDockVisualY = 612.0F;
   private final SmoothScroll scroll = new SmoothScroll();
   private float scrollOffset;
   private int maxContentBottom = 560;
   private int[] cardColumns;
   private Feature triggeredFeature;

   public ModulePage() {
      this.categoryAnimation.animate(1.0F, 1.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
      float index = categoryIndex(this.selectedCategory);
      this.categoryIndicatorAnimation.animate(index, index, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public void layout(Component frame, MenuOverlayState state, int mouseX, int mouseY) {
      this.attach(frame, frame.x(), frame.y(), frame.width(), frame.height());
      this.state = state;
      this.displayedPage = state.displayPage();
      this.transitionProgress = state.contentProgress();
      if (state.page() != MenuPage.SEARCH) {
         this.searchFocused = false;
         this.searchAllSelected = false;
      }

      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.dockProgress = state.isClosing() ? 1.0F : MathUtil.clamp01((state.openProgress() - 0.35F) / 0.65F);
      float screenBottomDesign = this.designY(class_310.method_1551().method_22683().method_4502());
      this.categoryDockVisualY = 612.0F + (1.0F - this.dockProgress) * Math.max(0.0F, screenBottomDesign - 612.0F);
      this.updateCategoryVisuals();
      this.scrollOffset = this.scroll.update(Math.max(0, this.maxContentBottom - 560));
      this.ensureCards();
      this.layoutCards();
      this.searchInput
         .place(this, 256, 310, 512, 48, mouseX, mouseY)
         .alpha(this.transitionProgress)
         .focused(this.searchFocused)
         .selected(this.searchAllSelected);
   }

   public boolean handleMouseButton(int mouseX, int mouseY, int button) {
      if (this.displayedPage == MenuPage.SEARCH && this.transitionProgress > 0.05F) {
         if (button == 0 && this.searchInput.contains(mouseX, mouseY)) {
            this.searchFocused = !this.searchFocused;
            this.searchAllSelected = false;
            return true;
         }

         Feature result = this.resultAt(mouseX, mouseY);
         if (result != null) {
            this.openSearchResult(result);
         }

         return this.contains(mouseX, mouseY) && mouseY >= this.y() + this.px(54.0F);
      } else if (this.displayedPage == MenuPage.NONE && !(mouseY < this.y() + this.px(54.0F))) {
         FeatureCategory category = this.categoryAt(mouseX, mouseY);
         if (this.categoryDockContains(mouseX, mouseY)) {
            if (button == 0 && category != null && category != this.selectedCategory) {
               float from = this.categoryIndicatorAnimation.getValue();
               this.selectedCategory = category;
               this.categoryIndicatorAnimation.animate(from, categoryIndex(category), 180L, Animation.Easing.EASE_OUT_QUAD);
               this.categoryAnimation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
               this.rebuildCards();
            }

            return true;
         } else {
            for (ModuleCard card : this.cards) {
               if (card.handleBindPopupClick(mouseX, mouseY, button)) {
                  return true;
               }
            }

            if (!this.contains(mouseX, mouseY)) {
               return false;
            }

            if (button == 2) {
               for (ModuleCard card : this.cards) {
                  if (card.handleMiddleClick(mouseX, mouseY)) {
                     this.closeBindPopupsExcept(card);
                     return true;
                  }
               }

               return this.contains(mouseX, mouseY);
            } else if (button == 1) {
               for (ModuleCard card : this.cards) {
                  if (card.handleRightClick(mouseX, mouseY)) {
                     return true;
                  }
               }

               return this.contains(mouseX, mouseY);
            } else {
               if (button != 0) {
                  return this.contains(mouseX, mouseY);
               }

               for (ModuleCard card : this.cards) {
                  if (card.handleDropdownPopupClick(mouseX, mouseY)) {
                     return true;
                  }
               }

               for (ModuleCard card : this.cards) {
                  if (card.handleClick(mouseX, mouseY)) {
                     return true;
                  }
               }

               return true;
            }
         }
      } else {
         return false;
      }
   }

   public void drag(int mouseX) {
      for (ModuleCard card : this.cards) {
         card.drag(mouseX);
      }
   }

   public void releasePointer() {
      for (ModuleCard card : this.cards) {
         card.releasePointer();
      }
   }

   public boolean handleKey(int key) {
      if (this.displayedPage == MenuPage.SEARCH) {
         if (this.searchFocused && MenuClipboard.shortcutDown()) {
            if (key == 65) {
               this.searchAllSelected = !this.searchQuery.isEmpty();
               return true;
            }

            if (key == 67) {
               if (this.searchAllSelected) {
                  MenuClipboard.set(this.searchQuery.toString());
               }

               return true;
            }

            if (key == 88) {
               if (this.searchAllSelected) {
                  MenuClipboard.set(this.searchQuery.toString());
                  this.replaceSearchText("");
               }

               return true;
            }

            if (key == 86) {
               String base = this.searchAllSelected ? "" : this.searchQuery.toString();
               this.replaceSearchText(base + MenuClipboard.get());
               return true;
            }
         }

         if (this.searchFocused && key == 258) {
            this.acceptSearchSuggestion();
            return true;
         }

         if (this.searchFocused && key == 257) {
            List<Feature> results = this.searchResults();
            if (!results.isEmpty()) {
               this.openSearchResult((Feature)results.getFirst());
            }

            return true;
         } else if (this.searchFocused && key == 259) {
            this.backspace();
            return true;
         } else {
            return false;
         }
      } else {
         if (this.displayedPage != MenuPage.NONE) {
            return false;
         }

         for (ModuleCard card : this.cards) {
            if (card.handleKey(key)) {
               return true;
            }
         }

         return false;
      }
   }

   public boolean isCapturingBind() {
      if (this.displayedPage != MenuPage.NONE) {
         return false;
      }

      for (ModuleCard card : this.cards) {
         if (card.isCapturingBind()) {
            return true;
         }
      }

      return false;
   }

   public boolean handleCharacter(int codePoint) {
      if (this.displayedPage != MenuPage.NONE) {
         return false;
      }

      for (ModuleCard card : this.cards) {
         if (card.handleCharacter(codePoint)) {
            return true;
         }
      }

      return false;
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
      if (!this.categoryDockContains(mouseX, mouseY)) {
         if (this.displayedPage == MenuPage.NONE && this.contains(mouseX, mouseY) && mouseY > this.y() + this.px(54.0F)) {
            for (ModuleCard card : this.cards) {
               if (card.handleScroll(mouseX, mouseY, vertical)) {
                  return;
               }
            }

            this.scroll.scroll(vertical, Math.max(0, this.maxContentBottom - 560));

            for (ModuleCard card : this.cards) {
               card.closeOverlays();
            }
         }
      }
   }

   public boolean isSearchOpen() {
      return this.state != null && this.state.page() == MenuPage.SEARCH;
   }

   public boolean isSearchFocused() {
      return this.isSearchOpen() && this.searchFocused;
   }

   public void appendCodePoint(int codePoint) {
      if (this.isSearchFocused() && !MenuClipboard.shortcutDown() && !Character.isISOControl(codePoint)) {
         if (this.searchAllSelected) {
            this.searchQuery.setLength(0);
            this.searchAllSelected = false;
         }

         if (this.searchQuery.codePointCount(0, this.searchQuery.length()) < 40) {
            this.searchQuery.appendCodePoint(codePoint);
         }
      }
   }

   public void backspace() {
      if (this.isSearchFocused() && !this.searchQuery.isEmpty()) {
         if (this.searchAllSelected) {
            this.replaceSearchText("");
         } else {
            int lastCodePoint = this.searchQuery.codePointBefore(this.searchQuery.length());
            this.searchQuery.delete(this.searchQuery.length() - Character.charCount(lastCodePoint), this.searchQuery.length());
         }
      }
   }

   private void replaceSearchText(String value) {
      String resolved = value == null ? "" : value.replaceAll("\\R", " ");
      int codePoints = resolved.codePointCount(0, resolved.length());
      if (codePoints > 40) {
         resolved = resolved.substring(0, resolved.offsetByCodePoints(0, 40));
      }

      this.searchQuery.setLength(0);
      this.searchQuery.append(resolved);
      this.searchAllSelected = false;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.displayedPage == MenuPage.NONE || this.displayedPage == MenuPage.SEARCH) {
         this.updateCategoryVisuals();
         Render2DUtil.pushScissor(this.x(), this.y() + this.px(54.0F), this.width(), this.height() - this.px(54.0F));

         for (Component child : this.children) {
            child.render(minecraft, guiGraphicsExtractor);
         }

         if (this.children.isEmpty()) {
            this.textCentered(512.0F, 290.0F, 17.0F, MenuText.ui("No modules in this category yet"), Theme.Colors.PRIMARY);
            this.textCentered(
               512.0F, 320.0F, 11.0F, MenuText.ui("Modules registered in this category will appear here automatically."), Theme.Colors.SECONDARY_DARK
            );
         }

         Render2DUtil.popScissor();

         for (ModuleCard card : this.cards) {
            card.renderOverlay(minecraft, guiGraphicsExtractor);
         }

         this.visualAlpha = 1.0F;
         this.visualOffsetX = 0;
         this.renderCategoryDock();
         if (this.displayedPage == MenuPage.SEARCH && this.transitionProgress > 0.001F) {
            Render2DUtil.flush();
            guiGraphicsExtractor.method_71048();
            this.renderSearch(minecraft, guiGraphicsExtractor);
         }
      }
   }

   private void ensureCards() {
      if (this.cardsCategory != this.selectedCategory) {
         this.rebuildCards();
      }
   }

   private void rebuildCards() {
      this.cards.clear();

      for (Feature feature : FeatureManager.INSTANCE.getFeatures(this.selectedCategory)) {
         this.cards.add(new ModuleCard(feature));
      }

      this.cardsCategory = this.selectedCategory;
      int[] tops = this.computeMasonry();
      if (this.triggeredFeature != null) {
         for (int index = 0; index < this.cards.size(); index++) {
            ModuleCard card = this.cards.get(index);
            if (card.feature() == this.triggeredFeature) {
               card.flashHighlight();
               float maxScroll = Math.max(0.0F, this.maxContentBottom - 560.0F);
               float scrollTarget = tops[index] - 68.0F - (560.0F - card.designHeight()) / 2.0F;
               this.scroll.setTarget(class_3532.method_15363(scrollTarget, 0.0F, maxScroll));
               break;
            }
         }

         this.triggeredFeature = null;
      }
   }

   private int[] computeMasonry() {
      int[] tops = new int[this.cards.size()];
      if (this.cardColumns == null || this.cardColumns.length != this.cards.size()) {
         this.cardColumns = new int[this.cards.size()];
      }

      int[] columnBottom = new int[]{68, 68, 68};

      for (int index = 0; index < this.cards.size(); index++) {
         int column = shortestColumn(columnBottom);
         this.cardColumns[index] = column;
         tops[index] = columnBottom[column];
         columnBottom[column] += this.cards.get(index).designHeight() + 12;
      }

      this.maxContentBottom = Math.max(columnBottom[0], Math.max(columnBottom[1], columnBottom[2]));
      return tops;
   }

   private void closeBindPopupsExcept(ModuleCard except) {
      for (ModuleCard card : this.cards) {
         if (card != except) {
            card.closeBindPopup();
         }
      }
   }

   private void layoutCards() {
      this.children.clear();
      int columnWidth = 320;
      boolean dockHovered = this.categoryDockContains(this.mouseX, this.mouseY);
      int cardMouseX = dockHovered ? Integer.MIN_VALUE : this.mouseX;
      int cardMouseY = dockHovered ? Integer.MIN_VALUE : this.mouseY;
      int[] tops = this.computeMasonry();

      for (int index = 0; index < this.cards.size(); index++) {
         ModuleCard card = this.cards.get(index);
         int x = 16 + this.cardColumns[index] * (columnWidth + 16);
         int y = tops[index] - Math.round(this.scrollOffset);
         card.place(this, x, y, columnWidth, this.visualAlpha, cardMouseX, cardMouseY, this.visualOffsetX, 612);
         this.children.add(card);
      }
   }

   private void renderCategoryDock() {
      int dockX = categoryDockX();
      float dockY = this.categoryDockVisualY;
      Render2DUtil.rect(this.sx(dockX), this.sy(dockY), this.px(CATEGORY_DOCK_WIDTH), this.px(56.0F))
         .color(Theme.Colors.BACKGROUND_PRIMARY_50)
         .radius(this.px(12.0F))
         .border(Math.max(0.5F, this.px(0.5F)), Theme.Colors.OUTLINES_MEDIUM)
         .blur(this.px(8.0F))
         .draw();
      FeatureCategory[] categories = FeatureCategory.values();
      float indicatorIndex = this.categoryIndicatorAnimation.getValue();
      float indicatorX = categoryButtonX(dockX, 0) + indicatorIndex * 44.0F;
      float buttonY = dockY + 12.0F;
      Render2DUtil.rect(this.sx(indicatorX), this.sy(buttonY), this.px(32.0F), this.px(32.0F))
         .color(Theme.Colors.OUTLINES_MEDIUM)
         .radius(this.px(8.0F))
         .border(Math.max(0.5F, this.px(0.5F)), Theme.Colors.OUTLINES_MEDIUM)
         .draw();

      for (int index = 0; index < categories.length && index < CATEGORY_ICONS.length; index++) {
         int buttonX = categoryButtonX(dockX, index);
         boolean active = categories[index] == this.selectedCategory;
         float iconX = buttonX + 8.0F;
         float iconY = buttonY + 8.0F;
         this.texture(iconX, iconY, 16.0F, CATEGORY_ICONS[index], active ? -1 : Theme.Colors.ICON);
      }
   }

   private void renderSearch(class_310 minecraft, class_332 guiGraphicsExtractor) {
      Render2DUtil.rect(this.x(), this.y(), this.width(), this.height())
         .color(0)
         .radius(this.px(12.0F))
         .shadow(Theme.Colors.PANEL_SHADOW, this.px(8.0F))
         .draw();
      Render2DUtil.rect(this.x(), this.y() + this.px(54.0F), this.width(), this.height() - this.px(54.0F))
         .color(ColorUtil.withAlpha(Theme.Colors.OVERLAY, Math.round(218.0F * this.transitionProgress)))
         .radius(0.0F, 0.0F, this.px(12.0F), this.px(12.0F))
         .draw();
      this.textCentered(512.0F, 250.0F, 22.0F, MenuText.ui("Search"), Theme.Colors.PRIMARY, this.transitionProgress);
      this.textCentered(512.0F, 282.0F, 11.0F, MenuText.ui("Start typing to find a module or setting."), Theme.Colors.SECONDARY_DARK, this.transitionProgress);
      this.searchInput.render(minecraft, guiGraphicsExtractor);
      float chipX = 256.0F;

      for (Feature feature : this.searchResults()) {
         float chipWidth = searchResultWidth(feature);
         if (chipX + chipWidth > 768.0F) {
            break;
         }

         boolean hovered = this.hit(this.mouseX, this.mouseY, chipX, 370.0F, chipWidth, 34.0F);
         this.renderSearchResult(feature, chipX, 370.0F, chipWidth, hovered);
         chipX += chipWidth + 8.0F;
      }
   }

   private void renderSearchResult(Feature feature, float x, float y, float width, boolean hovered) {
      int contentColor = hovered ? Theme.Colors.PRIMARY : Theme.Colors.ICON;
      this.rect(x, y, width, 34.0F, hovered ? Theme.Colors.OUTLINES_MEDIUM : Theme.Colors.OUTLINES_SMALL, 10.0F, this.transitionProgress);
      float iconX = x + 14.0F;
      float iconY = y + 11.0F;
      this.texture(iconX, iconY, 12.0F, categoryIcon(feature.getCategory()), contentColor, this.transitionProgress);
      this.text(iconX + 12.0F + 8.0F, this.centeredTextY(y + 17.0F, 12.0F), 12.0F, feature.getName(), contentColor, this.transitionProgress, UiFontStyle.MEDIUM);
   }

   private FeatureCategory categoryAt(int mouseX, int mouseY) {
      if (!(this.dockProgress < 0.98F) && this.categoryDockContains(mouseX, mouseY)) {
         int dockX = categoryDockX();
         float dockY = this.categoryDockVisualY + 12.0F;
         FeatureCategory[] categories = FeatureCategory.values();

         for (int index = 0; index < categories.length && index < CATEGORY_ICONS.length; index++) {
            int buttonX = categoryButtonX(dockX, index);
            if (this.hit(mouseX, mouseY, buttonX, dockY, 32.0F, 32.0F)) {
               return categories[index];
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private boolean categoryDockContains(float mouseX, float mouseY) {
      return this.displayedPage == MenuPage.NONE
         && this.dockProgress > 0.001F
         && this.hit(mouseX, mouseY, categoryDockX(), this.categoryDockVisualY, CATEGORY_DOCK_WIDTH, 56.0F);
   }

   private List<Feature> searchResults() {
      String query = this.searchQuery.toString().trim().toLowerCase(Locale.ROOT);
      return query.isEmpty() ? List.of() : FeatureManager.INSTANCE.getFeatures().stream().filter(feature -> matches(feature, query)).limit(5L).toList();
   }

   private String searchSuggestionSuffix() {
      String query = this.searchQuery.toString();
      if (!query.isEmpty() && query.equals(query.trim())) {
         String normalizedQuery = query.toLowerCase(Locale.ROOT);
         return FeatureManager.INSTANCE
            .getFeatures()
            .stream()
            .map(Feature::getName)
            .filter(name -> name.length() > query.length() && name.toLowerCase(Locale.ROOT).startsWith(normalizedQuery))
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .map(name -> name.substring(query.length()))
            .findFirst()
            .orElse("");
      } else {
         return "";
      }
   }

   private void acceptSearchSuggestion() {
      String suffix = this.searchSuggestionSuffix();
      if (!suffix.isEmpty()) {
         this.searchQuery.append(suffix);
      }
   }

   private void openSearchResult(Feature result) {
      if (result.getCategory() != this.selectedCategory) {
         float from = this.categoryIndicatorAnimation.getValue();
         this.selectedCategory = result.getCategory();
         this.categoryIndicatorAnimation.animate(from, categoryIndex(this.selectedCategory), 180L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.triggeredFeature = result;
      this.searchQuery.setLength(0);
      this.searchFocused = false;
      this.searchAllSelected = false;
      this.state.openPage(MenuPage.NONE);
      this.rebuildCards();
   }

   private Feature resultAt(int mouseX, int mouseY) {
      float chipX = 256.0F;

      for (Feature feature : this.searchResults()) {
         float chipWidth = searchResultWidth(feature);
         if (chipX + chipWidth > 768.0F) {
            break;
         }

         if (this.hit(mouseX, mouseY, chipX, 370.0F, chipWidth, 34.0F)) {
            return feature;
         }

         chipX += chipWidth + 8.0F;
      }

      return null;
   }

   private static float searchResultWidth(Feature feature) {
      MsdfFont font = UiFonts.sfProDisplay();
      float letterSpacing = 12.0F * UiFontStyle.MEDIUM.letterSpacingEm();
      float textWidth = Math.round(font.measureWidth(feature.getName(), 12.0F, letterSpacing));
      return 48.0F + textWidth;
   }

   private static class_2960 categoryIcon(FeatureCategory category) {
      int index = category.ordinal();
      return index >= 0 && index < CATEGORY_ICONS.length ? CATEGORY_ICONS[index] : CATEGORY_ICONS[0];
   }

   private static boolean matches(Feature feature, String query) {
      return matchPriority(feature, query) < Integer.MAX_VALUE;
   }

   private static int matchPriority(Feature feature, String query) {
      String name = feature.getName().toLowerCase(Locale.ROOT);
      if (name.startsWith(query)) {
         return 0;
      }

      if (name.contains(query)) {
         return 1;
      }

      for (Setting<?> setting : feature.getSettings()) {
         String canonicalSettingName = setting.getName().toLowerCase(Locale.ROOT);
         String settingName = MenuText.setting(feature.getName(), setting.getName()).toLowerCase(Locale.ROOT);
         if (!settingName.startsWith(query) && !canonicalSettingName.startsWith(query)) {
            if (!settingName.contains(query) && !canonicalSettingName.contains(query)) {
               Iterable<String> opts;
               if (setting instanceof ModeSetting mode) opts = (java.util.Collection<String>) (java.util.Collection<?>) mode.getModes();
               else if (setting instanceof MultiSelectSetting multi) opts = multi.getOptions();
               else opts = List.of();
               for (String option : opts) {
                  if (option.toLowerCase(Locale.ROOT).contains(query) || MenuText.option(option).toLowerCase(Locale.ROOT).contains(query)) {
                     return 3;
                  }
               }
               continue;
            }

            return 3;
         }

         return 2;
      }

      return !feature.getDescription().toLowerCase(Locale.ROOT).contains(query)
            && !MenuText.featureDescription(feature.getDescription()).toLowerCase(Locale.ROOT).contains(query)
         ? Integer.MAX_VALUE
         : 4;
   }

   private static int shortestColumn(int[] values) {
      int column = 0;

      for (int index = 1; index < values.length; index++) {
         if (values[index] < values[column]) {
            column = index;
         }
      }

      return column;
   }

   private void updateCategoryVisuals() {
      float categoryProgress = this.categoryAnimation.getValue();
      this.visualAlpha = categoryProgress;
      this.visualOffsetX = Math.round(14.0F * (1.0F - categoryProgress));
   }

   private static int categoryDockX() {
      return (1024 - CATEGORY_DOCK_WIDTH) / 2;
   }

   private static int categoryButtonX(int dockX, int index) {
      return dockX + 12 + index * 44;
   }

   private static float categoryIndex(FeatureCategory category) {
      FeatureCategory[] categories = FeatureCategory.values();

      for (int index = 0; index < categories.length; index++) {
         if (categories[index] == category) {
            return index;
         }
      }

      return 0.0F;
   }
}
