package org.ryzen.utils.render.post;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.pipeline.RenderPipeline.Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline.Snippet;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat.class_5596;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10789;
import net.minecraft.class_10799;
import net.minecraft.class_290;
import net.minecraft.class_2960;

@Environment(EnvType.CLIENT)
public final class PostPipelines {
   private static final String[] NONE = new String[0];
   public static final RenderPipeline ESP_KAWASE_DOWN = fullscreen(
      "post/blurs/kawase_down", "ryzen:post/blurs/esp_kawase_down", new String[]{"CurrentInput"}, new String[]{"KawaseDownUniforms"}, null
   );
   public static final RenderPipeline ESP_KAWASE_UP = fullscreen(
      "post/blurs/kawase_up", "ryzen:post/blurs/esp_kawase_up", new String[]{"CurrentInput"}, new String[]{"KawaseUpUniforms"}, null
   );
   public static final TextureFormat MASK_RAW_FORMAT = TextureFormat.RGBA8;
   public static final TextureFormat SKY_CLOUDS_FORMAT = TextureFormat.RGBA8;
   public static final TextureFormat EFFECT_FORMAT = TextureFormat.RGBA8;
   public static final RenderPipeline HAND_MASK = fullscreen(
      "post/hand_mask",
      "ryzen:post/hand_mask",
      new String[]{"BeforeTexture", "AfterTexture", "BeforeDepth", "AfterDepth"},
      new String[]{"HandMaskUniforms"},
      null
   );
   public static final RenderPipeline HAND_MASK_SMOOTH = fullscreen(
      "post/hand_mask_smooth", "ryzen:post/hand_mask_smooth", new String[]{"RawMask"}, new String[]{"HandMaskUniforms"}, null
   );
   public static final RenderPipeline SHADER_HANDS = fullscreen(
      "post/shader_hands",
      "ryzen:post/shader_hands",
      new String[]{"BlurredSampler", "MaskSampler"},
      new String[]{"HandCompositeUniforms"},
      BlendFunction.LIGHTNING
   );
   public static final RenderPipeline HAND_FILL = fullscreen(
      "post/hand_fill", "ryzen:post/hand_fill", new String[]{"MaskSampler"}, new String[]{"HandFillUniforms"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline HAND_PLASMA = fullscreen(
      "post/hand_plasma", "ryzen:post/hand_plasma", new String[]{"MaskSampler"}, new String[]{"HandFillUniforms"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline HAND_GLASS = fullscreen(
      "post/hand_glass", "ryzen:post/hand_glass", new String[]{"SceneSampler", "MaskSampler"}, new String[]{"HandGlassUniforms"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline HAND_OUTLINE = fullscreen(
      "post/hand_outline", "ryzen:post/hand_outline", new String[]{"MaskSampler"}, new String[]{"HandOutlineUniforms"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline HAND_HALO = fullscreen(
      "post/hand_halo", "ryzen:post/hand_halo", new String[]{"BlurredSampler", "MaskSampler"}, new String[]{"HandHaloUniforms"}, BlendFunction.LIGHTNING
   );
   public static final RenderPipeline HAND_BLUR_DOWN = fullscreen(
      "post/hand_blur_down", "ryzen:post/hand_blur_down", new String[]{"CurrentInput"}, new String[]{"HandBlurUniforms"}, null
   );
   public static final RenderPipeline HAND_BLUR_UP = fullscreen(
      "post/hand_blur_up", "ryzen:post/hand_blur_up", new String[]{"CurrentInput"}, new String[]{"HandBlurUniforms"}, null
   );
   public static final RenderPipeline HAND_TRAIL = fullscreen(
      "post/hand_trail", "ryzen:post/hand_trail", new String[]{"PrevSampler", "InjectSampler"}, new String[]{"HandTrailUniforms"}, null
   );
   public static final RenderPipeline POINT_LIGHTS = fullscreen(
      "post/point_lights", "ryzen:post/point_lights", new String[]{"SceneSampler", "DepthSampler"}, new String[]{"PointLightUniforms", "PointLights"}, null
   );
   public static final RenderPipeline BLOCK_OUTLINE_CLASSIC = blockOutlinePipeline("classic", "classic", true);
   public static final RenderPipeline BLOCK_OUTLINE_CLASSIC_THROUGH = blockOutlinePipeline("classic_through", "classic", false);
   public static final RenderPipeline BLOCK_OUTLINE_CAUSTICS = blockOutlinePipeline("caustics", "caustics", true);
   public static final RenderPipeline BLOCK_OUTLINE_CAUSTICS_THROUGH = blockOutlinePipeline("caustics_through", "caustics", false);
   public static final RenderPipeline BLOCK_OUTLINE_PRISMATIC = blockOutlinePipeline("prismatic", "prismatic", true);
   public static final RenderPipeline BLOCK_OUTLINE_PRISMATIC_THROUGH = blockOutlinePipeline("prismatic_through", "prismatic", false);
   public static final RenderPipeline BLOCK_OUTLINE_GLOSSY = blockOutlinePipeline("glossy", "glossy", true);
   public static final RenderPipeline BLOCK_OUTLINE_GLOSSY_THROUGH = blockOutlinePipeline("glossy_through", "glossy", false);
   public static final RenderPipeline BLOCK_OUTLINE_DEEP_SPACE = blockOutlinePipeline("deep_space", "deep_space", true);
   public static final RenderPipeline BLOCK_OUTLINE_DEEP_SPACE_THROUGH = blockOutlinePipeline("deep_space_through", "deep_space", false);
   public static final RenderPipeline BLOCK_OUTLINE_NEBULA = blockOutlinePipeline("nebula", "nebula", true);
   public static final RenderPipeline BLOCK_OUTLINE_NEBULA_THROUGH = blockOutlinePipeline("nebula_through", "nebula", false);
   public static final RenderPipeline WORLD_SKY_CLOUDS_DEEP_SPACE = skyCloudsPipeline("sky_clouds_deep_space", "ryzen:world/sky/clouds_deep_space");
   public static final RenderPipeline WORLD_SKY_CLOUDS_NEBULA = skyCloudsPipeline("sky_clouds_nebula", "ryzen:world/sky/clouds_nebula");
   public static final RenderPipeline WORLD_SKY_CLOUDS_PLASMA = skyCloudsPipeline("sky_clouds_plasma", "ryzen:world/sky/clouds_plasma");
   public static final RenderPipeline WORLD_SKY_DEEP_SPACE = worldPostPipeline(
      "sky_deep_space", "ryzen:world/sky/deep_space", new String[]{"DepthSampler", "CloudSampler"}, new String[]{"WorldSkyUniforms"}
   );
   public static final RenderPipeline WORLD_SKY_NEBULA = worldPostPipeline(
      "sky_nebula", "ryzen:world/sky/nebula", new String[]{"DepthSampler", "CloudSampler"}, new String[]{"WorldSkyUniforms"}
   );
   public static final RenderPipeline WORLD_SKY_PLASMA = worldPostPipeline(
      "sky_plasma", "ryzen:world/sky/plasma", new String[]{"DepthSampler", "CloudSampler"}, new String[]{"WorldSkyUniforms"}
   );
   public static final RenderPipeline SKYSHADER_SPACE = skyShaderPipeline("space");
   public static final RenderPipeline SKYSHADER_SUMMER = skyShaderPipeline("summer");
   public static final RenderPipeline SKYSHADER_PLASMA = skyShaderPipeline("plasma");
   public static final RenderPipeline SKYSHADER_PULSAR = skyShaderPipeline("pulsar");
   public static final RenderPipeline SKYSHADER_SAKURA = skyShaderPipeline("sakura");
   public static final RenderPipeline WORLD_SATURATION = worldPostPipeline(
      "saturation", "ryzen:post/world_saturation", new String[]{"SceneSampler"}, new String[]{"SaturationUniforms"}
   );
   public static final RenderPipeline CHAMS_SOLID = chamsPipeline("solid", "ryzen:post/chams_solid", new String[]{"MaskSampler"}, BlendFunction.TRANSLUCENT);
   public static final RenderPipeline CHAMS_PLASMA = chamsPipeline("plasma", "ryzen:post/chams_plasma", new String[]{"MaskSampler"}, BlendFunction.TRANSLUCENT);
   public static final RenderPipeline CHAMS_NEBULA = chamsPipeline("nebula", "ryzen:post/chams_nebula", new String[]{"MaskSampler"}, BlendFunction.TRANSLUCENT);
   public static final RenderPipeline CHAMS_GLASS = chamsPipeline(
      "glass", "ryzen:post/chams_glass", new String[]{"SceneSampler", "MaskSampler"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline CHAMS_OUTLINE = chamsPipeline(
      "outline", "ryzen:post/chams_outline", new String[]{"MaskSampler"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline CHAMS_INTERNAL = chamsPipeline(
      "internal", "ryzen:post/chams_internal", new String[]{"BlurredSampler", "MaskSampler"}, BlendFunction.LIGHTNING
   );
   public static final RenderPipeline CHAMS_GLOW = chamsPipeline(
      "glow", "ryzen:post/chams_glow", new String[]{"BlurredSampler", "MaskSampler"}, BlendFunction.TRANSLUCENT
   );
   public static final RenderPipeline CHAMS_GLOW_ADDITIVE = chamsPipeline(
      "glow_additive", "ryzen:post/chams_glow", new String[]{"BlurredSampler", "MaskSampler"}, BlendFunction.LIGHTNING
   );
   public static final RenderPipeline POPCHAMS_ADDITIVE = popChamsModelPipeline("additive_no_depth", BlendFunction.LIGHTNING, false, false);
   public static final RenderPipeline POPCHAMS_ADDITIVE_SOLID = popChamsModelPipeline("additive_solid_no_depth", BlendFunction.LIGHTNING, true, false);
   public static final RenderPipeline POPCHAMS_TRANSLUCENT = popChamsModelPipeline("translucent_no_depth", BlendFunction.TRANSLUCENT, false, false);
   public static final RenderPipeline POPCHAMS_TRANSLUCENT_SOLID = popChamsModelPipeline("translucent_solid_no_depth", BlendFunction.TRANSLUCENT, true, false);
   public static final RenderPipeline POPCHAMS_MASK = popChamsModelPipeline("mask", null, false, true);
   public static final RenderPipeline POPCHAMS_MASK_SOLID = popChamsModelPipeline("mask_solid", null, true, false);
   public static final RenderPipeline POPCHAMS_COMPOSITE = fullscreen(
      "chams/popchams_composite", "ryzen:post/popchams_composite", new String[]{"BlurredSampler"}, new String[]{"PopChamsComposite"}, BlendFunction.LIGHTNING
   );

   private static RenderPipeline blockOutlinePipeline(String name, String fragment, boolean depthTest) {
      Builder builder = RenderPipeline.builder(new Snippet[0])
         .withLocation(class_2960.method_60654("ryzen:pipeline/world/block_outline/" + name))
         .withVertexShader(class_2960.method_60654("ryzen:world/block_outline"))
         .withFragmentShader(class_2960.method_60654("ryzen:world/block_outline/" + fragment))
         .withUniform("BlockOutlineTransform", class_10789.field_60031)
         .withUniform("BlockOutlineStyle", class_10789.field_60031)
         .withBlend(BlendFunction.TRANSLUCENT)
         .withDepthWrite(false)
         .withVertexFormat(class_290.field_1592, class_5596.field_27379)
         .withCull(false);
      return builder.withDepthTestFunction(depthTest ? DepthTestFunction.LEQUAL_DEPTH_TEST : DepthTestFunction.NO_DEPTH_TEST).build();
   }

   private static RenderPipeline worldPostPipeline(String name, String fragment, String[] samplers, String[] uniforms) {
      return fullscreen("world/" + name, fragment, samplers, uniforms, null);
   }

   private static RenderPipeline skyCloudsPipeline(String name, String fragment) {
      return fullscreen("world/" + name, fragment, new String[]{"DepthSampler"}, new String[]{"WorldSkyUniforms"}, null);
   }

   private static RenderPipeline skyShaderPipeline(String name) {
      return fullscreen("world/skyshader_" + name, "ryzen:world/skyshader/" + name, new String[]{"DepthSampler"}, new String[]{"SkyShaderUniforms"}, null);
   }

   private static RenderPipeline chamsPipeline(String name, String fragment, String[] samplers, BlendFunction blend) {
      return fullscreen("chams/" + name, fragment, samplers, new String[]{"ChamsStyle"}, blend);
   }

   private static RenderPipeline fullscreen(String name, String fragment, String[] samplers, String[] uniforms, BlendFunction blend) {
      Builder builder = RenderPipeline.builder(new Snippet[0])
         .withLocation(class_2960.method_60654("ryzen:pipeline/" + name))
         .withVertexShader(class_2960.method_60654("ryzen:post/blurs/kawase_common"))
         .withFragmentShader(class_2960.method_60654(fragment))
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withVertexFormat(class_290.field_1585, class_5596.field_27379)
         .withCull(false);

      for (String sampler : samplers) {
         builder.withSampler(sampler);
      }

      for (String uniform : uniforms) {
         builder.withUniform(uniform, class_10789.field_60031);
      }

      if (blend != null) {
         builder.withBlend(blend);
      }

      return builder.build();
   }

   private static RenderPipeline popChamsModelPipeline(String name, BlendFunction blend, boolean solid, boolean alphaCutout) {
      Builder builder = RenderPipeline.builder(new Snippet[]{class_10799.field_56854})
         .withLocation(class_2960.method_60654("ryzen:pipeline/world/popchams_" + name))
         .withShaderDefine("NO_CARDINAL_LIGHTING")
         .withShaderDefine("EMISSIVE")
         .withShaderDefine("NO_OVERLAY")
         .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
         .withDepthWrite(false)
         .withCull(false);
      if (blend != null) {
         builder.withBlend(blend);
      } else {
         builder.withoutBlend();
      }

      if (solid) {
         builder.withFragmentShader(class_2960.method_60654("ryzen:core/popchams_solid"));
      }

      if (alphaCutout) {
         builder.withShaderDefine("ALPHA_CUTOUT", 0.1F);
      }

      return builder.build();
   }

   private PostPipelines() {
   }
}
