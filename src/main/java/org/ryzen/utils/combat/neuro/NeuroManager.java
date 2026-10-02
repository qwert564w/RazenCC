package org.ryzen.utils.combat.neuro;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.combat.AiTrainingFeature;

@Environment(EnvType.CLIENT)
public final class NeuroManager {
   private static final NeuroRotation ACTIVE = new NeuroRotation();
   private static String selectedName = "";

   private NeuroManager() {
   }

   public static NeuroRotation activeRotation() {
      return ACTIVE;
   }

   public static String selectedName() {
      return selectedName;
   }

   public static boolean hasSelection() {
      return !selectedName.isEmpty() && ACTIVE.hasData();
   }

   public static boolean select(String name) {
      NeuroRotationData data = NeuroSampleStore.load(name);
      if (data != null && data.sampleCount() != 0) {
         ACTIVE.setData(data);
         selectedName = name;
         return true;
      } else {
         return false;
      }
   }

   public static void clearSelection() {
      ACTIVE.setData(null);
      selectedName = "";
   }

   public static AiTrainingFeature training() {
      return FeatureManager.INSTANCE.getFeature(AiTrainingFeature.class);
   }
}
