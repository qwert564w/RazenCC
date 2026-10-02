package org.ryzen.feature.impl.misc.autobuy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_465;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.pve.AuctionRelistFeature;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.menu.clickgui.ClickGuiRedactorPage;
import org.ryzen.menu.core.MenuOverlay;
import org.ryzen.menu.core.MenuPage;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AutoBuyFeature extends Feature implements MinecraftContext {
   private static final int[] FT_ANARCHIES = new int[]{101, 102, 103, 104, 105, 201, 202, 203, 204, 205, 301, 302, 303};
   private static final int MAX_BOUGHT_KEYS = 200;
   private static final int MAX_LOT_SLOTS = 45;
   private static final long CONFIRM_WINDOW_MS = 5000L;
   private static final long WALK_TIMEOUT_MS = 5000L;
   private static final Set<String> GENERIC_WORDS = Set.of("на", "с", "к", "и", "ур", "уровень", "уровня", "уровнем");
   public final ModeSetting serverMode = this.register(new ModeSetting("Server", AutoBuyServer.FUNTIME.getId(), AutoBuyServer.ids()));
   public final BooleanSetting autoParse = this.register(new BooleanSetting("Auto Parse", false));
   public final NumberSetting parseDiscount = this.register(
      new NumberSetting("Parse Discount", 20.0, 1.0, 100.0, 1.0, "%").visibleWhen(this.autoParse::getValue)
   );
   public final NumberSetting reparseMinutes = this.register(new NumberSetting("ReParse", 0.0, 0.0, 240.0, 5.0, " min").visibleWhen(this.autoParse::getValue));
   public final NumberSetting updateDelay = this.register(new NumberSetting("Refresh Delay", 350.0, 100.0, 5000.0, 50.0, " ms"));
   public final NumberSetting buyDelay = this.register(new NumberSetting("Buy Delay", 120.0, 50.0, 5000.0, 10.0, " ms"));
   public final NumberSetting buyHoldMs = this.register(new NumberSetting("Buy Hold", 100.0, 40.0, 400.0, 10.0, " ms"));
   public final NumberSetting postBuyPauseMs = this.register(new NumberSetting("Post Buy Pause", 250.0, 50.0, 1500.0, 25.0, " ms"));
   public final NumberSetting confirmDelay = this.register(new NumberSetting("Confirm Delay", 50.0, 0.0, 1000.0, 10.0, " ms"));
   public final BooleanSetting anarchySwap = this.register(
      new BooleanSetting("Anarchy Swap", true).visibleWhen(() -> !AutoBuyServer.isHolyFamily(this.serverMode.getValue()))
   );
   public final NumberSetting anarchyMinSec = this.register(
      new NumberSetting("Anarchy Min", 90.0, 30.0, 600.0, 5.0, " s").visibleWhen(() -> this.anarchySwap.getValue() && this.serverMode.is("FunTime"))
   );
   public final NumberSetting anarchyMaxSec = this.register(
      new NumberSetting("Anarchy Max", 120.0, 40.0, 900.0, 5.0, " s").visibleWhen(() -> this.anarchySwap.getValue() && this.serverMode.is("FunTime"))
   );
   public final BooleanSetting spWalk = this.register(new BooleanSetting("Spooky Walk", true).visibleWhen(() -> this.serverMode.is("SpookyTime")));
   public final NumberSetting spWalkBlocks = this.register(
      new NumberSetting("Walk Distance", 5.0, 2.0, 15.0, 0.5, " b").visibleWhen(() -> this.serverMode.is("SpookyTime") && this.spWalk.getValue())
   );
   public final NumberSetting spWalkIntervalSec = this.register(
      new NumberSetting("Walk Interval", 60.0, 15.0, 300.0, 5.0, " s").visibleWhen(() -> this.serverMode.is("SpookyTime") && this.spWalk.getValue())
   );
   public final BooleanSetting autoOpenAh = this.register(new BooleanSetting("Auto Open AH", true));
   public final NumberSetting ahReopenMs = this.register(
      new NumberSetting("AH Reopen", 1500.0, 500.0, 30000.0, 100.0, " ms").visibleWhen(this.autoOpenAh::getValue)
   );
   public final BooleanSetting notifications = this.register(new BooleanSetting("Notifications", true));
   public final BooleanSetting autoRelist = this.register(new BooleanSetting("Auto Relist", false));
   public final ButtonSetting openEditor = this.register(new ButtonSetting("Open Editor", "Redactor", () -> {
      ClickGuiRedactorPage.requestAutoBuy();
      if (MenuOverlay.isOpen()) {
         MenuOverlay.state().openPage(MenuPage.AUTOBUY);
      }
   }));
   private final Set<String> boughtKeys = new HashSet<>();
   private final List<String> parseQueue = new ArrayList<>();
   private long updateAt;
   private long buyAt;
   private long confirmAt;
   private long ahAt;
   private long anarchyAt;
   private long parseCommandAt;
   private long parseWaitAt;
   private long reparseAt;
   private long nextAnarchyMs;
   private boolean inAuction;
   private boolean relistOwned;
   private AutoBuyFeature.WalkState walkState = AutoBuyFeature.WalkState.IDLE;
   private long nextWalkAtMs;
   private long walkCloseAtMs;
   private long walkMoveAtMs;
   private boolean walkForward = true;
   private double walkStartX;
   private double walkStartZ;
   private AutoBuyFeature.FtSwitch ftSwitch = AutoBuyFeature.FtSwitch.IDLE;
   private long ftPhaseAtMs;
   private int pendingAnarchy = -1;
   private int pendingSlot = -1;
   private String pendingKey = "";
   private String pendingName = "";
   private int pendingPrice = -1;
   private int pendingCount = -1;
   private long pendingSinceMs;
   private long postBuyUntilMs;
   private long lastBuyClickMs;
   private long carriedWarnedAt;
   private boolean parseRunning;
   private boolean parseWaitingResult;
   private int parseIndex;
   private int parseRetries;
   private int parseConfirmClicks;
   private String parseCommand = "";
   private String parseCurrentName = "";
   private int parseUpdatedCount;

   public AutoBuyFeature() {
      super("AutoBuy", "Buys auction lots below your price limits", FeatureCategory.MISC, -1);
   }

   public static AutoBuyFeature get() {
      return FeatureManager.INSTANCE.getFeature(AutoBuyFeature.class);
   }

   private AutoBuyServer server() {
      return AutoBuyServer.of(this.serverMode.getValue());
   }

   private boolean holyLike() {
      return this.server().isHolyFamily();
   }

   public boolean isAutoParseEnabled() {
      return this.autoParse.getValue();
   }

   public int getParseQueueSize() {
      return this.parseQueue.size();
   }

   public String getParseCurrentName() {
      return this.parseCurrentName == null ? "" : this.parseCurrentName;
   }

   public void toggleAutoParse() {
      this.ensureEnabled();
      this.autoParse.setValue(!this.autoParse.getValue());
      if (this.autoParse.getValue()) {
         this.message("AutoParse включён (скидка " + this.parseDiscount.getValue().intValue() + "%)");
         this.startParse(false);
      } else {
         this.stopParse();
         this.message("AutoParse выключен");
      }
   }

   public void startParseNow() {
      this.ensureEnabled();
      if (!this.autoParse.getValue()) {
         this.autoParse.setValue(true);
      }

      this.startParse(false);
   }

   private void ensureEnabled() {
      if (!this.isEnabled()) {
         this.setEnabled(true);
      }
   }

   @Override
   protected void onEnable() {
      AutoBuyManager.get().ensureLoaded();
      if (!this.parseRunning) {
         this.resetBuyRuntime();
      }

      this.nextAnarchyMs = this.randomAnarchyDelay();
      this.anarchyAt = now();
      this.reparseAt = now();
      this.scheduleNextWalk();
      this.walkState = AutoBuyFeature.WalkState.IDLE;
      this.ftSwitch = AutoBuyFeature.FtSwitch.IDLE;
      this.pendingAnarchy = -1;
      this.stopWalkKeys();
      this.syncRelist();
      this.message("AutoBuy включён (" + this.serverMode.getValue() + ")");
   }

   @Override
   protected void onDisable() {
      this.stopParse();
      this.resetBuyRuntime();
      this.stopWalkKeys();
      this.walkState = AutoBuyFeature.WalkState.IDLE;
      this.ftSwitch = AutoBuyFeature.FtSwitch.IDLE;
      this.stopOwnedRelist();
      this.message("AutoBuy выключен");
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.player() != null && this.level() != null && this.gameMode() != null) {
         this.syncRelist();
         if (this.parseRunning) {
            this.tickParse();
         } else if (!this.handleFunTimeAnarchy() && !this.handleSpookyWalk()) {
            if (this.autoParse.getValue()) {
               long reparseMs = this.reparseMinutes.getValue().longValue() * 60000L;
               if (reparseMs > 0L && elapsed(this.reparseAt, reparseMs)) {
                  this.startParse(true);
                  return;
               }
            }

            if (!this.serverMode.is("FunTime")) {
               this.handleAnarchySwap();
            }

            if (this.screen() instanceof class_465<?> container) {
               String title = container.method_25440().getString();
               if (AuctionUtils.isConfirmTitle(title) || this.isLikelyConfirm(container, title)) {
                  this.clearPendingBuy();
                  if (elapsed(this.confirmAt, this.confirmDelay.getValue().longValue())) {
                     this.clickConfirm(container);
                     this.confirmAt = now();
                     this.postBuyUntilMs = now() + 120L;
                     this.lastBuyClickMs = now();
                  }
               } else if (!AuctionUtils.isAuctionTitle(title) && !AuctionUtils.isSearchTitle(title)) {
                  this.inAuction = false;
                  this.clearPendingBuy();
               } else {
                  if (!this.inAuction) {
                     this.inAuction = true;
                     this.boughtKeys.clear();
                     this.clearPendingBuy();
                     this.message("Аукцион открыт. Включено правил: " + AutoBuyManager.get().enabledCount(this.serverMode.getValue()));
                  }

                  long now = now();
                  boolean postBuyFreeze = now < this.postBuyUntilMs;
                  boolean aiming = this.pendingSlot >= 0;
                  if (!postBuyFreeze && !aiming && elapsed(this.updateAt, this.effectiveUpdateDelay())) {
                     this.refreshAuction(container);
                     this.updateAt = now;
                     if (this.boughtKeys.size() > 200) {
                        this.boughtKeys.clear();
                     }
                  }

                  if (!postBuyFreeze) {
                     if (elapsed(this.buyAt, this.effectiveBuyDelay()) && this.scanAndBuy(container)) {
                        this.buyAt = now();
                     }
                  }
               }
            } else {
               this.inAuction = false;
               this.boughtKeys.clear();
               this.clearPendingBuy();
               if (this.autoOpenAh.getValue() && this.screen() == null && !MenuOverlay.isOpen() && elapsed(this.ahAt, this.ahReopenMs.getValue().longValue())) {
                  this.player().field_3944.method_45730("ah");
                  this.ahAt = now();
               }
            }
         }
      }
   }

   private void syncRelist() {
      AuctionRelistFeature relist = FeatureManager.INSTANCE.getFeature(AuctionRelistFeature.class);
      if (relist != null) {
         if (this.autoRelist.getValue()) {
            if (!relist.isEnabled()) {
               relist.setEnabled(true);
               this.relistOwned = true;
            }
         } else if (this.relistOwned) {
            relist.setEnabled(false);
            this.relistOwned = false;
         }
      }
   }

   private void stopOwnedRelist() {
      if (this.relistOwned) {
         AuctionRelistFeature relist = FeatureManager.INSTANCE.getFeature(AuctionRelistFeature.class);
         if (relist != null && relist.isEnabled()) {
            relist.setEnabled(false);
         }

         this.relistOwned = false;
      }
   }

   private void resetBuyRuntime() {
      this.boughtKeys.clear();
      this.inAuction = false;
      this.clearPendingBuy();
      this.postBuyUntilMs = 0L;
      this.lastBuyClickMs = 0L;
      this.carriedWarnedAt = 0L;
      long now = now();
      this.updateAt = now;
      this.buyAt = now;
      this.confirmAt = now;
      this.ahAt = now;
      this.anarchyAt = now;
   }

   private void clearPendingBuy() {
      this.pendingSlot = -1;
      this.pendingKey = "";
      this.pendingName = "";
      this.pendingPrice = -1;
      this.pendingCount = -1;
      this.pendingSinceMs = 0L;
   }

   private void scheduleNextWalk() {
      this.nextWalkAtMs = now() + Math.max(5L, this.spWalkIntervalSec.getValue().longValue()) * 1000L;
   }

   private void stopWalkKeys() {
      if (mc.field_1690 != null) {
         mc.field_1690.field_1894.method_23481(false);
         mc.field_1690.field_1881.method_23481(false);
      }
   }

   private void startParse(boolean reparse) {
      AutoBuyManager.get().ensureLoaded();
      this.parseQueue.clear();
      List<AutoBuyItem> source = AutoBuyManager.get().enabledItems(this.serverMode.getValue());
      if (source.isEmpty()) {
         this.message("AutoParse: нет включённых предметов для " + this.serverMode.getValue());
         this.parseRunning = false;
      } else {
         for (AutoBuyItem item : source) {
            this.parseQueue.add(item.getName());
         }

         this.parseRunning = true;
         this.parseWaitingResult = false;
         this.parseIndex = 0;
         this.parseRetries = 0;
         this.parseConfirmClicks = 0;
         this.parseUpdatedCount = 0;
         this.parseCurrentName = this.parseQueue.get(0);
         this.parseCommandAt = now();
         this.parseWaitAt = now();
         this.reparseAt = now();
         this.closeOpenContainer();
         this.message(
            (reparse ? "AutoParse ReParse: " : "AutoParse старт: ")
               + this.parseQueue.size()
               + " предм., скидка "
               + this.parseDiscount.getValue().intValue()
               + "%"
         );
      }
   }

   private void tickParse() {
      if (!this.parseQueue.isEmpty() && this.parseIndex < this.parseQueue.size()) {
         this.parseCurrentName = this.parseQueue.get(this.parseIndex);
         if (this.parseWaitingResult) {
            if (!(this.screen() instanceof class_465<?> container)) {
               if (elapsed(this.parseWaitAt, 4500L)) {
                  if (++this.parseRetries >= 3) {
                     this.message("AutoParse: " + shortName(this.parseCurrentName) + " пропущен, GUI не открылся  •  /" + this.parseCommand);
                     this.advanceParse();
                  } else {
                     this.parseWaitingResult = false;
                     this.parseCommandAt = now();
                  }
               }
            } else if (elapsed(this.parseWaitAt, 700L)) {
               String title = container.method_25440().getString();
               if (AuctionUtils.isConfirmTitle(title) && this.parseConfirmClicks < 3) {
                  this.parseConfirmClicks++;
                  this.clickConfirm(container);
                  this.parseWaitAt = now();
               } else {
                  boolean usableGui = AuctionUtils.isAuctionTitle(title)
                     || AuctionUtils.isSearchTitle(title)
                     || this.titleLooksLikeSearchFor(title, this.parseCurrentName)
                     || this.hasPricedLots(container);
                  if (!usableGui) {
                     if (elapsed(this.parseWaitAt, 5000L)) {
                        if (++this.parseRetries >= 3) {
                           this.message("AutoParse: " + shortName(this.parseCurrentName) + " пропущен, неверный GUI" + this.guiReport(container, title));
                           this.advanceParse();
                        } else {
                           this.parseWaitingResult = false;
                           this.parseCommandAt = now();
                           this.closeOpenContainer();
                        }
                     }
                  } else {
                     AutoBuyFeature.ParsePrice result = this.findLowestUnitPrice(container, this.parseCurrentName);
                     if (result != null) {
                        int discount = this.parseDiscount.getValue().intValue();
                        long discounted = Math.max(1L, result.unitPrice() * (100L - discount) / 100L);
                        AutoBuyItem item = AutoBuyManager.get().findByName(this.parseCurrentName, this.serverMode.getValue());
                        if (item != null) {
                           AutoBuyManager.get().setBuyPrice(item, (int)Math.min(2147483647L, discounted));
                           this.parseUpdatedCount++;
                           this.message(
                              "AutoParse: "
                                 + shortName(this.parseCurrentName)
                                 + " -> "
                                 + discounted
                                 + "$ (-"
                                 + discount
                                 + "% от "
                                 + result.unitPrice()
                                 + "$/шт)"
                                 + (result.pass() == AutoBuyFeature.MatchPass.EXACT ? "" : "  •  по лоту \"" + shortName(result.lotName()) + "\"")
                           );
                        }

                        this.advanceParse();
                     } else if (elapsed(this.parseWaitAt, 2500L)) {
                        if (++this.parseRetries >= 3) {
                           this.message("AutoParse: " + shortName(this.parseCurrentName) + " не найден на аукционе" + this.guiReport(container, title));
                           this.advanceParse();
                        } else {
                           this.parseWaitingResult = false;
                           this.parseCommandAt = now();
                           this.closeOpenContainer();
                        }
                     }
                  }
               }
            }
         } else if (elapsed(this.parseCommandAt, 1100L)) {
            if (this.isContainerOpen()) {
               this.closeOpenContainer();
               this.parseCommandAt = now();
            } else {
               String query = searchQueryFor(this.parseCurrentName, this.parseRetries);
               this.parseCommand = this.server().searchCommand(query);
               this.player().field_3944.method_45730(this.parseCommand);
               this.parseWaitingResult = true;
               this.parseConfirmClicks = 0;
               this.parseWaitAt = now();
            }
         }
      } else {
         this.finishParse();
      }
   }

   private void advanceParse() {
      this.parseIndex++;
      this.parseRetries = 0;
      this.parseConfirmClicks = 0;
      this.parseWaitingResult = false;
      this.parseCommandAt = now();
      this.parseWaitAt = now();
      this.closeOpenContainer();
      if (this.parseIndex < this.parseQueue.size()) {
         this.parseCurrentName = this.parseQueue.get(this.parseIndex);
      } else {
         this.finishParse();
      }
   }

   private void finishParse() {
      int updated = this.parseUpdatedCount;
      this.stopParse();
      this.reparseAt = now();
      if (this.reparseMinutes.getValue().intValue() <= 0) {
         this.autoParse.setValue(false);
      }

      this.message("AutoParse готов, обновлено позиций: " + updated);
   }

   private void stopParse() {
      this.parseRunning = false;
      this.parseWaitingResult = false;
      this.parseIndex = 0;
      this.parseRetries = 0;
      this.parseConfirmClicks = 0;
      this.parseCurrentName = "";
      this.parseQueue.clear();
      this.parseUpdatedCount = 0;
      this.parseCommandAt = now();
      this.parseWaitAt = now();
   }

   private AutoBuyFeature.ParsePrice findLowestUnitPrice(class_465<?> screen, String targetName) {
      boolean wholeName = this.searchedWholeName(targetName);
      boolean announced = AuctionUtils.isSearchTitle(screen.method_25440().getString())
         || this.titleLooksLikeSearchFor(screen.method_25440().getString(), targetName);

      for (AutoBuyFeature.MatchPass pass : AutoBuyFeature.MatchPass.values()) {
         if (pass != AutoBuyFeature.MatchPass.EXACT && !wholeName || pass == AutoBuyFeature.MatchPass.ANY_LOT && !announced) {
            break;
         }

         AutoBuyFeature.ParsePrice found = this.scanLots(screen, targetName, pass);
         if (found != null) {
            return found;
         }
      }

      return null;
   }

   private AutoBuyFeature.ParsePrice scanLots(class_465<?> screen, String targetName, AutoBuyFeature.MatchPass pass) {
      int lowestUnit = -1;
      int bestLot = -1;
      int bestCount = 1;
      String bestName = "";
      int limit = Math.min(45, screen.method_17577().field_7761.size());

      for (int index = 0; index < limit; index++) {
         class_1799 stack = ((class_1735)screen.method_17577().field_7761.get(index)).method_7677();
         if (!stack.method_7960()) {
            int price = AuctionUtils.getPrice(stack);
            if (price > 0) {
               boolean matches = switch (pass) {
                  case EXACT -> this.matchName(stack, targetName);
                  case LOOSE -> this.looseMatch(stack, targetName);
                  case ANY_LOT -> true;
               };
               if (matches) {
                  int count = Math.max(1, stack.method_7947());
                  int unit = AuctionUtils.unitPrice(price, count);
                  if (unit > 0 && (lowestUnit < 0 || unit < lowestUnit || unit == lowestUnit && price < bestLot)) {
                     lowestUnit = unit;
                     bestLot = price;
                     bestCount = count;
                     bestName = AuctionUtils.stripColors(stack.method_7964().getString()).trim();
                  }
               }
            }
         }
      }

      return lowestUnit <= 0 ? null : new AutoBuyFeature.ParsePrice(lowestUnit, bestLot, bestCount, bestName, pass);
   }

   private int countPricedLots(class_465<?> screen) {
      int count = 0;
      int limit = Math.min(45, screen.method_17577().field_7761.size());

      for (int index = 0; index < limit; index++) {
         class_1799 stack = ((class_1735)screen.method_17577().field_7761.get(index)).method_7677();
         if (!stack.method_7960() && AuctionUtils.getPrice(stack) > 0) {
            count++;
         }
      }

      return count;
   }

   private boolean hasPricedLots(class_465<?> screen) {
      return this.countPricedLots(screen) > 0;
   }

   private boolean titleLooksLikeSearchFor(String title, String itemName) {
      String cleanTitle = AuctionUtils.cleanName(title);
      String cleanItem = AuctionUtils.cleanName(itemName);
      if (cleanTitle.isEmpty() || cleanItem.isEmpty()) {
         return false;
      }

      if (!cleanTitle.contains(cleanItem) && !cleanItem.contains(cleanTitle)) {
         String[] parts = cleanItem.split(" ");
         if (parts.length > 0 && parts[0].length() >= 4 && cleanTitle.contains(parts[0])) {
            return true;
         }

         String search = AuctionUtils.cleanName(searchWord(itemName));
         return !search.isEmpty() && cleanTitle.contains(search);
      } else {
         return true;
      }
   }

   private static String searchQueryFor(String name, int attempt) {
      return attempt >= 2 ? searchPhrase(name) : searchWord(name);
   }

   private static String searchPhrase(String name) {
      return name == null ? "" : name.replaceAll("[★\\[\\]⚒❄\ud83c\udf79]", " ").replaceAll("\\s+", " ").trim();
   }

   private static String searchWord(String name) {
      String phrase = searchPhrase(name);
      if (phrase.isEmpty()) {
         return "";
      }

      String best = "";
      String longest = "";

      for (String word : phrase.split("[\\s\\u00a0]+")) {
         String trimmed = word.replaceAll("^[^\\p{L}\\p{N}]+|[^\\p{L}\\p{N}]+$", "");
         if (trimmed.length() >= longest.length()) {
            longest = trimmed;
         }

         if (!GENERIC_WORDS.contains(trimmed.toLowerCase(Locale.ROOT)) && trimmed.length() >= best.length()) {
            best = trimmed;
         }
      }

      String word = best.isEmpty() ? longest : best;
      return word.length() >= 3 ? word : phrase;
   }

   private boolean searchedWholeName(String name) {
      String query = AuctionUtils.cleanName(searchQueryFor(name, this.parseRetries));
      return !query.isEmpty() && query.equals(AuctionUtils.cleanName(name));
   }

   private String guiReport(class_465<?> container, String title) {
      return "  •  GUI \""
         + shortName(AuctionUtils.stripColors(title).trim())
         + "\", лотов с ценой "
         + this.countPricedLots(container)
         + "/"
         + Math.min(45, container.method_17577().field_7761.size());
   }

   private static String shortName(String name) {
      if (name == null) {
         return "?";
      } else {
         return name.length() > 28 ? name.substring(0, 28) + "..." : name;
      }
   }

   private boolean matchName(class_1799 stack, String target) {
      if (stack != null && !stack.method_7960() && target != null) {
         AutoBuyItem item = AutoBuyManager.get().findByName(target, this.serverMode.getValue());
         if (item != null) {
            return item.matchesName(stack.method_7964().getString());
         }

         String lot = AuctionUtils.cleanName(stack.method_7964().getString());
         String wanted = AuctionUtils.cleanName(target);
         return !lot.isEmpty() && !wanted.isEmpty() && (lot.contains(wanted) || wanted.contains(lot));
      } else {
         return false;
      }
   }

   private boolean looseMatch(class_1799 stack, String target) {
      String lot = AuctionUtils.cleanName(stack.method_7964().getString());
      String wanted = AuctionUtils.cleanName(target);
      if (!lot.isEmpty() && !wanted.isEmpty()) {
         if (!lot.contains(wanted) && !wanted.contains(lot)) {
            for (String token : wanted.split(" ")) {
               if (token.length() >= 4 && lot.contains(token)) {
                  return true;
               }
            }

            return false;
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private void handleAnarchySwap() {
      if (this.anarchySwap.getValue() && !this.parseRunning && !this.holyLike()) {
         if (this.nextAnarchyMs <= 0L) {
            this.nextAnarchyMs = this.randomAnarchyDelay();
         }

         if (elapsed(this.anarchyAt, this.nextAnarchyMs)) {
            int world = ThreadLocalRandom.current().nextInt(1, 101);
            this.closeOpenContainer();
            this.player().field_3944.method_45730("an" + world);
            this.message("Свап анархии -> /an" + world);
            this.nextAnarchyMs = this.randomAnarchyDelay();
            this.anarchyAt = now();
            this.inAuction = false;
            this.boughtKeys.clear();
            this.ahAt = now();
         }
      }
   }

   private boolean handleFunTimeAnarchy() {
      if (this.anarchySwap.getValue() && this.serverMode.is("FunTime") && !this.parseRunning) {
         long now = now();
         switch (this.ftSwitch) {
            case IDLE:
               if (this.nextAnarchyMs <= 0L) {
                  this.nextAnarchyMs = this.randomAnarchyDelay();
                  this.anarchyAt = now;
                  return false;
               } else {
                  if (!elapsed(this.anarchyAt, this.nextAnarchyMs)) {
                     return false;
                  }

                  this.pendingAnarchy = FT_ANARCHIES[ThreadLocalRandom.current().nextInt(FT_ANARCHIES.length)];
                  this.stopWalkKeys();
                  this.closeOpenContainer();
                  this.ftSwitch = AutoBuyFeature.FtSwitch.SEND;
                  this.ftPhaseAtMs = now;
                  return true;
               }
            case SEND:
               if (this.isContainerOpen()) {
                  this.closeOpenContainer();
                  return true;
               }

               this.player().field_3944.method_45730("an" + this.pendingAnarchy);
               this.message("FunTime: анархия -> /an" + this.pendingAnarchy);
               this.ftPhaseAtMs = now;
               this.ftSwitch = AutoBuyFeature.FtSwitch.WAIT;
               return true;
            case WAIT:
               this.closeOpenContainer();
               if (now - this.ftPhaseAtMs < 12000L) {
                  return true;
               }

               this.ftSwitch = AutoBuyFeature.FtSwitch.OPEN_AH;
               return true;
            case OPEN_AH:
               if (this.isContainerOpen()) {
                  this.closeOpenContainer();
                  return true;
               }

               if (this.autoOpenAh.getValue()) {
                  this.player().field_3944.method_45730("ah");
               }

               this.nextAnarchyMs = this.randomAnarchyDelay();
               this.anarchyAt = now;
               this.ftSwitch = AutoBuyFeature.FtSwitch.IDLE;
               this.pendingAnarchy = -1;
               this.inAuction = false;
               this.boughtKeys.clear();
               this.clearPendingBuy();
               return true;
            default:
               this.ftSwitch = AutoBuyFeature.FtSwitch.IDLE;
               return false;
         }
      } else {
         return false;
      }
   }

   private boolean handleSpookyWalk() {
      if (this.spWalk.getValue() && this.serverMode.is("SpookyTime") && !this.parseRunning && !MenuOverlay.isOpen()) {
         long now = now();
         double needed = Math.max(1.0, this.spWalkBlocks.getValue());
         double neededSq = needed * needed;
         switch (this.walkState) {
            case IDLE:
               if (now < this.nextWalkAtMs) {
                  return false;
               } else {
                  if (!(this.screen() instanceof class_465)) {
                     this.scheduleNextWalk();
                     return false;
                  }

                  this.walkForward = !this.walkForward;
                  this.player().method_7346();
                  this.walkCloseAtMs = now;
                  this.walkState = AutoBuyFeature.WalkState.CLOSING;
                  return true;
               }
            case CLOSING:
               if (this.screen() != null) {
                  if (now - this.walkCloseAtMs > 2000L) {
                     this.stopWalkKeys();
                     this.walkState = AutoBuyFeature.WalkState.IDLE;
                     this.scheduleNextWalk();
                  } else {
                     this.closeOpenContainer();
                  }

                  return true;
               }

               this.walkStartX = this.player().method_23317();
               this.walkStartZ = this.player().method_23321();
               this.walkMoveAtMs = now;
               this.pressWalkKey();
               this.walkState = AutoBuyFeature.WalkState.MOVING;
               return true;
            case MOVING:
               double dx = this.player().method_23317() - this.walkStartX;
               double dz = this.player().method_23321() - this.walkStartZ;
               boolean arrived = dx * dx + dz * dz >= neededSq;
               if (!arrived && now - this.walkMoveAtMs < 5000L && this.screen() == null) {
                  this.pressWalkKey();
                  return true;
               }

               this.stopWalkKeys();
               if (!arrived) {
                  this.walkForward = !this.walkForward;
               }

               this.walkState = AutoBuyFeature.WalkState.OPENING;
               return true;
            case OPENING:
               if (this.screen() != null) {
                  return true;
               }

               if (this.autoOpenAh.getValue()) {
                  this.player().field_3944.method_45730("ah");
               }

               this.scheduleNextWalk();
               this.walkState = AutoBuyFeature.WalkState.IDLE;
               return true;
            default:
               this.walkState = AutoBuyFeature.WalkState.IDLE;
               return false;
         }
      } else {
         if (this.walkState != AutoBuyFeature.WalkState.IDLE) {
            this.stopWalkKeys();
            this.walkState = AutoBuyFeature.WalkState.IDLE;
            this.scheduleNextWalk();
         }

         return false;
      }
   }

   private void pressWalkKey() {
      if (mc.field_1690 != null) {
         if (this.walkForward) {
            mc.field_1690.field_1894.method_23481(true);
         } else {
            mc.field_1690.field_1881.method_23481(true);
         }
      }
   }

   private long randomAnarchyDelay() {
      if (this.serverMode.is("FunTime")) {
         long min = Math.max(10L, this.anarchyMinSec.getValue().longValue()) * 1000L;
         long max = Math.max(min + 1000L, this.anarchyMaxSec.getValue().longValue() * 1000L);
         return ThreadLocalRandom.current().nextLong(min, max + 1L);
      } else {
         return ThreadLocalRandom.current().nextLong(300000L, 600001L);
      }
   }

   private boolean isLikelyConfirm(class_465<?> screen, String title) {
      if (now() - this.lastBuyClickMs > 5000L) {
         return false;
      }

      if (!AuctionUtils.isAuctionTitle(title) && !AuctionUtils.isSearchTitle(title)) {
         String normalized = title == null ? "" : title.toLowerCase(Locale.ROOT);
         if (!normalized.contains("инвентарь") && !normalized.contains("inventory")) {
            int slots = this.containerSlotCount(screen);
            return slots > 0 && slots <= 27;
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   private void clickConfirm(class_465<?> screen) {
      int containerId = screen.method_17577().field_7763;
      int slots = this.containerSlotCount(screen);
      if (slots > 0) {
         int confirmSlot = this.findConfirmSlot(screen, slots);
         if (confirmSlot >= 0) {
            this.gameMode().method_2906(containerId, confirmSlot, 0, class_1713.field_7790, this.player());
         } else {
            for (int slot : new int[]{11, 13, 15, 1, 2, 3, 20, 21, 22, 24}) {
               if (slot < slots && !((class_1735)screen.method_17577().field_7761.get(slot)).method_7677().method_7960()) {
                  this.gameMode().method_2906(containerId, slot, 0, class_1713.field_7790, this.player());
                  return;
               }
            }

            if (slots > 1) {
               this.gameMode().method_2906(containerId, 1, 0, class_1713.field_7790, this.player());
            }
         }
      }
   }

   private int findConfirmSlot(class_465<?> screen, int limit) {
      for (int index = 0; index < limit; index++) {
         class_1799 stack = ((class_1735)screen.method_17577().field_7761.get(index)).method_7677();
         if (!stack.method_7960()) {
            String name = AuctionUtils.cleanName(stack.method_7964().getString());
            if (name.contains("купить") || name.contains("подтверд") || name.contains("accept") || name.contains("buy")) {
               return index;
            }

            if (stack.method_31574(class_1802.field_8581)
               || stack.method_31574(class_1802.field_8656)
               || stack.method_31574(class_1802.field_8839)
               || stack.method_31574(class_1802.field_8120)) {
               return index;
            }
         }
      }

      return -1;
   }

   private void refreshAuction(class_465<?> screen) {
      int slots = this.containerSlotCount(screen);
      if (slots > 0) {
         int refreshSlot;
         if (this.holyLike()) {
            refreshSlot = Math.max(9, slots) - 7;
            this.boughtKeys.clear();
         } else {
            refreshSlot = slots - 5;
         }

         if (refreshSlot >= 0 && refreshSlot < slots) {
            this.gameMode().method_2906(screen.method_17577().field_7763, refreshSlot, 0, class_1713.field_7790, this.player());
         }
      }
   }

   private int containerSlotCount(class_465<?> screen) {
      List<class_1735> slots = screen.method_17577().field_7761;

      for (int index = 0; index < slots.size(); index++) {
         if (slots.get(index).field_7871 == this.player().method_31548()) {
            return index;
         }
      }

      return slots.size();
   }

   private boolean isContainerOpen() {
      return this.screen() instanceof class_465;
   }

   private void closeOpenContainer() {
      if (this.player() != null && this.isContainerOpen()) {
         this.player().method_7346();
      }
   }

   private boolean scanAndBuy(class_465<?> screen) {
      List<AutoBuyItem> enabled = AutoBuyManager.get().enabledItems(this.serverMode.getValue());
      if (enabled.isEmpty()) {
         this.clearPendingBuy();
         return false;
      }

      if (!this.player().field_7512.method_34255().method_7960()) {
         this.clearPendingBuy();
         if (elapsed(this.carriedWarnedAt, 15000L)) {
            this.carriedWarnedAt = now();
            this.message("на курсоре предмет, покупка на паузе");
         }

         return false;
      } else {
         int containerId = screen.method_17577().field_7763;
         int limit = Math.min(this.containerSlotCount(screen), 45);
         AutoBuyFeature.BuyCandidate best = null;

         for (int index = 0; index < limit; index++) {
            class_1735 slot = (class_1735)screen.method_17577().field_7761.get(index);
            class_1799 stack = slot.method_7677();
            if (!stack.method_7960()) {
               int price = AuctionUtils.getPrice(stack);
               if (price > 0) {
                  int count = Math.max(1, stack.method_7947());
                  int unit = AuctionUtils.unitPrice(price, count);
                  String name = stack.method_7964().getString();
                  String key = index + "|" + price + "|" + count + "|" + name.hashCode();
                  if (!this.boughtKeys.contains(key)) {
                     AutoBuyItem matched = null;
                     int matchedScore = 0;

                     for (AutoBuyItem item : enabled) {
                        int score = item.matchScore(name);
                        if (score > 0 && score > matchedScore && count >= item.getMinQty() && unit <= item.getBuyPrice()) {
                           matchedScore = score;
                           matched = item;
                        }
                     }

                     if (matched != null
                        && (
                           best == null
                              || unit < best.unit()
                              || unit == best.unit() && matchedScore > best.matchScore()
                              || unit == best.unit() && matchedScore == best.matchScore() && price < best.price()
                        )) {
                        best = new AutoBuyFeature.BuyCandidate(index, key, matched.getName(), price, count, unit, matchedScore);
                     }
                  }
               }
            }
         }

         long now = now();
         if (best == null) {
            this.clearPendingBuy();
            return false;
         }

         if (this.pendingSlot != best.slot() || !this.pendingKey.equals(best.key())) {
            this.pendingSlot = best.slot();
            this.pendingKey = best.key();
            this.pendingName = best.ruleName();
            this.pendingPrice = best.price();
            this.pendingCount = best.count();
            this.pendingSinceMs = now;
            return false;
         }

         if (this.pendingSlot >= 0 && this.pendingSlot < limit) {
            class_1799 live = ((class_1735)screen.method_17577().field_7761.get(this.pendingSlot)).method_7677();
            if (live.method_7960()) {
               this.clearPendingBuy();
               return false;
            } else {
               int livePrice = AuctionUtils.getPrice(live);
               int liveCount = Math.max(1, live.method_7947());
               if (livePrice != this.pendingPrice || liveCount != this.pendingCount) {
                  this.pendingKey = this.pendingSlot + "|" + livePrice + "|" + liveCount + "|" + live.method_7964().getString().hashCode();
                  this.pendingPrice = livePrice;
                  this.pendingCount = liveCount;
                  this.pendingSinceMs = now;
                  return false;
               } else if (livePrice <= 0) {
                  this.clearPendingBuy();
                  return false;
               } else {
                  long hold = this.holyLike() ? Math.min(60L, this.buyHoldMs.getValue().longValue()) : this.buyHoldMs.getValue().longValue();
                  if (now - this.pendingSinceMs >= hold && now - this.lastBuyClickMs >= 40L) {
                     boolean holy = this.holyLike();
                     this.gameMode().method_2906(containerId, this.pendingSlot, 0, holy ? class_1713.field_7790 : class_1713.field_7794, this.player());
                     this.boughtKeys.add(this.pendingKey);
                     this.lastBuyClickMs = now;
                     this.postBuyUntilMs = now
                        + (holy ? Math.max(this.postBuyPauseMs.getValue().longValue(), 650L) : this.postBuyPauseMs.getValue().longValue());
                     this.message("Покупка: " + this.pendingName + " x" + this.pendingCount + " за " + this.pendingPrice + "$ (" + best.unit() + "$/шт)");
                     this.clearPendingBuy();
                     this.updateAt = now;
                     return true;
                  } else {
                     return false;
                  }
               }
            }
         } else {
            this.clearPendingBuy();
            return false;
         }
      }
   }

   private long effectiveUpdateDelay() {
      long base = this.updateDelay.getValue().longValue();
      return this.holyLike() ? Math.max(50L, Math.min(base, 450L)) : base + ThreadLocalRandom.current().nextLong(0L, 121L);
   }

   private long effectiveBuyDelay() {
      long base = this.buyDelay.getValue().longValue();
      return this.holyLike() ? Math.max(40L, base) : Math.max(50L, base);
   }

   private void message(String text) {
      if (this.notifications.getValue()) {
         ChatUtil.info("AutoBuy: " + text);
      }
   }

   private static long now() {
      return System.currentTimeMillis();
   }

   private static boolean elapsed(long since, long delayMs) {
      return now() - since >= delayMs;
   }

   @Generated
   public boolean isParseRunning() {
      return this.parseRunning;
   }

   @Generated
   public int getParseIndex() {
      return this.parseIndex;
   }

   @Environment(EnvType.CLIENT)
   private record BuyCandidate(int slot, String key, String ruleName, int price, int count, int unit, int matchScore) {
   }

   @Environment(EnvType.CLIENT)
   private enum FtSwitch {
      IDLE,
      SEND,
      WAIT,
      OPEN_AH;
   }

   @Environment(EnvType.CLIENT)
   private enum MatchPass {
      EXACT,
      LOOSE,
      ANY_LOT;
   }

   @Environment(EnvType.CLIENT)
   private record ParsePrice(int unitPrice, int lotPrice, int count, String lotName, AutoBuyFeature.MatchPass pass) {
   }

   @Environment(EnvType.CLIENT)
   private enum WalkState {
      IDLE,
      CLOSING,
      MOVING,
      OPENING;
   }
}
