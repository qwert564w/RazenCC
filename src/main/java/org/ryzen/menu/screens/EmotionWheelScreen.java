package org.ryzen.menu.screens;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_437;
import org.ryzen.context.RenderContext;
import org.ryzen.feature.impl.visual.EmotionsFeature;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.EmotionAnimator;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class EmotionWheelScreen extends class_437 {
   public static final int SLOT_NONE = -2;
   private static final float INNER_RADIUS = 28.0F;
   private static final float OUTER_RADIUS = 105.0F;
   private static final float LABEL_RADIUS = 72.0F;
   private static final float BACKDROP_RADIUS = 115.0F;
   private static final float LABEL_SIZE = 12.0F;
   private MsdfFont font;
   private int hoveredSlot = -2;

   public EmotionWheelScreen() {
      super(class_2561.method_43470("Emotions"));
   }

   public int getHoveredSlot() {
      return this.hoveredSlot;
   }

   protected void method_25426() {
      this.font = UiFonts.sfProDisplay();
   }

   public boolean method_25421() {
      return false;
   }

   public void method_25394(class_332 graphics, int mouseX, int mouseY, float partialTick) {
      float centerX = this.field_22789 / 2.0F;
      float centerY = this.field_22790 / 2.0F;
      this.hoveredSlot = resolveSlot(mouseX - centerX, mouseY - centerY);
      EmotionsFeature emotions = EmotionsFeature.getInstance();
      if (emotions != null) {
         emotions.updatePreview();
      }

      int accent = Theme.getAccent();
      RenderContext.enter2D(null, graphics, null);
      Render2DUtil.beginFrame();

      try {
         Render2DUtil.rect(centerX - 115.0F, centerY - 115.0F, 230.0F, 230.0F)
            .radius(115.0F)
            .color(-2013265920)
            .border(1.0F, ColorUtil.withAlpha(accent, 90))
            .draw();
         Render2DUtil.rect(centerX - 28.0F, centerY - 28.0F, 56.0F, 56.0F)
            .radius(28.0F)
            .color(-1441787888)
            .border(1.0F, this.hoveredSlot == -2 ? -43691 : ColorUtil.withAlpha(accent, 60))
            .draw();
         String[] labels = EmotionAnimator.EMOTIONS;

         for (int index = 0; index < labels.length; index++) {
            double angle = (Math.PI * 2) * index / labels.length - (Math.PI / 2);
            float x = centerX + (float)(Math.cos(angle) * 72.0);
            float y = centerY + (float)(Math.sin(angle) * 72.0);
            Render2DUtil.text(x, this.font.centeredTextY(y, 12.0F), 12.0F, labels[index])
               .font(this.font)
               .align(TextAlign.CENTER)
               .color(index == this.hoveredSlot ? accent : -1)
               .draw();
         }

         Render2DUtil.text(centerX, this.font.centeredTextY(centerY, 12.0F), 12.0F, "Отмена")
            .font(this.font)
            .align(TextAlign.CENTER)
            .color(this.hoveredSlot == -2 ? -43691 : -5592406)
            .draw();
         Render2DUtil.flush();
      } finally {
         RenderContext.exit2D();
      }
   }

   private static int resolveSlot(float deltaX, float deltaY) {
      double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
      if (!(distance < 28.0) && !(distance > 105.0)) {
         double angle = Math.atan2(deltaY, deltaX) + (Math.PI / 2);
         if (angle < 0.0) {
            angle += Math.PI * 2;
         }

         int count = EmotionAnimator.EMOTIONS.length;
         return class_3532.method_15357(angle / (Math.PI * 2) * count) % count;
      } else {
         return -2;
      }
   }
}
