package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class BlockOutlineFeature extends Feature {
   public static final String MODE_SHADER = "Shader";
   public static final String MODE_NORMAL = "Normal";
   public static final String VARIANT_CLASSIC = "Classic";
   public static final String VARIANT_CAUSTICS = "Water Caustics";
   public static final String VARIANT_PRISMATIC = "Prismatic Flow";
   public static final String VARIANT_GLOSSY = "Glossy Gradients";
   public static final String VARIANT_DEEP_SPACE = "Deep Space";
   public static final String VARIANT_NEBULA = "Nebula";
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Shader", "Shader", "Normal").configKey("render.blockoutline.mode"));
   public final ModeSetting variant = this.register(
      new ModeSetting("Variant", "Classic", "Classic", "Water Caustics", "Prismatic Flow", "Glossy Gradients", "Deep Space", "Nebula")
         .configKey("render.blockoutline.variant")
         .visibleWhen(this::usesShader)
   );
   public final ModeSetting colorMode = this.register(ColorMode.setting().configKey("render.blockoutline.tintMode").visibleWhen(this::usesShader));
   public final ColorSetting tint = this.register(
      new ColorSetting("Tint", -1).configKey("render.blockoutline.tint").visibleWhen(() -> this.usesShader() && ColorMode.isCustom(this.colorMode))
   );
   public final BooleanSetting ignoreDepth = this.register(
      new BooleanSetting("Ignore Depth", false).configKey("render.blockoutline.ignoreDepth").visibleWhen(this::usesShader)
   );
   public final NumberSetting animationSpeed = this.register(
      new NumberSetting("Animation Speed", 15.0, 1.0, 30.0, 1.0, "").configKey("render.blockoutline.animationSpeed").visibleWhen(this::usesShader)
   );
   public final NumberSetting shaderSpeed = this.register(
      new NumberSetting("Shader Speed", 1.0, 0.1, 3.0, 0.05, "x").configKey("render.blockoutline.shaderSpeed").visibleWhen(this::usesShader)
   );
   public final NumberSetting shaderIntensity = this.register(
      new NumberSetting("Shader Intensity", 1.5, 0.1, 3.0, 0.05, "x").configKey("render.blockoutline.shaderIntensity").visibleWhen(this::usesShader)
   );

   public BlockOutlineFeature() {
      super("BlockOutline", "Animated shader over the selected block", FeatureCategory.VISUAL, -1);
   }

   public static BlockOutlineFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(BlockOutlineFeature.class);
   }

   public boolean usesShader() {
      return this.mode.is("Shader");
   }

   public int resolvedTint() {
      return ColorMode.resolve(this.colorMode, this.tint);
   }
}
