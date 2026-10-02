package org.ryzen.pve.economy;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class EconomyAutomationPolicy {
   private EconomyAutomationPolicy() {
   }

   public static EconomyAutomationPolicy.CrafterAction crafterAction(
      int output, int apples, int goldBlocks, int goldIngots, boolean autoSell, boolean canTakeResources, boolean auctionReady
   ) {
      if (!autoSell || !auctionReady || output <= 0 || output < 64 && (canTakeResources || apples >= 1 && (goldBlocks >= 8 || goldIngots >= 9))) {
         if (apples >= 1 && goldBlocks >= 8) {
            return EconomyAutomationPolicy.CrafterAction.CRAFT_APPLE;
         } else if (goldBlocks < 8 && goldIngots >= 9) {
            return EconomyAutomationPolicy.CrafterAction.CRAFT_GOLD_BLOCK;
         } else if (canTakeResources) {
            return apples < 1 ? EconomyAutomationPolicy.CrafterAction.TAKE_APPLES : EconomyAutomationPolicy.CrafterAction.TAKE_GOLD;
         } else {
            return EconomyAutomationPolicy.CrafterAction.WAIT;
         }
      } else {
         return EconomyAutomationPolicy.CrafterAction.SELL;
      }
   }

   public static EconomyAutomationPolicy.TradeAction tradeAction(
      int emeralds,
      int goldIngots,
      int goldBlocks,
      int emeraldTarget,
      boolean buyEmeralds,
      boolean depositGold,
      boolean craftBlocks,
      boolean sellBlocks,
      boolean moneyDry,
      boolean shopReady,
      boolean auctionReady,
      boolean hasSellableBlockStack
   ) {
      if (sellBlocks && auctionReady && hasSellableBlockStack) {
         return EconomyAutomationPolicy.TradeAction.SELL_BLOCKS;
      }

      if (craftBlocks && goldIngots >= 9) {
         return EconomyAutomationPolicy.TradeAction.CRAFT_BLOCKS;
      }

      if (!moneyDry || !depositGold || goldIngots <= 0 && goldBlocks <= 0) {
         if (emeralds >= emeraldTarget) {
            return EconomyAutomationPolicy.TradeAction.TRADE;
         } else if (buyEmeralds && shopReady) {
            return EconomyAutomationPolicy.TradeAction.BUY_EMERALDS;
         } else {
            return !depositGold || goldIngots <= 0 && goldBlocks <= 0
               ? EconomyAutomationPolicy.TradeAction.WAIT
               : EconomyAutomationPolicy.TradeAction.DEPOSIT_GOLD;
         }
      } else {
         return EconomyAutomationPolicy.TradeAction.DEPOSIT_GOLD;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum CrafterAction {
      SELL,
      CRAFT_APPLE,
      CRAFT_GOLD_BLOCK,
      TAKE_APPLES,
      TAKE_GOLD,
      WAIT;
   }

   @Environment(EnvType.CLIENT)
   public enum TradeAction {
      SELL_BLOCKS,
      CRAFT_BLOCKS,
      DEPOSIT_GOLD,
      TRADE,
      BUY_EMERALDS,
      WAIT;
   }
}
