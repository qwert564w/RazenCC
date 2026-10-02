package org.ryzen.menu.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.controls.IconButton;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;
import org.ryzen.utils.text.NameProtectUtil;
import org.ryzen.utils.text.StringUtil;

@Environment(EnvType.CLIENT)
public final class MenuHeader extends Component {
   private final IconButton searchButton = new IconButton(Textures.Header.SEARCH, 0, null)
      .radius(8)
      .tint(Theme.Colors.ICON, Theme.Colors.ICON)
      .activeTint(-1)
      .hoverBackground(Theme.Colors.SURFACE_HOVER)
      .activeBackground(Theme.Colors.SURFACE_ACTIVE, Theme.Colors.SURFACE_ACTIVE);
   private final IconButton chevronLeftButton = new IconButton(Textures.Header.CHEVRON_LEFT, 0, null).tint(Theme.Colors.ICON, Theme.Colors.ICON);
   private final IconButton chevronRightButton = new IconButton(Textures.Header.CHEVRON_RIGHT, 0, null).tint(Theme.Colors.ICON, Theme.Colors.ICON);
   private static final int CONTENT_RIGHT_EDGE = 1008;
   private static final int ACTION_BUTTON_GAP = 8;
   private static final int ACTION_DIVIDER_GAP = 8;
   private static final int PROFILE_DIVIDER_GAP = 20;
   private static final int PROFILE_HIT_HEIGHT = 24;
   private static final int PROFILE_HIT_PADDING_X = 6;
   private static final int PROFILE_POPUP_WIDTH = 185;
   private static final int PROFILE_POPUP_HEIGHT = 143;
   private static final int PROFILE_POPUP_PADDING = 12;
   private static final int PROFILE_POPUP_GAP = 8;
   private static final int PROFILE_POPUP_RADIUS = 16;
   private static final int PROFILE_POPUP_HEADER_HEIGHT = 32;
   private static final int PROFILE_POPUP_AVATAR_SIZE = 32;
   private static final int PROFILE_POPUP_AVATAR_RADIUS = 10;
   private static final int PROFILE_POPUP_BADGE_HEIGHT = 14;
   private static final int PROFILE_POPUP_BADGE_RADIUS = 65;
   private static final int PROFILE_POPUP_INFO_HEIGHT = 16;
   private static final int PROFILE_POPUP_SWATCH_SIZE = 12;
   private static final int PROFILE_POPUP_SWATCH_GAP = 4;
   private static final long POPUP_ANIMATION_MS = 150L;
   private static final long ACCENT_ANIMATION_MS = 180L;
   private static final class_2960[] ACTION_TEXTURES = new class_2960[]{
      Textures.Header.HUD, Textures.Header.SETTINGS, Textures.Header.FRIENDS, Textures.Header.PROFILE_ADD, Textures.Header.DOCUMENT
   };
   private static final MenuPage[] ACTION_PAGES = MenuPage.actionPages();
   private int mouseX;
   private int mouseY;
   private MenuPage activePage = MenuPage.NONE;
   private int focusedAction = -1;
   private String username = "";
   private float profileX;
   private float profileY;
   private float profileWidth;
   private float profileHeight;
   private float popupX;
   private float popupY;
   private float popupWidth;
   private float popupHeight;
   private int screenHeight;
   private boolean profilePopupOpen;
   private final Animation profilePopupAnimation = new Animation(150L, Animation.Easing.EASE_OUT_QUAD);
   private final Animation accentAnimation = new Animation(180L, Animation.Easing.EASE_OUT_QUAD);
   private int animatedAccentTarget = Theme.accentIndex();
   private MenuOverlayState state;

   public MenuHeader() {
      float accent = Theme.accentIndex();
      this.profilePopupAnimation.animate(0.0F, 0.0F, 0L, Animation.Easing.EASE_OUT_QUAD);
      this.accentAnimation.animate(accent, accent, 0L, Animation.Easing.EASE_OUT_QUAD);
   }

