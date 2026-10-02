package org.ryzen.utils.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10034;
import net.minecraft.class_10055;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_572;
import net.minecraft.class_630;
import org.ryzen.feature.impl.visual.EmotionsFeature;

@Environment(EnvType.CLIENT)
public final class EmotionAnimator {
   public static final String GREETING = "Приветствие";
   public static final String DANCE = "Танец";
   public static final String WANK = "Дрочка";
   public static final String ALPHA_WALK = "Альфа ходьба";
   public static final String ALPHA_MAN = "Альфа-Мужик";
   public static final String[] EMOTIONS = new String[]{"Приветствие", "Танец", "Дрочка", "Альфа ходьба", "Альфа-Мужик"};
   private static final float SWING_DAMP_THRESHOLD = 0.2F;
   private static final long LONG_CYCLE_MS = 10000L;
   private static final long DANCE_CYCLE_MS = 6000L;

   private EmotionAnimator() {
   }

   public static void apply(class_572<?> model, class_10034 state) {
      EmotionsFeature emotions = EmotionsFeature.getInstance();
      if (emotions != null && emotions.isEnabled()) {
         String preview = emotions.getPreviewEmotion();
         if (preview == null) {
            String selected = emotions.getSelectedEmotion();
            if (selected != null) {
               class_1657 player = resolvePlayer(state);
               if (player != null && emotions.appliesTo(player)) {
                  float swingDamp = swingDamp(player);
                  applyAnimatedPose(model, selected, state.field_53450, state.field_53451, swingDamp);
                  model.field_3394.method_32085(model.field_3398.method_32084());
               }
            }
         } else if (isLocalPlayer(state)) {
            resetToBasePose(model);
            applyStillPose(model, preview, state);
            model.field_3394.method_32085(model.field_3398.method_32084());
         }
      }
   }

   private static boolean isLocalPlayer(class_10034 state) {
      return state instanceof class_10055 avatar
         && class_310.method_1551().field_1724 != null
         && avatar.field_53528 == class_310.method_1551().field_1724.method_5628();
   }

   private static class_1657 resolvePlayer(class_10034 state) {
      if (state instanceof class_10055 avatar && class_310.method_1551().field_1687 != null) {
         return class_310.method_1551().field_1687.method_8469(avatar.field_53528) instanceof class_1657 player ? player : null;
      } else {
         return null;
      }
   }

   private static float swingDamp(class_1657 player) {
      float damp = Math.max(1.0F, (float)player.method_18798().method_1027() / 0.2F);
      return damp * damp * damp;
   }

   private static void resetToBasePose(class_572<?> model) {
      for (class_630 part : new class_630[]{model.field_3398, model.field_3391, model.field_3401, model.field_27433, model.field_3392, model.field_3397}) {
         part.method_33425(0.0F, 0.0F, 0.0F);
      }

      model.field_3398.field_3656 = 0.0F;
      model.field_3391.field_3656 = 0.0F;
      model.field_3401.field_3657 = -5.0F;
      model.field_3401.field_3656 = 2.0F;
      model.field_3401.field_3655 = 0.0F;
      model.field_27433.field_3657 = 5.0F;
      model.field_27433.field_3656 = 2.0F;
      model.field_27433.field_3655 = 0.0F;
      model.field_3392.field_3656 = 12.0F;
      model.field_3392.field_3655 = 0.1F;
      model.field_3397.field_3656 = 12.0F;
      model.field_3397.field_3655 = 0.1F;
   }

   private static void applyStillPose(class_572<?> model, String emotion, class_10034 state) {
      switch (emotion) {
         case "Приветствие":
            model.field_3401.field_3654 = -3.0F;
            model.field_3401.field_3674 = -0.5F;
            break;
         case "Танец":
            model.field_3391.field_3675 = 0.2F;
            model.field_3398.field_3675 = 0.3F;
            model.field_3401.field_3654 = -1.5F;
            model.field_3401.field_3674 = 0.5F;
            model.field_27433.field_3654 = -1.2F;
            model.field_27433.field_3674 = -0.5F;
            model.field_3392.field_3654 = 0.3F;
            model.field_3397.field_3654 = -0.3F;
            break;
         case "Дрочка":
            model.field_3401.field_3654 = -0.3F;
            model.field_3401.field_3674 = -0.5F;
            model.field_3398.field_3654 = 0.3F;
            break;
         case "Альфа ходьба":
            alphaWalk(model, state.field_53450, state.field_53451, 1.0F);
            break;
         case "Альфа-Мужик":
            alphaMan(model, state.field_53450, state.field_53451, 1.0F);
      }
   }

