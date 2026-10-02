package org.ryzen.mixin.gui;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_11223;
import net.minecraft.class_11224;
import net.minecraft.class_2561;
import net.minecraft.class_266;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_329;
import net.minecraft.class_332;
import net.minecraft.class_9779;
import org.ryzen.context.RenderContext;
import org.ryzen.event.EventManager;
import org.ryzen.event.Events;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.visual.BoardSpooferFeature;
import org.ryzen.feature.impl.visual.CrosshairFeature;
import org.ryzen.feature.impl.visual.HudFeature;
import org.ryzen.feature.impl.visual.RemovalsFeature;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(class_329.class)
public abstract class HudMixin {
   @Shadow
   @Final
   private class_310 field_2035;
   @Unique
   private boolean blade$hotbarDecorationsShifted;

   @Inject(method = "method_1757", at = @At("HEAD"), cancellable = true)
   private void onDisplayScoreboardSidebar(class_332 guiGraphicsExtractor, class_266 objective, CallbackInfo ci) {
      if (RemovalsFeature.shouldRemoveScoreboard()) {
         ci.cancel();
      }
   }

   @ModifyArg(
      method = "method_1757",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_332;method_51439(Lnet/minecraft/class_327;Lnet/minecraft/class_2561;IIIZ)V"),
      index = 1
   )
   private class_2561 ryzen$spoofScoreboardText(class_2561 original) {
      return BoardSpooferFeature.transform(original);
   }

   @Inject(method = "method_55805", at = @At("HEAD"), cancellable = true)
   private void blade$shiftHotbarDecorations(class_332 guiGraphicsExtractor, class_9779 deltaTracker, CallbackInfo ci) {
      float offsetX = HudFeature.hotbarDecorationOffsetX(this.field_2035);
      float offsetY = HudFeature.hotbarDecorationOffsetY(this.field_2035);
      this.blade$hotbarDecorationsShifted = offsetX != 0.0F || offsetY != 0.0F;
      if (this.blade$hotbarDecorationsShifted) {
         guiGraphicsExtractor.method_51448().pushMatrix();
         guiGraphicsExtractor.method_51448().translate(offsetX, offsetY);
      }
   }

   @Inject(method = "method_55805", at = @At("RETURN"))
   private void blade$unshiftHotbarDecorations(class_332 guiGraphicsExtractor, class_9779 deltaTracker, CallbackInfo ci) {
      if (this.blade$hotbarDecorationsShifted) {
         guiGraphicsExtractor.method_51448().popMatrix();
         this.blade$hotbarDecorationsShifted = false;
      }
   }

   @WrapOperation(method = "method_55805", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_329;method_1760(Lnet/minecraft/class_332;)V"))
   private void blade$scalePlayerStatus(class_329 instance, class_332 guiGraphicsExtractor, Operation<Void> original) {
      this.blade$withScaledStatus(guiGraphicsExtractor, () -> original.call(new Object[]{instance, guiGraphicsExtractor}));
   }

   @WrapOperation(method = "method_55805", at = @At(value = "INVOKE", target = "Lnet/minecraft/class_329;method_1741(Lnet/minecraft/class_332;)V"))
   private void blade$scaleVehicleStatus(class_329 instance, class_332 guiGraphicsExtractor, Operation<Void> original) {
      this.blade$withScaledStatus(guiGraphicsExtractor, () -> original.call(new Object[]{instance, guiGraphicsExtractor}));
   }

   @Unique
   private void blade$withScaledStatus(class_332 guiGraphicsExtractor, Runnable draw) {
      float scale = HudFeature.hotbarDecorationScale(this.field_2035);
      if (Math.abs(scale - 1.0F) < 0.001F) {
         draw.run();
      } else {
         float pivotX = this.field_2035.method_22683().method_4486() / 2.0F;
         float pivotY = this.field_2035.method_22683().method_4502() - 22.0F;
         guiGraphicsExtractor.method_51448().pushMatrix();
         guiGraphicsExtractor.method_51448().translate(pivotX, pivotY);
         guiGraphicsExtractor.method_51448().scale(scale);
         guiGraphicsExtractor.method_51448().translate(-pivotX, -pivotY);

         try {
            draw.run();
         } finally {
            guiGraphicsExtractor.method_51448().popMatrix();
         }
      }
   }

   @Inject(method = "method_1759", at = @At("HEAD"), cancellable = true)
   private void blade$hideVanillaHotbar(class_332 guiGraphicsExtractor, class_9779 deltaTracker, CallbackInfo ci) {
      if (HudFeature.customHotbarActive()) {
         ci.cancel();
      }
   }

   @Redirect(
      method = "method_55805",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11223;method_70865(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V")
   )
   private void blade$skipXpBarBackground(class_11223 bar, class_332 guiGraphicsExtractor, class_9779 deltaTracker) {
      if (!(bar instanceof class_11224) || !HudFeature.customHotbarActive()) {
         bar.method_70865(guiGraphicsExtractor, deltaTracker);
      }
   }

   @Redirect(
      method = "method_55805",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11223;method_70868(Lnet/minecraft/class_332;Lnet/minecraft/class_9779;)V")
   )
   private void blade$skipXpBarFill(class_11223 bar, class_332 guiGraphicsExtractor, class_9779 deltaTracker) {
      if (!(bar instanceof class_11224) || !HudFeature.customHotbarActive()) {
         bar.method_70868(guiGraphicsExtractor, deltaTracker);
      }
   }

   @Redirect(
      method = "method_55805",
      at = @At(value = "INVOKE", target = "Lnet/minecraft/class_11223;method_70866(Lnet/minecraft/class_332;Lnet/minecraft/class_327;I)V")
   )
   private void blade$skipXpLevel(class_332 guiGraphics, class_327 font, int experienceLevel) {
      if (!HudFeature.customHotbarActive()) {
         class_11223.method_70866(guiGraphics, font, experienceLevel);
      }
   }

   @Inject(method = "method_1749", at = @At("HEAD"), cancellable = true)
   private void blade$hideVanillaSelectedItemName(class_332 guiGraphicsExtractor, CallbackInfo ci) {
      if (HudFeature.customHotbarActive()) {
         ci.cancel();
      }
   }

   @Inject(method = "method_1736", at = @At("HEAD"), cancellable = true)
   private void hideVanillaCrosshair(class_332 guiGraphicsExtractor, class_9779 deltaTracker, CallbackInfo ci) {
      if (FeatureManager.INSTANCE.getEnabled(CrosshairFeature.class) != null) {
         ci.cancel();
      }
   }

   @Inject(method = "method_1753", at = @At("TAIL"))
   private void onExtractRenderState(class_332 guiGraphicsExtractor, class_9779 deltaTracker, CallbackInfo ci) {
      class_329 gui = (class_329)(Object)this;
      RenderContext.enter2D(gui, guiGraphicsExtractor, deltaTracker);

      try {
         Render2DUtil.beginFrame();
         if (EventManager.hasListeners(Render2DEvent.class)) {
            EventManager.call(Events.RENDER_2D.set(this.field_2035, gui, guiGraphicsExtractor, deltaTracker));
         }

         Render2DUtil.flush();
      } finally {
         RenderContext.exit2D();
      }
   }
}