   public void place(Component frame, MenuOverlayState state, class_310 minecraft, int mouseX, int mouseY) {
      this.attach(frame, frame.x(), frame.y(), frame.width(), frame.px(54.0F));
      this.state = state;
      this.mouseX = mouseX;
      this.mouseY = mouseY;
      this.activePage = state.page();
      this.focusedAction = state.focusedHeaderAction();
      this.username = NameProtectUtil.protect(minecraft.method_1548().method_1676());
      this.screenHeight = minecraft.method_22683().method_4502();
      this.updateProfileBounds();
      this.updatePopupBounds();
      this.syncAccentAnimation();
   }

   public MenuPage pageAt(float mouseX, float mouseY) {
      float hitSize = this.px(24.0F);
      float iconSize = this.px(16.0F);
      float hitY = this.controlButtonY();
      if (!(mouseY < hitY) && !(mouseY > hitY + hitSize)) {
         for (int index = 0; index < ACTION_PAGES.length; index++) {
            float buttonX = this.actionButtonX(index);
            float iconX = buttonX + (hitSize - iconSize) / 2.0F;
            float hitX = iconX - (hitSize - iconSize) / 2.0F;
            if (mouseX >= hitX && mouseX <= hitX + hitSize) {
               return ACTION_PAGES[index];
            }
         }

         return MenuPage.NONE;
      } else {
         return MenuPage.NONE;
      }
   }

   public boolean isSearchAt(float mouseX, float mouseY) {
      return this.searchButton.contains(mouseX, mouseY);
   }

   public boolean isProfileAt(float mouseX, float mouseY) {
      return mouseX >= this.profileX && mouseX <= this.profileX + this.profileWidth && mouseY >= this.profileY && mouseY <= this.profileY + this.profileHeight;
   }

   public boolean isProfilePopupAt(float mouseX, float mouseY) {
      return this.profilePopupOpen
         && mouseX >= this.popupX
         && mouseX <= this.popupX + this.popupWidth
         && mouseY >= this.popupY
         && mouseY <= this.popupY + this.popupHeight;
   }

   public boolean handleMouseButton(float mouseX, float mouseY, int button) {
      boolean profileHit = this.isProfileAt(mouseX, mouseY);
      boolean popupHit = this.isProfilePopupAt(mouseX, mouseY);
      if (button == 0) {
         if (profileHit) {
            if (this.profilePopupOpen) {
               this.closeProfilePopup();
            } else {
               this.openProfilePopup();
            }

            return true;
         } else {
            if (!this.profilePopupOpen) {
               return false;
            }

            if (popupHit) {
               int accentIndex = this.accentAt(mouseX, mouseY);
               if (accentIndex >= 0) {
                  this.setAccentIndex(accentIndex);
               }

               return true;
            } else {
               this.closeProfilePopup();
               return false;
            }
         }
      } else {
         return profileHit || popupHit;
      }
   }

   public boolean handleScroll(float mouseX, float mouseY, double vertical) {
      if (this.profilePopupOpen && this.accentAt(mouseX, mouseY) >= 0) {
         int next = Math.floorMod(Theme.accentIndex() + (vertical > 0.0 ? -1 : 1), Theme.accentCount());
         this.setAccentIndex(next);
         return true;
      } else {
         return false;
      }
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.drawNavigation();
      this.drawBrand();
      this.drawProfile(minecraft);
      this.drawActions();
      Render2DUtil.rect(this.x(), this.sy(55.0F), this.width(), 1.0F).color(Theme.Colors.DIVIDER_HEADER).draw();
      float popupProgress = this.profilePopupAnimation.getValue();
      if (popupProgress > 0.001F) {
         this.drawProfilePopup(popupProgress);
      }
   }

   private void openProfilePopup() {
      this.profilePopupOpen = true;
      this.profilePopupAnimation.animate(0.0F, 1.0F, 150L, Animation.Easing.EASE_OUT_QUAD);
   }

