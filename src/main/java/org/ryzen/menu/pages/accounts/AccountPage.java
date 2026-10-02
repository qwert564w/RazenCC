package org.ryzen.menu.pages.accounts;

import dev.ryzen.client.account.AccountStore;
import dev.ryzen.client.account.AltAccount;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_332;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.menu.ui.CardGrid;
import org.ryzen.menu.ui.Component;
import org.ryzen.menu.ui.PageComponent;
import org.ryzen.menu.ui.controls.IconButton;
import org.ryzen.menu.ui.controls.InputComponent;
import org.ryzen.menu.ui.popups.ModalDialog;
import org.ryzen.utils.AccountSwitcher;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.text.NameProtectUtil;

@Environment(EnvType.CLIENT)
public final class AccountPage extends PageComponent {
   private static final int ROW_STEP = 73;
   private static final int DROPDOWN_WIDTH = 85;
   private static final int DROPDOWN_HEIGHT = 36;
   private static final int DROPDOWN_PADDING_LEFT = 16;
   private static final int DROPDOWN_PADDING_RIGHT = 8;
   private static final int PLUS_BOX = 24;
   private static final int PLUS_ICON = 16;
   private static final int CONTROLS_RIGHT = 992;
   private static final int CONTROLS_GAP = 8;
   private static final int CONTROLS_ROW_Y = 104;
   private static final int PLUS_X = 968;
   private static final float PLUS_Y = 110.0F;
   private static final int DROPDOWN_X = 875;
   private static final String[] NICK_PREFIXES = new String[]{
      "Shadow",
      "Frost",
      "Void",
      "Pixel",
      "Aqua",
      "Night",
      "Storm",
      "Ember",
      "Ghost",
      "Nova",
      "Cyber",
      "Lunar",
      "Rapid",
      "Toxic",
      "Magma",
      "Blaze",
      "Astro",
      "Neon",
      "Grim",
      "Hyper",
      "Iron",
      "Zero",
      "Drako",
      "Mystic",
      "Silent",
      "Wicked",
      "Prime",
      "Retro",
      "Sour",
      "Vex"
   };
   private static final String[] NICK_SUFFIXES = new String[]{
      "Byte",
      "Craft",
      "Rush",
      "Fox",
      "Ryzen",
      "Wing",
      "Strike",
      "Core",
      "Drift",
      "Zap",
      "Wolf",
      "Hawk",
      "Reign",
      "Spark",
      "Flux",
      "Dash",
      "Rage",
      "Snipe",
      "King",
      "Lord",
      "Punch",
      "Shot",
      "Fang",
      "Peak",
      "Vibe",
      "Loop",
      "Crypt",
      "Gaze",
      "Husk",
      "Riot"
   };
   private final List<Component> children = new ArrayList<>();
   private final List<AccountCard> cards = new ArrayList<>();
   private MenuPage displayedPage = MenuPage.NONE;
   private AccountCard selectedCard;
   private boolean oldestFirst;
   private final CardGrid grid = new CardGrid(188, 320, 73, 624);
   private AccountStore store;
   private String pendingNickname = "";
   private final InputComponent nicknameInput = new InputComponent(() -> this.pendingNickname, value -> this.pendingNickname = value)
      .placeholder("Type Nickname")
      .filter(val -> val.length() <= 16 && val.matches("^[a-zA-Z0-9_]*$"));
   private final IconButton addButton = new IconButton(Textures.Icons.CIRCLE_PLUS, 16, this::openModal);
   private final IconButton dicesButton = new IconButton(Textures.Icons.DICES, 16, () -> this.pendingNickname = this.randomNickname());
   private final ModalDialog createModal = new ModalDialog(
      Textures.Icons.PLUS,
      "Create Account",
      new String[]{"Enter a new account nickname", "below."},
      this.nicknameInput,
      this::saveModal,
      new ModalDialog.Extras() {
         @Override
         public void render(ModalDialog modal, float alpha) {
            AccountPage.this.dicesButton.render(class_310.method_1551(), null);
         }

         @Override
         public boolean click(ModalDialog modal, int mouseX, int mouseY) {
            return AccountPage.this.dicesButton.handleClick(mouseX, mouseY);
         }
      }
   );

