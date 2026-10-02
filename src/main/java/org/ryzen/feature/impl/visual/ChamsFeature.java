package org.ryzen.feature.impl.visual;

import java.util.List;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1303;
import net.minecraft.class_1311;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ChamsFeature extends Feature {
   public static final String TARGET_PLAYERS = "Players";
   public static final String TARGET_HOSTILE = "Hostile";
   public static final String TARGET_PASSIVE = "Passive";
   public static final String TARGET_ITEMS = "Items";
   public static final String EFFECT_SHADER_FILL = "Shader Fill";
   public static final String EFFECT_SOLID = "Solid";
   public static final String EFFECT_GLASS = "Glass";
   public static final String EFFECT_OUTLINE = "Outline";
   public static final String EFFECT_GLOW = "Glow";
   public static final String SHADER_PLASMA = "Plasma";
   public static final String SHADER_NEBULA = "Nebula";
   public static final String MODE_EXTERNAL = "External";
   public static final String MODE_INTERNAL = "Internal";
   public static final String MODE_BOTH = "Both";
   public final MultiSelectSetting targets = this.register(
      new MultiSelectSetting("Targets", Set.of("Players", "Hostile"), "Players", "Hostile", "Passive", "Items").configKey("render.chams.targets")
   );
   public final NumberSetting distance = this.register(new NumberSetting("Distance", 96.0, 8.0, 192.0, 1.0, " blocks").configKey("render.chams.distance"));
   public final MultiSelectSetting effects = this.register(
      new MultiSelectSetting("Effect", List.of("Shader Fill", "Glow"), "Shader Fill", "Solid", "Glass", "Outline", "Glow").configKey("render.chams.effect")
   );
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "External", "External", "Internal", "Both").configKey("render.chams.mode"));
   public final BooleanSetting originalTexture = this.register(new BooleanSetting("Original Texture", false).configKey("render.chams.originalTexture"));
   public final ModeSetting shader = this.register(
      new ModeSetting("Shader", "Plasma", "Plasma", "Nebula").configKey("render.chams.shader").visibleWhen(this::hasShaderFill)
   );
   public final NumberSetting shaderSpeed = this.register(
      new NumberSetting("Shader Speed", 4.0, 0.1, 8.0, 0.1, "x").configKey("render.chams.shader.speed").visibleWhen(this::hasShaderFill)
   );
   public final ModeSetting colorMode = this.register(ColorMode.setting().configKey("render.chams.colorMode"));
   public final ColorSetting color = this.register(
      new ColorSetting("Visible Color", -8812853).configKey("render.chams.color").visibleWhen(() -> ColorMode.isCustom(this.colorMode))
   );
   public final NumberSetting opacity = this.register(new NumberSetting("Opacity", 1.0, 0.0, 1.0, 0.01, "").configKey("render.chams.opacity"));
   public final NumberSetting outlineThickness = this.register(
      new NumberSetting("Outline Thickness", 1.5, 0.5, 5.0, 0.1, "px").configKey("render.chams.thickness").visibleWhen(this::hasOutline)
   );
   public final NumberSetting glowRadius = this.register(
      new NumberSetting("Glow Radius", 1.3, 0.5, 8.0, 0.1, "x").configKey("render.chams.glowRadius").visibleWhen(this::hasGlow)
   );
   public final NumberSetting glowStrength = this.register(
      new NumberSetting("Glow Strength", 0.33, 0.0, 3.0, 0.01, "x").configKey("render.chams.glowStrength").visibleWhen(this::hasGlow)
   );
   public final BooleanSetting additiveBlending = this.register(
      new BooleanSetting("Additive Glow", true).configKey("render.chams.blending").visibleWhen(this::hasGlow)
   );
   public final NumberSetting glassBlur = this.register(
      new NumberSetting("Glass Blur", 0.0, 0.0, 60.0, 1.0, "px").configKey("render.chams.glassBlur").visibleWhen(this::hasGlass)
   );
   public final BooleanSetting mirror = this.register(new BooleanSetting("Mirror", true).configKey("render.chams.mirror").visibleWhen(this::hasGlass));

   public ChamsFeature() {
      super("Chams", "Replace selected entity models with shader silhouettes", FeatureCategory.VISUAL, -1);
   }

   public static ChamsFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(ChamsFeature.class);
   }

   public boolean shouldRender(class_1297 entity) {
      class_310 minecraft = class_310.method_1551();
      if (minecraft.field_1724 == null
         || entity == null
         || entity == minecraft.field_1724 && minecraft.field_1690.method_31044().method_31034()
         || entity.method_31481()
         || !entity.method_5805()
         || minecraft.field_1724.method_5858(entity) > this.distance.getValue() * this.distance.getValue()) {
         return false;
      } else if (entity instanceof class_1657) {
         return this.targets.isSelected("Players");
      } else if (!(entity instanceof class_1542) && !(entity instanceof class_1303)) {
         class_1311 category = entity.method_5864().method_5891();
         return category == class_1311.field_6302 ? this.targets.isSelected("Hostile") : this.targets.isSelected("Passive");
      } else {
         return this.targets.isSelected("Items");
      }
   }

   public int resolvedColor() {
      return ColorMode.resolve(this.colorMode, this.color);
   }

   public boolean hasShaderFill() {
      return this.effects.isSelected("Shader Fill");
   }

   public boolean hasSolid() {
      return this.effects.isSelected("Solid");
   }

   public boolean hasGlass() {
      return this.effects.isSelected("Glass");
   }

   public boolean hasOutline() {
      return this.effects.isSelected("Outline");
   }

   public boolean hasGlow() {
      return this.effects.isSelected("Glow") && this.usesExternal();
   }

   public boolean keepsOriginalModel() {
      return this.originalTexture.getValue();
   }

   public boolean usesInternal() {
      return this.mode.is("Internal") || this.mode.is("Both");
   }

   public boolean usesExternal() {
      return this.mode.is("External") || this.mode.is("Both");
   }

   public boolean hasAnyVisual() {
      return this.hasShaderFill() || this.hasSolid() || this.hasGlass() || this.hasOutline() || this.hasGlow() || this.usesInternal();
   }
}