   private void closeProfilePopup() {
      if (this.profilePopupOpen) {
         this.profilePopupAnimation.animate(this.profilePopupAnimation.getValue(), 0.0F, 120L, Animation.Easing.EASE_OUT_QUAD);
      }

      this.profilePopupOpen = false;
   }

   private void setAccentIndex(int index) {
      float from = this.accentAnimation.getValue();
      Theme.setAccentIndex(index);
      MenuConfigStore.saveAccentIndex(Theme.accentIndex());
      this.animatedAccentTarget = Theme.accentIndex();
      this.accentAnimation.animate(from, this.animatedAccentTarget, 180L, Animation.Easing.EASE_OUT_QUAD);
   }

   private void syncAccentAnimation() {
      int target = Theme.accentIndex();
      if (target != this.animatedAccentTarget) {
         float from = this.accentAnimation.getValue();
         this.animatedAccentTarget = target;
         this.accentAnimation.animate(from, target, 180L, Animation.Easing.EASE_OUT_QUAD);
      }
   }

   private void drawNavigation() {
      float currentX = this.sx(16.0F);
      float controlsY = this.sy(12.0F);
      float controlsHeight = this.px(32.0F);
      float trafficSize = this.px(12.0F);
      float trafficY = controlsY + (controlsHeight - trafficSize) / 2.0F;
      float trafficRadius = trafficSize / 2.0F;
      Render2DUtil.rect(currentX, trafficY, trafficSize, trafficSize).color(Theme.Colors.TRAFFIC_CLOSE).radius(trafficRadius).draw();
      currentX += trafficSize + this.px(10.0F);
      Render2DUtil.rect(currentX, trafficY, trafficSize, trafficSize).color(Theme.Colors.TRAFFIC_MINIMIZE).radius(trafficRadius).draw();
      currentX += trafficSize + this.px(10.0F);
      Render2DUtil.rect(currentX, trafficY, trafficSize, trafficSize).color(Theme.Colors.TRAFFIC_MAXIMIZE).radius(trafficRadius).draw();
      currentX += trafficSize + this.px(8.0F);
      float dividerY = controlsY + (controlsHeight - this.px(12.0F)) / 2.0F;
      Render2DUtil.rect(currentX, dividerY, 1.0F, this.px(12.0F)).color(Theme.Colors.OUTLINES_LARGE).draw();
      currentX += 1.0F + this.px(8.0F);
      float buttonSize = this.px(24.0F);
      float buttonY = controlsY + (controlsHeight - buttonSize) / 2.0F;
      int headerIconSize = 16;
      class_310 minecraft = class_310.method_1551();
      this.searchButton
         .placeAt(this, currentX, buttonY, buttonSize, this.mouseX, this.mouseY)
         .iconSize(headerIconSize)
         .active(this.activePage == MenuPage.SEARCH)
         .render(minecraft, null);
      currentX += buttonSize + this.px(8.0F);
      this.chevronLeftButton
         .placeAt(this, currentX, buttonY, buttonSize, this.mouseX, this.mouseY)
         .iconSize(headerIconSize)
         .enabled(this.state != null && this.state.canGoBack())
         .render(minecraft, null);
      currentX += this.px(24.0F);
      this.chevronRightButton
         .placeAt(this, currentX, buttonY, buttonSize, this.mouseX, this.mouseY)
         .iconSize(headerIconSize)
         .enabled(this.state != null && this.state.canGoForward())
         .render(minecraft, null);
   }

   private void drawBrand() {
      float pillWidth = Math.round(this.px(330.66666F));
      float pillX = this.x() + (this.width() - pillWidth) / 2.0F;
      Render2DUtil.rect(pillX, this.sy(12.0F), pillWidth, this.px(32.0F))
         .color(Theme.Colors.CARD)
         .radius(this.px(8.0F))
         .border(Math.max(0.5F, this.px(0.5F)), Theme.Colors.DIVIDER_HEADER)
         .draw();
      Render2DUtil.text(
            pillX + pillWidth / 2.0F,
            UiFonts.sfProDisplay().centeredTextY(this.sy(12.0F) + this.px(32.0F) / 2.0F, this.px(12.0F)),
            this.px(12.0F),
            "ryzendlc.org"
         )
         .style(UiFontStyle.MEDIUM)
         .color(Theme.Colors.PRIMARY_BRIGHT)
         .align(TextAlign.CENTER)
         .draw();
   }

