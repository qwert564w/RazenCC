package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_5498;
import net.minecraft.class_746;
import org.joml.Matrix3x2fStack;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.ScaleUtil;

@Environment(EnvType.CLIENT)
public final class CrosshairFeature extends Feature {
   public static final String DEFAULT = "Default";
   public static final String CIRCLE = "Circle";
   private static final int OUTLINE_COLOR = ColorUtil.rgba(0, 0, 0, 215);
   public final ModeSetting type = this.register(new ModeSetting("Type", "Default", "Default", "Circle").configKey("render.crosshair.type"));
   public final ModeSetting colorMode = this.register(ColorMode.setting().configKey("render.crosshair.colorMode"));
   public final ColorSetting color = this.register(
      new ColorSetting("Color", -1).configKey("render.crosshair.color").visibleWhen(() -> ColorMode.isCustom(this.colorMode))
   );
   public final NumberSetting radius = this.register(
      new NumberSetting("Radius", 12.0, 12.0, 20.0, 1.0, "px").configKey("render.crosshair.radius").visibleWhen(() -> this.type.is("Circle"))
   );
   public final NumberSetting gap = this.register(
      new NumberSetting("Gap", 2.0, 2.0, 20.0, 1.0, "px").configKey("render.crosshair.gap").visibleWhen(() -> this.type.is("Default"))
   );
   public final NumberSetting length = this.register(
      new NumberSetting("Length", 5.0, 5.0, 20.0, 1.0, "px").configKey("render.crosshair.length").visibleWhen(() -> this.type.is("Default"))
   );
   public final NumberSetting thickness = this.register(new NumberSetting("Thickness", 2.0, 2.0, 5.0, 0.5, "px").configKey("render.crosshair.thickness"));
   public final BooleanSetting cooldown = this.register(new BooleanSetting("Cooldown Animation", true).configKey("render.crosshair.cooldown"));
   public final NumberSetting cooldownMultiplier = this.register(
      new NumberSetting("Cooldown Gap Multiplier", 8.0, 8.0, 20.0, 1.0, "px")
         .configKey("render.crosshair.cooldownMultiplier")
         .visibleWhen(() -> this.cooldown.getValue())
   );
   public final BooleanSetting outline = this.register(new BooleanSetting("Outline", true).configKey("render.crosshair.outline"));
   public final BooleanSetting tMode = this.register(
      new BooleanSetting("T-Mode", false).configKey("render.crosshair.tmode").visibleWhen(() -> this.type.is("Default"))
   );
   public final BooleanSetting centerDot = this.register(
      new BooleanSetting("Center Dot", true).configKey("render.crosshair.centerdot").visibleWhen(() -> this.type.is("Default"))
   );
   public final NumberSetting centerSize = this.register(
      new NumberSetting("Center Size", 2.0, 2.0, 10.0, 1.0, "px")
         .configKey("render.crosshair.centersize")
         .visibleWhen(() -> this.type.is("Default") && this.centerDot.getValue())
   );
   private float animatedCooldown;
   private long lastFrameNanos;

   public CrosshairFeature() {
      super("Crosshair", "Procedural configurable crosshair", FeatureCategory.VISUAL, -1);
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      if (player != null && client.field_1690.method_31044() == class_5498.field_26664 && !client.method_53526().method_53536()) {
         float centerX = event.getGuiGraphicsExtractor().method_51421() / 2.0F;
         float centerY = event.getGuiGraphicsExtractor().method_51443() / 2.0F;
         float unit = ScaleUtil.toGuiPixels(1.0F, client.method_22683().method_4495());
         float delta = this.frameDelta();
         float cooldownTarget = this.cooldown.getValue() ? 1.0F - player.method_7261(0.5F) : 0.0F;
         this.animatedCooldown = this.animatedCooldown + (cooldownTarget - this.animatedCooldown) * (1.0F - (float)Math.exp(-16.0F * delta));
         float expansion = this.animatedCooldown * this.cooldownMultiplier.getValue().floatValue() * unit;
         int resolvedColor = ColorMode.resolve(this.colorMode, this.color);
         if (this.type.is("Circle")) {
            this.drawCircle(
               event,
               centerX,
               centerY,
               this.radius.getValue().floatValue() * unit + expansion,
               this.thickness.getValue().floatValue() * unit,
               resolvedColor,
               unit
            );
         } else {
            this.drawDefault(event, centerX, centerY, expansion, resolvedColor, unit);
         }
      }
   }

