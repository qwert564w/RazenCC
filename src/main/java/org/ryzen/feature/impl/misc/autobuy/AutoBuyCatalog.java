package org.ryzen.feature.impl.misc.autobuy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_2561;
import net.minecraft.class_9290;
import net.minecraft.class_9334;

@Environment(EnvType.CLIENT)
public final class AutoBuyCatalog {
   private static final List<AutoBuyItem> ITEMS = new ArrayList<>();
   private static final Set<String> SEEN_IDS = new HashSet<>();
   private static boolean built;

   private AutoBuyCatalog() {
   }

   public static List<AutoBuyItem> all() {
      ensureBuilt();
      return Collections.unmodifiableList(ITEMS);
   }

   public static List<AutoBuyItem> forFunTimeFamily() {
      ensureBuilt();
      List<AutoBuyItem> result = new ArrayList<>();

      for (AutoBuyItem item : ITEMS) {
         if (!item.getCategory().isHolyWorld()) {
            result.add(item);
         }
      }

      return result;
   }

   public static List<AutoBuyItem> forHolyWorld() {
      return byCategory(AutoBuyItemCategory.HOLYWORLD);
   }

   public static List<AutoBuyItem> forServer(String serverMode) {
      return AutoBuyServer.isHolyFamily(serverMode) ? forHolyWorld() : forFunTimeFamily();
   }

   public static List<AutoBuyItem> byCategory(AutoBuyItemCategory category) {
      ensureBuilt();
      List<AutoBuyItem> result = new ArrayList<>();

      for (AutoBuyItem item : ITEMS) {
         if (item.getCategory() == category) {
            result.add(item);
         }
      }

      return result;
   }

   public static List<AutoBuyItem> byCategoryForServer(AutoBuyItemCategory category, String serverMode) {
      if (category == null) {
         return forServer(serverMode);
      } else {
         return category.matchesServer(serverMode) ? byCategory(category) : List.of();
      }
   }

   private static synchronized void ensureBuilt() {
      if (!built) {
         built = true;
         SEEN_IDS.clear();
         ITEMS.clear();
         buildKrush();
         buildSpheres();
         buildTalismans();
         buildPotions();
         buildMisc();
         buildHolyWorld();
      }
   }

   private static void buildKrush() {
      krush("Шлем Крушителя", 150000, class_1802.field_22027);
      krush("Нагрудник Крушителя", 250000, class_1802.field_22028);
      krush("Поножи Крушителя", 200000, class_1802.field_22029);
      krush("Ботинки Крушителя", 150000, class_1802.field_22030);
      krush("Меч Крушителя", 300000, class_1802.field_22022);
      krush("Булава Крушителя", 280000, class_1802.field_49814, "Булава");
      krush("Кирка Крушителя", 200000, class_1802.field_22024);
   }

   private static void buildSpheres() {
      sphere("Сфера Афины", 80000);
      sphere("Сфера Хаоса", 80000);
      sphere("Сфера Сатира", 80000);
      sphere("Сфера Бестии", 80000);
      sphere("Сфера Ареса", 80000);
      sphere("Сфера Гидры", 80000);
      sphere("Сфера Икара", 80000);
      sphere("Сфера Титана", 80000);
      sphere("Сфера Эрида", 80000);
   }

   private static void buildTalismans() {
      talisman("Талисман Крушителя", 150000);
      talisman("Талисман Раздора", 100000);
      talisman("Талисман Тирана", 100000);
      talisman("Талисман Ярости", 100000);
      talisman("Талисман Вихря", 100000);
      talisman("Талисман Мрака", 100000);
      talisman("Талисман Демона", 100000);
      talisman("Талисман Карателя", 100000);
   }

