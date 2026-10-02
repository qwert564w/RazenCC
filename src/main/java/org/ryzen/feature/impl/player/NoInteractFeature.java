package org.ryzen.feature.impl.player;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2199;
import net.minecraft.class_2269;
import net.minecraft.class_2281;
import net.minecraft.class_2304;
import net.minecraft.class_2315;
import net.minecraft.class_2323;
import net.minecraft.class_2349;
import net.minecraft.class_2377;
import net.minecraft.class_2401;
import net.minecraft.class_2428;
import net.minecraft.class_2533;
import net.minecraft.class_2680;
import net.minecraft.class_3865;
import net.minecraft.class_3965;
import org.ryzen.context.MinecraftContext;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;

@Environment(EnvType.CLIENT)
public final class NoInteractFeature extends Feature implements MinecraftContext {
   private static final String DOORS = "Doors";
   private static final String BUTTONS = "Buttons";
   private static final String CHESTS = "Chests";
   private static final String HOPPERS = "Hoppers";
   private static final String DISPENSERS = "Dispensers";
   private static final String NOTE_BLOCKS = "Note Blocks";
   private static final String CRAFTING_TABLES = "Crafting Tables";
   private static final String TRAPDOORS = "Trapdoors";
   private static final String FURNACES = "Furnaces";
   private static final String FENCE_GATES = "Fence Gates";
   private static final String ANVILS = "Anvils";
   private static final String LEVERS = "Levers";
   public final BooleanSetting allBlocks = this.register(new BooleanSetting("All Blocks", false));
   public final MultiSelectSetting blocks = this.register(
      new MultiSelectSetting(
            "Blocks",
            List.of(
               "Doors",
               "Buttons",
               "Chests",
               "Hoppers",
               "Dispensers",
               "Note Blocks",
               "Crafting Tables",
               "Trapdoors",
               "Furnaces",
               "Fence Gates",
               "Anvils",
               "Levers"
            ),
            "Doors",
            "Buttons",
            "Chests",
            "Hoppers",
            "Dispensers",
            "Note Blocks",
            "Crafting Tables",
            "Trapdoors",
            "Furnaces",
            "Fence Gates",
            "Anvils",
            "Levers"
         )
         .visibleWhen(() -> !this.allBlocks.getValue())
   );

   public NoInteractFeature() {
      super("NoInteract", "Blocks right-click use on chosen blocks", FeatureCategory.PLAYER, -1);
   }

   public static boolean shouldCancel(class_3965 hit) {
      NoInteractFeature feature = FeatureManager.INSTANCE.getEnabled(NoInteractFeature.class);
      if (feature == null || hit == null || mc.field_1687 == null || mc.field_1724 == null) {
         return false;
      }

      if (mc.field_1724.method_5715()) {
         return false;
      }

      class_2680 state = mc.field_1687.method_8320(hit.method_17777());
      return feature.allBlocks.getValue() || feature.isSelected(state);
   }

   private boolean isSelected(class_2680 state) {
      return this.matches(state, class_2323.class, "Doors")
         || this.matches(state, class_2269.class, "Buttons")
         || this.matches(state, class_2281.class, "Chests")
         || this.matches(state, class_2377.class, "Hoppers")
         || this.matches(state, class_2315.class, "Dispensers")
         || this.matches(state, class_2428.class, "Note Blocks")
         || this.matches(state, class_2304.class, "Crafting Tables")
         || this.matches(state, class_2533.class, "Trapdoors")
         || this.matches(state, class_3865.class, "Furnaces")
         || this.matches(state, class_2349.class, "Fence Gates")
         || this.matches(state, class_2199.class, "Anvils")
         || this.matches(state, class_2401.class, "Levers");
   }

   private boolean matches(class_2680 state, Class<?> type, String option) {
      return type.isInstance(state.method_26204()) && this.blocks.isSelected(option);
   }
}
