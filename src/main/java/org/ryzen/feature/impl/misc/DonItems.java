package org.ryzen.feature.impl.misc;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1291;
import net.minecraft.class_1293;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1844;
import net.minecraft.class_2487;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_642;
import net.minecraft.class_6880;
import net.minecraft.class_7923;
import net.minecraft.class_9279;
import net.minecraft.class_9280;
import net.minecraft.class_9285;
import net.minecraft.class_9290;
import net.minecraft.class_9334;
import net.minecraft.class_9285.class_9287;

@Environment(EnvType.CLIENT)
public final class DonItems {
   private static final Pattern LEGACY_FORMATTING = Pattern.compile("(?i)§[0-9a-fk-orx]");
   private static final Pattern DECORATIVE_X = Pattern.compile("(?iu)(^|\\s)[xх]{2,}(?=\\s|$)");
   private static final Pattern NON_NAME_CHARACTER = Pattern.compile("[^\\p{L}\\p{N}]+");
   private static final Pattern WHITESPACE = Pattern.compile("\\s+");
   private static final double AMOUNT_EPSILON = 1.0E-6;

   private DonItems() {
   }

   public static Optional<DonItems.Server> currentServer() {
      class_642 server = class_310.method_1551().method_1558();
      return server == null ? Optional.empty() : DonItems.Server.fromAddress(server.field_3761);
   }

   public static List<DonItems.DonItem> all(DonItems.Server server) {
      return DonItems.Index.ITEMS_BY_SERVER.getOrDefault(server, List.of());
   }

   public static List<DonItems.DonItem> all(DonItems.Server server, DonItems.Category category) {
      return server != null && category != null ? all(server).stream().filter(item -> item.category() == category).toList() : List.of();
   }

   public static Optional<DonItems.DonItem> find(class_1799 stack) {
      Optional<DonItems.Server> server = currentServer();
      return server.isPresent() ? find(stack, server.get()) : findAny(stack);
   }

   public static Optional<DonItems.DonItem> find(class_1799 stack, String serverAddress) {
      return DonItems.Server.fromAddress(serverAddress).flatMap(server -> find(stack, server));
   }

   public static Optional<DonItems.FunTime> findFunTime(class_1799 stack) {
      return find(stack, DonItems.Server.FUNTIME).filter(DonItems.FunTime.class::isInstance).map(DonItems.FunTime.class::cast);
   }

   public static Optional<DonItems.HolyWorld> findHolyWorld(class_1799 stack) {
      return find(stack, DonItems.Server.HOLYWORLD).filter(DonItems.HolyWorld.class::isInstance).map(DonItems.HolyWorld.class::cast);
   }

   public static Optional<DonItems.DonItem> find(class_1799 stack, DonItems.Server server) {
      if (stack != null && !stack.method_7960() && server != null) {
         DonItems.StackView view = new DonItems.StackView(stack);
         if (server == DonItems.Server.FUNTIME) {
            Optional<String> stableId = view.funTimeId();
            if (stableId.isPresent()) {
               DonItems.FunTime item = DonItems.Index.FUNTIME_BY_ID.get(stableId.get());
               if (item != null && item.baseItemId().equals(view.baseItemId)) {
                  return Optional.of(item);
               }
            }
         }

         List<DonItems.DonItem> candidates = DonItems.Index.ITEMS_BY_BASE_ITEM.getOrDefault(server, Map.of()).getOrDefault(view.baseItemId, List.of());

         for (DonItems.DonItem candidate : candidates) {
            if (rule(candidate).strongMatches(view)) {
               return Optional.of(candidate);
            }
         }

         for (DonItems.DonItem candidate : candidates) {
            if (rule(candidate).nameMatches(view.normalizedName)) {
               return Optional.of(candidate);
            }
         }

         return Optional.empty();
      } else {
         return Optional.empty();
      }
   }

   public static Optional<DonItems.DonItem> findAny(class_1799 stack) {
      for (DonItems.Server server : DonItems.Server.values()) {
         Optional<DonItems.DonItem> item = find(stack, server);
         if (item.isPresent()) {
            return item;
         }
      }

      return Optional.empty();
   }

   public static boolean matches(class_1799 stack, DonItems.DonItem expected) {
      if (stack != null && !stack.method_7960() && expected != null) {
         if (!expected.baseItemId().equals(baseItemId(stack))) {
            return false;
         }

         DonItems.StackView view = new DonItems.StackView(stack);
         DonItems.MatchRule rule = rule(expected);
         return expected instanceof DonItems.FunTime && rule.stableId != null && rule.stableId.equals(view.funTimeId().orElse(null))
            ? true
            : rule.strongMatches(view) || rule.nameMatches(view.normalizedName);
      } else {
         return false;
      }
   }

   public static boolean isDonItem(class_1799 stack) {
      return find(stack).isPresent();
   }

   public static boolean isDonItem(class_1799 stack, DonItems.Server server) {
      return find(stack, server).isPresent();
   }

   public static Optional<String> funTimeId(class_1799 stack) {
      return stack != null && !stack.method_7960() ? new DonItems.StackView(stack).funTimeId() : Optional.empty();
   }

   public static Optional<DonItems.FunTime> funTimeById(String id) {
      return id != null && !id.isBlank() ? Optional.ofNullable(DonItems.Index.FUNTIME_BY_ID.get(id)) : Optional.empty();
   }

   public static List<DonItems.PotionEffectProfile> potionProfile(DonItems.DonItem item) {
      return item == null
         ? List.of()
         : rule(item).effects.stream().map(effect -> new DonItems.PotionEffectProfile(effect.id, effect.level, effect.durationTicks)).toList();
   }

   public static boolean hasPotionProfile(DonItems.DonItem item) {
      if (item == null) {
         return false;
      }

      DonItems.MatchRule rule = rule(item);
      return !rule.effects.isEmpty() || !rule.loreEffects.isEmpty();
   }

   public static class_1799 displayStack(DonItems.DonItem item) {
      if (item == null) {
         return class_1799.field_8037;
      }

      class_1792 baseItem = (class_1792)class_7923.field_41178.method_63535(class_2960.method_60654(item.baseItemId()));
      class_1799 stack = new class_1799(baseItem);
      DonItems.MatchRule rule = rule(item);
      Integer color = rule.potionColor;
      List<class_1293> effects = new ArrayList<>();
      if (!rule.effects.isEmpty()) {
         for (DonItems.EffectSignature signature : rule.effects) {
            class_1291 effect = (class_1291)class_7923.field_41174.method_63535(class_2960.method_60654(signature.id));
            if (effect != null) {
               effects.add(new class_1293(class_7923.field_41174.method_47983(effect), Math.max(1, signature.durationTicks), Math.max(0, signature.level - 1)));
            }
         }

         if (color == null) {
            OptionalInt mixed = class_1844.method_58111(effects);
            if (mixed.isPresent()) {
               color = mixed.getAsInt();
            }
         }
      }

      if (color != null) {
         stack.method_57379(class_9334.field_49651, new class_1844(Optional.empty(), Optional.of(color), List.copyOf(effects), Optional.empty()));
      }

      return stack;
   }