   public AccountPage() {
      this.loadAccounts();
      this.oldestFirst = MenuConfigStore.getBoolean("accountsOldestFirst", false);
   }

   private void loadAccounts() {
      this.store = AccountStore.load(class_310.method_1551().method_1548().method_1676());
      this.rebuildCards();
   }

   private void rebuildCards() {
      this.cards.clear();
      List<AltAccount> accounts = this.store.accountsForUi();
      int avatarIndex = 0;

      for (AltAccount account : accounts) {
         String activity = account.id().equals(this.store.selectedId()) ? "Playing now" : "Never played";
         this.cards.add(new AccountCard(account.nickname(), activity, avatarIndex, account.favorite()));
         avatarIndex++;
      }

      String selectedName = MenuConfigStore.getString("selectedAccount", "");
      this.selectedCard = this.cards
         .stream()
         .filter(card -> card.name().equals(selectedName))
         .findFirst()
         .orElse(this.cards.isEmpty() ? null : this.cards.get(0));
   }

   @Override
   protected void onLayout() {
      MenuPage previous = this.displayedPage;
      this.displayedPage = this.state.displayPage();
      if (this.displayedPage == MenuPage.ACCOUNT_SWITCHER && previous != MenuPage.ACCOUNT_SWITCHER) {
         this.loadAccounts();
      }

      if (this.displayedPage != MenuPage.ACCOUNT_SWITCHER && this.createModal.isOpen()) {
         this.closeModal();
      }

      this.grid.update(this.cards.size());
      String activeName = activeProfileName();
      List<AccountCard> display = this.displayOrder();

      for (int index = 0; index < display.size(); index++) {
         AccountCard card = display.get(index);
         boolean selected = card == this.selectedCard;
         card.setSelected(selected);
         card.setShowGamepad(!selected || !card.name().equals(activeName));
         card.place(this, this.grid.x(index), this.grid.y(index), this.mouseX, this.mouseY, this.progress);
      }

      this.addButton.place(this, 968.0F, 110.0F, 24, this.mouseX, this.mouseY).alpha(this.progress);
      this.createModal.place(this, this.mouseX, this.mouseY, this.progress);
      this.dicesButton
         .place(
            this,
            this.createModal.contentX() + this.createModal.contentWidth() - 26,
            this.createModal.inputY() + (this.createModal.inputHeight() - 24) / 2.0F,
            24,
            this.mouseX,
            this.mouseY
         )
         .alpha(this.progress);
   }

   public void handleScroll(int mouseX, int mouseY, double vertical) {
      if (!this.createModal.isOpen()) {
         if (this.contains(mouseX, mouseY) && mouseY >= this.sy(168.0F)) {
            this.grid.scroll(vertical, this.cards.size());
         }
      }
   }

   @Override
   public boolean handleClick(int mouseX, int mouseY) {
      if (!this.contentContains(mouseX, mouseY)) {
         return false;
      }

      if (this.createModal.isOpen()) {
         return this.createModal.handleClick(mouseX, mouseY);
      }

      if (this.hit(mouseX, mouseY, 875.0F, 104.0F, 85.0F, 36.0F)) {
         this.oldestFirst = !this.oldestFirst;
         MenuConfigStore.save(data -> data.addProperty("accountsOldestFirst", this.oldestFirst));
         return true;
      }

      if (this.addButton.handleClick(mouseX, mouseY)) {
         return true;
      }

      if (mouseY < this.sy(168.0F)) {
         return true;
      }

      for (AccountCard card : this.cards) {
         AltAccount account = this.findAccount(card.name());
         if (account != null) {
            if (card.isPinAt(mouseX, mouseY)) {
               this.store.toggleFavorite(account.id());
               this.rebuildCards();
               return true;
            }

            if (card.isDeleteAt(mouseX, mouseY)) {
               this.store.delete(account.id());
               this.rebuildCards();
               return true;
            }

            if (card.isGamepadAt(mouseX, mouseY)) {
               this.store.select(account.id());
               MenuConfigStore.save(data -> data.addProperty("selectedAccount", card.name()));
               this.rebuildCards();
               MenuOverlay.close(class_310.method_1551());
               AccountSwitcher.relogin(card.name());
               return true;
            }

            if (card.handleClick(mouseX, mouseY)) {
               this.store.select(account.id());
               AccountSwitcher.switchTo(card.name());
               MenuConfigStore.save(data -> data.addProperty("selectedAccount", card.name()));
               this.rebuildCards();
               return true;
            }
         }
      }

      return true;
   }

