package org.ryzen.menu.pages.accounts;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.MenuCard;
import org.ryzen.menu.ui.controls.IconButton;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class AccountCard extends MenuCard {
   static final int HEAD_COUNT = 7;
   private static final int PADDING_X = 16;
   private static final int CONTENT_GAP = 8;
   private static final int AVATAR_RADIUS = 6;
   private static final int INACTIVE_MASK = ColorUtil.rgba(0, 0, 0, 110);
   private static final int TEXT_X = 56;
   private static final float TEXT_GAP = 4.0F;
   private final String name;
   private final String activity;
   private final int avatarIndex;
   private final class_2960 avatar;
   private final IconButton pinButton = new IconButton(Textures.Icons.PIN, 16, null);
   private final IconButton gamepadButton = new IconButton(Textures.Icons.GAMEPAD, 16, null);
   private final IconButton deleteButton = new IconButton(Textures.Icons.DELETE_LEFT, 16, null);
   private boolean pinned;
   private boolean selected;
   private boolean showGamepad = true;

   public AccountCard(String name, String activity, int avatarIndex) {
      this(name, activity, avatarIndex, false);
   }

   public AccountCard(String name, String activity, int avatarIndex, boolean pinned) {
      this.name = name;
      this.activity = activity;
      this.avatarIndex = avatarIndex;
      this.avatar = menuTexturePng("accounts/head_" + Math.floorMod(avatarIndex, 7));
      this.pinned = pinned;
   }

   public String name() {
      return this.name;
   }

   public String activity() {
      return this.activity;
   }

   public int avatarIndex() {
      return this.avatarIndex;
   }

   public boolean pinned() {
      return this.pinned;
   }

   public void place(Component owner, int x, int y, int mouseX, int mouseY, float alpha) {
      this.placeBounds(owner, x, y, mouseX, mouseY, alpha);
      int deleteSlot = 0;
      int gamepadSlot = 1;
      int pinSlot = this.showGamepad ? 2 : 1;
      float actionY = this.actionRowY();
      int white38 = ColorUtil.withAlpha(-1, 38);
      this.pinButton
         .place(owner, x + this.actionSlotX(pinSlot), actionY, 24, mouseX, mouseY)
         .active(this.pinned)
         .activeTint(this.selected ? -1 : Theme.getAccent())
         .activeBackground(
            this.selected ? ColorUtil.withAlpha(-1, 22) : ColorUtil.withAlpha(Theme.getAccent(), 30),
            this.selected ? white38 : ColorUtil.withAlpha(Theme.getAccent(), 48)
         )
         .hoverBackground(this.selected ? white38 : Theme.Colors.SURFACE_HOVER)
         .tint(this.selected ? -1 : Theme.Colors.ICON, -1)
         .alpha(alpha);
      this.gamepadButton
         .place(owner, x + this.actionSlotX(gamepadSlot), actionY, 24, mouseX, mouseY)
         .hoverBackground(this.selected ? white38 : Theme.Colors.SURFACE_HOVER)
         .tint(this.selected ? -1 : Theme.Colors.ICON, -1)
         .alpha(alpha);
      this.deleteButton
         .place(owner, x + this.actionSlotX(deleteSlot), actionY, 24, mouseX, mouseY)
         .hoverBackground(ColorUtil.withAlpha(Theme.Colors.SYSTEM_RED, 28))
         .tint(this.selected ? -1 : Theme.Colors.ICON, Theme.Colors.SYSTEM_RED)
         .alpha(alpha);
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      return this.contains(mouseX, mouseY);
   }

   public boolean isDeleteAt(int mouseX, int mouseY) {
      return this.deleteButton.contains(mouseX, mouseY);
   }

   public boolean isPinAt(int mouseX, int mouseY) {
      return this.pinButton.contains(mouseX, mouseY);
   }

   public boolean isGamepadAt(int mouseX, int mouseY) {
      return this.showGamepad && this.gamepadButton.contains(mouseX, mouseY);
   }

   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   public void setShowGamepad(boolean showGamepad) {
      this.showGamepad = showGamepad;
   }

   public void togglePinned() {
      this.pinned = !this.pinned;
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.selected) {
         this.rect(this.designX, this.designY, 320.0F, 57.0F, Theme.getAccent(), 8.0F, this.alpha);
      } else {
         this.rect(this.designX, this.designY, 320.0F, 57.0F, Theme.Colors.BACKGROUND_SURFACE_S, 8.0F, this.alpha);
         this.outline(this.designX, this.designY, 320.0F, 57.0F, Theme.Colors.OUTLINES_SMALL, 8.0F, 0.5F, this.alpha);
      }

      this.texture(this.designX + 16, this.designY + 12.5F, 32.0F, this.avatar, -1, this.alpha);
      if (!this.selected) {
         this.rect(this.designX + 16, this.designY + 12.5F, 32.0F, 32.0F, INACTIVE_MASK, 6.0F, this.alpha);
      }

      int textColor = this.selected ? -1 : Theme.Colors.ICON;
      float textMaxWidth = this.actionSlotX(this.showGamepad ? 2 : 1) - 56 - 8;
      String shownName = UiFonts.sfProDisplay().ellipsize(this.name, 14.0F, 14.0F * UiFontStyle.MEDIUM.letterSpacingEm(), textMaxWidth);
      String shownActivity = UiFonts.sfProDisplay().ellipsize(MenuText.ui(this.activity), 12.0F, 12.0F * UiFontStyle.REGULAR.letterSpacingEm(), textMaxWidth);
      float nameHeight = UiFonts.sfProDisplay().textHeight(14.0F);
      float activityHeight = UiFonts.sfProDisplay().textHeight(12.0F);
      float blockTop = this.designY + (57.0F - (nameHeight + 4.0F + activityHeight)) / 2.0F;
      this.text(this.designX + 56, blockTop, 14.0F, shownName, textColor, this.alpha, UiFontStyle.MEDIUM);
      this.text(this.designX + 56, blockTop + nameHeight + 4.0F, 12.0F, shownActivity, textColor, this.alpha, UiFontStyle.REGULAR);
      this.pinButton.render(minecraft, guiGraphicsExtractor);
      if (this.showGamepad) {
         this.gamepadButton.render(minecraft, guiGraphicsExtractor);
      }

      this.deleteButton.render(minecraft, guiGraphicsExtractor);
   }
}
