package org.ryzen.mixin.gui;

import java.util.Optional;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3532;
import net.minecraft.class_4011;
import net.minecraft.class_425;
import org.ryzen.context.RenderContext;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_425.class)
public abstract class LoadingOverlayMixin {
   @Shadow
   @Final
   private class_310 field_18217;
   @Shadow
   @Final
   private class_4011 field_17767;
   @Shadow
   @Final
   private Consumer<Optional<Throwable>> field_18218;
   @Shadow
   @Final
   private boolean field_18219;
   @Shadow
   private float field_17770;
   @Shadow
   private long field_17771;
   @Shadow
   private long field_18220;
   @Unique
   private static final class_2960 LOGO_BOOT = Textures.Logos.BOOT;
   @Unique
   private long lastTime = -1L;
   @Unique
   private float animTime = 0.0F;

   @Inject(method = "method_25394", at = @At("HEAD"), cancellable = true)
   private void onRender(class_332 guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
      ci.cancel();
      if (Thread.currentThread().getPriority() != 10) {
         try {
            Thread.currentThread().setPriority(10);
         } catch (Throwable var45) {
         }
      }

      int width = this.field_18217.method_22683().method_4486();
      int height = this.field_18217.method_22683().method_4502();
      long currentTime = class_156.method_658();
      if (this.field_18219 && this.field_18220 == -1L) {
         this.field_18220 = currentTime;
      }

      if (RenderContext.overlayStartTime == -1L) {
         RenderContext.overlayStartTime = currentTime;
      }

      long elapsed = currentTime - RenderContext.overlayStartTime;
      boolean isStartup = this.field_18217.field_1755 == null;
      long animStartDelay = isStartup ? 1200L : 0L;
      if (this.lastTime == -1L) {
         this.lastTime = currentTime;
      }

      float deltaTime = (float)(currentTime - this.lastTime) / 1000.0F;
      this.lastTime = currentTime;
      float progressDelta = Math.min(deltaTime, 0.03F);
      if (elapsed >= animStartDelay) {
         this.animTime += progressDelta;
      }

      float targetProgress;
      if (elapsed < animStartDelay) {
         targetProgress = 0.0F;
      } else if (!this.field_17767.method_18787()) {
         float progressTime = Math.max(0.0F, (float)(elapsed - animStartDelay) / 1000.0F);
         float simulated = class_3532.method_15363(progressTime * 0.51F, 0.0F, 0.92F);
         targetProgress = Math.max(simulated, this.field_17767.method_18229() * 0.92F);
      } else {
         targetProgress = 1.0F;
      }

      float catchUpSpeed = !this.field_17767.method_18787() ? 0.2F : 0.5F;
      if (this.field_17770 < targetProgress) {
         this.field_17770 = Math.min(this.field_17770 + catchUpSpeed * progressDelta, targetProgress);
      } else {
         this.field_17770 = class_3532.method_15363(this.field_17770 * 0.98F + targetProgress * 0.02F, 0.0F, 1.0F);
      }

      float fadeOutProgress = this.field_17771 > -1L ? (float)(currentTime - this.field_17771) / 1500.0F : 0.0F;
      if (fadeOutProgress >= 1.0F) {
         this.field_18217.method_18502(null);
         RenderContext.overlayStartTime = -1L;
      } else {
         if (this.field_17767.method_18787() && this.field_18217.field_1755 != null) {
            this.field_18217.field_1755.method_47413(guiGraphics, mouseX, mouseY, partialTick);
         }

         float alpha = 1.0F;
         if (this.field_17771 > -1L) {
            alpha = class_3532.method_15363(1.0F - fadeOutProgress, 0.0F, 1.0F);
         } else if (this.field_18219 && this.field_18220 > -1L) {
            float fadeInProgress = (float)(currentTime - this.field_18220) / 1000.0F;
            alpha = class_3532.method_15363(fadeInProgress, 0.0F, 1.0F);
         }

         int bgAlpha = Math.round(255.0F * alpha);
         int bgCol = ColorUtil.rgba(21, 21, 22, bgAlpha);
         RenderContext.enter2D(null, guiGraphics, null);

         try {
            Render2DUtil.beginFrame();
            Render2DUtil.rect(0.0F, 0.0F, width, height).color(bgCol).draw();
            float baseLogoY = (height - 96.0F) / 2.0F - 25.0F;
            float logoSize = 96.0F;
            if (this.field_17771 > -1L) {
               baseLogoY -= 15.0F * fadeOutProgress;
            }

            float logoX = (width - logoSize) / 2.0F;
            float logoY = baseLogoY;
            int logoColor = ColorUtil.rgba(255, 255, 255, Math.round(255.0F * alpha));
            Render2DUtil.texture(logoX, logoY, logoSize, logoSize, LOGO_BOOT).color(logoColor).draw();
            float barWidth = 200.0F;
            float barHeight = 4.0F;
            float barX = (width - barWidth) / 2.0F;
            float barY = (height - 96.0F) / 2.0F - 25.0F + 96.0F + 55.0F;
            if (this.field_17771 > -1L) {
               barY += 15.0F * fadeOutProgress;
            }

            int trackColor = ColorUtil.rgba(38, 38, 43, Math.round(255.0F * alpha));
            int borderColor = ColorUtil.rgba(255, 255, 255, Math.round(10.2F * alpha));
            Render2DUtil.rect(barX, barY, barWidth, barHeight).color(trackColor).radius(2.0F).border(0.5F, borderColor).draw();
            int accentColor = Theme.getAccent();
            int accentFadeColor = ColorUtil.withAlpha(accentColor, Math.round(255.0F * alpha));
            float fillWidth = barWidth * this.field_17770;
            if (fillWidth > 0.0F) {
               Render2DUtil.rect(barX, barY, fillWidth, barHeight).color(accentFadeColor).radius(2.0F).draw();
               float glareWidth = 40.0F;
               float glareProgress = (float)(currentTime % 1500L) / 1500.0F;
               float glareX = barX + (fillWidth + glareWidth) * glareProgress - glareWidth;
               float drawGlareX = Math.max(barX, glareX);
               float drawGlareWidth = Math.min(barX + fillWidth, glareX + glareWidth) - drawGlareX;
               if (drawGlareWidth > 0.0F) {
                  Render2DUtil.rect(drawGlareX, barY, drawGlareWidth, barHeight)
                     .color(ColorUtil.rgba(255, 255, 255, Math.round(75.0F * alpha)))
                     .radius(2.0F)
                     .draw();
               }
            }

            Render2DUtil.flush();
         } finally {
            RenderContext.exit2D();
         }
      }
   }
}
