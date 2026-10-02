package org.ryzen.feature.impl.visual;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ShaderHandsFeature extends Feature {
   public static final String FILL = "Fill";
   public static final String OUTLINE = "Outline";
   public static final String GLOW = "Glow";
   public static final String GLASS = "Glass";
   public static final String SOLID = "Solid";
   public static final String PLASMA = "Plasma";
   public final MultiSelectSetting effects = this.register(
      new MultiSelectSetting("Effects", List.of("Fill", "Glow"), "Fill", "Outline", "Glow").configKey("render.armtweaks.fill.effects")
   );
   public final ModeSetting colorMode = this.register(ColorMode.setting().configKey("render.armtweaks.fill.colorMode"));
   public final ColorSetting color = this.register(
      new ColorSetting("Color", 7964363).configKey("render.armtweaks.fill.color").visibleWhen(() -> ColorMode.isCustom(this.colorMode))
   );
   public final NumberSetting fillOpacity = this.register(
      new NumberSetting("Fill Opacity", 0.19, 0.0, 1.0, 0.01, "").configKey("render.armtweaks.fill.opacity").visibleWhen(this::hasFill)
   );
   public final ModeSetting fillType = this.register(
      new ModeSetting("Fill Type", "Glass", "Glass", "Solid", "Plasma").configKey("render.armtweaks.fill.type").visibleWhen(this::hasFill)
   );
   public final NumberSetting plasmaSpeed = this.register(
      new NumberSetting("Plasma Speed", 1.0, 0.1, 5.0, 0.1, "x").configKey("render.armtweaks.fill.plasma.speed").visibleWhen(this::hasPlasma)
   );
   public final NumberSetting glassBlur = this.register(
      new NumberSetting("Glass Blur", 19.0, 0.0, 60.0, 1.0, "px").configKey("render.armtweaks.fill.glass.blur").visibleWhen(this::hasGlass)
   );
   public final BooleanSetting mirror = this.register(
      new BooleanSetting("Mirror", true).configKey("render.armtweaks.fill.glass.mirror").visibleWhen(this::hasGlass)
   );
   public final NumberSetting glowRadius = this.register(
      new NumberSetting("Glow Radius", 18.0, 1.0, 50.0, 1.0, "px").configKey("render.armtweaks.fill.glow.radius").visibleWhen(this::hasGlow)
   );
   public final NumberSetting glowStrength = this.register(
      new NumberSetting("Glow Strength", 1.0, 0.0, 1.0, 0.01, "").configKey("render.armtweaks.fill.glow.strength").visibleWhen(this::hasGlow)
   );
   public final BooleanSetting flame = this.register(new BooleanSetting("Flame", true).configKey("render.armtweaks.fill.glow.flame").visibleWhen(this::hasGlow));
   public final NumberSetting flameSpeed = this.register(
      new NumberSetting("Flame Speed", 1.1, 0.1, 5.0, 0.1, "x").configKey("render.armtweaks.fill.glow.flameSpeed").visibleWhen(this::hasFlame)
   );
   public final NumberSetting flameTrail = this.register(
      new NumberSetting("Flame Trail", 1.0, 0.0, 1.0, 0.01, "").configKey("render.armtweaks.fill.glow.flameTrail").visibleWhen(this::hasFlame)
   );
   public final NumberSetting outlineThickness = this.register(
      new NumberSetting("Outline Thickness", 1.5, 0.5, 5.0, 0.1, "px").configKey("render.armtweaks.fill.outline.thickness").visibleWhen(this::hasOutline)
   );
   public final BooleanSetting bothHands = this.register(new BooleanSetting("Both Hands", true).configKey("render.armtweaks.fill.bothHands"));

   public ShaderHandsFeature() {
      super("ShaderHands", "Renders a colored silhouette over your first-person hands", FeatureCategory.VISUAL, -1);
   }

   public int resolvedColor() {
      return ColorMode.resolve(this.colorMode, this.color);
   }

   public boolean hasFill() {
      return this.effects.isSelected("Fill");
   }

   public boolean hasGlass() {
      return this.hasFill() && this.fillType.is("Glass");
   }

   public boolean hasPlasma() {
      return this.hasFill() && this.fillType.is("Plasma");
   }

   public boolean hasOutline() {
      return this.effects.isSelected("Outline");
   }

   public boolean hasGlow() {
      return this.effects.isSelected("Glow");
   }

   public boolean hasFlame() {
      return this.hasGlow() && this.flame.getValue();
   }
}
