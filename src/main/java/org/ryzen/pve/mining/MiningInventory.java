package org.ryzen.pve.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2960;
import net.minecraft.class_3489;
import net.minecraft.class_746;
import net.minecraft.class_7923;

@Environment(EnvType.CLIENT)
public final class MiningInventory {
   private static final List<class_2248> COMMON_TUNNEL_BLOCKS = List.of(
      class_2246.field_10340,
      class_2246.field_10445,
      class_2246.field_28888,
      class_2246.field_29031,
      class_2246.field_27165,
      class_2246.field_10115,
      class_2246.field_10508,
      class_2246.field_10474,
      class_2246.field_10566,
      class_2246.field_10253,
      class_2246.field_10255,
      class_2246.field_10515,
      class_2246.field_23869,
      class_2246.field_22091,
      class_2246.field_10471
   );

   private MiningInventory() {
   }

   public static int freeSlots(class_746 player) {
      int free = 0;

      for (int slot = 0; slot < 36; slot++) {
         if (player.method_31548().method_5438(slot).method_7960()) {
            free++;
         }
      }

      return free;
   }

   public static int bestPickaxeSlot(class_746 player) {
      int bestSlot = -1;
      int bestRemaining = -1;

      for (int slot = 0; slot < 36; slot++) {
         class_1799 stack = player.method_31548().method_5438(slot);
         if (isPickaxe(stack)) {
            int remaining = remainingDurability(stack);
            if (remaining > bestRemaining) {
               bestRemaining = remaining;
               bestSlot = slot;
            }
         }
      }

      return bestSlot;
   }

   public static boolean isPickaxe(class_1799 stack) {
      return stack != null && !stack.method_7960() && stack.method_31573(class_3489.field_42614);
   }

   public static int remainingDurability(class_1799 stack) {
      return stack != null && !stack.method_7960() && stack.method_7963() ? Math.max(0, stack.method_7936() - stack.method_7919()) : Integer.MAX_VALUE;
   }

   public static double durabilityPercent(class_1799 stack) {
      return stack != null && !stack.method_7960() && stack.method_7963() ? remainingDurability(stack) * 100.0 / Math.max(1, stack.method_7936()) : 100.0;
   }

   public static String itemId(class_1799 stack) {
      return stack != null && !stack.method_7960() ? class_7923.field_41178.method_10221(stack.method_7909()).toString().toLowerCase(Locale.ROOT) : "";
   }

   public static boolean isTrash(class_1799 stack, Set<String> trashIds) {
      if (stack == null || stack.method_7960() || trashIds == null || trashIds.isEmpty()) {
         return false;
      } else {
         return !stack.method_7963() && stack.method_58657().method_57543() ? trashIds.contains(itemId(stack)) : false;
      }
   }

   public static boolean isDepositItem(class_1799 stack, Set<String> configuredIds) {
      if (stack != null && !stack.method_7960()) {
         String id = itemId(stack);
         if (configuredIds != null && configuredIds.contains(id)) {
            return true;
         }

         String path = id.substring(id.indexOf(58) + 1);
         return path.startsWith("raw_")
            || path.endsWith("_ore")
            || path.equals("coal")
            || path.equals("diamond")
            || path.equals("emerald")
            || path.equals("lapis_lazuli")
            || path.equals("redstone")
            || path.equals("amethyst_shard")
            || path.equals("nether_quartz")
            || path.equals("ancient_debris")
            || path.equals("gold_nugget");
      } else {
         return false;
      }
   }

   public static List<class_2248> resolveBlocks(Set<String> ids) {
      List<class_2248> blocks = new ArrayList<>();
      if (ids == null) {
         return blocks;
      }

      for (String id : ids) {
         try {
            class_2248 block = (class_2248)class_7923.field_41175.method_63535(class_2960.method_60654(id));
            if (block != null && block != class_2246.field_10124 && !blocks.contains(block)) {
               blocks.add(block);
            }
         } catch (RuntimeException var5) {
         }
      }

      return List.copyOf(blocks);
   }

   public static boolean isOre(class_2248 block) {
      if (block != null && block != class_2246.field_10124) {
         String path = class_7923.field_41175.method_10221(block).method_12832();
         return path.endsWith("_ore") || path.equals("ancient_debris") || path.equals("gilded_blackstone");
      } else {
         return false;
      }
   }

   public static List<class_2248> commonTunnelBlocks() {
      return COMMON_TUNNEL_BLOCKS;
   }

   public static boolean isCommonTunnelBlock(class_2248 block) {
      return COMMON_TUNNEL_BLOCKS.contains(block);
   }

   public static boolean isSafeBreakTarget(class_2248 block) {
      if (block != null && block != class_2246.field_10124) {
         String path = class_7923.field_41175.method_10221(block).method_12832();
         return !Set.of(
               "bedrock",
               "barrier",
               "command_block",
               "chain_command_block",
               "repeating_command_block",
               "structure_block",
               "jigsaw",
               "end_portal",
               "end_portal_frame",
               "nether_portal",
               "moving_piston",
               "light"
            )
            .contains(path);
      } else {
         return false;
      }
   }
}