   private static void buildPotions() {
      potion("Зелье Ассасина", 25000, 3355443);
      potion("Зелье Гнева", 25000, 10040115);
      potion("Хлопушка", 20000, 16738740);
      potion("Святая Вода", 20000, 16777215, "Святая вода");
      potion("Зелье Палладина", 30000, 65535);
      potion("Зелье Радиации", 25000, 3329330);
      potion("Снотворное", 20000, 4737096);
      potion("Мандариновый сок", 15000, 14077507);
      potion("Зелье Агента", 20000, 5635925);
      potion("Зелье Киллера", 25000, 11141120);
      potion("Зелье Медика", 20000, 5635925);
      potion("Зелье Победителя", 25000, 16755200, "Зелье победителя");
   }

   private static void buildMisc() {
      item("Отмычка к сферам", AutoBuyItemCategory.MISC, 50000, class_1802.field_8366, "Отмычка к Сферам", "Отмычка");
      item("Чарка", AutoBuyItemCategory.MISC, 25000, class_1802.field_8367, "Зачарованное золотое яблоко");
      item("Маяк", AutoBuyItemCategory.MISC, 80000, class_1802.field_8668, "Загадочный маяк");
      item("Модификатор полёта", AutoBuyItemCategory.MISC, 60000, class_1802.field_8153, "Модификатор полета");
      item("Незеритовый слиток", AutoBuyItemCategory.MISC, 3000, class_1802.field_22020);
      item("Шалкер", AutoBuyItemCategory.MISC, 20000, class_1802.field_8545, "Шалкеровый ящик");
      item("Явная Пыль", AutoBuyItemCategory.MISC, 15000, class_1802.field_8479, "Явная пыль");
      item("Дезориентация", AutoBuyItemCategory.MISC, 15000, class_1802.field_8449);
      item("Трапка", AutoBuyItemCategory.MISC, 40000, class_1802.field_22021);
      item("Пласт", AutoBuyItemCategory.MISC, 30000, class_1802.field_8551);
      item("Опыт 15", AutoBuyItemCategory.MISC, 5000, class_1802.field_8287, "Пузырек опыта [15 ур]", "Пузырёк опыта [15 ур]");
      item("Опыт 30", AutoBuyItemCategory.MISC, 10000, class_1802.field_8287, "Пузырек опыта [30 ур]", "Пузырёк опыта [30 Ур.]");
      item("Опыт 50", AutoBuyItemCategory.MISC, 20000, class_1802.field_8287, "Пузырек опыта [50 ур]");
      item("Вайт", AutoBuyItemCategory.MISC, 25000, class_1802.field_8626, "TNT - TIER WHITE", "TNT WHITE");
      item("Блек", AutoBuyItemCategory.MISC, 40000, class_1802.field_8626, "TNT - TIER BLACK", "TNT BLACK");
      item("Тотем бессмертия", AutoBuyItemCategory.MISC, 5000, class_1802.field_8288);
      item("Элитры", AutoBuyItemCategory.MISC, 50000, class_1802.field_8833);
      item("Эндер жемчуг", AutoBuyItemCategory.MISC, 500, class_1802.field_8634, "Эндер-жемчуг");
      item("Золотое яблоко", AutoBuyItemCategory.MISC, 1000, class_1802.field_8463);
      item("Спавнер", AutoBuyItemCategory.MISC, 100000, class_1802.field_8849);
      item("Незеритовый блок", AutoBuyItemCategory.MISC, 25000, class_1802.field_22018);
      item("Заряд ветра", AutoBuyItemCategory.MISC, 5000, class_1802.field_49098);
   }

