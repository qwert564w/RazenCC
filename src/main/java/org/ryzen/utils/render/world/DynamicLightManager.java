package org.ryzen.utils.render.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_1542;
import net.minecraft.class_1747;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1944;
import net.minecraft.class_2338;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3562;
import net.minecraft.class_638;
import net.minecraft.class_746;
import org.ryzen.feature.impl.player.FullBrightFeature;
import org.ryzen.utils.render.Render3DUtil;

@Environment(EnvType.CLIENT)
public final class DynamicLightManager {
   public static final DynamicLightManager INSTANCE = new DynamicLightManager();
   private static final int MAX_CANDIDATES = 64;
   private static final int MAX_SHADER_LIGHTS = 16;
   private static final double MAX_DISTANCE_SQR = 4096.0;
   private volatile Map<Long, Integer> virtualLuminance = Map.of();
   private List<DynamicLightManager.Candidate> candidates = List.of();
   private class_638 engineLevel;

   private DynamicLightManager() {
   }

   public static int virtualLuminance(long blockPos) {
      return INSTANCE.virtualLuminance.getOrDefault(blockPos, 0);
   }

   public void tick(class_310 minecraft, FullBrightFeature feature) {
      class_746 player = minecraft.field_1724;
      class_638 level = minecraft.field_1687;
      if (player != null && level != null) {
         List<DynamicLightManager.Candidate> collected = new ArrayList<>();
         DynamicLightManager.LightSpec localLight = strongest(lightFor(player.method_6047()), lightFor(player.method_6079()));
         if (localLight != null) {
            collected.add(DynamicLightManager.Candidate.entity(player, player.method_5751() * 0.72, localLight));
         }

         for (class_1297 entity : level.method_18112()) {
            if (entity != player && !entity.method_31481() && !(entity.method_5858(player) > 4096.0)) {
               DynamicLightManager.LightSpec source = null;
               double yOffset = entity.method_17682() * 0.5;
               if (feature.lightItems.getValue() && entity instanceof class_1542 itemEntity) {
                  source = lightFor(itemEntity.method_6983());
                  yOffset = 0.2;
               }

               if (feature.lightOthers.getValue() && entity instanceof class_1309 living) {
                  source = strongest(source, strongest(lightFor(living.method_6047()), lightFor(living.method_6079())));
               }

               if (feature.lightOthers.getValue() && entity.method_5862()) {
                  source = strongest(source, DynamicLightManager.LightSpec.FIRE);
               }

               if (source != null) {
                  collected.add(DynamicLightManager.Candidate.entity(entity, yOffset, source));
               }
            }
         }

         collected.sort(Comparator.comparingDouble(candidate -> candidate.position(1.0F).method_1025(player.method_73189())));
         if (collected.size() > 64) {
            collected = new ArrayList<>(collected.subList(0, 64));
         }

         this.candidates = List.copyOf(collected);
         this.updateEngineLights(level, feature);
      } else {
         this.clear();
      }
   }

   public List<DynamicLightManager.RenderLight> shaderLights(FullBrightFeature feature, float partialTick, class_243 cameraPosition) {
      if (feature.usesShaderLights() && !this.candidates.isEmpty()) {
         float radiusMultiplier = feature.lightRadius.getValue().floatValue();
         return this.candidates
            .stream()
            .filter(candidate -> !candidate.entity.method_31481())
            .map(candidate -> candidate.toRenderLight(partialTick, radiusMultiplier))
            .sorted(Comparator.comparingDouble(light -> light.position.method_1025(cameraPosition)))
            .limit(16L)
            .toList();
      } else {
         return List.of();
      }
   }

   public void clear() {
      this.candidates = List.of();
      this.clearEngineLights();
   }

   public void revalidateAfterVanillaUpdates(class_638 level) {
      Map<Long, Integer> sources = this.virtualLuminance;
      if (level != null && level == this.engineLevel && !sources.isEmpty()) {
         class_3562 blockLight = level.method_2935().method_12130().method_15562(class_1944.field_9282);

         for (long packedPos : sources.keySet()) {
            blockLight.method_15513(class_2338.method_10092(packedPos));
         }

         blockLight.method_15516();
      }
   }

   private void updateEngineLights(class_638 level, FullBrightFeature feature) {
      if (this.engineLevel != null && this.engineLevel != level) {
         this.clearEngineLights();
      }

      this.engineLevel = level;
      Map<Long, Integer> next = new HashMap<>();
      if (feature.usesEngineLights()) {
         float radiusMultiplier = feature.lightRadius.getValue().floatValue();
         float intensity = feature.lightIntensity.getValue().floatValue();

         for (DynamicLightManager.Candidate candidate : this.candidates) {
            class_243 position = candidate.position(1.0F);
            class_2338 blockPos = class_2338.method_49638(position);
            if (level.method_22340(blockPos)) {
               int luminance = Math.clamp(Math.round(candidate.spec.luminance * radiusMultiplier * Math.min(1.5F, intensity)), 1, 15);
               next.merge(blockPos.method_10063(), luminance, Math::max);
            }
         }
      }

      Map<Long, Integer> previous = this.virtualLuminance;
      this.virtualLuminance = Map.copyOf(next);
      if (!previous.equals(next)) {
         Set<Long> changed = new HashSet<>(previous.keySet());
         changed.addAll(next.keySet());
         class_3562 lightEngine = level.method_2935().method_12130().method_15562(class_1944.field_9282);

         for (long packedPos : changed) {
            if (previous.getOrDefault(packedPos, 0) != next.getOrDefault(packedPos, 0)) {
               lightEngine.method_15513(class_2338.method_10092(packedPos));
            }
         }
      }
   }

