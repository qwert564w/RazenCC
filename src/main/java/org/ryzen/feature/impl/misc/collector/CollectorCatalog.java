package org.ryzen.feature.impl.misc.collector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1293;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1844;
import net.minecraft.class_9334;
import org.ryzen.feature.impl.misc.DonItems;

@Environment(EnvType.CLIENT)
public final class CollectorCatalog {
   private static final List<CollectorItem> ITEMS = build();

   private CollectorCatalog() {
   }

   public static List<CollectorItem> all() {
      return ITEMS;
   }

   private static List<CollectorItem> build() {
      List<CollectorItem> items = new ArrayList<>();
      items.add(
         configured(
            "Незеритовый меч",
            class_1802.field_22022,
            1,
            true,
            true,
            enchant("sharpness", "Острота", 7),
            enchant("fire_aspect", "Заговор огня", 2),
            lore("Яд", 3),
            lore("Вампиризм", 2),
            lore("Окисление", 2),
            lore("Опытный", 3, false),
            lore("Детекция", 3)
         )
      );
      items.add(
         configured(
            "Булава",
            class_1802.field_49814,
            1,
            true,
            true,
            enchant("sharpness", "Острота", 7),
            enchant("breach", "Пробитие", 3),
            enchant("density", "Плотность", 5)
         )
      );
      items.add(
         configured(
            "Трезубец",
            class_1802.field_8547,
            1,
            true,
            true,
            lore("Ступор", 3),
            lore("Притяжение", 2),
            lore("Скаут", 3),
            lore("Возвращение", 0),
            lore("Подрывник", 0)
         )
      );
      items.add(
         configured(
            "Незеритовый шлем",
            class_1802.field_22027,
            1,
            true,
            true,
            enchant("protection", "Защита", 5),
            enchant("unbreaking", "Прочность", 5),
            enchant("respiration", "Подводное дыхание", 3),
            enchant("mending", "Починка", 1)
         )
      );
      items.add(
         configured(
            "Незеритовый нагрудник",
            class_1802.field_22028,
            1,
            true,
            true,
            enchant("protection", "Защита", 5),
            enchant("unbreaking", "Прочность", 5),
            enchant("mending", "Починка", 1)
         )
      );
      items.add(
         configured(
            "Незеритовые поножи",
            class_1802.field_22029,
            1,
            true,
            true,
            enchant("protection", "Защита", 5),
            enchant("unbreaking", "Прочность", 5),
            enchant("mending", "Починка", 1)
         )
      );
      items.add(
         configured(
            "Незеритовые ботинки",
            class_1802.field_22030,
            1,
            true,
            true,
            enchant("protection", "Защита", 5),
            enchant("unbreaking", "Прочность", 5),
            enchant("depth_strider", "Подводная ходьба", 3),
            enchant("mending", "Починка", 1)
         )
      );
      items.add(configured("Трапка", class_1802.field_22021, 8, true, false, lore("Каст: Нерушимая клетка", 0)));
      items.add(configured("Явная пыль", class_1802.field_8479, 12, true, false, lore("Каст: Световая вспышка", 0)));
      items.add(configured("Божья аура", class_1802.field_8614, 4, true, false, lore("Каст: Божественная аура", 0)));
      items.add(configured("Дезориентация", class_1802.field_8449, 16, true, false, lore("Каст: Звуковая волна", 0)));
      items.add(plain("Заряд ветра", class_1802.field_49098, 32, true));
      items.add(configured("Пласт", class_1802.field_8551, 16, true, false, lore("Каст: Нерушимая стена", 0)));
      items.add(configured("Снежок заморозка", class_1802.field_8543, 4, true, false, lore("Каст: Ледяная сфера", 0)));
      items.add(plain("Перка", class_1802.field_8634, 16, true));
      items.add(plain("Тотем бессмертия", class_1802.field_8288, 1, true));
      items.add(
         configured(
            "Арбалет",
            class_1802.field_8399,
            1,
            true,
            false,
            enchant("quick_charge", "Быстрая перезарядка", 3),
            enchant("mending", "Починка", 1),
            enchant("multishot", "Тройной выстрел", 1)
         )
      );
      items.add(plain("Золотое яблоко", class_1802.field_8463, 16, true));
      items.add(plain("Зачарованное золотое яб", class_1802.field_8367, 8, true));
      items.add(plain("Золотая морковь", class_1802.field_8071, 64, true));
      items.add(plain("Хорус", class_1802.field_8233, 64, true));
      items.add(plain("Элитры", class_1802.field_8833, 1, true));
      items.add(plain("Фейерверк", class_1802.field_8639, 64, true));
      items.add(profile("Хлопушка", class_1802.field_8436, 1, true, false, DonItems.FunTime.POPPER));
      items.add(profile("Святая вода", class_1802.field_8436, 1, true, false, DonItems.FunTime.HOLY_WATER));
      items.add(profile("Зелье Гнева", class_1802.field_8436, 1, true, true, DonItems.FunTime.RAGE_POTION));
      items.add(profile("Зелье Палладина", class_1802.field_8436, 1, true, false, DonItems.FunTime.PALADIN_POTION));
      items.add(profile("Зелье Ассасина", class_1802.field_8436, 1, true, false, DonItems.FunTime.ASSASSIN_POTION));
      items.add(profile("Зелье Радиации", class_1802.field_8436, 1, true, false, DonItems.FunTime.RADIATION_POTION));
      items.add(profile("Снотворное", class_1802.field_8436, 1, false, false, DonItems.FunTime.DROWSINESS_POTION));
      items.add(signature("Зелье", class_1802.field_8574, 1, true, false, potionEffects("strength", 3, "speed", 3)));
      items.add(signature("Зелье регенерации", class_1802.field_8574, 1, true, false, potionEffects("instant_health", 2, "regeneration", 1)));
      items.add(profile("Кровавая стрела", class_1802.field_8087, 32, true, false, DonItems.FunTime.BLOOD_ARROW));
      items.add(profile("Стрела обледенения", class_1802.field_8087, 64, false, false, DonItems.FunTime.FREEZE_ARROW));
      items.add(profile("Мучительная стрела", class_1802.field_8087, 64, false, false, DonItems.FunTime.AGONY_ARROW));
      return List.copyOf(items);
   }