   private static void buildHolyWorld() {
      holy("Шлем Infinity", 200000, class_1802.field_22027);
      holy("Нагрудник Infinity", 300000, class_1802.field_22028);
      holy("Поножи Infinity", 250000, class_1802.field_22029);
      holy("Ботинки Infinity", 200000, class_1802.field_22030);
      holy("Шлем Eternity", 120000, class_1802.field_22027);
      holy("Нагрудник Eternity", 180000, class_1802.field_22028);
      holy("Штаны Eternity", 150000, class_1802.field_22029, "Поножи Eternity");
      holy("Ботинки Eternity", 120000, class_1802.field_22030);
      holy("Шлем солнца", 100000, class_1802.field_8862);
      holy("Броневая элитра", 250000, class_1802.field_8833);
      holy("Меч Eternity", 200000, class_1802.field_22022);
      holy("Кирка Eternity", 150000, class_1802.field_22024);
      holy("Арбалет Eternity", 100000, class_1802.field_8399);
      holy("Громовержец", 150000, class_1802.field_8547);
      holySphere("Сфера Цербера", 100000, "Cerber");
      holySphere("Сфера Флеша", 100000, "Flash");
      holySphere("Сфера Имморталити", 100000, "Сфера ɪᴍᴍᴏʀᴛᴀʟɪᴛʏ", "Immortal");
      holySphere("Сфера Арморталити", 100000, "Сфера ᴀʀᴍᴏʀᴛᴀʟɪᴛʏ", "Armortality");
      holySphere("Сфера на Скорость III", 80000, "Сфера на скорость 3", "Speed3");
      holySphere("Сфера Eternity", 100000, "Eternity");
      holySphere("Сфера Stinger", 100000, "Stinger");
      holySphere("Сфера на броня III скорость II", 90000, "Сфера на броня 3", "Mythical3");
      holySphere("Сфера на урон II броня III", 90000);
      holySphere("Сфера на броня II урон III", 90000);
      holy("Талисман Stinger", 120000, class_1802.field_8288);
      holy("Талисман Infinity", 150000, class_1802.field_8288);
      holy("Талисман Eternity", 130000, class_1802.field_8288);
      holy("Легендарный талисман", 100000, class_1802.field_8288);
      holy("Пузырек с 15 уровнем", 5000, class_1802.field_8287, "15");
      holy("Пузырек с 50 уровнем", 25000, class_1802.field_8287, "50");
      holy("Пузырек с 100 уровнем", 80000, class_1802.field_8287, "100");
      holy("Обычный пузырек опыта", 1000, class_1802.field_8287, "опыт");
      holy("Рюкзак I уровень", 30000, class_1802.field_8520, "рюкзак 1 уровень");
      holy("Рюкзак II уровень", 50000, class_1802.field_8829, "рюкзак 2 уровень");
      holy("Рюкзак III уровень", 80000, class_1802.field_8676, "рюкзак 3 уровень");
      holy("Рюкзак IV уровень", 120000, class_1802.field_8050, "рюкзак 4 уровень");
      holy("Рюкзак Infinity", 200000, class_1802.field_8548, "рюкзак infinity");
      holy("Взрывная трапка", 40000, class_1802.field_8662);
      holy("Стан", 35000, class_1802.field_8137);
      holy("Взрывная штучка", 20000, class_1802.field_8814);
      holy("Ком снега", 15000, class_1802.field_8543);
      holy("Руна «Бессмертие»", 50000, class_1802.field_8492, "Бессмертие", "Руна Бессмертие");
      holyPotion("Улучшенное зелье силы", 20000, 16733440);
      holyPotion("Улучшенное зелье скорости", 20000, 3386111);
      holyPotion("Зелье исцеления", 15000, 16711680, "Зелье исцеление");
      holyPotion("Зелье черепашьей мощи", 18000, 8369336);
      holyPotion("Зелье черепашьей мощи II", 25000, 8369336);
      holy("Охотник", 40000, class_1802.field_22022);
      holy("Снеговик", 30000, class_1802.field_8246);
      holy("Иллюминатор", 30000, class_1802.field_8305);
      holy("Эндермен", 35000, class_1802.field_8634);
      holy("Анти Фантом", 25000, class_1802.field_8614);
      holy("Телекинез", 30000, class_1802.field_21086);
      holy("Гравитация", 30000, class_1802.field_8153);
      holy("Вампиризм", 40000, class_1802.field_8791);
      holy("Справедливость", 35000, class_1802.field_8574);
      holy("Универсальный ключ", 50000, class_1802.field_8366);
      holy("Фармер", 40000, class_1802.field_8802);
      holy("Золотая морковь", 500, class_1802.field_8071);
      holy("Плод хоруса", 300, class_1802.field_8233);
      holy("Артефакт", 80000, class_1802.field_8140);
      holy("Фейерверк", 200, class_1802.field_8639);
      holy("Порох", 100, class_1802.field_8054);
      holy("Боевой фрагмент", 15000, class_1802.field_8434);
      holy("Взрывчатое вещество", 20000, class_1802.field_19060);
      holy("Динамит А", 25000, class_1802.field_8626, "Динамит A");
      holy("Динамит B", 30000, class_1802.field_8626, "динамит б");
      holy("Динамит B2", 35000, class_1802.field_8626, "динамит б2");
      holy("C4 ВзРыВчАтКа", 50000, class_1802.field_8626, "с4 взрывчатка", "C4");
      holy("Золотая кирка Джейка", 60000, class_1802.field_8335);
      holy("Осколок сферы", 20000, class_1802.field_8575);
   }