   private void clearEngineLights() {
      Map<Long, Integer> previous = this.virtualLuminance;
      this.virtualLuminance = Map.of();
      class_638 level = this.engineLevel;
      this.engineLevel = null;
      if (level != null && !previous.isEmpty()) {
         class_3562 lightEngine = level.method_2935().method_12130().method_15562(class_1944.field_9282);

         for (long packedPos : previous.keySet()) {
            lightEngine.method_15513(class_2338.method_10092(packedPos));
         }
      }
   }

   private static DynamicLightManager.LightSpec lightFor(class_1799 stack) {
      if (stack == null || stack.method_7960()) {
         return null;
      }

      if (stack.method_31574(class_1802.field_22001) || stack.method_31574(class_1802.field_22016) || stack.method_31574(class_1802.field_23842)) {
         return new DynamicLightManager.LightSpec(7002623, 12, 0.08F);
      }

      if (stack.method_31574(class_1802.field_8530)) {
         return new DynamicLightManager.LightSpec(16727332, 9, 0.04F);
      }

      if (stack.method_31574(class_1802.field_8305) || stack.method_31574(class_1802.field_8056)) {
         return new DynamicLightManager.LightSpec(14219519, 14, 0.02F);
      }

      if (stack.method_31574(class_1802.field_37539)) {
         return new DynamicLightManager.LightSpec(16765802, 15, 0.01F);
      }

      if (stack.method_31574(class_1802.field_37540)) {
         return new DynamicLightManager.LightSpec(9306049, 15, 0.01F);
      }

      if (stack.method_31574(class_1802.field_37541)) {
         return new DynamicLightManager.LightSpec(14919935, 15, 0.01F);
      }

      if (stack.method_31574(class_1802.field_8187)
         || stack.method_31574(class_1802.field_8814)
         || stack.method_31574(class_1802.field_8894)
         || stack.method_31574(class_1802.field_8183)
         || stack.method_31574(class_1802.field_8135)) {
         return new DynamicLightManager.LightSpec(16738852, 15, 0.18F);
      }

      if (stack.method_31574(class_1802.field_28659) || stack.method_31574(class_1802.field_28410)) {
         return new DynamicLightManager.LightSpec(9306032, 10, 0.04F);
      }

      if (stack.method_31574(class_1802.field_8137)) {
         return new DynamicLightManager.LightSpec(13101311, 15, 0.08F);
      }

      if (stack.method_7909() instanceof class_1747 blockItem) {
         int luminance = blockItem.method_7711().method_9564().method_26213();
         if (luminance > 0) {
            return new DynamicLightManager.LightSpec(16765072, luminance, luminance >= 14 ? 0.07F : 0.03F);
         }
      }

      return null;
   }

   private static DynamicLightManager.LightSpec strongest(DynamicLightManager.LightSpec first, DynamicLightManager.LightSpec second) {
      if (first == null) {
         return second;
      } else if (second == null) {
         return first;
      } else {
         return first.luminance >= second.luminance ? first : second;
      }
   }

   @Environment(EnvType.CLIENT)
   private record Candidate(class_1297 entity, double yOffset, DynamicLightManager.LightSpec spec) {
      private static DynamicLightManager.Candidate entity(class_1297 entity, double yOffset, DynamicLightManager.LightSpec spec) {
         return new DynamicLightManager.Candidate(entity, yOffset, spec);
      }

      private class_243 position(float partialTick) {
         return Render3DUtil.interpolatedPosition(this.entity, partialTick).method_1031(0.0, this.yOffset, 0.0);
      }

      private DynamicLightManager.RenderLight toRenderLight(float partialTick, float radiusMultiplier) {
         return new DynamicLightManager.RenderLight(
            this.position(partialTick),
            this.spec.rgb,
            (3.0F + this.spec.luminance * 0.62F) * radiusMultiplier,
            this.spec.flicker,
            this.entity.method_5628() * 0.731F
         );
      }
   }

   @Environment(EnvType.CLIENT)
   private record LightSpec(int rgb, int luminance, float flicker) {
      private static final DynamicLightManager.LightSpec FIRE = new DynamicLightManager.LightSpec(16738852, 15, 0.22F);
   }

   @Environment(EnvType.CLIENT)
   public record RenderLight(class_243 position, int rgb, float radius, float flicker, float phase) {
   }
}
