package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Theme;

@Environment(EnvType.CLIENT)
public final class SkyShaderFeature extends Feature {
   public static final String MODE_SPACE = "Space";
   public static final String MODE_SUMMER = "Summer";
   public static final String MODE_PLASMA = "Plasma";
   public static final String MODE_PULSAR = "Pulsar";
   public static final String MODE_SAKURA = "Sakura";
   public static final String SUMMER_DAY = "Day";
   public static final String SUMMER_NIGHT = "Night";
   public static final String SUMMER_COLOR_REAL = "Real";
   public static final String SUMMER_COLOR_CUSTOM = "Custom";
   private static final int NIGHT_BLEND = -13803626;
   private static final int DEEP_BLEND = -16381420;
   public final ModeSetting mode = this.register(new ModeSetting("Mode", "Space", "Space", "Summer", "Plasma", "Pulsar", "Sakura"));
   public final ModeSetting summerVariant = this.register(new ModeSetting("Time Of Day", "Day", "Day", "Night").visibleWhen(() -> this.mode.is("Summer")));
   public final BooleanSetting useTheme = this.register(new BooleanSetting("Theme Colors", true).visibleWhen(this::themeApplicable));
   public final ModeSetting summerColorMode = this.register(
      new ModeSetting("Sky Color", "Real", "Real", "Custom").visibleWhen(() -> this.mode.is("Summer") && this.summerVariant.is("Day"))
   );
   public final ColorSetting color = this.register(new ColorSetting("Color", -7722014).visibleWhen(this::colorVisible));
   public final NumberSetting patternScale = this.register(
      new NumberSetting("Pattern Scale", 1.0, 0.2, 3.0, 0.05, "x").visibleWhen(this::patternSlidersVisible)
   );
   public final NumberSetting patternSpeed = this.register(
      new NumberSetting("Pattern Speed", 1.0, 0.0, 15.0, 0.1, "x").visibleWhen(this::patternSlidersVisible)
   );
   public final BooleanSetting respectResourcePack = this.register(new BooleanSetting("Respect Resource Pack", true).visibleWhen(() -> this.mode.is("Space")));
   public final NumberSetting speed = this.register(new NumberSetting("Speed", 1.0, 0.1, 15.0, 0.1, "x").visibleWhen(this::spaceSlidersVisible));
   public final NumberSetting intensity = this.register(new NumberSetting("Intensity", 1.0, 0.1, 2.0, 0.05, "x").visibleWhen(this::spaceSlidersVisible));
   public final NumberSetting overlay = this.register(
      new NumberSetting("Overlay", 0.55, 0.15, 1.0, 0.05, "").visibleWhen(() -> this.mode.is("Space") && this.respectResourcePack.getValue())
   );

   public SkyShaderFeature() {
      super("SkyShader", "Draws a procedural sky in place of the vanilla one", FeatureCategory.VISUAL, -1);
   }

   public static SkyShaderFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(SkyShaderFeature.class);
   }

   private boolean themeApplicable() {
      return this.mode.is("Space") || this.mode.is("Plasma") || this.mode.is("Pulsar");
   }

   private boolean patternSlidersVisible() {
      return this.mode.is("Plasma") || this.mode.is("Pulsar") || this.mode.is("Summer") || this.mode.is("Sakura");
   }

   private boolean spaceSlidersVisible() {
      return this.mode.is("Space");
   }

   private boolean colorVisible() {
      return this.themeApplicable() && !this.useTheme.getValue() || this.isSummerCustomColor();
   }

   public boolean isSpace() {
      return this.mode.is("Space");
   }

   public boolean isSummerCustomColor() {
      return this.mode.is("Summer") && this.summerVariant.is("Day") && this.summerColorMode.is("Custom");
   }

   public boolean isSummerNight() {
      return this.mode.is("Summer") && this.summerVariant.is("Night");
   }

   public String getMode() {
      return this.mode.getValue();
   }

   public float patternScale() {
      return this.patternScale.getValue().floatValue();
   }

   public float effectiveSpeed() {
      return this.isSpace() ? this.speed.getValue().floatValue() : this.patternSpeed.getValue().floatValue();
   }

   public float effectiveScale() {
      return this.isSpace() ? 1.0F : this.patternScale();
   }

   public float effectiveIntensity() {
      return this.intensity.getValue().floatValue();
   }

   public float effectiveOverlay() {
      return this.isSpace() && this.respectResourcePack.getValue() ? this.overlay.getValue().floatValue() : 1.0F;
   }

   public int primaryColor() {
      if (this.isSummerCustomColor()) {
         return this.color.getValue();
      } else {
         return this.useTheme.getValue() ? Theme.accent(0) : this.color.getValue();
      }
   }

   public int secondaryColor() {
      return !this.isSummerCustomColor() && this.useTheme.getValue() ? Theme.accent(1) : ColorUtil.lerp(this.color.getValue(), -1, 0.35F);
   }

   public int backgroundColor() {
      if (this.isSummerCustomColor()) {
         return ColorUtil.lerp(this.color.getValue(), -13803626, 0.65F);
      } else {
         return this.useTheme.getValue() ? Theme.Colors.PANEL : ColorUtil.lerp(this.color.getValue(), -16381420, 0.82F);
      }
   }
}
