package org.ryzen.utils.render;

import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2960;
import net.minecraft.class_8685;
import net.minecraft.class_12079.class_12081;
import org.ryzen.context.MinecraftContext;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.visual.CapeFeature;

@Environment(EnvType.CLIENT)
public final class ClientCape {
   private ClientCape() {
   }

   public static boolean shouldForceCape(UUID playerUuid) {
      if (MinecraftContext.mc.field_1724 == null) {
         return false;
      }

      CapeFeature feature = getFeature();
      return feature != null && feature.isEnabled() ? MinecraftContext.mc.field_1724.method_5667().equals(playerUuid) : false;
   }

   public static class_8685 apply(class_8685 skin) {
      CapeFeature feature = getFeature();
      String fileName = feature != null ? feature.currentStyle().getFileName() : "glass.png";
      final class_2960 dynamicTextureId = class_2960.method_60655("ryzen", "textures/cape/" + fileName);
      class_12081 dynamicTexture = new class_12081() {
         public class_2960 comp_3626() {
            return dynamicTextureId;
         }

         public class_2960 comp_3627() {
            return dynamicTextureId;
         }
      };
      return new class_8685(skin.comp_1626(), dynamicTexture, skin.comp_1628(), skin.comp_1629(), skin.comp_1630());
   }

   private static CapeFeature getFeature() {
      return FeatureManager.INSTANCE.getFeature(CapeFeature.class);
   }
}