   private static void applyAnimatedPose(class_572<?> model, String emotion, float walkPos, float walkSpeed, float damp) {
      float time = (float)(System.currentTimeMillis() % 10000L) / 1000.0F;
      switch (emotion) {
         case "Приветствие":
            waveArm(model, walkPos, walkSpeed, damp, time, 10.0F, 0.2F, -3.0F, false);
            break;
         case "Танец":
            dance(model);
            break;
         case "Дрочка":
            waveArm(model, walkPos, walkSpeed, damp, time, 30.0F, 0.2F, -1.1F, true);
            break;
         case "Альфа ходьба":
            alphaWalk(model, walkPos, walkSpeed, damp);
            break;
         case "Альфа-Мужик":
            alphaMan(model, walkPos, walkSpeed, damp);
      }
   }

   private static void waveArm(
      class_572<?> model, float walkPos, float walkSpeed, float damp, float time, float frequency, float amplitude, float pitchBase, boolean swingPitch
   ) {
      float wave = class_3532.method_15374(time * frequency) * amplitude;
      model.field_3401.field_3654 = swingPitch ? pitchBase + wave : pitchBase;
      model.field_3401.field_3675 = wave;
      model.field_3401.field_3674 = -0.5F + wave;
      model.field_27433.field_3654 = 0.0F;
      model.field_3392.field_3654 = class_3532.method_15362(walkPos * 0.6662F) * 1.4F * walkSpeed / damp;
      model.field_3397.field_3654 = class_3532.method_15362(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed / damp;
      model.field_3398.field_3654 = 0.0F;
   }

   private static void dance(class_572<?> model) {
      float time = (float)(System.currentTimeMillis() % 6000L) / 1000.0F;
      model.field_3391.field_3675 = class_3532.method_15374(time * 2.0F) * 0.3F;
      model.field_3391.field_3654 = class_3532.method_15374(time * 1.5F) * 0.1F;
      model.field_3398.field_3675 = class_3532.method_15374(time * 1.8F) * 0.4F;
      model.field_3398.field_3654 = class_3532.method_15374(time * 2.2F) * 0.2F;
      model.field_3401.field_3654 = -1.5F + class_3532.method_15374(time * 3.0F) * 0.8F;
      model.field_3401.field_3675 = class_3532.method_15374(time * 1.5F) * 0.5F;
      model.field_3401.field_3674 = class_3532.method_15374(time * 2.5F) * 0.7F;
      model.field_27433.field_3654 = -1.5F + class_3532.method_15374(time * 3.0F + (float) Math.PI) * 0.8F;
      model.field_27433.field_3675 = class_3532.method_15374(time * 1.5F + (float) Math.PI) * 0.5F;
      model.field_27433.field_3674 = class_3532.method_15374(time * 2.5F + (float) Math.PI) * 0.7F;
      model.field_3392.field_3654 = 0.5F + class_3532.method_15374(time * 2.0F) * 0.6F;
      model.field_3397.field_3654 = 0.5F + class_3532.method_15374(time * 2.0F + (float) Math.PI) * 0.6F;
      if (time > 3.0F && time < 4.0F) {
         float lift = (time - 3.0F) * 2.0F;
         model.field_3391.field_3656 = lift;
         model.field_3392.field_3656 = 12.0F + lift;
         model.field_3397.field_3656 = 12.0F + lift;
         model.field_3401.field_3656 = 2.0F + lift;
         model.field_27433.field_3656 = 2.0F + lift;
      } else {
         model.field_3391.field_3656 = 0.0F;
         model.field_3392.field_3656 = 12.0F;
         model.field_3397.field_3656 = 12.0F;
         model.field_3401.field_3656 = 2.0F;
         model.field_27433.field_3656 = 2.0F;
      }

      if (time > 4.5F && time < 5.0F) {
         float dip = class_3532.method_15374((time - 4.5F) * (float) Math.PI * 2.0F);
         model.field_3391.field_3656 = -dip * 1.5F;
         model.field_3398.field_3656 = dip * 1.5F;
      }
   }

   private static void alphaWalk(class_572<?> model, float walkPos, float walkSpeed, float damp) {
      model.field_3401.field_3654 = class_3532.method_15362(walkPos * 0.6662F + (float) Math.PI) * 2.0F * walkSpeed / damp;
      model.field_27433.field_3654 = class_3532.method_15362(walkPos * 0.6662F) * 2.0F * walkSpeed / damp;
      float armRoll = (class_3532.method_15362(walkPos * 0.2312F) + 1.0F) * walkSpeed / damp;
      model.field_3401.field_3674 = armRoll;
      model.field_27433.field_3674 = -armRoll;
      model.field_3401.field_3675 = 0.0F;
      model.field_27433.field_3675 = 0.0F;
      model.field_3392.field_3654 = class_3532.method_15362(walkPos * 0.6662F) * 1.4F * walkSpeed / damp;
      model.field_3397.field_3654 = class_3532.method_15362(walkPos * 0.6662F + (float) Math.PI) * 1.4F * walkSpeed / damp;
      float legRoll = class_3532.method_15362(walkPos * 0.6662F) * 0.4F * walkSpeed / damp;
      model.field_3392.field_3674 = legRoll;
      model.field_3397.field_3674 = -legRoll;
      model.field_3391.field_3654 = 0.0F;
      model.field_3391.field_3675 = 0.0F;
      model.field_3391.field_3674 = 0.0F;
   }

   private static void alphaMan(class_572<?> model, float walkPos, float walkSpeed, float damp) {
      float time = (float)(System.currentTimeMillis() % 10000L) / 1000.0F;
      float swing = Math.max(0.25F, walkSpeed);
      model.field_3391.field_3654 = -0.1F + class_3532.method_15374(time * 1.15F) * 0.04F;
      model.field_3391.field_3674 = class_3532.method_15374(time * 2.1F) * 0.14F;
      model.field_3391.field_3675 = class_3532.method_15374(time * 1.4F) * 0.09F;
      model.field_3398.field_3654 = -0.18F + class_3532.method_15374(time * 1.05F) * 0.05F;
      model.field_3398.field_3674 = class_3532.method_15374(time * 2.25F) * 0.11F;
      model.field_3398.field_3675 = class_3532.method_15374(time * 0.95F) * 0.13F;
      float flexPeriod = 2.8F;
      float phase = time % flexPeriod / flexPeriod;
      if (phase < 0.32F) {
         float rise = class_3532.method_15374(phase / 0.32F * (float) Math.PI);
         model.field_3401.field_3654 = -0.55F - rise * 1.05F;
         model.field_27433.field_3654 = -0.55F - rise * 1.05F;
         model.field_3401.field_3675 = -0.55F * rise;
         model.field_27433.field_3675 = 0.55F * rise;
         model.field_3401.field_3674 = -0.35F * rise;
         model.field_27433.field_3674 = 0.35F * rise;
      } else {
         float beat = time * 2.0F;
         model.field_3401.field_3654 = -0.42F + class_3532.method_15374(beat) * 0.32F;
         model.field_27433.field_3654 = -0.32F + class_3532.method_15374(beat + (float) Math.PI) * 0.38F;
         model.field_3401.field_3674 = 0.22F + class_3532.method_15362(beat * 0.65F) * 0.22F;
         model.field_27433.field_3674 = -0.28F + class_3532.method_15362(beat * 0.65F) * 0.18F;
         model.field_3401.field_3675 = 0.28F + class_3532.method_15374(beat) * 0.18F;
         model.field_27433.field_3675 = -0.32F + class_3532.method_15374(beat) * 0.14F;
      }

      model.field_3392.field_3654 = 0.12F + class_3532.method_15362(walkPos * 0.6662F) * 1.15F * swing / damp + class_3532.method_15374(time * 3.2F) * 0.07F;
      model.field_3397.field_3654 = 0.1F
         + class_3532.method_15362(walkPos * 0.6662F + (float) Math.PI) * 1.15F * swing / damp
         + class_3532.method_15374(time * 3.2F) * 0.07F;
      model.field_3392.field_3674 = 0.1F + class_3532.method_15374(time * 2.05F) * 0.07F;
      model.field_3397.field_3674 = -0.1F - class_3532.method_15374(time * 2.05F) * 0.07F;
   }
}