   public static Optional<Boolean> matchesPotionProfile(class_1799 stack, DonItems.DonItem item, boolean exactLevel, boolean exactDuration) {
      if (stack != null && !stack.method_7960() && item != null) {
         DonItems.MatchRule rule = rule(item);
         if (!rule.effects.isEmpty()) {
            return Optional.of(effectsMatch(stack, rule.effects, exactLevel, exactDuration));
         } else {
            return !rule.loreEffects.isEmpty() ? Optional.of(loreEffectsMatch(stack, rule.loreEffects, exactLevel, exactDuration)) : Optional.empty();
         }
      } else {
         return Optional.empty();
      }
   }

   public static String normalizedName(class_1799 stack) {
      return stack != null && !stack.method_7960() ? normalizeName(stack.method_7964().getString()) : "";
   }

   public static String normalizeName(String value) {
      if (value != null && !value.isBlank()) {
         String normalized = Normalizer.normalize(value, Form.NFKC);
         normalized = LEGACY_FORMATTING.matcher(normalized).replaceAll("");
         normalized = normalized.toLowerCase(Locale.ROOT).replace('ё', 'е');
         normalized = NON_NAME_CHARACTER.matcher(normalized).replaceAll(" ");
         normalized = DECORATIVE_X.matcher(normalized).replaceAll(" ");
         return WHITESPACE.matcher(normalized).replaceAll(" ").trim();
      } else {
         return "";
      }
   }

   private static DonItems.MatchRule rule(DonItems.DonItem item) {
      if (item instanceof DonItems.FunTime funTime) {
         return funTime.rule;
      } else if (item instanceof DonItems.HolyWorld holyWorld) {
         return holyWorld.rule;
      } else if (item instanceof DonItems.ReallyWorld reallyWorld) {
         return reallyWorld.rule;
      } else {
         throw new IllegalArgumentException("Unsupported DonItem implementation: " + item.getClass().getName());
      }
   }

   private static String baseItemId(class_1799 stack) {
      class_2960 id = class_7923.field_41178.method_10221(stack.method_7909());
      return id == null ? "unregistered" : id.toString();
   }

   private static String holderId(class_6880<?> holder) {
      return holder.method_40230().map(key -> key.method_29177().toString()).orElse("");
   }

   private static DonItems.MatchRule ft(String stableId) {
      return new DonItems.MatchRule(stableId);
   }

   private static DonItems.MatchRule named() {
      return new DonItems.MatchRule(null);
   }

   private static DonItems.MatchRule prefix(String value) {
      return named().prefix(value);
   }

   private static DonItems.EffectSignature effect(String id, int level, int durationTicks) {
      return new DonItems.EffectSignature(id, level, durationTicks);
   }

   private static DonItems.LoreEffectSignature loreEffect(String name, String level, String... durations) {
      return new DonItems.LoreEffectSignature(
         normalizeName(name), level == null ? null : normalizeName(level), Arrays.stream(durations).map(DonItems::normalizeName).toList()
      );
   }

   private static DonItems.AttributeSignature attribute(String id, double amount, String operation) {
      return new DonItems.AttributeSignature(id, amount, operation);
   }

   private static boolean modelMatches(class_1799 stack, class_2487 customData, float expected) {
      class_9280 model = (class_9280)stack.method_58694(class_9334.field_49637);
      return model != null && !model.comp_3354().isEmpty() && Math.abs(model.method_65362(0) - expected) <= 1.0E-6
         ? true
         : customData.method_10583("CustomModelData").map(value -> Math.abs(value - expected) <= 1.0E-6).orElse(false);
   }

   private static boolean potionColorMatches(class_1799 stack, int expected) {
      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      return contents != null && contents.comp_2379().filter(color -> color == expected).isPresent();
   }

   private static boolean effectsMatch(class_1799 stack, List<DonItems.EffectSignature> expected) {
      return effectsMatch(stack, expected, true, true);
   }

   private static boolean effectsMatch(class_1799 stack, List<DonItems.EffectSignature> expected, boolean exactLevel, boolean exactDuration) {
      class_1844 contents = (class_1844)stack.method_58694(class_9334.field_49651);
      if (contents == null) {
         return false;
      }

      List<class_1293> actual = new ArrayList<>();
      contents.method_57397().forEach(actual::add);
      if (actual.size() != expected.size()) {
         return false;
      }

      boolean[] used = new boolean[actual.size()];

      for (DonItems.EffectSignature signature : expected) {
         boolean matched = false;

         for (int index = 0; index < actual.size(); index++) {
            if (!used[index] && signature.matches(actual.get(index), exactLevel, exactDuration)) {
               used[index] = true;
               matched = true;
               break;
            }
         }

         if (!matched) {
            return false;
         }
      }

      return true;
   }

   private static boolean loreEffectsMatch(class_1799 stack, List<DonItems.LoreEffectSignature> expected, boolean exactLevel, boolean exactDuration) {
      class_9290 lore = (class_9290)stack.method_58694(class_9334.field_49632);
      if (lore == null) {
         return false;
      }

      List<String> lines = lore.comp_2400().stream().map(line -> normalizeName(line.getString())).toList();
      boolean[] used = new boolean[lines.size()];

      for (DonItems.LoreEffectSignature signature : expected) {
         boolean matched = false;

         for (int index = 0; index < lines.size(); index++) {
            if (!used[index] && signature.matches(lines.get(index), exactLevel, exactDuration)) {
               used[index] = true;
               matched = true;
               break;
            }
         }

         if (!matched) {
            return false;
         }
      }

      return true;
   }

