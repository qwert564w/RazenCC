package org.ryzen.utils.render;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;

@Environment(EnvType.CLIENT)
public final class EntityEspStateCache {
   private static volatile List<class_10017> frameStates = Collections.emptyList();

   private EntityEspStateCache() {
   }

   public static void capture(List<class_10017> states) {
      frameStates = states != null && !states.isEmpty() ? new ArrayList<>(states) : Collections.emptyList();
   }

   public static List<class_10017> currentStates() {
      return frameStates;
   }

   public static void clear() {
      frameStates = Collections.emptyList();
   }
}
