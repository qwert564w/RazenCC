package org.ryzen.utils.render.world;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.impl.player.FullBrightFeature;
import org.ryzen.feature.impl.visual.AncientXrayFeature;
import org.ryzen.feature.impl.visual.BlockOutlineFeature;
import org.ryzen.feature.impl.visual.ChamsFeature;
import org.ryzen.feature.impl.visual.CosmeticsFeature;
import org.ryzen.feature.impl.visual.CubesFeature;
import org.ryzen.feature.impl.visual.HitParticlesFeature;
import org.ryzen.feature.impl.visual.JumpCirclesFeature;
import org.ryzen.feature.impl.visual.KillEffectFeature;
import org.ryzen.feature.impl.visual.PopChamsFeature;
import org.ryzen.feature.impl.visual.SkeletonEspFeature;
import org.ryzen.feature.impl.visual.SkyShaderFeature;
import org.ryzen.feature.impl.visual.TrajectoriesFeature;
import org.ryzen.feature.impl.visual.WorldParticlesFeature;
import org.ryzen.feature.impl.visual.WorldTweaksFeature;
import org.ryzen.utils.render.chams.ChamsPipeline;
import org.ryzen.utils.render.chams.PopChamsRenderer;
import org.ryzen.utils.render.target.TargetMarkers;

@Environment(EnvType.CLIENT)
public final class WorldEffects {
   private static final List<WorldEffect> EFFECTS = new ArrayList<>();

   private WorldEffects() {
   }

   public static void bootstrap() {
      if (EFFECTS.isEmpty()) {
         register(
            WorldEffect.lazy(
               SkyShaderFeature::getEnabled,
               SkyShaderRenderer::new,
               (feature, renderer, context) -> renderer.render(feature, context.cameraRenderState(), context.skyColor()),
               SkyShaderRenderer::release
            )
         );
         register(
            WorldEffect.lazy(
               () -> gate(WorldTweaksFeature.getEnabled(), WorldTweaksFeature::usesSky),
               WorldTweaksRenderer::new,
               (feature, renderer, context) -> renderer.renderSky(feature, context.cameraRenderState()),
               WorldTweaksRenderer::release
            )
         );
         register(
            WorldEffect.lazy(
               () -> gate(FullBrightFeature.getEnabled(), FullBrightFeature::usesShaderLights),
               DynamicLightRenderer::new,
               (feature, renderer, context) -> renderer.render(feature, context.cameraRenderState(), context.tickDelta()),
               DynamicLightRenderer::release
            )
         );
         register(
            WorldEffect.lazy(
               () -> gate(BlockOutlineFeature.getEnabled(), BlockOutlineFeature::usesShader),
               BlockOutlineRenderer::new,
               (feature, renderer, context) -> renderer.render(feature, context.cameraRenderState()),
               BlockOutlineRenderer::release
            )
         );
         register(
            WorldEffect.lazy(
               ChamsFeature::getEnabled,
               ChamsPipeline::new,
               (feature, pipeline, context) -> pipeline.render(context.levelRenderState(), feature),
               ChamsPipeline::release
            )
         );
         register(WorldEffect.direct(JumpCirclesFeature::getEnabled, (feature, context) -> feature.renderWorld()));
         register(WorldEffect.direct(TrajectoriesFeature::getEnabled, (feature, context) -> feature.renderWorld()));
         register(WorldEffect.direct(CubesFeature::getEnabled, (feature, context) -> feature.renderWorld()));
         register(WorldEffect.direct(SkeletonEspFeature::getEnabled, (feature, context) -> feature.renderWorld(context.tickDelta())));
         register(
            WorldEffect.direct(
               WorldParticlesFeature::getEnabled, (feature, context) -> feature.renderWorld(context.levelRenderState().field_63082, context.tickDelta())
            )
         );
         register(
            WorldEffect.lazy(
               PopChamsFeature::getEnabled,
               PopChamsRenderer::new,
               (feature, renderer, context) -> renderer.render(context.levelRenderState(), feature),
               PopChamsRenderer::release
            )
         );
         register(WorldEffect.direct(HitParticlesFeature::getEnabled, (feature, context) -> feature.renderWorld(context.tickDelta())));
         register(WorldEffect.direct(KillEffectFeature::getEnabled, (feature, context) -> feature.renderWorld()));
         register(WorldEffect.direct(CosmeticsFeature::getEnabled, (feature, context) -> feature.renderWorld(context.tickDelta())));
         register(WorldEffect.direct(AncientXrayFeature::getEnabled, (feature, context) -> feature.renderWorld()));
         register(
            WorldEffect.lazy(
               AuraFeature::getMarkerFeature,
               TargetMarkers::new,
               (feature, markers, context) -> markers.render(feature, context.tickDelta()),
               TargetMarkers::release
            )
         );
         register(
            WorldEffect.lazy(
               () -> gate(WorldTweaksFeature.getEnabled(), WorldTweaksFeature::usesSaturation),
               WorldTweaksRenderer::new,
               (feature, renderer, context) -> renderer.renderSaturation(feature),
               WorldTweaksRenderer::release
            )
         );
      }
   }

   public static void register(WorldEffect effect) {
      EFFECTS.add(effect);
   }

   public static void render(WorldEffectContext context) {
      for (WorldEffect effect : EFFECTS) {
         if (effect.active()) {
            effect.render(context);
         } else {
            effect.release();
         }
      }
   }

   private static <F> F gate(F feature, Predicate<F> enabled) {
      return feature != null && enabled.test(feature) ? feature : null;
   }
}
