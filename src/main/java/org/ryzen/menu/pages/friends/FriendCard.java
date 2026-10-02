package org.ryzen.menu.pages.friends;

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
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.PlayerHead;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class FriendCard extends MenuCard {
   private static final int TEXT_X = 58;
   private static final int DELETE_BOX_X = 280;
   private static final int PIN_BOX_X = 252;
   private final FriendManager.FriendEntry entry;
   private final IconButton pinButton = new IconButton(Textures.Icons.PIN, 16, null);
   private final IconButton deleteButton = new IconButton(Textures.Icons.DELETE_LEFT, 16, null)
      .hoverBackground(ColorUtil.withAlpha(Theme.Colors.SYSTEM_RED, 28))
      .tint(Theme.Colors.ICON, Theme.Colors.SYSTEM_RED);

   public FriendCard(FriendManager.FriendEntry entry) {
      this.entry = entry;
   }

   public String name() {
      return this.entry.name();
   }

   public void place(Component owner, int x, int y, int mouseX, int mouseY, float alpha) {
      this.placeBounds(owner, x, y, mouseX, mouseY, alpha);
      float actionY = this.actionRowY();
      this.pinButton
         .place(owner, x + this.actionSlotX(1), actionY, 24, mouseX, mouseY)
         .active(this.entry.pinned())
         .activeTint(Theme.getAccent())
         .activeBackground(ColorUtil.withAlpha(Theme.getAccent(), 30), ColorUtil.withAlpha(Theme.getAccent(), 48))
         .hoverBackground(Theme.Colors.SURFACE_HOVER)
         .alpha(alpha);
      this.deleteButton.place(owner, x + this.actionSlotX(0), actionY, 24, mouseX, mouseY).alpha(alpha);
   }

   public boolean isPinAt(int mouseX, int mouseY) {
      return this.pinButton.contains(mouseX, mouseY);
   }

   public boolean isDeleteAt(int mouseX, int mouseY) {
      return this.deleteButton.contains(mouseX, mouseY);
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      this.rect(this.designX, this.designY, 320.0F, 57.0F, this.cardHovered() ? Theme.Colors.CARD_HOVER : Theme.Colors.BACKGROUND_SURFACE_S, 8.0F, this.alpha);
      this.outline(this.designX, this.designY, 320.0F, 57.0F, Theme.Colors.OUTLINES_SMALL, 8.0F, 0.5F, this.alpha);
      boolean online = FriendSkinCache.isOnline(minecraft, this.entry.name());
      if (online) {
         FriendManager.INSTANCE.markSeen(this.entry.name());
      }

      this.drawHead(FriendSkinCache.texture(minecraft, this.entry.name()));
      float nameHeight = UiFonts.sfProDisplay().textHeight(14.0F);
      float statusHeight = UiFonts.sfProDisplay().textHeight(11.0F);
      float textTop = this.designY + (57.0F - nameHeight - 4.0F - statusHeight) / 2.0F;
      float maxTextWidth = 186.0F;
      String shownName = UiFonts.sfProDisplay().ellipsize(this.entry.name(), 14.0F, 14.0F * UiFontStyle.MEDIUM.letterSpacingEm(), maxTextWidth);
      this.text(this.designX + 58, textTop, 14.0F, shownName, -1, this.alpha, UiFontStyle.MEDIUM);
      this.text(
         this.designX + 58,
         textTop + nameHeight + 4.0F,
         11.0F,
         MenuText.friendStatus(online, this.entry.lastSeen()),
         online ? Theme.Colors.TRAFFIC_MAXIMIZE : Theme.Colors.SECONDARY,
         this.alpha,
         UiFontStyle.REGULAR
      );
      this.pinButton.render(minecraft, guiGraphicsExtractor);
      this.deleteButton.render(minecraft, guiGraphicsExtractor);
   }

   private void drawHead(class_2960 skin) {
      float x = this.sx(this.designX + 16);
      float y = this.sy(this.designY + 12.5F);
      PlayerHead.draw(x, y, this.px(32.0F), skin, this.px(6.0F), this.alpha(-1, this.alpha));
   }
}
