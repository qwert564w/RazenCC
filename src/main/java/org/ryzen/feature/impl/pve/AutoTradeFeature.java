package org.ryzen.feature.impl.pve;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalLong;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1646;
import net.minecraft.class_1703;
import net.minecraft.class_1707;
import net.minecraft.class_1713;
import net.minecraft.class_1714;
import net.minecraft.class_1728;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1914;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_243;
import net.minecraft.class_2863;
import net.minecraft.class_310;
import net.minecraft.class_3852;
import net.minecraft.class_3965;
import net.minecraft.class_746;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.lifecycle.DisconnectEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveAutomationCoordinator;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.PveStateMachine;
import org.ryzen.pve.economy.AuctionPriceScanner;
import org.ryzen.pve.economy.CommandCooldown;
import org.ryzen.pve.economy.CraftingMenuController;
import org.ryzen.pve.economy.EconomyAutomationPolicy;
import org.ryzen.pve.economy.EconomyChat;
import org.ryzen.pve.economy.EconomyCommands;
import org.ryzen.pve.economy.EconomyInventory;
import org.ryzen.pve.economy.EconomyItemText;
import org.ryzen.pve.economy.EconomyMenus;
import org.ryzen.pve.economy.EconomyNavigator;
import org.ryzen.pve.economy.EconomyTextParser;
import org.ryzen.pve.economy.NearbyEconomyBlocks;
import org.ryzen.pve.navigation.BaritoneNavigator;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerAdapters;
import org.ryzen.pve.server.ServerProfile;
import org.ryzen.utils.inventory.ContainerLootService;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class AutoTradeFeature extends PveFeature {
   private static final long MOVE_TIMEOUT_TICKS = 240L;
   private static final long OPEN_TIMEOUT_TICKS = 100L;
   private static final long AUCTION_RETRY_TICKS = 1200L;
   public final BooleanSetting buyEmeralds = this.register(new BooleanSetting("Buy Emeralds", true));
   public final NumberSetting emeraldReserve = this.register(new NumberSetting("Emerald Reserve", 192.0, 64.0, 640.0, 64.0, ""));
   public final NumberSetting scanRadius = this.register(new NumberSetting("Villager Radius", 16.0, 8.0, 28.0, 1.0, ""));
   public final BooleanSetting depositGold = this.register(new BooleanSetting("Store Gold", true));
   public final NumberSetting chestScanRadius = this.register(new NumberSetting("Chest Radius", 16.0, 4.0, 32.0, 1.0, ""));
   public final TextSetting chestKeyword = this.register(new TextSetting("Chest Sign", "золото", 32).visibleWhen(this.depositGold::getValue));
   public final BooleanSetting autoSellBlocks = this.register(new BooleanSetting("Sell Blocks", true));
   public final BooleanSetting craftBlocks = this.register(new BooleanSetting("Craft Blocks", true));
   public final TextSetting auctionQuery = this.register(new TextSetting("Auction Search", "золотой блок", 40).visibleWhen(this.autoSellBlocks::getValue));
   public final NumberSetting restockCheck = this.register(new NumberSetting("Restock Check", 180.0, 30.0, 600.0, 10.0, " s"));
   private final PveStateMachine<AutoTradeFeature.State> machine = new PveStateMachine<>(AutoTradeFeature.State.WAIT);
   private final EconomyNavigator navigator = new EconomyNavigator(BaritoneNavigator.INSTANCE);
   private final CraftingMenuController crafting = new CraftingMenuController();
   private final CommandCooldown actionCooldown = new CommandCooldown();
   private final CommandCooldown commandCooldown = new CommandCooldown();
   private final Map<UUID, Long> exhaustedVillagers = new HashMap<>();
   private long lastTick;
   private long nextWorkTick;
   private long shopRetryTick;
   private long auctionRetryTick;
   private boolean resourcesClaimed;
   private boolean moneyDry;
   private class_1646 targetVillager;
   private class_2338 targetBlock;
   private int ownedContainerId = -1;
   private int selectedOffer = -1;
   private long offerSelectedTick;
   private int tradeActions;
   private int shopEmeraldBefore;
   private int depositActions;
   private int saleCount;
   private long salePrice;
   private int originalSelectedSlot = -1;
   private int saleSwapMenuSlot = -1;
   private int saleHotbarSlot = -1;
   private boolean saleConfirmed;
   private boolean saleRejected;

   public AutoTradeFeature() {
      super("AutoTrade", "Buys emeralds, trades with clerics, and processes the resulting gold", -1, AutomationPriority.FEATURE);
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         long tick = client.field_1687.method_75260();
         this.lastTick = tick;
         if (ServerAdapters.current().profile() == ServerProfile.FUNTIME) {
            this.exhaustedVillagers.entrySet().removeIf(entry -> tick >= entry.getValue());
            switch ((AutoTradeFeature.State)this.machine.state()) {
               case WAIT:
                  if (tick >= this.nextWorkTick) {
                     this.machine.transition(AutoTradeFeature.State.SELECT, tick);
                  }
                  break;
               case SELECT:
                  this.selectWork(client, player, tick);
                  break;
               case OPEN_SHOP:
                  this.openShop(client, player, tick);
                  break;
               case WAIT_SHOP_MENU:
                  this.waitShopMenu(client, player, tick);
                  break;
               case BUY_SHOP:
                  this.buyFromShop(client, player, tick);
                  break;
               case FIND_VILLAGER:
                  this.findVillager(client, player, tick);
                  break;
               case MOVE_VILLAGER:
                  this.moveVillager(player, tick);
                  break;
               case OPEN_TRADE:
                  this.openTrade(client, player, tick);
                  break;
               case TRADE:
                  this.trade(client, player, tick);
                  break;
               case FIND_TABLE:
                  this.findTable(client, player, tick);
                  break;
               case MOVE_TABLE:
                  this.moveBlock(player, AutoTradeFeature.State.OPEN_TABLE, tick);
                  break;
               case OPEN_TABLE:
                  this.openTable(client, player, tick);
                  break;
               case CRAFT_BLOCKS:
                  this.craftBlocks(client, tick);
                  break;
               case FIND_DEPOSIT:
                  this.findDeposit(client, player, tick);
                  break;
               case MOVE_DEPOSIT:
                  this.moveBlock(player, AutoTradeFeature.State.OPEN_DEPOSIT, tick);
                  break;
               case OPEN_DEPOSIT:
                  this.openDeposit(client, player, tick);
                  break;
               case DEPOSIT:
                  this.deposit(client, player, tick);
                  break;
               case OPEN_AUCTION_SEARCH:
                  this.openAuctionSearch(client, player, tick);
                  break;
               case WAIT_AUCTION_SEARCH:
                  this.waitAuctionSearch(client, tick);
                  break;
               case LIST_AUCTION:
                  this.listAuction(client, player, tick);
                  break;
               case WAIT_SALE_CONFIRMATION:
                  this.waitSaleConfirmation(client, tick);
            }
         } else {
            if (this.resourcesClaimed || !this.machine.is(AutoTradeFeature.State.WAIT) || this.ownedContainerId >= 0) {
               this.finishCycle(client, tick, 20L);
            }
         }
      }
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE) {
         String text = EconomyChat.incomingText(event.getPacket());
         if (text != null) {
            String normalized = EconomyTextParser.normalize(text);
            class_310.method_1551().execute(() -> this.handleChat(normalized));
         }
      }
   }

   @EventTarget
   public void onDisconnect(DisconnectEvent event) {
      this.resetRuntime(false);
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntime(false);
   }

   @Override
   protected void onPveDisable() {
      this.resetRuntime(true);
   }

   @Override
   protected void onPvePreempted(PveAutomationCoordinator.RevocationReason reason) {
      this.resetRuntime(true);
   }

   private void selectWork(class_310 client, class_746 player, long tick) {
      if (client.field_1755 != null || player.field_7512 != player.field_7498) {
         this.finishCycle(client, tick, 20L);
      } else if (this.ensureCycleResources(tick)) {
         int emeralds = EconomyInventory.count(player, class_1802.field_8687);
         int ingots = EconomyInventory.count(player, class_1802.field_8695);
         int blocks = EconomyInventory.count(player, class_1802.field_8494);
         switch (EconomyAutomationPolicy.tradeAction(
            emeralds,
            ingots,
            blocks,
            Math.toIntExact(Math.round(this.emeraldReserve.getValue())),
            this.buyEmeralds.getValue(),
            this.depositGold.getValue(),
            this.craftBlocks.getValue(),
            this.autoSellBlocks.getValue(),
            this.moneyDry,
            tick >= this.shopRetryTick,
            tick >= this.auctionRetryTick,
            largestStackCount(player, class_1802.field_8494) >= 64
         )) {
            case SELL_BLOCKS:
               this.machine.transition(AutoTradeFeature.State.OPEN_AUCTION_SEARCH, tick);
               break;
            case CRAFT_BLOCKS:
               this.machine.transition(AutoTradeFeature.State.FIND_TABLE, tick);
               break;
            case DEPOSIT_GOLD:
               this.machine.transition(AutoTradeFeature.State.FIND_DEPOSIT, tick);
               break;
            case TRADE:
               this.machine.transition(AutoTradeFeature.State.FIND_VILLAGER, tick);
               break;
            case BUY_EMERALDS:
               this.machine.transition(AutoTradeFeature.State.OPEN_SHOP, tick);
               break;
            case WAIT:
               this.finishCycle(client, tick, 40L);
         }
      }
   }

   private void openShop(class_310 client, class_746 player, long tick) {
      if (this.commandCooldown.tryAcquire(tick, 40L)) {
         ServerAdapters.current().sendCommand(player, EconomyCommands.shop());
         this.machine.transition(AutoTradeFeature.State.WAIT_SHOP_MENU, tick);
      }
   }

   private void waitShopMenu(class_310 client, class_746 player, long tick) {
      if (this.machine.ticksInState(tick) > 100L) {
         this.finishCycle(client, tick, 100L);
      } else if (EconomyMenus.titleContains(client, "магазин", "shop") && player.field_7512 != player.field_7498) {
         class_1703 menu = player.field_7512;
         int emerald = EconomyMenus.findContainerSlot(
            menu, stack -> stack.method_31574(class_1802.field_8687) || EconomyItemText.containsAny(stack, "изумруд", "emerald")
         );
         if (emerald >= 0) {
            this.ownedContainerId = menu.field_7763;
            this.shopEmeraldBefore = EconomyInventory.count(player, class_1802.field_8687);
            if (EconomyMenus.click(client, menu, emerald, 0, class_1713.field_7790)) {
               this.actionCooldown.tryAcquire(tick, 8L);
               this.machine.transition(AutoTradeFeature.State.BUY_SHOP, tick);
            }
         }
      }
   }

   private void buyFromShop(class_310 client, class_746 player, long tick) {
      int emeralds = EconomyInventory.count(player, class_1802.field_8687);
      if (emeralds >= Math.round(this.emeraldReserve.getValue())) {
         this.moneyDry = false;
         this.closeOwned(client);
         this.machine.transition(AutoTradeFeature.State.SELECT, tick);
      } else {
         if (emeralds > this.shopEmeraldBefore) {
            this.shopEmeraldBefore = emeralds;
            this.actionCooldown.defer(tick, 8L);
         }

         if (this.machine.ticksInState(tick) <= 200L && player.field_7512 != player.field_7498) {
            if (this.actionCooldown.ready(tick) && EconomyMenus.titleContains(client, "магазин", "shop")) {
               class_1703 menu = player.field_7512;
               int buy = EconomyMenus.findContainerSlot(menu, stack -> EconomyItemText.containsAny(stack, "купить", "buy", "приобрести"));
               if (buy >= 0) {
                  this.ownedContainerId = menu.field_7763;
                  if (EconomyMenus.click(client, menu, buy, 0, class_1713.field_7790)) {
                     this.actionCooldown.tryAcquire(tick, 10L);
                  }
               }
            }
         } else {
            this.finishCycle(client, tick, 100L);
         }
      }
   }

   private void findVillager(class_310 client, class_746 player, long tick) {
      double radius = this.scanRadius.getValue();
      this.targetVillager = client.field_1687
         .method_8390(
            class_1646.class,
            player.method_5829().method_1014(radius),
            villager -> villager.method_5805()
               && villager.method_7231().comp_3521().method_40225(class_3852.field_17055)
               && !this.exhaustedVillagers.containsKey(villager.method_5667())
         )
         .stream()
         .min(Comparator.comparingDouble(player::method_5858))
         .orElse(null);
      if (this.targetVillager != null) {
         this.machine.transition(AutoTradeFeature.State.MOVE_VILLAGER, tick);
      } else {
         if (!this.depositGold.getValue()
            || EconomyInventory.count(player, class_1802.field_8695) <= 0 && EconomyInventory.count(player, class_1802.field_8494) <= 0) {
            this.finishCycle(client, tick, 100L);
         } else {
            this.machine.transition(AutoTradeFeature.State.FIND_DEPOSIT, tick);
         }
      }
   }

   private void moveVillager(class_746 player, long tick) {
      if (this.targetVillager != null && this.targetVillager.method_5805()) {
         if (player.method_5858(this.targetVillager) <= 10.25) {
            this.navigator.cancel();
            this.machine.transition(AutoTradeFeature.State.OPEN_TRADE, tick);
         } else {
            this.navigator.moveTo(player, this.targetVillager.method_24515(), 2);
            if (this.machine.ticksInState(tick) > 240L) {
               this.markVillagerExhausted(tick);
               this.machine.transition(AutoTradeFeature.State.FIND_VILLAGER, tick);
            }
         }
      } else {
         this.machine.transition(AutoTradeFeature.State.FIND_VILLAGER, tick);
      }
   }

   private void openTrade(class_310 client, class_746 player, long tick) {
      if (player.field_7512 instanceof class_1728 menu) {
         this.ownedContainerId = menu.field_7763;
         this.selectedOffer = -1;
         this.tradeActions = 0;
         this.machine.transition(AutoTradeFeature.State.TRADE, tick);
      } else if (this.targetVillager == null || !this.targetVillager.method_5805() || this.machine.ticksInState(tick) > 100L) {
         this.markVillagerExhausted(tick);
         this.machine.transition(AutoTradeFeature.State.FIND_VILLAGER, tick);
      } else if (this.actionCooldown.tryAcquire(tick, 10L)) {
         client.field_1761.method_2905(player, this.targetVillager, class_1268.field_5808);
         player.method_6104(class_1268.field_5808);
      }
   }

   private void trade(class_310 client, class_746 player, long tick) {
      if (!(player.field_7512 instanceof class_1728 menu && menu.field_7763 == this.ownedContainerId)) {
         this.finishCycle(client, tick, 60L);
      } else if (this.tradeActions >= 128) {
         this.closeOwned(client);
         this.machine.transition(AutoTradeFeature.State.SELECT, tick);
      } else if (this.selectedOffer >= 0) {
         if (tick - this.offerSelectedTick >= 3L) {
            class_1799 result = menu.method_7611(2).method_7677();
            if (isGold(result)) {
               if (EconomyMenus.quickMove(client, menu, 2)) {
                  this.tradeActions++;
                  this.selectedOffer = -1;
                  this.actionCooldown.defer(tick, 3L);
               }
            } else if (tick - this.offerSelectedTick > 40L) {
               this.markVillagerExhausted(tick);
               this.closeOwned(client);
               this.machine.transition(AutoTradeFeature.State.SELECT, tick);
            }
         }
      } else if (this.actionCooldown.ready(tick)) {
         int offer = bestOffer(menu, EconomyInventory.count(player, class_1802.field_8687));
         if (offer < 0) {
            this.markVillagerExhausted(tick);
            this.closeOwned(client);
            this.machine.transition(AutoTradeFeature.State.SELECT, tick);
         } else {
            menu.method_7650(offer);
            menu.method_20215(offer);
            player.field_3944.method_52787(new class_2863(offer));
            this.selectedOffer = offer;
            this.offerSelectedTick = tick;
         }
      }
   }

   private static int bestOffer(class_1728 menu, int emeralds) {
      int best = -1;
      double bestValue = 0.0;

      for (int index = 0; index < menu.method_17438().size(); index++) {
         class_1914 offer = (class_1914)menu.method_17438().get(index);
         int cost = emeraldCost(offer);
         int gold = goldValue(offer.method_8250());
         if (!offer.method_8255() && cost > 0 && cost <= emeralds && gold > 0) {
            double value = (double)gold / cost;
            if (value > bestValue) {
               bestValue = value;
               best = index;
            }
         }
      }

      return best;
   }

   private void findTable(class_310 client, class_746 player, long tick) {
      this.targetBlock = NearbyEconomyBlocks.nearestCraftingTable(client.field_1687, player, Math.round(this.chestScanRadius.getValue().floatValue()));
      if (this.targetBlock == null) {
         this.finishCycle(client, tick, 100L);
      } else {
         this.machine.transition(AutoTradeFeature.State.MOVE_TABLE, tick);
      }
   }

   private void findDeposit(class_310 client, class_746 player, long tick) {
      this.targetBlock = NearbyEconomyBlocks.nearestSignedChest(
         client.field_1687, player, Math.round(this.chestScanRadius.getValue().floatValue()), this.chestKeyword.getValue()
      );
      if (this.targetBlock == null) {
         this.finishCycle(client, tick, 100L);
      } else {
         this.machine.transition(AutoTradeFeature.State.MOVE_DEPOSIT, tick);
      }
   }

   private void moveBlock(class_746 player, AutoTradeFeature.State next, long tick) {
      if (this.targetBlock == null) {
         this.finishCycle(class_310.method_1551(), tick, 100L);
      } else if (EconomyNavigator.arrived(player, this.targetBlock, 3.7)) {
         this.navigator.cancel();
         this.machine.transition(next, tick);
      } else {
         this.navigator.moveTo(player, this.targetBlock, 3);
         if (this.machine.ticksInState(tick) > 240L) {
            this.finishCycle(class_310.method_1551(), tick, 100L);
         }
      }
   }

   private void openTable(class_310 client, class_746 player, long tick) {
      if (player.field_7512 instanceof class_1714 menu) {
         this.ownedContainerId = menu.field_7763;
         this.crafting.reset();
         this.machine.transition(AutoTradeFeature.State.CRAFT_BLOCKS, tick);
      } else if (this.machine.ticksInState(tick) > 100L) {
         this.finishCycle(client, tick, 100L);
      } else {
         this.interactBlock(client, player, tick);
      }
   }

   private void openDeposit(class_310 client, class_746 player, long tick) {
      if (player.field_7512 instanceof class_1707 menu) {
         this.ownedContainerId = menu.field_7763;
         this.depositActions = 0;
         this.machine.transition(AutoTradeFeature.State.DEPOSIT, tick);
      } else if (this.machine.ticksInState(tick) > 100L) {
         this.finishCycle(client, tick, 100L);
      } else {
         this.interactBlock(client, player, tick);
      }
   }

   private void interactBlock(class_310 client, class_746 player, long tick) {
      if (this.targetBlock != null && this.actionCooldown.tryAcquire(tick, 10L)) {
         client.field_1761
            .method_2896(
               player, class_1268.field_5808, new class_3965(class_243.method_24953(this.targetBlock), class_2350.field_11036, this.targetBlock, false)
            );
         player.method_6104(class_1268.field_5808);
      }
   }

   private void craftBlocks(class_310 client, long tick) {
      if (!(client.field_1724.field_7512 instanceof class_1714 menu && menu.field_7763 == this.ownedContainerId)) {
         this.finishCycle(client, tick, 60L);
      } else if (this.actionCooldown.ready(tick)) {
         CraftingMenuController.Result result = this.crafting.tick(client, menu, CraftingMenuController.Recipe.GOLD_BLOCK);
         this.actionCooldown.tryAcquire(tick, 2L);
         if (result == CraftingMenuController.Result.CRAFTED) {
            this.closeOwned(client);
            this.machine.transition(AutoTradeFeature.State.SELECT, tick);
         } else if (result == CraftingMenuController.Result.FAILED) {
            this.crafting.cleanup(client, menu);
            this.finishCycle(client, tick, 100L);
         }
      }
   }

   private void deposit(class_310 client, class_746 player, long tick) {
      if (!(player.field_7512 instanceof class_1707 menu && menu.field_7763 == this.ownedContainerId)) {
         this.finishCycle(client, tick, 60L);
      } else if (this.actionCooldown.ready(tick)) {
         int firstPlayerSlot = ContainerLootService.containerSlotCount(menu);
         int goldSlot = -1;

         for (int slotId = firstPlayerSlot; slotId < menu.field_7761.size(); slotId++) {
            if (menu.method_40442(slotId) && isGold(menu.method_7611(slotId).method_7677())) {
               goldSlot = slotId;
               break;
            }
         }

         if (goldSlot >= 0 && this.depositActions++ < 24) {
            if (EconomyMenus.quickMove(client, menu, goldSlot)) {
               this.actionCooldown.tryAcquire(tick, 3L);
            }
         } else {
            this.moneyDry = false;
            this.closeOwned(client);
            this.finishCycle(client, tick, 100L);
         }
      }
   }

   private void openAuctionSearch(class_310 client, class_746 player, long tick) {
      if (this.commandCooldown.ready(tick)) {
         ServerAdapter adapter = ServerAdapters.current();
         adapter.auctionCommand().flatMap(root -> EconomyCommands.auctionSearch(root, this.auctionQuery.getValue())).ifPresentOrElse(command -> {
            this.saleCount = largestStackCount(player, class_1802.field_8494);
            if (this.saleCount <= 0) {
               this.finishCycle(client, tick, 40L);
            } else {
               adapter.sendCommand(player, command);
               this.commandCooldown.tryAcquire(tick, 40L);
               this.machine.transition(AutoTradeFeature.State.WAIT_AUCTION_SEARCH, tick);
            }
         }, () -> this.finishCycle(client, tick, 100L));
      }
   }

   private void waitAuctionSearch(class_310 client, long tick) {
      if (this.machine.ticksInState(tick) > 100L) {
         this.finishCycle(client, tick, 100L);
      } else if (EconomyMenus.titleContains(client, "аукцион", "auction")) {
         class_1703 menu = client.field_1724.field_7512;
         this.ownedContainerId = menu.field_7763;
         OptionalLong price = AuctionPriceScanner.competitivePrice(menu, class_1802.field_8494, this.auctionQuery.getValue(), this.saleCount);
         if (!price.isEmpty()) {
            this.salePrice = price.getAsLong();
            this.closeOwned(client);
            this.machine.transition(AutoTradeFeature.State.LIST_AUCTION, tick);
         }
      }
   }

   private void listAuction(class_310 client, class_746 player, long tick) {
      if (client.field_1755 == null && this.commandCooldown.ready(tick) && this.prepareSaleStack(player)) {
         ServerAdapter adapter = ServerAdapters.current();
         adapter.auctionCommand().flatMap(root -> EconomyCommands.auctionSell(root, this.salePrice)).ifPresentOrElse(command -> {
            adapter.sendCommand(player, command);
            this.commandCooldown.tryAcquire(tick, 100L);
            this.saleConfirmed = false;
            this.saleRejected = false;
            this.machine.transition(AutoTradeFeature.State.WAIT_SALE_CONFIRMATION, tick);
         }, () -> this.finishCycle(client, tick, 100L));
      } else {
         if (this.machine.ticksInState(tick) > 100L) {
            this.finishCycle(client, tick, 100L);
         }
      }
   }

   private void waitSaleConfirmation(class_310 client, long tick) {
      if (this.saleConfirmed || this.saleRejected || this.machine.ticksInState(tick) > 100L) {
         this.finishCycle(client, tick, this.saleRejected ? 1200L : 40L);
      }
   }

   private boolean prepareSaleStack(class_746 player) {
      if (this.saleCount < 64) {
         return false;
      }

      class_1799 mainHand = player.method_6047();
      if (mainHand.method_31574(class_1802.field_8494) && mainHand.method_7947() == this.saleCount) {
         return true;
      }

      int menuSlot = InventoryUtil.findPlayerMenuSlot(player, stack -> stack.method_31574(class_1802.field_8494) && stack.method_7947() == this.saleCount);
      if (menuSlot >= 0 && player.field_7512 == player.field_7498) {
         if (this.originalSelectedSlot < 0) {
            this.originalSelectedSlot = player.method_31548().method_67532();
         }

         if (menuSlot >= 36 && menuSlot <= 44) {
            this.saleHotbarSlot = menuSlot - 36;
            return EconomyInventory.selectHotbar(player, this.saleHotbarSlot);
         } else {
            this.saleHotbarSlot = player.method_31548().method_67532();
            this.saleSwapMenuSlot = menuSlot;
            return InventoryUtil.swapWithHotbar(menuSlot, this.saleHotbarSlot);
         }
      } else {
         return false;
      }
   }

   private void restoreSaleStack(class_746 player) {
      if (player != null && player.field_7512 == player.field_7498) {
         if (this.saleSwapMenuSlot >= 0 && this.saleHotbarSlot >= 0) {
            InventoryUtil.swapWithHotbar(this.saleSwapMenuSlot, this.saleHotbarSlot);
         }

         if (this.originalSelectedSlot >= 0) {
            EconomyInventory.selectHotbar(player, this.originalSelectedSlot);
         }
      }

      this.originalSelectedSlot = -1;
      this.saleSwapMenuSlot = -1;
      this.saleHotbarSlot = -1;
   }

   private void handleChat(String text) {
      if (this.isEnabled()) {
         if (EconomyTextParser.containsAny(text, "недостаточно денег", "не хватает денег", "недостаточно средств", "insufficient funds", "not enough money")) {
            long requested = EconomyTextParser.largestAmount(text).orElse(100000L);
            SynchronizationFeature.requestMoney((int)Math.min(2147483647L, requested));
            this.moneyDry = true;
            this.shopRetryTick = this.lastTick + 200L;
            this.closeOwned(class_310.method_1551());
            this.machine.transition(AutoTradeFeature.State.SELECT, this.lastTick);
         } else {
            if (EconomyTextParser.containsAny(text, "не удалось выставить", "хранилищ", "слот", "ah rent", "auction slots are full")) {
               this.saleRejected = true;
               this.auctionRetryTick = this.lastTick + 1200L;
            } else if (EconomyTextParser.containsAny(text, "выставлен на продажу", "listed for sale")) {
               this.saleConfirmed = true;
               this.auctionRetryTick = 0L;
            }
         }
      }
   }

   private void markVillagerExhausted(long tick) {
      if (this.targetVillager != null) {
         long delay = Math.round(this.restockCheck.getValue() * 20.0);
         this.exhaustedVillagers.put(this.targetVillager.method_5667(), tick + Math.max(1L, delay));
      }

      this.targetVillager = null;
   }

   private boolean ensureCycleResources(long tick) {
      if (this.resourcesClaimed) {
         return true;
      } else if (!this.claim(
         AutomationResource.MOVEMENT, AutomationResource.NAVIGATION, AutomationResource.INVENTORY, AutomationResource.SCREEN, AutomationResource.CHAT
      )) {
         this.nextWorkTick = tick + 5L;
         return false;
      } else {
         this.resourcesClaimed = true;
         return true;
      }
   }

   private void closeOwned(class_310 client) {
      if (client.field_1724 == null
         || !(client.field_1724.field_7512 instanceof class_1714 menu && menu.field_7763 == this.ownedContainerId && !this.crafting.cleanup(client, menu))) {
         EconomyMenus.closeOwned(client, this.ownedContainerId);
         this.ownedContainerId = -1;
      }
   }

   private void finishCycle(class_310 client, long tick, long delayTicks) {
      this.closeOwned(client);
      this.navigator.close();
      this.restoreSaleStack(client.field_1724);
      this.targetVillager = null;
      this.targetBlock = null;
      this.selectedOffer = -1;
      this.tradeActions = 0;
      this.depositActions = 0;
      this.saleCount = 0;
      this.salePrice = 0L;
      this.saleConfirmed = false;
      this.saleRejected = false;
      this.crafting.reset();
      this.machine.transition(AutoTradeFeature.State.WAIT, tick);
      this.nextWorkTick = tick + Math.max(1L, delayTicks);
      if (this.resourcesClaimed) {
         this.resourcesClaimed = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }
   }

   private void resetRuntime(boolean closeScreen) {
      class_310 client = class_310.method_1551();
      if (closeScreen) {
         this.closeOwned(client);
         this.restoreSaleStack(client.field_1724);
      }

      this.navigator.close();
      this.machine.reset(0L);
      this.actionCooldown.reset();
      this.commandCooldown.reset();
      this.crafting.reset();
      this.exhaustedVillagers.clear();
      this.nextWorkTick = 0L;
      this.shopRetryTick = 0L;
      this.auctionRetryTick = 0L;
      if (this.resourcesClaimed) {
         this.resourcesClaimed = false;
         PveAutomationCoordinator.INSTANCE.release(this);
      }

      this.moneyDry = false;
      this.targetVillager = null;
      this.targetBlock = null;
      this.ownedContainerId = -1;
      this.selectedOffer = -1;
      this.tradeActions = 0;
      this.depositActions = 0;
      this.saleCount = 0;
      this.salePrice = 0L;
      this.originalSelectedSlot = -1;
      this.saleSwapMenuSlot = -1;
      this.saleHotbarSlot = -1;
      this.saleConfirmed = false;
      this.saleRejected = false;
      this.lastTick = 0L;
   }

   private static int emeraldCost(class_1914 offer) {
      int cost = 0;
      if (offer.method_19272().method_31574(class_1802.field_8687)) {
         cost += offer.method_19272().method_7947();
      }

      if (offer.method_8247().method_31574(class_1802.field_8687)) {
         cost += offer.method_8247().method_7947();
      }

      return cost;
   }

   private static int goldValue(class_1799 stack) {
      if (stack.method_31574(class_1802.field_8494)) {
         return stack.method_7947() * 9;
      } else if (stack.method_31574(class_1802.field_8695)) {
         return stack.method_7947();
      } else {
         return stack.method_31574(class_1802.field_8397) ? Math.max(1, stack.method_7947() / 9) : 0;
      }
   }

   private static boolean isGold(class_1799 stack) {
      return stack.method_31574(class_1802.field_8494) || stack.method_31574(class_1802.field_8695) || stack.method_31574(class_1802.field_8397);
   }

   private static int largestStackCount(class_746 player, class_1792 item) {
      int largest = 0;

      for (int slot = 0; slot < 36; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (stack.method_31574(item)) {
            largest = Math.max(largest, stack.method_7947());
         }
      }

      return largest;
   }

   @Environment(EnvType.CLIENT)
   enum State {
      WAIT,
      SELECT,
      OPEN_SHOP,
      WAIT_SHOP_MENU,
      BUY_SHOP,
      FIND_VILLAGER,
      MOVE_VILLAGER,
      OPEN_TRADE,
      TRADE,
      FIND_TABLE,
      MOVE_TABLE,
      OPEN_TABLE,
      CRAFT_BLOCKS,
      FIND_DEPOSIT,
      MOVE_DEPOSIT,
      OPEN_DEPOSIT,
      DEPOSIT,
      OPEN_AUCTION_SEARCH,
      WAIT_AUCTION_SEARCH,
      LIST_AUCTION,
      WAIT_SALE_CONFIRMATION;
   }
}