   private static boolean attributesMatch(class_1799 stack, List<DonItems.AttributeSignature> expected) {
      class_9285 modifiers = (class_9285)stack.method_58694(class_9334.field_49636);
      if (modifiers != null && modifiers.comp_2393().size() == expected.size()) {
         List<class_9287> actual = modifiers.comp_2393();
         boolean[] used = new boolean[actual.size()];

         for (DonItems.AttributeSignature signature : expected) {
            boolean matched = false;

            for (int index = 0; index < actual.size(); index++) {
               if (!used[index] && signature.matches(actual.get(index))) {
                  used[index] = true;
                  matched = true;
                  break;
               }
            }

            if (!matched) {
               return false;
            }
         }

         return true;
      } else {
         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   private record AttributeSignature(String id, double amount, String operation) {
      private boolean matches(class_9287 entry) {
         return this.id.equals(DonItems.holderId(entry.comp_2395()))
            && Math.abs(this.amount - entry.comp_2396().comp_2449()) <= 1.0E-6
            && this.operation.equals(entry.comp_2396().comp_2450().method_15434());
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Category {
      WEAPON,
      ARMOR,
      COMBAT_ITEM,
      POTION,
      ARROW,
      TALISMAN,
      SPHERE,
      EXPLOSIVE,
      CUSTOM_ENCHANTMENT,
      RUNE,
      BACKPACK,
      TOOL,
      MISC;
   }

   @Environment(EnvType.CLIENT)
   public interface DonItem {
      DonItems.Server server();

      DonItems.Category category();

      String displayName();

      String baseItemId();

      default boolean matches(class_1799 stack) {
         return DonItems.matches(stack, this);
      }
   }

   @Environment(EnvType.CLIENT)
   private record EffectSignature(String id, int level, int durationTicks) {
      private boolean matches(class_1293 effect) {
         return this.matches(effect, true, true);
      }

      private boolean matches(class_1293 effect, boolean exactLevel, boolean exactDuration) {
         return this.id.equals(DonItems.holderId(effect.method_5579()))
            && (!exactLevel || this.level == effect.method_5578() + 1)
            && (!exactDuration || this.durationTicks == effect.method_5584());
      }
   }

   @Environment(EnvType.CLIENT)
   public enum FunTime implements DonItems.DonItem {
      CRUSHER_TRIDENT(DonItems.Category.WEAPON, "Трезубец Крушителя", "minecraft:trident", DonItems.ft("krush-trident")),
      CRUSHER_MACE(DonItems.Category.WEAPON, "Булава Крушителя", "minecraft:mace", DonItems.ft("krush-mace")),
      DISORIENTATION(DonItems.Category.COMBAT_ITEM, "Дезориентация", "minecraft:ender_eye", DonItems.ft("desorientation")),
      SHEER_DUST(DonItems.Category.COMBAT_ITEM, "Явная пыль", "minecraft:sugar", DonItems.ft("sheerdust")),
      GODS_AURA(DonItems.Category.COMBAT_ITEM, "Божья аура", "minecraft:phantom_membrane", DonItems.ft("godsaura")),
      CRUSHER_SWORD(DonItems.Category.WEAPON, "Меч Крушителя", "minecraft:netherite_sword", DonItems.ft("krush-sword")),
      SATAN_SWORD(DonItems.Category.WEAPON, "Меч Сатаны", "minecraft:netherite_sword", DonItems.ft("satan-sword")),
      KATANA(DonItems.Category.WEAPON, "Катана", "minecraft:netherite_sword", DonItems.ft("katana")),
      FREEZE_SNOWBALL(DonItems.Category.COMBAT_ITEM, "Снежок заморозка", "minecraft:snowball", DonItems.ft("freezeball")),
      TRAP(DonItems.Category.COMBAT_ITEM, "Трапка", "minecraft:netherite_scrap", DonItems.ft("trap")),
      CRUSHER_CROSSBOW(DonItems.Category.WEAPON, "Арбалет Крушителя", "minecraft:crossbow", DonItems.ft("krush-crossbow")),
      CRUSHER_BOW(DonItems.Category.WEAPON, "Лук Крушителя", "minecraft:bow", DonItems.ft("krush-bow")),
      SATAN_BOW(DonItems.Category.WEAPON, "Лук Сатаны", "minecraft:bow", DonItems.ft("satan-bow")),
      PHANTOM_BOW(DonItems.Category.WEAPON, "Лук Фантома", "minecraft:bow", DonItems.ft("phantom-bow")),
      STRATUM(DonItems.Category.COMBAT_ITEM, "Пласт", "minecraft:dried_kelp", DonItems.ft("stratum")),
      FIERY_TORNADO(DonItems.Category.COMBAT_ITEM, "Огненный смерч", "minecraft:fire_charge", DonItems.ft("fierytornado")),
      EMERALD_HELMET(DonItems.Category.ARMOR, "Изумрудный шлем", "minecraft:diamond_helmet", DonItems.ft("emerald-helmet")),
      EMERALD_CHESTPLATE(DonItems.Category.ARMOR, "Изумрудный нагрудник", "minecraft:diamond_chestplate", DonItems.ft("emerald-chestplate")),
      EMERALD_LEGGINGS(DonItems.Category.ARMOR, "Изумрудные поножи", "minecraft:diamond_leggings", DonItems.ft("emerald-leggings")),
      EMERALD_BOOTS(DonItems.Category.ARMOR, "Изумрудные ботинки", "minecraft:diamond_boots", DonItems.ft("emerald-boots")),
      SATAN_HELMET(DonItems.Category.ARMOR, "Шлем Сатаны", "minecraft:netherite_helmet", DonItems.ft("satan-helmet")),
      SATAN_CHESTPLATE(DonItems.Category.ARMOR, "Нагрудник Сатаны", "minecraft:netherite_chestplate", DonItems.ft("satan-chestplate")),
      SATAN_LEGGINGS(DonItems.Category.ARMOR, "Поножи Сатаны", "minecraft:netherite_leggings", DonItems.ft("satan-leggings")),
      SATAN_BOOTS(DonItems.Category.ARMOR, "Ботинки Сатаны", "minecraft:netherite_boots", DonItems.ft("satan-boots")),
      CRUSHER_HELMET(DonItems.Category.ARMOR, "Шлем Крушителя", "minecraft:netherite_helmet", DonItems.ft("krush-helmet")),
      CRUSHER_CHESTPLATE(DonItems.Category.ARMOR, "Нагрудник Крушителя", "minecraft:netherite_chestplate", DonItems.ft("krush-chestplate")),
      CRUSHER_LEGGINGS(DonItems.Category.ARMOR, "Поножи Крушителя", "minecraft:netherite_leggings", DonItems.ft("krush-leggings")),
      CRUSHER_BOOTS(DonItems.Category.ARMOR, "Ботинки Крушителя", "minecraft:netherite_boots", DonItems.ft("krush-boots")),
      ENHANCED_STRENGTH_POTION(
         DonItems.Category.POTION, "Зелье силы", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:strength", 3, 6000))
      ),
      ENHANCED_INVISIBILITY_POTION(
         DonItems.Category.POTION, "Зелье невидимости", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:invisibility", 1, 18000))
      ),
      ENHANCED_SPEED_POTION(
         DonItems.Category.POTION, "Зелье скорости", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:speed", 3, 7200))
      ),
      ENHANCED_LEAPING_POTION(
         DonItems.Category.POTION, "Зелье прыгучести", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:jump_boost", 1, 7200))
      ),
      ENHANCED_REGENERATION_POTION(
         DonItems.Category.POTION, "Зелье регенерации", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:regeneration", 1, 600))
      ),
      ENHANCED_NIGHT_VISION_POTION(
         DonItems.Category.POTION, "Зелье ночного зрения", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:night_vision", 1, 18000))
      ),
      ENHANCED_FIRE_RESISTANCE_POTION(
         DonItems.Category.POTION, "Зелье огнестойкости", "minecraft:potion", DonItems.named().effects(DonItems.effect("minecraft:fire_resistance", 1, 18000))
      ),
      ENHANCED_WATER_BREATHING_POTION(
         DonItems.Category.POTION,
         "Зелье водного дыхания",
         "minecraft:potion",
         DonItems.named().effects(DonItems.effect("minecraft:water_breathing", 1, 18000))
      ),
      POPPER(
         DonItems.Category.POTION,
         "Хлопушка",
         "minecraft:splash_potion",
         DonItems.ft("potion-popper")
            .effects(
               DonItems.effect("minecraft:slowness", 10, 200),
               DonItems.effect("minecraft:speed", 5, 400),
               DonItems.effect("minecraft:blindness", 10, 100),
               DonItems.effect("minecraft:glowing", 1, 3600)
            )
      ),
      HOLY_WATER(
         DonItems.Category.POTION,
         "Святая вода",
         "minecraft:splash_potion",
         DonItems.ft("potion-holy-water")
            .effects(
               DonItems.effect("minecraft:regeneration", 2, 900),
               DonItems.effect("minecraft:invisibility", 2, 12000),
               DonItems.effect("minecraft:instant_health", 2, 0)
            )
      ),
      RAGE_POTION(
         DonItems.Category.POTION,
         "Зелье Гнева",
         "minecraft:splash_potion",
         DonItems.ft("potion-rage").effects(DonItems.effect("minecraft:strength", 5, 600), DonItems.effect("minecraft:slowness", 4, 600))
      ),
      PALADIN_POTION(
         DonItems.Category.POTION,
         "Зелье Палладина",
         "minecraft:splash_potion",
         DonItems.ft("potion-paladin")
            .effects(
               DonItems.effect("minecraft:resistance", 1, 12000),
               DonItems.effect("minecraft:fire_resistance", 1, 12000),
               DonItems.effect("minecraft:health_boost", 3, 1200),
               DonItems.effect("minecraft:invisibility", 1, 18000)
            )
      ),
      ASSASSIN_POTION(
         DonItems.Category.POTION,
         "Зелье Ассасина",
         "minecraft:splash_potion",
         DonItems.ft("potion-assassin")
            .effects(
               DonItems.effect("minecraft:strength", 4, 1200),
               DonItems.effect("minecraft:speed", 3, 6000),
               DonItems.effect("minecraft:haste", 1, 1200),
               DonItems.effect("minecraft:instant_damage", 2, 0)
            )
      ),
      RADIATION_POTION(
         DonItems.Category.POTION,
         "Зелье Радиации",
         "minecraft:splash_potion",
         DonItems.ft("potion-radiation")
            .effects(
               DonItems.effect("minecraft:poison", 2, 1200),
               DonItems.effect("minecraft:wither", 2, 1200),
               DonItems.effect("minecraft:slowness", 3, 1800),
               DonItems.effect("minecraft:hunger", 5, 1200),
               DonItems.effect("minecraft:glowing", 1, 2400)
            )
      ),
      DROWSINESS_POTION(
         DonItems.Category.POTION,
         "Снотворное",
         "minecraft:splash_potion",
         DonItems.ft("potion-drowsiness")
            .effects(
               DonItems.effect("minecraft:weakness", 2, 1800),
               DonItems.effect("minecraft:mining_fatigue", 2, 200),
               DonItems.effect("minecraft:wither", 3, 1800),
               DonItems.effect("minecraft:blindness", 1, 200)
            )
      ),
      AGONY_ARROW(
         DonItems.Category.ARROW,
         "Мучительная стрела",
         "minecraft:tipped_arrow",
         DonItems.ft("arrow-agony")
            .effects(DonItems.effect("minecraft:slowness", 3, 800), DonItems.effect("minecraft:wither", 3, 800), DonItems.effect("minecraft:poison", 3, 800))
      ),
      ZEUS_ARROW(
         DonItems.Category.ARROW,
         "Стрела Зевса",
         "minecraft:tipped_arrow",
         DonItems.ft("arrow-zeus").effects(DonItems.effect("minecraft:instant_damage", 2, 0), DonItems.effect("minecraft:slowness", 3, 480))
      ),
      BLOOD_ARROW(
         DonItems.Category.ARROW,
         "Кровавая стрела",
         "minecraft:tipped_arrow",
         DonItems.ft("arrow-blood")
            .effects(
               DonItems.effect("minecraft:weakness", 3, 480),
               DonItems.effect("minecraft:blindness", 1, 320),
               DonItems.effect("minecraft:mining_fatigue", 1, 320),
               DonItems.effect("minecraft:nausea", 1, 800)
            )
      ),
      FREEZE_ARROW(
         DonItems.Category.ARROW,
         "Стрела обледенения",
         "minecraft:tipped_arrow",
         DonItems.ft("arrow-freeze").effects(DonItems.effect("minecraft:slowness", 10, 800), DonItems.effect("minecraft:mining_fatigue", 3, 320))
      ),
      LIGHT_ARROW(
         DonItems.Category.ARROW,
         "Световая стрела",
         "minecraft:tipped_arrow",
         DonItems.ft("arrow-light")
            .effects(DonItems.effect("minecraft:speed", 5, 800), DonItems.effect("minecraft:haste", 3, 800), DonItems.effect("minecraft:blindness", 1, 480))
      ),
      REJUVENATION_ARROW(
         DonItems.Category.ARROW,
         "Стрела терапии",
         "minecraft:tipped_arrow",
         DonItems.ft("arrow-rejuvenation")
            .effects(
               DonItems.effect("minecraft:slowness", 5, 1120),
               DonItems.effect("minecraft:weakness", 1, 1120),
               DonItems.effect("minecraft:instant_health", 1, 0)
            )
      ),
      DARKNESS_TALISMAN(DonItems.Category.TALISMAN, "Талисман Мрака", "minecraft:totem_of_undying", DonItems.ft("tal-mraka").model(1.0F)),
      WHIRLWIND_TALISMAN(DonItems.Category.TALISMAN, "Талисман Вихря", "minecraft:totem_of_undying", DonItems.ft("tal-vihrya").model(2.0F)),
      DEMON_TALISMAN(DonItems.Category.TALISMAN, "Талисман Демона", "minecraft:totem_of_undying", DonItems.ft("tal-demona").model(3.0F)),
      DISCORD_TALISMAN(DonItems.Category.TALISMAN, "Талисман Раздора", "minecraft:totem_of_undying", DonItems.ft("tal-razdora").model(4.0F)),
      FURY_TALISMAN(DonItems.Category.TALISMAN, "Талисман Ярости", "minecraft:totem_of_undying", DonItems.ft("tal-yarosti").model(5.0F)),
      CRUSHER_TALISMAN(DonItems.Category.TALISMAN, "Талисман Крушителя", "minecraft:totem_of_undying", DonItems.ft("tal-krush").model(6.0F)),
      PUNISHER_TALISMAN(DonItems.Category.TALISMAN, "Талисман Карателя", "minecraft:totem_of_undying", DonItems.named().model(7.0F)),
      TYRANT_TALISMAN(DonItems.Category.TALISMAN, "Талисман Тирана", "minecraft:totem_of_undying", DonItems.ft("tal-tirana").model(8.0F)),
      CHAOS_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Хаоса",
         "minecraft:player_head",
         DonItems.ft("sphere-haosa")
            .attributes(
               DonItems.attribute("minecraft:max_health", -4.0, "add_value"),
               DonItems.attribute("minecraft:armor", 1.5, "add_value"),
               DonItems.attribute("minecraft:attack_damage", 2.5, "add_value"),
               DonItems.attribute("minecraft:movement_speed", 0.07, "add_multiplied_base"),
               DonItems.attribute("minecraft:attack_speed", 0.13, "add_multiplied_base"),
               DonItems.attribute("minecraft:gravity", 0.09, "add_multiplied_base")
            )
      ),
      SATYR_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Сатира",
         "minecraft:player_head",
         DonItems.ft("sphere-satira")
            .attributes(
               DonItems.attribute("minecraft:attack_damage", 2.0, "add_value"),
               DonItems.attribute("minecraft:jump_strength", -0.1, "add_multiplied_base"),
               DonItems.attribute("minecraft:attack_speed", 0.15, "add_multiplied_base")
            )
      ),
      BEAST_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Бестии",
         "minecraft:player_head",
         DonItems.ft("sphere-bestia")
            .attributes(
               DonItems.attribute("minecraft:armor", 1.0, "add_value"),
               DonItems.attribute("minecraft:max_health", 4.0, "add_value"),
               DonItems.attribute("minecraft:movement_speed", 0.1, "add_multiplied_base"),
               DonItems.attribute("minecraft:attack_speed", 0.1, "add_multiplied_base")
            )
      ),
      ARES_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Ареса",
         "minecraft:player_head",
         DonItems.ft("sphere-aresa")
            .attributes(
               DonItems.attribute("minecraft:attack_damage", 6.0, "add_value"),
               DonItems.attribute("minecraft:armor", -2.0, "add_value"),
               DonItems.attribute("minecraft:max_health", -2.0, "add_value")
            )
      ),
      HYDRA_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Гидры",
         "minecraft:player_head",
         DonItems.ft("sphere-gidra")
            .attributes(
               DonItems.attribute("minecraft:max_health", 4.0, "add_value"),
               DonItems.attribute("minecraft:armor", 2.0, "add_value"),
               DonItems.attribute("minecraft:submerged_mining_speed", 0.5, "add_multiplied_base"),
               DonItems.attribute("minecraft:oxygen_bonus", 0.5, "add_multiplied_base")
            )
      ),
      ICARUS_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Икара",
         "minecraft:player_head",
         DonItems.ft("sphere-ikara")
            .attributes(DonItems.attribute("minecraft:attack_damage", 2.0, "add_value"), DonItems.attribute("minecraft:max_health", 2.0, "add_value"))
      ),
      ERIS_SPHERE(
         DonItems.Category.SPHERE,
         "Сфера Эрида",
         "minecraft:player_head",
         DonItems.ft("sphere-erida")
            .attributes(
               DonItems.attribute("minecraft:luck", 1.0, "add_value"),
               DonItems.attribute("minecraft:max_health", 2.0, "add_value"),
               DonItems.attribute("minecraft:block_interaction_range", 1.0, "add_value")
            )
      );

      private final DonItems.Category category;
      private final String displayName;
      private final String baseItemId;
      private final DonItems.MatchRule rule;

      FunTime(DonItems.Category category, String displayName, String baseItemId, DonItems.MatchRule rule) {
         this.category = category;
         this.displayName = displayName;
         this.baseItemId = baseItemId;
         this.rule = rule.alias(displayName);
      }

      @Override
      public DonItems.Server server() {
         return DonItems.Server.FUNTIME;
      }

      @Override
      public DonItems.Category category() {
         return this.category;
      }

      @Override
      public String displayName() {
         return this.displayName;
      }

      @Override
      public String baseItemId() {
         return this.baseItemId;
      }

      public Optional<String> stableId() {
         return Optional.ofNullable(this.rule.stableId);
      }
   }

   @Environment(EnvType.CLIENT)
   public enum HolyWorld implements DonItems.DonItem {
      WINNER_POTION(
         DonItems.Category.POTION,
         "Зелье победителя",
         "minecraft:potion",
         DonItems.named()
            .potionColor(1834794)
            .loreEffects(
               DonItems.loreEffect("скорость", "III", "8:00"),
               DonItems.loreEffect("сила", "III", "8:00"),
               DonItems.loreEffect("спешка", "II", "1:30"),
               DonItems.loreEffect("невидимость", "II", "8:00"),
               DonItems.loreEffect("сопротивление", "II"),
               DonItems.loreEffect("регенерация", "II")
            )
      ),
      ENHANCED_STRENGTH_POTION(
         DonItems.Category.POTION,
         "Улучшенное зелье силы",
         "minecraft:potion",
         DonItems.named().potionColor(16718619).loreEffects(DonItems.loreEffect("сила", null, "6:00"))
      ),
      ENHANCED_SPEED_POTION(
         DonItems.Category.POTION,
         "Улучшенное зелье скорости",
         "minecraft:potion",
         DonItems.named().potionColor(1434111).loreEffects(DonItems.loreEffect("скорость", null, "3:00", "6:00"))
      ),
      EXPLOSIVE_MATERIAL(DonItems.Category.EXPLOSIVE, "Взрывчатое вещество", "minecraft:clay", DonItems.named()),
      EXPLOSIVE_TRAP(DonItems.Category.EXPLOSIVE, "Взрывная трапка", "minecraft:prismarine_shard", DonItems.named()),
      TRAP(DonItems.Category.EXPLOSIVE, "Трапка", "minecraft:popped_chorus_fruit", DonItems.named()),
      TNT_CANNON(
         DonItems.Category.EXPLOSIVE, "Тнт-Пушка", "minecraft:dispenser", DonItems.named().numberMarker("PublicBukkitValues/litetntcannon:tnt-cannon", 1.0)
      ),
      DYNAMITE(DonItems.Category.EXPLOSIVE, "Динамит", "minecraft:tnt", DonItems.named()),
      DYNAMITE_A(DonItems.Category.EXPLOSIVE, "Динамит A", "minecraft:tnt", DonItems.named()),
      DYNAMITE_B(DonItems.Category.EXPLOSIVE, "Динамит B", "minecraft:tnt", DonItems.named()),
      C4(DonItems.Category.EXPLOSIVE, "С4 Взрывчатка", "minecraft:tnt", DonItems.named()),
      BLAST_WAVE(DonItems.Category.EXPLOSIVE, "Разрывная волна", "minecraft:tnt", DonItems.named()),
      DYNAMITE_B2(DonItems.Category.EXPLOSIVE, "Динамит Б2", "minecraft:tnt", DonItems.named()),
      STEALER(DonItems.Category.EXPLOSIVE, "Стиллер", "minecraft:tnt", DonItems.named()),
      RELIABLE_STEALER(DonItems.Category.EXPLOSIVE, "Надёжный стиллер", "minecraft:tnt", DonItems.named().alias("Надежный стиллер")),
      ICE_WAVE(DonItems.Category.EXPLOSIVE, "Ледяная волна", "minecraft:tnt", DonItems.named()),
      FILTER_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Фильтр", "minecraft:enchanted_book", DonItems.prefix("Фильтр")),
      STUN_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Оглушение", "minecraft:enchanted_book", DonItems.prefix("Оглушение")),
      SOWING_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Посев", "minecraft:enchanted_book", DonItems.prefix("Посев")),
      IMPENETRABLE_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Непробиваемый", "minecraft:enchanted_book", DonItems.prefix("Непробиваемый")),
      INDESTRUCTIBILITY_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Неразрушимость", "minecraft:enchanted_book", DonItems.prefix("Неразрушимость")),
      EXPERIENCED_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Опытный", "minecraft:enchanted_book", DonItems.prefix("Опытный")),
      CRITICAL_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Критический", "minecraft:enchanted_book", DonItems.prefix("Критический")),
      ENCHANTMENT_COMBINER(DonItems.Category.CUSTOM_ENCHANTMENT, "Объединение зачарований", "minecraft:enchanted_book", DonItems.named()),
      DRILL_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Бур", "minecraft:enchanted_book", DonItems.prefix("Бур")),
      RICH_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Богач", "minecraft:enchanted_book", DonItems.prefix("Богач")),
      AUTO_SMELT_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Автоплавка", "minecraft:enchanted_book", DonItems.prefix("Автоплавка")),
      LAVA_WALKER_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Лаваход", "minecraft:enchanted_book", DonItems.prefix("Лаваход")),
      DESTROYER_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Разрушитель", "minecraft:enchanted_book", DonItems.prefix("Разрушитель")),
      FARMER_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Фермер", "minecraft:enchanted_book", DonItems.prefix("Фермер")),
      MAGNETISM_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Магнетизм", "minecraft:enchanted_book", DonItems.named()),
      LUMBERJACK_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Дровосек", "minecraft:enchanted_book", DonItems.prefix("Дровосек")),
      MEGA_DRILL_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Мега-Бур", "minecraft:enchanted_book", DonItems.prefix("Мега-Бур")),
      HOMING_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Самонаводка", "minecraft:enchanted_book", DonItems.prefix("Самонаводка")),
      CRUSHER_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Крушитель", "minecraft:enchanted_book", DonItems.prefix("Крушитель")),
      DELICATE_ENCHANT(DonItems.Category.CUSTOM_ENCHANTMENT, "Деликатный", "minecraft:enchanted_book", DonItems.prefix("Деликатный")),
      COMMON_SPHERE(DonItems.Category.SPHERE, "Обычная сфера", "minecraft:player_head", DonItems.named()),
      EPIC_SPHERE(DonItems.Category.SPHERE, "Эпическая сфера", "minecraft:player_head", DonItems.named()),
      LEGENDARY_SPHERE(DonItems.Category.SPHERE, "Легендарная сфера", "minecraft:player_head", DonItems.named()),
      MYTHIC_SPHERE(DonItems.Category.SPHERE, "Мифическая сфера", "minecraft:player_head", DonItems.named()),
      COMMON_TALISMAN(DonItems.Category.TALISMAN, "Обычный талисман", "minecraft:totem_of_undying", DonItems.named()),
      EPIC_TALISMAN(DonItems.Category.TALISMAN, "Эпический талисман", "minecraft:totem_of_undying", DonItems.named()),
      LEGENDARY_TALISMAN(DonItems.Category.TALISMAN, "Легендарный талисман", "minecraft:totem_of_undying", DonItems.named()),
      ETERNITY_TALISMAN(
         DonItems.Category.TALISMAN,
         "Талисман Eternity",
         "minecraft:totem_of_undying",
         DonItems.named().stringMarker("itemServiceId/name", "сфера_eternity").stringMarker("sphereEffect/name", "Eternity")
      ),
      IMMORTALITY_RUNE(DonItems.Category.RUNE, "Бессмертие", "minecraft:orange_dye", DonItems.named()),
      RESTORATION_RUNE(DonItems.Category.RUNE, "Восстановление", "minecraft:red_dye", DonItems.named()),
      BACKPACK_I(DonItems.Category.BACKPACK, "Рюкзак I уровень", "minecraft:pink_shulker_box", DonItems.named().alias("Рюкзак (I уровень)")),
      BACKPACK_II(DonItems.Category.BACKPACK, "Рюкзак II уровень", "minecraft:light_blue_shulker_box", DonItems.named().alias("Рюкзак (II уровень)")),
      BACKPACK_III(DonItems.Category.BACKPACK, "Рюкзак III уровень", "minecraft:red_shulker_box", DonItems.named().alias("Рюкзак (III уровень)")),
      BACKPACK_IV(DonItems.Category.BACKPACK, "Рюкзак IV уровень", "minecraft:magenta_shulker_box", DonItems.named().alias("Рюкзак (IV уровень)")),
      INFINITY_BACKPACK(DonItems.Category.BACKPACK, "Рюкзак Infinity", "minecraft:lime_shulker_box", DonItems.named().alias("Рюкзак Iɴғɪɴɪᴛʏ")),
      SPECIAL_COMPASS(DonItems.Category.MISC, "Особый компас", "minecraft:compass", DonItems.named().stringMarker("kringeItems/type", "RegionRadar")),
      ENCHANTED_APPLE(DonItems.Category.MISC, "Зачарованное яблоко", "minecraft:enchanted_golden_apple", DonItems.named()),
      STAN(DonItems.Category.COMBAT_ITEM, "Стан", "minecraft:nether_star", DonItems.named()),
      JAKES_GOLDEN_PICKAXE(DonItems.Category.TOOL, "Золотая кирка Джейка", "minecraft:golden_pickaxe", DonItems.named()),
      GOLDEN_SPAWNER(DonItems.Category.MISC, "Золотой Спавнер", "minecraft:spawner", DonItems.named()),
      UNIQUE_CLAIM(DonItems.Category.MISC, "Уникальный приват", "minecraft:ancient_debris", DonItems.named()),
      ANCIENT_DEBRIS(DonItems.Category.MISC, "Древние обломки", "minecraft:ancient_debris", DonItems.named()),
      SUN_HELMET(DonItems.Category.ARMOR, "Шлем солнца", "minecraft:golden_helmet", DonItems.named().stringMarker("kringeItems/type", "SunHelmet")),
      UNIVERSAL_KEY(DonItems.Category.MISC, "Универсальный ключ", "minecraft:tripwire_hook", DonItems.named().model(123433.0F)),
      VEX_SPAWN_EGG(DonItems.Category.MISC, "Vex Spawn Egg", "minecraft:vex_spawn_egg", DonItems.named()),
      ETERNITY_SHOVEL(DonItems.Category.TOOL, "Лопата Eternity", "minecraft:netherite_shovel", DonItems.named().alias("Лопата ᴇᴛᴇʀɴɪᴛʏ")),
      UNBREAKABLE_ELYTRA(DonItems.Category.ARMOR, "Нерушимые элитры", "minecraft:elytra", DonItems.named());

      private final DonItems.Category category;
      private final String displayName;
      private final String baseItemId;
      private final DonItems.MatchRule rule;

      HolyWorld(DonItems.Category category, String displayName, String baseItemId, DonItems.MatchRule rule) {
         this.category = category;
         this.displayName = displayName;
         this.baseItemId = baseItemId;
         this.rule = rule.alias(displayName);
      }

      @Override
      public DonItems.Server server() {
         return DonItems.Server.HOLYWORLD;
      }

      @Override
      public DonItems.Category category() {
         return this.category;
      }

      @Override
      public String displayName() {
         return this.displayName;
      }

      @Override
      public String baseItemId() {
         return this.baseItemId;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class Index {
      private static final Map<DonItems.Server, List<DonItems.DonItem>> ITEMS_BY_SERVER;
      private static final Map<DonItems.Server, Map<String, List<DonItems.DonItem>>> ITEMS_BY_BASE_ITEM;
      private static final Map<String, DonItems.FunTime> FUNTIME_BY_ID;

      static {
         EnumMap<DonItems.Server, List<DonItems.DonItem>> byServer = new EnumMap<>(DonItems.Server.class);
         byServer.put(DonItems.Server.FUNTIME, List.copyOf(Arrays.asList(DonItems.FunTime.values())));
         byServer.put(DonItems.Server.HOLYWORLD, List.copyOf(Arrays.asList(DonItems.HolyWorld.values())));
         byServer.put(DonItems.Server.REALLYWORLD, List.copyOf(Arrays.asList(DonItems.ReallyWorld.values())));
         ITEMS_BY_SERVER = Collections.unmodifiableMap(byServer);
         EnumMap<DonItems.Server, Map<String, List<DonItems.DonItem>>> byBaseItem = new EnumMap<>(DonItems.Server.class);

         for (Entry<DonItems.Server, List<DonItems.DonItem>> entry : byServer.entrySet()) {
            Map<String, List<DonItems.DonItem>> index = new LinkedHashMap<>();

            for (DonItems.DonItem item : entry.getValue()) {
               index.computeIfAbsent(item.baseItemId(), ignored -> new ArrayList<>()).add(item);
            }

            index.replaceAll((ignored, items) -> List.copyOf((Collection<? extends DonItems.DonItem>)items));
            byBaseItem.put(entry.getKey(), Collections.unmodifiableMap(index));
         }

         ITEMS_BY_BASE_ITEM = Collections.unmodifiableMap(byBaseItem);
         Map<String, DonItems.FunTime> byFunTimeId = new HashMap<>();

         for (DonItems.FunTime item : DonItems.FunTime.values()) {
            if (item.rule.stableId != null) {
               DonItems.FunTime previous = byFunTimeId.put(item.rule.stableId, item);
               if (previous != null) {
                  throw new IllegalStateException("Duplicate FunTime item id: " + item.rule.stableId);
               }
            }
         }

         FUNTIME_BY_ID = Collections.unmodifiableMap(byFunTimeId);
      }
   }

   @Environment(EnvType.CLIENT)
   private record LoreEffectSignature(String name, String level, List<String> durations) {
      private LoreEffectSignature {
         durations = List.copyOf(durations);
      }

      private boolean matches(String normalizedLine, boolean exactLevel, boolean exactDuration) {
         if (!normalizedLine.contains(this.name)) {
            return false;
         } else {
            return exactLevel && this.level != null && !normalizedLine.contains(this.level)
               ? false
               : !exactDuration || this.durations.isEmpty() || !this.durations.stream().noneMatch(normalizedLine::contains);
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class MatchRule {
      private final String stableId;
      private final Set<String> aliases = new LinkedHashSet<>();
      private final Set<String> prefixes = new LinkedHashSet<>();
      private final List<DonItems.NbtMarker> markers = new ArrayList<>();
      private Float model;
      private Integer potionColor;
      private List<DonItems.EffectSignature> effects = List.of();
      private List<DonItems.LoreEffectSignature> loreEffects = List.of();
      private List<DonItems.AttributeSignature> attributes = List.of();

      private MatchRule(String stableId) {
         this.stableId = stableId;
      }

      private DonItems.MatchRule alias(String... values) {
         for (String value : values) {
            String normalized = DonItems.normalizeName(value);
            if (!normalized.isEmpty()) {
               this.aliases.add(normalized);
            }
         }

         return this;
      }

      private DonItems.MatchRule prefix(String... values) {
         for (String value : values) {
            String normalized = DonItems.normalizeName(value);
            if (!normalized.isEmpty()) {
               this.prefixes.add(normalized);
            }
         }

         return this;
      }

      private DonItems.MatchRule model(float value) {
         this.model = value;
         return this;
      }

      private DonItems.MatchRule potionColor(int value) {
         this.potionColor = value;
         return this;
      }

      private DonItems.MatchRule effects(DonItems.EffectSignature... values) {
         this.effects = List.of(values);
         return this;
      }

      private DonItems.MatchRule loreEffects(DonItems.LoreEffectSignature... values) {
         this.loreEffects = List.of(values);
         return this;
      }

      private DonItems.MatchRule attributes(DonItems.AttributeSignature... values) {
         this.attributes = List.of(values);
         return this;
      }

      private DonItems.MatchRule stringMarker(String path, String value) {
         this.markers.add(DonItems.NbtMarker.string(path, value));
         return this;
      }

      private DonItems.MatchRule numberMarker(String path, double value) {
         this.markers.add(DonItems.NbtMarker.number(path, value));
         return this;
      }

      private boolean nameMatches(String normalizedName) {
         if (this.aliases.contains(normalizedName)) {
            return true;
         }

         for (String prefix : this.prefixes) {
            if (normalizedName.equals(prefix) || normalizedName.startsWith(prefix + " ")) {
               return true;
            }
         }

         return false;
      }

      private boolean strongMatches(DonItems.StackView view) {
         if (this.stableId != null && this.stableId.equals(view.funTimeId().orElse(null))) {
            return true;
         }

         if (this.model != null && DonItems.modelMatches(view.stack, view.customData, this.model)) {
            return true;
         }

         if (this.potionColor != null && DonItems.potionColorMatches(view.stack, this.potionColor)) {
            return true;
         }

         if (!this.effects.isEmpty() && DonItems.effectsMatch(view.stack, this.effects)) {
            return true;
         }

         if (!this.attributes.isEmpty() && DonItems.attributesMatch(view.stack, this.attributes)) {
            return true;
         }

         for (DonItems.NbtMarker marker : this.markers) {
            if (marker.matches(view.customData)) {
               return true;
            }
         }

         return false;
      }
   }

   @Environment(EnvType.CLIENT)
   private record NbtMarker(List<String> path, String stringValue, Double numberValue) {
      private static DonItems.NbtMarker string(String path, String value) {
         return new DonItems.NbtMarker(splitPath(path), value, null);
      }

      private static DonItems.NbtMarker number(String path, double value) {
         return new DonItems.NbtMarker(splitPath(path), null, value);
      }

      private boolean matches(class_2487 root) {
         if (this.path.isEmpty()) {
            return false;
         }

         class_2487 current = root;

         for (int index = 0; index < this.path.size() - 1; index++) {
            Optional<class_2487> child = current.method_10562(this.path.get(index));
            if (child.isEmpty()) {
               return false;
            }

            current = child.get();
         }

         String key = (String)this.path.getLast();
         if (this.stringValue != null) {
            return current.method_10558(key).map(this.stringValue::equals).orElse(false);
         } else {
            return this.numberValue != null ? current.method_10574(key).map(value -> Math.abs(value - this.numberValue) <= 1.0E-6).orElse(false) : false;
         }
      }

      private static List<String> splitPath(String path) {
         return path != null && !path.isBlank() ? Arrays.stream(path.split("/")).filter(part -> !part.isBlank()).toList() : List.of();
      }
   }

   @Environment(EnvType.CLIENT)
   public record PotionEffectProfile(String effectId, int level, int durationTicks) {
   }

   @Environment(EnvType.CLIENT)
   public enum ReallyWorld implements DonItems.DonItem {
      GRINCH_POTION(DonItems.Category.POTION, "Зелье Гринча", "minecraft:potion", DonItems.named().alias("гринч")),
      NEW_YEAR_HORROR(DonItems.Category.COMBAT_ITEM, "Новогодний ужас", "minecraft:snowball", DonItems.named().alias("новогодний ужас")),
      DARKNESS_ESSENCE(DonItems.Category.COMBAT_ITEM, "Эссенция кромешника", "minecraft:dragon_breath", DonItems.named().alias("эссенция кромешника")),
      SNOWBALL(DonItems.Category.COMBAT_ITEM, "Снежок", "minecraft:snowball", DonItems.named().alias("снежок")),
      TRAP(DonItems.Category.COMBAT_ITEM, "Ловушка", "minecraft:netherite_scrap", DonItems.named().alias("ловушка"));

      private final DonItems.Category category;
      private final String displayName;
      private final String baseItemId;
      private final DonItems.MatchRule rule;

      ReallyWorld(DonItems.Category category, String displayName, String baseItemId, DonItems.MatchRule rule) {
         this.category = category;
         this.displayName = displayName;
         this.baseItemId = baseItemId;
         this.rule = rule.alias(displayName);
      }

      @Override
      public DonItems.Server server() {
         return DonItems.Server.REALLYWORLD;
      }

      @Override
      public DonItems.Category category() {
         return this.category;
      }

      @Override
      public String displayName() {
         return this.displayName;
      }

      @Override
      public String baseItemId() {
         return this.baseItemId;
      }
   }

   @Environment(EnvType.CLIENT)
   public enum Server {
      FUNTIME("funtime", "spookytime", "lonygrief", "lony-grief"),
      HOLYWORLD("holyworld"),
      REALLYWORLD("reallyworld", "really-world");

      private final Set<String> addressMarkers;

      Server(String... addressMarkers) {
         this.addressMarkers = Set.of(addressMarkers);
      }

      public boolean matchesAddress(String address) {
         if (address != null && !address.isBlank()) {
            String normalized = address.toLowerCase(Locale.ROOT);
            return this.addressMarkers.stream().anyMatch(normalized::contains);
         } else {
            return false;
         }
      }

      public static Optional<DonItems.Server> fromAddress(String address) {
         return Arrays.stream(values()).filter(server -> server.matchesAddress(address)).findFirst();
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class StackView {
      private final class_1799 stack;
      private final String baseItemId;
      private final String normalizedName;
      private final class_2487 customData;

      private StackView(class_1799 stack) {
         this.stack = stack;
         this.baseItemId = DonItems.baseItemId(stack);
         this.normalizedName = DonItems.normalizeName(stack.method_7964().getString());
         class_9279 customData = (class_9279)stack.method_58694(class_9334.field_49628);
         this.customData = customData == null ? new class_2487() : customData.method_57461();
      }

      private Optional<String> funTimeId() {
         return this.customData.method_10562("PublicBukkitValues").flatMap(values -> values.method_10558("minecraft:ftid")).filter(value -> !value.isBlank());
      }
   }
}
