package org.ryzen.menu.pages.friends;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.CardGrid;
import org.ryzen.menu.ui.PageComponent;
import org.ryzen.menu.ui.controls.IconButton;
import org.ryzen.menu.ui.controls.InputComponent;
import org.ryzen.menu.ui.popups.ModalDialog;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class FriendPage extends PageComponent {
   private static final int CONTROLS_RIGHT = 992;
   private static final int CONTROLS_Y = 104;
   private static final int COUNT_WIDTH = 100;
   private static final int COUNT_HEIGHT = 36;
   private static final int PLUS_BOX = 24;
   private static final int PLUS_X = 968;
   private static final float PLUS_Y = 110.0F;
   private static final int COUNT_X = 860;
   private static final int LIST_TOP = 188;
   private static final int ROW_STEP = 73;
   private final List<FriendCard> cards = new ArrayList<>();
   private final CardGrid grid = new CardGrid(188, 320, 73, 624);
   private final InputComponent nameInput = new InputComponent(() -> this.pendingName, value -> this.pendingName = value)
      .placeholder("Player name")
      .filter(value -> value.length() <= 16 && value.matches("^[A-Za-z0-9_]*$"));
   private final ModalDialog addModal = new ModalDialog(
      Textures.Icons.USER_ROUND_PLUS,
      "Add Friend",
      new String[]{"Enter a Minecraft player name."},
      this.nameInput,
      this::saveModal,
      new ModalDialog.Extras() {
         @Override
         public void render(ModalDialog modal, float alpha) {
            if (!FriendPage.this.modalError.isEmpty()) {
               FriendPage.this.text(
                  modal.contentX(), modal.headerY() + 35, 9.0F, MenuText.ui(FriendPage.this.modalError), Theme.Colors.SYSTEM_RED, alpha, UiFontStyle.REGULAR
               );
            }
         }
      }
   );
   private final IconButton addButton = new IconButton(Textures.Icons.USER_ROUND_PLUS, 16, this::openModal);
   private MenuPage displayedPage = MenuPage.NONE;
   private List<FriendManager.FriendEntry> snapshot = List.of();
   private String pendingName = "";
   private String modalError = "";

   @Override
   protected void onLayout() {
      this.displayedPage = this.state.displayPage();
      if (this.displayedPage != MenuPage.FRIENDS && this.addModal.isOpen()) {
         this.closeModal();
      }

      this.syncCards();
      this.grid.update(this.cards.size());

      for (int index = 0; index < this.cards.size(); index++) {
         this.cards.get(index).place(this, this.grid.x(index), this.grid.y(index), this.mouseX, this.mouseY, this.progress);
      }

      this.addButton.place(this, 968.0F, 110.0F, 24, this.mouseX, this.mouseY).alpha(this.progress);
      this.addModal.place(this, this.mouseX, this.mouseY, this.progress);
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contentContains(mouseX, mouseY)) {
         return false;
      }

      if (this.addModal.isOpen()) {
         return this.addModal.handleClick(mouseX, mouseY);
      }

      if (this.addButton.handleClick(mouseX, mouseY)) {
         return true;
      }

      for (FriendCard card : List.copyOf(this.cards)) {
         if (card.isPinAt(mouseX, mouseY)) {
            FriendManager.INSTANCE.togglePinned(card.name());
            this.syncCards();
            return true;
         }

         if (card.isDeleteAt(mouseX, mouseY)) {
            FriendManager.INSTANCE.remove(card.name());
            this.syncCards();
            return true;
         }
      }

      return true;
   }

   public boolean handleKey(int key) {
      return this.displayedPage != MenuPage.FRIENDS ? false : this.addModal.handleKey(key);
   }

   public boolean handleCharacter(int codePoint) {
      return this.displayedPage == MenuPage.FRIENDS && this.addModal.handleCharacter(codePoint);
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
      if (!this.addModal.isOpen() && this.contains(mouseX, mouseY) && !(mouseY < this.sy(168.0F))) {
         this.grid.scroll(vertical, this.cards.size());
      }
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.displayedPage == MenuPage.FRIENDS && !(this.progress <= 0.001F)) {
         this.pageHeader("Friends", "Manage trusted players ignored by combat features.");
         this.rect(860.0F, 104.0F, 100.0F, 36.0F, Theme.Colors.OUTLINES_MEDIUM, 999.0F, this.progress);
         this.text(876.0F, this.centeredTextY(122.0F, 14.0F), 14.0F, MenuText.friendCount(this.cards.size()), -1, this.progress, UiFontStyle.MEDIUM);
         this.addButton.render(minecraft, guiGraphicsExtractor);
         Render2DUtil.pushScissor(this.x(), this.sy(168.0F), this.width(), this.height() - this.px(168.0F));
         if (this.cards.isEmpty()) {
            this.renderEmptyState();
         } else {
            for (FriendCard card : this.cards) {
               card.render(minecraft, guiGraphicsExtractor);
            }
         }

         Render2DUtil.popScissor();
         this.addModal.render(minecraft, guiGraphicsExtractor);
      }
   }

   private void renderEmptyState() {
      float centerY = 478.0F;
      this.texture(500.0F, centerY - 58.0F, 24.0F, Textures.Header.FRIENDS, Theme.Colors.ICON_MUTED, this.progress);
      float titleHeight = UiFonts.sfProDisplay().textHeight(16.0F);
      this.textCentered(512.0F, centerY - 20.0F, 16.0F, MenuText.ui("No friends yet"), Theme.Colors.PRIMARY, this.progress, UiFontStyle.MEDIUM);
      this.textCentered(
         512.0F,
         centerY - 20.0F + titleHeight + 8.0F,
         12.0F,
         MenuText.ui("Add a player here or use .friend add <name>"),
         Theme.Colors.SECONDARY,
         this.progress,
         UiFontStyle.REGULAR
      );
   }

   private void openModal() {
      this.pendingName = "";
      this.modalError = "";
      this.addModal.open();
   }

   private void closeModal() {
      this.pendingName = "";
      this.modalError = "";
      this.addModal.close();
   }

   private void saveModal() {
      String name = this.pendingName.trim();
      if (!FriendManager.isValidName(name)) {
         this.modalError = "Enter a valid player name";
      } else if (!FriendManager.INSTANCE.add(name)) {
         this.modalError = "This player is already a friend";
      } else {
         this.syncCards();
         this.closeModal();
      }
   }

   private void syncCards() {
      List<FriendManager.FriendEntry> current = FriendManager.INSTANCE.getEntries();
      if (!current.equals(this.snapshot)) {
         this.snapshot = current;
         this.cards.clear();

         for (FriendManager.FriendEntry friend : current) {
            this.cards.add(new FriendCard(friend));
         }
      }
   }
}