   private void drawProfile(class_310 minecraft) {
      String label = StringUtil.abbreviate(NameProtectUtil.protect(minecraft.method_1548().method_1676()), 18);
      if (this.isProfileAt(this.mouseX, this.mouseY) || this.profilePopupOpen) {
         Render2DUtil.rect(this.profileX, this.profileY, this.profileWidth, this.profileHeight)
            .color(this.profilePopupOpen ? Theme.Colors.SURFACE_ACTIVE : Theme.Colors.SURFACE_HOVER)
            .radius(this.px(10.0F))
            .draw();
      }

      float avatarSize = this.px(20.0F);
      float avatarY = this.profileY + (this.profileHeight - avatarSize) / 2.0F;
      float contentX = this.profileX + this.px(6.0F);
      float gap = this.px(8.0F);
      Render2DUtil.texture(contentX, avatarY, avatarSize, avatarSize, Textures.Header.DEV_AVATAR).draw();
      Render2DUtil.text(
            contentX + avatarSize + gap, UiFonts.sfProDisplay().centeredTextY(this.profileY + this.profileHeight / 2.0F, this.px(12.0F)), this.px(12.0F), label
         )
         .style(UiFontStyle.REGULAR_TRACKED)
         .color(Theme.Colors.TEXT)
         .draw();
   }