   private static void krush(String name, int price, class_1792 icon, String... aliases) {
      item(name, AutoBuyItemCategory.KRUSH, price, namedIcon(icon, name), aliases);
   }

   private static void sphere(String name, int price) {
      item(name, AutoBuyItemCategory.SPHERES, price, class_1802.field_8575, "[★] " + name);
   }

   private static void talisman(String name, int price, String... aliases) {
      item(name, AutoBuyItemCategory.TALISMANS, price, class_1802.field_8288, aliases);
   }

   private static void holy(String name, int price, class_1792 icon, String... aliases) {
      item(name, AutoBuyItemCategory.HOLYWORLD, price, icon, aliases);
   }

   private static void holySphere(String name, int price, String... aliases) {
      item(name, AutoBuyItemCategory.HOLYWORLD, price, class_1802.field_8575, aliases);
   }

   private static void holyPotion(String name, int price, int color, String... aliases) {
      addUnique(new AutoBuyItem(name, AutoBuyItemCategory.HOLYWORLD, price, tintedPotion(class_1802.field_8574, name, color), aliases));
   }

   private static void potion(String name, int price, int color, String... aliases) {
      addUnique(new AutoBuyItem(name, AutoBuyItemCategory.POTIONS, price, tintedPotion(class_1802.field_8436, name, color), aliases));
   }

   private static void item(String name, AutoBuyItemCategory category, int price, class_1792 icon, String... aliases) {
      addUnique(new AutoBuyItem(name, category, price, icon, aliases));
   }

   private static void item(String name, AutoBuyItemCategory category, int price, Supplier<class_1799> icon, String... aliases) {
      addUnique(new AutoBuyItem(name, category, price, icon, aliases));
   }

   private static void addUnique(AutoBuyItem item) {
      if (item != null && !item.getId().isEmpty() && SEEN_IDS.add(item.getId())) {
         ITEMS.add(item);
      }
   }

   private static Supplier<class_1799> tintedPotion(class_1792 base, String name, int color) {
      return () -> {
         class_1799 stack = new class_1799(base);
         stack.method_57379(class_9334.field_49631, class_2561.method_43470(name).method_27692(class_124.field_1075));
         stack.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(color), List.of(), Optional.empty()));
         return stack;
      };
   }

   private static Supplier<class_1799> namedIcon(class_1792 item, String name) {
      return () -> {
         class_1799 stack = new class_1799(item);
         stack.method_57379(class_9334.field_49631, class_2561.method_43470(name).method_27695(new class_124[]{class_124.field_1067, class_124.field_1079}));
         stack.method_57379(
            class_9334.field_49632, new class_9290(List.of(class_2561.method_43470("[★] Оригинальный предмет").method_27692(class_124.field_1080)))
         );
         return stack;
      };
   }
}