   @Override
   protected void onEnable() {
      this.resetAnimation();
   }

   @Override
   protected void onDisable() {
      this.resetAnimation();
   }

   private void drawDefault(Render2DEvent event, float centerX, float centerY, float expansion, int color, float unit) {
      float lineGap = this.gap.getValue().floatValue() * unit + expansion;
      float lineLength = this.length.getValue().floatValue() * unit;
      float lineThickness = this.thickness.getValue().floatValue() * unit;
      float halfThickness = lineThickness * 0.5F;
      this.drawBar(centerX - lineGap - lineLength, centerY - halfThickness, lineLength, lineThickness, color, unit);
      this.drawBar(centerX + lineGap, centerY - halfThickness, lineLength, lineThickness, color, unit);
      if (!this.tMode.getValue()) {
         this.drawBar(centerX - halfThickness, centerY - lineGap - lineLength, lineThickness, lineLength, color, unit);
      }

      this.drawBar(centerX - halfThickness, centerY + lineGap, lineThickness, lineLength, color, unit);
      if (this.centerDot.getValue()) {
         float dotSize = this.centerSize.getValue().floatValue() * unit;
         this.drawBar(centerX - dotSize * 0.5F, centerY - dotSize * 0.5F, dotSize, dotSize, color, unit);
      }
   }

   private void drawCircle(Render2DEvent event, float centerX, float centerY, float radius, float thickness, int color, float unit) {
      if (this.outline.getValue()) {
         this.drawRing(event, centerX, centerY, radius, thickness + 2.0F * unit, OUTLINE_COLOR, unit);
      }

      this.drawRing(event, centerX, centerY, radius, thickness, color, unit);
   }

   private void drawRing(Render2DEvent event, float centerX, float centerY, float radius, float thickness, int color, float unit) {
      int segments = Math.max(40, (int)Math.ceil(radius * 4.0F));

      for (int index = 0; index < segments; index++) {
         double firstAngle = (Math.PI * 2) * index / segments;
         double secondAngle = (Math.PI * 2) * (index + 1) / segments;
         float x1 = centerX + (float)Math.cos(firstAngle) * radius;
         float y1 = centerY + (float)Math.sin(firstAngle) * radius;
         float x2 = centerX + (float)Math.cos(secondAngle) * radius;
         float y2 = centerY + (float)Math.sin(secondAngle) * radius;
         this.drawSegment(event, x1, y1, x2, y2, thickness, color, unit);
      }
   }

   private void drawSegment(Render2DEvent event, float x1, float y1, float x2, float y2, float thickness, int color, float unit) {
      float dx = x2 - x1;
      float dy = y2 - y1;
      float segmentLength = (float)Math.sqrt(dx * dx + dy * dy);
      float overlap = Math.max(0.35F * unit, thickness * 0.2F);
      Matrix3x2fStack pose = event.getGuiGraphicsExtractor().method_51448();
      pose.pushMatrix();
      pose.translate(x1, y1);
      pose.rotate((float)Math.atan2(dy, dx));
      Render2DUtil.rect(-overlap, -thickness * 0.5F, segmentLength + overlap * 2.0F, thickness).color(color).radius(thickness * 0.5F).draw();
      pose.popMatrix();
   }

   private void drawBar(float x, float y, float width, float height, int color, float unit) {
      if (this.outline.getValue()) {
         float border = unit;
         Render2DUtil.rect(x - border, y - border, width + border * 2.0F, height + border * 2.0F)
            .color(OUTLINE_COLOR)
            .radius(Math.min(width, height) * 0.35F + border)
            .draw();
      }

      Render2DUtil.rect(x, y, width, height).color(color).radius(Math.min(width, height) * 0.35F).draw();
   }

   private float frameDelta() {
      long now = System.nanoTime();
      float delta = this.lastFrameNanos == 0L ? 0.016666668F : (float)(now - this.lastFrameNanos) / 1.0E9F;
      this.lastFrameNanos = now;
      return Math.clamp(delta, 0.001F, 0.05F);
   }

   private void resetAnimation() {
      this.animatedCooldown = 0.0F;
      this.lastFrameNanos = 0L;
   }
}