   private static CollectorItem plain(String name, class_1792 item, int count, boolean enabled) {
      return new CollectorItem(name, item, count, enabled, false, false);
   }

   private static CollectorItem configured(String name, class_1792 item, int count, boolean enabled, boolean scan, CollectorCondition... conditions) {
      return new CollectorItem(name, item, count, enabled, scan, false, null, null, List.of(conditions));
   }

   private static CollectorItem profile(String name, class_1792 item, int count, boolean enabled, boolean scan, DonItems.DonItem profile) {
      return new CollectorItem(name, item, count, enabled, scan, false, profile, null, List.of());
   }

   private static CollectorItem signature(String name, class_1792 item, int count, boolean enabled, boolean scan, Predicate<class_1799> matcher) {
      return new CollectorItem(name, item, count, enabled, scan, false, null, matcher, List.of());
   }

   private static CollectorCondition enchant(String path, String label, int level) {
      return CollectorCondition.enchant(path, label, level);
   }

   private static CollectorCondition lore(String marker, int level) {
      return CollectorCondition.lore(marker, level);
   }

   private static CollectorCondition lore(String marker, int level, boolean enabled) {
      return CollectorCondition.lore(marker, level, enabled);
   }

   private static Predicate<class_1799> potionEffects(Object... values) {
      return stack -> {
         class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
         if (contents == null) {
            return false;
         }

         List<class_1293> effects = new ArrayList<>();
         contents.method_57397().forEach(effects::add);

         for (int index = 0; index + 1 < values.length; index += 2) {
            String path = (String)values[index];
            int level = (Integer)values[index + 1];
            boolean found = effects.stream().anyMatch(effect -> effect.method_5586().endsWith(path) && effect.method_5578() + 1 >= level);
            if (!found) {
               return false;
            }
         }

         return true;
      };
   }
}
