package org.ryzen.feature.impl.misc;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1799;
import net.minecraft.class_1836;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_437;
import net.minecraft.class_465;
import net.minecraft.class_746;
import net.minecraft.class_1792.class_9635;
import org.ryzen.context.PlayerContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.KeyboardInputEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class AutoExchangeFeature extends Feature implements PlayerContext {
   private static final Pattern BALANCE = Pattern.compile("Ваш баланс:\\s*(\\d+)\\s*\\|");
   private static final Pattern EXPECTED = Pattern.compile("Ожидается:\\s*(\\d+)\\s*\\|");
   private static final Pattern GIVING = Pattern.compile("Вы отдадите:\\s*(\\d+)\\s*\\|");
   private static final Pattern ADD_BUTTON = Pattern.compile("▶ Добавить (\\d+) \\|❘\\| \\(коинов\\)");
   private static final String BUY_LINE = "[ЛКМ] — Приобрести";
   private static final String TITLE_EXCHANGE = "Биржа";
   private static final String TITLE_PURCHASE = "Покупка";
   private static final String COMMAND = "exchange";
   public final NumberSetting coins = this.register(new NumberSetting("Coins", 100.0, 1.0, 300.0, 1.0, ""));
   private int target = -1;
   private int openedOfferSlot = -1;
   private class_437 lastScreen;
   private int screenSettledAtTick;
   private int screenSettleDelay;

   public AutoExchangeFeature() {
      super("AutoExchange", "Buys coins through the server exchange menu", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onEnable() {
      this.target = this.coins.getValue().intValue();
      this.openedOfferSlot = -1;
      this.lastScreen = null;
   }

   @Override
   protected void onDisable() {
      this.target = -1;
      this.openedOfferSlot = -1;
      this.lastScreen = null;
   }

   @EventTarget
   public void onKeyboardInput(KeyboardInputEvent event) {
      if (event.getAction() == 1 && event.getKey() == 256) {
         ChatUtil.info("AutoExchange: остановлено");
         this.setEnabled(false);
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1687 != null && this.target > 0) {
         if (client.field_1755 == null) {
            if (player.field_6012 % 10 == 0 && player.field_3944 != null) {
               player.field_3944.method_45730("exchange");
            }

            this.openedOfferSlot = -1;
            this.lastScreen = null;
         } else if (client.field_1755 instanceof class_465<?> screen) {
            class_1703 menu = player.field_7512;
            if (menu != null) {
               if (this.settled(client, player)) {
                  int balance = this.findNumber(player, menu, BALANCE);
                  if (balance != -1 && balance < this.target) {
                     ChatUtil.error("AutoExchange: на балансе меньше коинов, чем запрошено");
                     player.method_7346();
                     this.setEnabled(false);
                  } else {
                     String title = screen.method_25440().getString();
                     boolean exchange = title.contains("Биржа");
                     boolean purchase = title.contains("Покупка");
                     if (!exchange && !purchase) {
                        player.method_7346();
                        this.openedOfferSlot = -1;
                     } else {
                        if (exchange && player.field_6012 % 10 == 0) {
                           this.openedOfferSlot = -1;
                        }

                        if (this.openedOfferSlot != -1) {
                           if (purchase) {
                              this.fillPurchase(client, player, menu);
                           }
                        } else {
                           if (exchange && !purchase) {
                              this.openOffer(client, player, menu);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   private boolean settled(class_310 client, class_746 player) {
      if (client.field_1755 != this.lastScreen) {
         this.lastScreen = client.field_1755;
         this.screenSettledAtTick = player.field_6012;
         this.screenSettleDelay = ThreadLocalRandom.current().nextInt(3, 5);
      }

      return player.field_6012 >= this.screenSettledAtTick + this.screenSettleDelay;
   }

   private void openOffer(class_310 client, class_746 player, class_1703 menu) {
      for (class_1735 slot : menu.field_7761) {
         if (slot != null && slot.method_7681()) {
            int expected = matchNumber(tooltip(player, slot.method_7677()), EXPECTED);
            if (expected >= this.target) {
               click(client, player, menu, slot.field_7874, 1);
               this.openedOfferSlot = slot.field_7874;
               return;
            }
         }
      }
   }

   private void fillPurchase(class_310 client, class_746 player, class_1703 menu) {
      if (player.field_6012 % 10 == 0) {
         int current = -1;

         for (class_1735 slot : menu.field_7761) {
            if (slot != null && slot.method_7681()) {
               int giving = matchNumber(tooltip(player, slot.method_7677()), GIVING);
               if (giving != -1) {
                  current = giving;
                  break;
               }
            }
         }

         if (current != -1) {
            if (current == this.target) {
               class_1735 buy = this.findBuySlot(player, menu);
               if (buy != null) {
                  click(client, player, menu, buy.field_7874, 0);
                  ChatUtil.success("AutoExchange: покупка отправлена");
                  this.setEnabled(false);
               }
            } else {
               int missing = this.target - current;
               if (missing > 0) {
                  class_1735 addButton = this.findAddButton(player, menu, missing);
                  if (addButton != null) {
                     click(client, player, menu, addButton.field_7874, 0);
                  }
               }
            }
         }
      }
   }

   private class_1735 findBuySlot(class_746 player, class_1703 menu) {
      for (class_1735 slot : menu.field_7761) {
         if (slot != null && slot.method_7681()) {
            List<class_2561> lines = tooltip(player, slot.method_7677());
            boolean buyable = lines.stream().anyMatch(line -> line.getString().contains("[ЛКМ] — Приобрести"));
            if (buyable && matchNumber(lines, GIVING) == this.target) {
               return slot;
            }
         }
      }

      return null;
   }

   private class_1735 findAddButton(class_746 player, class_1703 menu, int missing) {
      class_1735 best = null;
      int bestAmount = 0;

      for (class_1735 slot : menu.field_7761) {
         if (slot != null && slot.method_7681()) {
            int amount = matchNumber(tooltip(player, slot.method_7677()), ADD_BUTTON);
            if (amount > 0 && amount <= missing && amount > bestAmount) {
               bestAmount = amount;
               best = slot;
            }
         }
      }

      return best;
   }

   private int findNumber(class_746 player, class_1703 menu, Pattern pattern) {
      for (class_1735 slot : menu.field_7761) {
         if (slot != null && slot.method_7681()) {
            int value = matchNumber(tooltip(player, slot.method_7677()), pattern);
            if (value != -1) {
               return value;
            }
         }
      }

      return -1;
   }

   private static int matchNumber(List<class_2561> lines, Pattern pattern) {
      for (class_2561 line : lines) {
         Matcher matcher = pattern.matcher(line.getString());
         if (matcher.find()) {
            return Integer.parseInt(matcher.group(1).replace(",", ""));
         }
      }

      return -1;
   }

   private static List<class_2561> tooltip(class_746 player, class_1799 stack) {
      return stack.method_7950(class_9635.method_59528(player.method_73183()), player, class_1836.field_41070);
   }

   private static void click(class_310 client, class_746 player, class_1703 menu, int slot, int button) {
      if (client.field_1761 != null) {
         client.field_1761.method_2906(menu.field_7763, slot, button, class_1713.field_7790, player);
      }
   }
}