   private AltAccount findAccount(String nickname) {
      return this.store == null ? null : this.store.accountsForUi().stream().filter(account -> account.nickname().equals(nickname)).findFirst().orElse(null);
   }

   private static String activeProfileName() {
      class_310 mc = class_310.method_1551();
      return NameProtectUtil.protect(mc.field_1724 != null ? mc.field_1724.method_7334().name() : mc.method_1548().method_1676());
   }

   private List<AccountCard> displayOrder() {
      List<AccountCard> display = new ArrayList<>(this.cards);
      display.sort(Comparator.comparing(AccountCard::pinned).reversed().thenComparing(card -> card != this.selectedCard));
      return display;
   }

   public boolean handleKey(int key) {
      return this.createModal.handleKey(key);
   }

   public boolean handleCharacter(int codePoint) {
      return this.createModal.handleCharacter(codePoint);
   }

   @Override
   public void render(class_310 minecraft, class_332 guiGraphicsExtractor) {
      if (this.displayedPage == MenuPage.ACCOUNT_SWITCHER && !(this.progress <= 0.001F)) {
         this.pageHeader("Accounts", "Switch between your saved Minecraft accounts.");
         this.rect(875.0F, 104.0F, 85.0F, 36.0F, Theme.Colors.OUTLINES_MEDIUM, 999.0F, this.progress);
         this.text(891.0F, this.centeredTextY(122.0F, 14.0F), 14.0F, MenuText.ui(this.oldestFirst ? "Oldest" : "Recent"), -1, this.progress, UiFontStyle.MEDIUM);
         this.texture(940.0F, 116.0F, 12.0F, Textures.Icons.CHEVRONS_LEFT_RIGHT, Theme.Colors.ICON, this.progress);
         this.addButton.render(minecraft, guiGraphicsExtractor);
         Render2DUtil.pushScissor(this.x(), this.sy(168.0F), this.width(), this.height() - this.px(168.0F));

         for (AccountCard card : this.cards) {
            card.render(minecraft, guiGraphicsExtractor);
         }

         Render2DUtil.popScissor();
         this.createModal.render(minecraft, guiGraphicsExtractor);
      }
   }

   private void openModal() {
      this.pendingNickname = "";
      this.createModal.open();
   }

   private void closeModal() {
      this.pendingNickname = "";
      this.createModal.close();
   }

   private void saveModal() {
      String name = this.pendingNickname.trim();
      if (name.length() >= 3 && name.length() <= 16 && name.matches("^[a-zA-Z0-9_]+$")) {
         this.store.add(name);
         this.rebuildCards();
         this.closeModal();
      }
   }

   private String randomNickname() {
      ThreadLocalRandom random = ThreadLocalRandom.current();

      for (int attempt = 0; attempt < 20; attempt++) {
         String prefix = NICK_PREFIXES[random.nextInt(NICK_PREFIXES.length)];
         String suffix = NICK_SUFFIXES[random.nextInt(NICK_SUFFIXES.length)];

         String name = switch (random.nextInt(6)) {
            case 0 -> prefix + suffix + random.nextInt(10, 100);
            case 1 -> prefix + "_" + suffix;
            case 2 -> prefix.toLowerCase(Locale.ROOT) + suffix + random.nextInt(100, 1000);
            case 3 -> prefix.toLowerCase(Locale.ROOT) + "_" + suffix.toLowerCase(Locale.ROOT);
            case 4 -> prefix + suffix;
            default -> suffix + prefix + random.nextInt(10, 100);
         };
         if (name.length() <= 16 && this.cards.stream().noneMatch(card -> card.name().equals(name))) {
            return name;
         }
      }

      return NICK_PREFIXES[random.nextInt(NICK_PREFIXES.length)] + random.nextInt(1000, 10000);
   }
}