   private void drawProfilePopup(float progress) {
      float alpha = progress;
      float lift = -6.0F * (1.0F - progress);
      float drawY = this.popupY + lift;
      Render2DUtil.rect(this.popupX, drawY, this.popupWidth, this.popupHeight)
         .color(0)
         .radius(this.px(16.0F))
         .shadow(ColorUtil.multiplyAlpha(Theme.Colors.POPUP_SHADOW, alpha), this.px(12.0F))
         .draw();
      Render2DUtil.rect(this.popupX, drawY, this.popupWidth, this.popupHeight)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.BACKGROUND_PRIMARY_50, alpha))
         .radius(this.px(16.0F))
         .border(Math.max(0.5F, this.px(0.5F)), ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_MEDIUM, alpha))
         .blur(this.px(8.0F), alpha)
         .draw();
      float contentX = this.popupX + this.px(12.0F);
      float currentY = drawY + this.px(12.0F);
      this.drawPopupHeader(contentX, currentY, alpha);
      currentY += this.px(32.0F) + this.px(8.0F);
      this.drawPopupDivider(contentX, currentY, alpha);
      currentY += Math.max(1.0F, this.px(1.0F)) + this.px(8.0F);
      this.drawPopupInfoRow(contentX, currentY, "Username", StringUtil.abbreviate(this.username, 12), alpha);
      currentY += this.px(16.0F) + this.px(8.0F);
      this.drawPopupInfoRow(contentX, currentY, "Status", "Active", alpha);
      currentY += this.px(16.0F) + this.px(8.0F);
      this.drawPopupDivider(contentX, currentY, alpha);
      currentY += Math.max(1.0F, this.px(1.0F)) + this.px(8.0F);
      this.drawPopupAccentRow(currentY, alpha);
   }

   private void drawPopupHeader(float contentX, float y, float alpha) {
      float avatarSize = this.px(32.0F);
      Render2DUtil.rect(contentX, y, avatarSize, avatarSize).color(ColorUtil.multiplyAlpha(Theme.Colors.CONTROL_ALT, alpha)).radius(this.px(10.0F)).draw();
      Render2DUtil.texture(contentX, y, avatarSize, avatarSize, Textures.Header.DEV_AVATAR).color(ColorUtil.multiplyAlpha(-1, alpha)).draw();
      float textX = contentX + avatarSize + this.px(6.0F);
      float badgeWidth = this.px(40.0F);
      float badgeHeight = this.px(14.0F);
      float badgeY = y + this.px(2.0F);
      Render2DUtil.rect(textX, badgeY, badgeWidth, badgeHeight)
         .color(ColorUtil.multiplyAlpha(ColorUtil.withAlpha(Theme.Colors.SYSTEM_INFORMATION, 51), alpha))
         .radius(this.px(65.0F))
         .draw();
      float badgeTextSize = this.px(8.0F);
      float badgeTextY = UiFonts.sfProDisplay().centeredTextY(badgeY + badgeHeight / 2.0F, badgeTextSize);
      Render2DUtil.text(textX + badgeWidth / 2.0F, badgeTextY, badgeTextSize, "ACTIVE")
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.CENTER)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.SYSTEM_INFORMATION, alpha))
         .draw();
      float usernameCenterY = y + this.px(48.0F) / 2.0F;
      Render2DUtil.text(textX, UiFonts.sfProDisplay().centeredTextY(usernameCenterY, this.px(12.0F)), this.px(12.0F), StringUtil.abbreviate(this.username, 16))
         .style(UiFontStyle.SEMIBOLD)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
   }

   private void drawPopupInfoRow(float contentX, float y, String label, String value, float alpha) {
      float rowTextY = UiFonts.sfProDisplay().centeredTextY(y + this.px(16.0F) / 2.0F, this.px(10.0F));
      Render2DUtil.text(contentX, rowTextY, this.px(10.0F), label)
         .style(UiFontStyle.MEDIUM)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.TEXT_TEXT, alpha))
         .draw();
      Render2DUtil.text(this.popupX + this.popupWidth - this.px(12.0F), rowTextY, this.px(10.0F), value)
         .style(UiFontStyle.MEDIUM)
         .align(TextAlign.RIGHT)
         .color(ColorUtil.multiplyAlpha(Theme.Colors.SECONDARY, alpha))
         .draw();
   }

   private void drawPopupDivider(float contentX, float y, float alpha) {
      Render2DUtil.rect(contentX, y, this.popupWidth - this.px(24.0F), Math.max(1.0F, this.px(1.0F)))
         .color(ColorUtil.multiplyAlpha(Theme.Colors.OUTLINES_MEDIUM, alpha))
         .draw();
   }

   private void drawPopupAccentRow(float y, float alpha) {
      float size = this.px(12.0F);
      float gap = this.px(4.0F);
      float rowWidth = size * Theme.accentCount() + gap * (Theme.accentCount() - 1);
      float rowX = this.popupX + (this.popupWidth - rowWidth) / 2.0F;

      for (int index = 0; index < Theme.accentCount(); index++) {
         float swatchX = rowX + index * (size + gap);
         Render2DUtil.rect(swatchX, y, size, size).color(ColorUtil.multiplyAlpha(Theme.accent(index), alpha)).radius(this.px(4.0F)).draw();
      }

      float indicatorX = rowX + this.accentAnimation.getValue() * (size + gap);
      Render2DUtil.rect(
            indicatorX - Math.max(1.0F, this.px(1.0F)),
            y - Math.max(1.0F, this.px(1.0F)),
            size + Math.max(2.0F, this.px(2.0F)),
            size + Math.max(2.0F, this.px(2.0F))
         )
         .color(0)
         .radius(this.px(5.0F))
         .border(Math.max(0.5F, this.px(1.0F)), ColorUtil.multiplyAlpha(-1, alpha))
         .draw();
   }

   private void drawActions() {
      Render2DUtil.rect(this.actionDividerX(), this.sy(22.0F), 1.0F, this.px(12.0F)).color(Theme.Colors.SEPARATOR).draw();
      float hitSize = this.px(24.0F);
      float iconSize = this.px(16.0F);

      for (int index = 0; index < ACTION_PAGES.length; index++) {
         float hitX = this.actionButtonX(index);
         float iconX = hitX + (hitSize - iconSize) / 2.0F;
         boolean active = this.activePage == ACTION_PAGES[index];
         Render2DUtil.texture(iconX, this.controlIconY(), iconSize, iconSize, ACTION_TEXTURES[index]).color(active ? -1 : Theme.Colors.ICON).draw();
      }
   }

   private float searchButtonX() {
      float trafficSize = this.px(12.0F);
      return this.sx(16.0F) + trafficSize * 3.0F + this.px(10.0F) * 2.0F + this.px(8.0F) + 1.0F + this.px(8.0F);
   }

   private float controlButtonY() {
      return this.sy(12.0F) + (this.px(32.0F) - this.px(24.0F)) / 2.0F;
   }

   public boolean isChevronLeftAt(float mouseX, float mouseY) {
      return this.chevronLeftButton.contains(mouseX, mouseY);
   }

   public boolean isChevronRightAt(float mouseX, float mouseY) {
      return this.chevronRightButton.contains(mouseX, mouseY);
   }

   private float leftChevronX() {
      return this.searchButtonX() + this.px(24.0F) + this.px(8.0F);
   }

   private float rightChevronX() {
      return this.leftChevronX() + this.px(24.0F);
   }

   private float controlIconY() {
      return this.controlButtonY() + this.px(4.0F);
   }

   private float actionButtonX(int index) {
      int buttonDesignX = 988 - (ACTION_PAGES.length - 1 - index) * 32;
      return this.sx(buttonDesignX);
   }

   private float actionDividerX() {
      return this.actionButtonX(0) - this.px(8.0F) - 1.0F;
   }

   private void updateProfileBounds() {
      String label = StringUtil.abbreviate(this.username, 18);
      float fontSize = 12.0F;
      MsdfFont font = UiFonts.sfProDisplay();
      float letterSpacing = fontSize * UiFontStyle.REGULAR_TRACKED.letterSpacingEm();
      float textWidth = font.measureWidth(label, this.px(fontSize), this.px(letterSpacing));
      float avatarSize = this.px(20.0F);
      float gap = this.px(8.0F);
      float totalWidth = avatarSize + gap + textWidth;
      float rightBoundary = this.actionDividerX() - this.px(20.0F);
      this.profileX = rightBoundary - totalWidth - this.px(6.0F);
      this.profileY = this.sy(16.0F);
      this.profileWidth = totalWidth + this.px(12.0F);
      this.profileHeight = this.px(24.0F);
   }

   private void updatePopupBounds() {
      float rightBoundary = this.actionDividerX() - this.px(20.0F);
      this.popupWidth = this.px(185.0F);
      this.popupHeight = this.px(143.0F);
      this.popupX = rightBoundary - this.popupWidth;
      float gap = this.px(8.0F);
      float topY = this.y() - this.popupHeight - gap;
      float bottomY = this.y() + this.height() + gap;
      float maxY = Math.max(0.0F, this.screenHeight - this.popupHeight);
      this.popupY = topY >= 0.0F ? topY : Math.min(bottomY, maxY);
   }

   private int accentAt(float mouseX, float mouseY) {
      if (!this.isProfilePopupAt(mouseX, mouseY)) {
         return -1;
      }

      float size = this.px(12.0F);
      float gap = this.px(4.0F);
      float rowWidth = size * Theme.accentCount() + gap * (Theme.accentCount() - 1);
      float rowX = this.popupX + (this.popupWidth - rowWidth) / 2.0F;
      float rowY = this.accentRowY();

      for (int index = 0; index < Theme.accentCount(); index++) {
         float swatchX = rowX + index * (size + gap);
         if (mouseX >= swatchX && mouseX <= swatchX + size && mouseY >= rowY && mouseY <= rowY + size) {
            return index;
         }
      }

      return -1;
   }

   private float accentRowY() {
      return this.popupY + this.px(52.0F) + Math.max(1.0F, this.px(1.0F)) + this.px(56.0F) + Math.max(1.0F, this.px(1.0F)) + this.px(8.0F);
   }
}
