package org.ryzen.feature.impl.visual;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2394;
import net.minecraft.class_2398;
import net.minecraft.class_3414;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.MultiSelectSetting;

@Environment(EnvType.CLIENT)
public final class RemovalsFeature extends Feature {
   private static RemovalsFeature instance;
   private final MultiSelectSetting overlay = this.register(
      new MultiSelectSetting("Overlay", Set.of("Fire", "Bad Effects"), "Fire", "Bad Effects", "Shaking", "Totem Overlay", "Scoreboard", "Boss Bar")
   );
   private final MultiSelectSetting world = this.register(new MultiSelectSetting("World", Set.of("Weather"), "Weather", "Totem Particles", "Effects"));
   private final MultiSelectSetting sounds = this.register(
      new MultiSelectSetting("Sounds", Set.of(), "Hit", "Hurt", "Totem", "Step", "Eat", "Firework", "Warden")
   );

   public RemovalsFeature() {
      super("Removals", "Hides distracting overlays, world effects, particles, and sounds.", FeatureCategory.VISUAL, -1);
      instance = this;
   }

   public static boolean shouldRemoveFireOverlay() {
      return active() && instance.overlay.isSelected("Fire");
   }

   public static boolean shouldRemoveBadEffectsVisuals() {
      return active() && instance.overlay.isSelected("Bad Effects");
   }

   public static boolean shouldRemoveShaking() {
      return active() && instance.overlay.isSelected("Shaking");
   }

   public static boolean shouldRemoveTotemOverlay() {
      return active() && instance.overlay.isSelected("Totem Overlay");
   }

   public static boolean shouldRemoveScoreboard() {
      return active() && instance.overlay.isSelected("Scoreboard");
   }

   public static boolean shouldRemoveBossBar() {
      return active() && instance.overlay.isSelected("Boss Bar");
   }

   public static boolean shouldRemoveWeather() {
      return active() && instance.world.isSelected("Weather");
   }

   public static boolean shouldRemoveParticle(class_2394 options) {
      if (active() && options != null) {
         return instance.world.isSelected("Totem Particles") && options.method_10295() == class_2398.field_11220 ? true : instance.world.isSelected("Effects");
      } else {
         return false;
      }
   }

   public static boolean shouldRemoveSound(class_3414 soundEvent) {
      if (active() && soundEvent != null) {
         String soundPath = soundEvent.comp_3319().method_12832();
         if (instance.sounds.isSelected("Totem") && soundPath.contains("totem")) {
            return true;
         }

         if (instance.sounds.isSelected("Firework") && soundPath.contains("firework")) {
            return true;
         }

         if (instance.sounds.isSelected("Step") && soundPath.contains("step")) {
            return true;
         }

         if (!instance.sounds.isSelected("Eat") || !soundPath.contains("eat") && !soundPath.contains("drink") && !soundPath.contains("burp")) {
            if (!instance.sounds.isSelected("Hit") || !soundPath.contains("attack") && !soundPath.contains("hit")) {
               return !instance.sounds.isSelected("Warden")
                     || !soundPath.contains("warden")
                        && !soundPath.contains("sculk_sensor")
                        && !soundPath.contains("sculk_shrieker")
                        && !soundPath.contains("shriek")
                  ? instance.sounds.isSelected("Hurt") && soundPath.contains("hurt")
                  : true;
            } else {
               return true;
            }
         } else {
            return true;
         }
      } else {
         return false;
      }
   }

   private static boolean active() {
      return instance != null && instance.isEnabled();
   }
}
