package org.ryzen.utils.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.setting.MultiSelectSetting;

@Environment(EnvType.CLIENT)
public final class CombatTargets {
   public static final String PLAYERS = "Players";
   public static final String FRIENDS = "Friends";
   public static final String NAKED_PLAYERS = "Naked Players";
   public static final String INVISIBLES = "Invisibles";
   public static final String MONSTERS = "Monsters";
   public static final String ANIMALS = "Animals";
   public static final String VILLAGERS = "Villagers";

   private CombatTargets() {
   }

   public static TargetFilter.TargetFilterBuilder groups(TargetFilter.TargetFilterBuilder builder, MultiSelectSetting targets) {
      return builder.targetsPlayers(targets.isSelected("Players"))
         .targetsFriends(targets.isSelected("Friends"))
         .targetsNakedPlayers(targets.isSelected("Naked Players"))
         .targetsInvisibles(targets.isSelected("Invisibles"))
         .targetsMonsters(targets.isSelected("Monsters"))
         .targetsAnimals(targets.isSelected("Animals"))
         .targetsVillagers(targets.isSelected("Villagers"));
   }
}
