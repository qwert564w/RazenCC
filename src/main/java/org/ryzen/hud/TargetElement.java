package org.ryzen.hud;

import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1304;
import net.minecraft.class_1309;
import net.minecraft.class_1799;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_332;
import net.minecraft.class_3879;
import net.minecraft.class_630;
import net.minecraft.class_742;
import net.minecraft.class_897;
import net.minecraft.class_922;
import net.minecraft.class_630.class_593;
import net.minecraft.class_630.class_618;
import net.minecraft.class_630.class_628;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.RenderContext;
import org.ryzen.feature.impl.combat.AuraFeature;
import org.ryzen.feature.impl.combat.TriggerBotFeature;
import org.ryzen.mixin.accessor.ModelPartAccessor;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class TargetElement extends HudElement {
   private static final float DESIGN_WIDTH = 226.0F;
   private static final float DESIGN_HEIGHT = 65.0F;
   private static final class_1304[] EQUIPMENT_ORDER = new class_1304[]{
      class_1304.field_6173, class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166
   };
   private static final float[] EQUIPMENT_X = new float[]{75.0F, 92.877F, 109.38F, 125.882F, 146.1F};
   private static final float[] EQUIPMENT_Y = new float[]{42.0F, 43.375F, 43.375F, 43.375F, 45.365F};
   private static final float[] EQUIPMENT_SIZE = new float[]{13.752F, 11.001F, 11.001F, 11.001F, 7.9F};
   private class_1309 target;
   private class_1309 fadeEntity;
   private class_1309 smoothedFor;
   private float smoothedHealth;
   private float fadeAlpha;
   private long lastFrameTime;
   private long lastFadeTime;
   private boolean showcaseText;
   private boolean fadeShowcaseText;

   public TargetElement() {
      super("target", "Target");
   }

   @Override
   protected float defaultX(float unit) {
      return 740.0F * unit;
   }

   @Override
   protected float defaultY(float unit) {
      return 463.0F * unit;
   }

   @Override
   protected void layout(class_310 mc, float unit) {
      TargetElement.TargetResolution resolution = this.resolveTarget(mc);
      this.target = resolution.entity();
      this.showcaseText = resolution.showcase();
      if (this.target != null) {
         this.fadeEntity = this.target;
         this.fadeShowcaseText = this.showcaseText;
      }

      this.updateFade();
      if (this.fadeEntity != null && (this.target != null || !(this.fadeAlpha <= 0.02F))) {
         this.width = 226.0F * unit;
         this.height = 65.0F * unit;
      } else {
         this.width = 0.0F;
         this.height = 0.0F;
      }
   }

   @Override
   protected void draw(class_310 mc, float unit) {
      class_1309 entity = this.target != null ? this.target : this.fadeEntity;
      if (entity != null) {
         float alpha = this.fadeAlpha;
         Render2DUtil.rect(this.x, this.y, this.width, this.height)
            .color(ColorUtil.multiplyAlpha(HudPalette.PANEL, alpha))
            .radius(13.0F * unit)
            .blur(50.0F * unit, alpha)
            .draw();
         this.drawHead(entity, this.x + 5.0F * unit, this.y + 5.0F * unit, 54.0F * unit, 55.0F * unit, 13.0F * unit, alpha);
         float bodyX = this.x + 64.0F * unit;
         float bodyY = this.y + 5.0F * unit;
         Render2DUtil.rect(bodyX, bodyY, 157.0F * unit, 55.0F * unit)
            .color(ColorUtil.multiplyAlpha(HudPalette.SURFACE, alpha))
            .radius(11.0F * unit)
            .border(0.5F * unit, ColorUtil.multiplyAlpha(HudPalette.SURFACE_BORDER, alpha))
            .draw();
         MsdfFont font = UiFonts.sfProDisplay();
         boolean figmaShowcase = this.target != null ? this.showcaseText : this.fadeShowcaseText;
         String role = figmaShowcase ? "Дизайнер" : "Игрок";
         String rawName = figmaShowcase ? "https://t.me/crackdelay" : entity.method_5477().getString();
         float roleSize = 10.0F * unit;
         Render2DUtil.text(this.x + 75.0F * unit, font.centeredTextY(this.y + 15.0F * unit, roleSize), roleSize, role)
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(HudPalette.accent(), alpha))
            .draw();
         float nameSize = 13.0F * unit;
         float nameSpacing = nameSize * UiFontStyle.MEDIUM.letterSpacingEm();
         String name = font.ellipsize(rawName, nameSize, nameSpacing, 90.0F * unit);
         Render2DUtil.text(this.x + 75.0F * unit, font.centeredTextY(this.y + 31.0F * unit, nameSize), nameSize, name)
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(-1, alpha))
            .draw();
         float health = entity.method_6032() + entity.method_6067();
         float smoothHealth = figmaShowcase ? 14.0F : this.smoothHealth(entity, health);
         float maxHealth = figmaShowcase ? 20.0F : Math.max(1.0F, entity.method_6063() + entity.method_6067());
         float healthFraction = Math.clamp(smoothHealth / maxHealth, 0.0F, 1.0F);
         float ringCenterX = this.x + 191.5F * unit;
         float ringCenterY = this.y + 32.5F * unit;
         this.drawHealthRing(ringCenterX, ringCenterY, 17.25F * unit, 2.5F * unit, healthFraction, ColorUtil.multiplyAlpha(HudPalette.accent(), alpha));
         String healthText = Integer.toString(Math.max(0, (int)Math.ceil(smoothHealth)));
         float healthSize = 12.0F * unit;
         Render2DUtil.text(ringCenterX, font.centeredTextY(ringCenterY, healthSize), healthSize, healthText)
            .style(UiFontStyle.MEDIUM)
            .color(ColorUtil.multiplyAlpha(-1, alpha))
            .align(TextAlign.CENTER)
            .draw();
         this.drawEquipment(mc, entity, unit, alpha);
      }
   }

   private void updateFade() {
      long now = System.currentTimeMillis();
      float delta = this.lastFadeTime == 0L ? 0.016F : (float)Math.min(100L, now - this.lastFadeTime) / 1000.0F;
      this.lastFadeTime = now;
      float goal = this.target != null ? 1.0F : 0.0F;
      float step = Math.clamp(delta * 12.0F, 0.0F, 1.0F);
      this.fadeAlpha = this.fadeAlpha + (goal - this.fadeAlpha) * step;
   }

   private void drawHead(class_1309 entity, float x, float y, float width, float height, float radius, float alpha) {
      int tint = ColorUtil.multiplyAlpha(-1, alpha);
      if (entity instanceof class_742 player) {
         class_2960 skin = player.method_52814().comp_1626().comp_3627();
         Render2DUtil.texture(x, y, width, height, skin).managed().uv(0.125F, 0.125F, 0.25F, 0.25F).radius(radius).color(tint).draw();
         Render2DUtil.texture(x, y, width, height, skin).managed().uv(0.625F, 0.125F, 0.75F, 0.25F).radius(radius).color(tint).draw();
      } else {
         Render2DUtil.rect(x, y, width, height).color(ColorUtil.multiplyAlpha(HudPalette.SURFACE_BORDER, alpha)).radius(radius).draw();
         TargetElement.FaceIcon icon = this.resolveFaceIcon(entity);
         if (icon != null) {
            Render2DUtil.texture(x, y, width, height, icon.texture())
               .managed()
               .uv(icon.u0(), icon.v0(), icon.u1(), icon.v1())
               .radius(radius)
               .color(tint)
               .draw();
         }
      }
   }

   private void drawHealthRing(float centerX, float centerY, float radius, float thickness, float fraction, int color) {
      float sweep = (float)Math.toRadians(350.8F) * fraction;
      if (!(sweep <= 0.001F)) {
         int segments = Math.max(1, (int)Math.ceil(sweep / Math.toRadians(5.0)));
         float start = (float)Math.toRadians(-92.0);

         for (int index = 0; index < segments; index++) {
            float angle0 = start + sweep * index / segments;
            float angle1 = start + sweep * (index + 1) / segments;
            float x0 = centerX + (float)Math.cos(angle0) * radius;
            float y0 = centerY + (float)Math.sin(angle0) * radius;
            float x1 = centerX + (float)Math.cos(angle1) * radius;
            float y1 = centerY + (float)Math.sin(angle1) * radius;
            strokeSegment(x0, y0, x1, y1, thickness, color);
         }
      }
   }

   private static void strokeSegment(float x0, float y0, float x1, float y1, float thickness, int color) {
      float dx = x1 - x0;
      float dy = y1 - y0;
      float length = (float)Math.sqrt(dx * dx + dy * dy);
      if (!(length <= 0.001F)) {
         class_332 graphics = RenderContext.currentGuiGraphicsExtractor();
         if (graphics != null) {
            Matrix3x2fStack pose = graphics.method_51448();
            float half = thickness / 2.0F;
            float overlap = thickness * 0.18F;
            pose.pushMatrix();
            pose.translate(x0, y0);
            pose.rotate((float)Math.atan2(dy, dx));
            Render2DUtil.rect(-overlap, -half, length + overlap * 2.0F, thickness).color(color).radius(half).draw();
            pose.popMatrix();
         }
      }
   }

   private void drawEquipment(class_310 mc, class_1309 entity, float unit, float alpha) {
      boolean hasEquipment = false;

      for (class_1304 slot : EQUIPMENT_ORDER) {
         if (!entity.method_6118(slot).method_7960()) {
            hasEquipment = true;
            break;
         }
      }

      if (hasEquipment && !(alpha < 0.75F)) {
         class_332 graphics = RenderContext.currentGuiGraphicsExtractor();
         if (graphics != null) {
            Render2DUtil.flush();
            Matrix3x2fStack pose = graphics.method_51448();
            float guiScale = mc.method_22683().method_4495();

            for (int index = 0; index < EQUIPMENT_ORDER.length; index++) {
               class_1799 stack = entity.method_6118(EQUIPMENT_ORDER[index]);
               if (!stack.method_7960()) {
                  float size = EQUIPMENT_SIZE[index] * unit;
                  float itemX = Math.round((this.x + EQUIPMENT_X[index] * unit) * guiScale) / guiScale;
                  float itemY = Math.round((this.y + EQUIPMENT_Y[index] * unit) * guiScale) / guiScale;
                  pose.pushMatrix();
                  pose.translate(itemX, itemY);
                  pose.scale(size / 16.0F);
                  graphics.method_51427(stack, 0, 0);
                  pose.popMatrix();
               }
            }
         }
      }
   }

   private float smoothHealth(class_1309 entity, float health) {
      long now = System.currentTimeMillis();
      float delta = this.lastFrameTime == 0L ? 0.016F : (float)Math.min(100L, now - this.lastFrameTime) / 1000.0F;
      this.lastFrameTime = now;
      if (this.smoothedFor != entity) {
         this.smoothedFor = entity;
         this.smoothedHealth = health;
      }

      float step = Math.clamp(delta * 10.0F, 0.0F, 1.0F);
      this.smoothedHealth = this.smoothedHealth + (health - this.smoothedHealth) * step;
      return this.smoothedHealth;
   }

   private TargetElement.TargetResolution resolveTarget(class_310 mc) {
      AuraFeature aura = AuraFeature.getMarkerFeature();
      class_1309 current = aura != null ? aura.getCurrentTarget() : null;
      if (current == null) {
         TriggerBotFeature triggerBot = TriggerBotFeature.getEnabled();
         if (triggerBot != null) {
            current = triggerBot.getCurrentTarget();
         }
      }

      if (current != null && current.method_5805()) {
         return new TargetElement.TargetResolution(current, false);
      } else {
         return showcase(mc) && mc.field_1724 != null && mc.field_1724.method_5805()
            ? new TargetElement.TargetResolution(mc.field_1724, true)
            : new TargetElement.TargetResolution(null, false);
      }
   }

   private TargetElement.FaceIcon resolveFaceIcon(class_1309 entity) {
      class_897 renderer = class_310.method_1551().method_1561().method_3953(entity);
      class_10017 state = renderer.method_62425(entity, 1.0F);
      if (renderer instanceof class_922 livingRenderer && state instanceof class_10042 livingState) {
         class_2960 texture = livingRenderer.method_3885(livingState);
         class_3879<?> model = livingRenderer.method_4038();
         class_630 root = model.method_63512();
         Function<String, class_630> lookup = root.method_72015();
         class_630 iconPart = lookup.apply("head");
         if (iconPart == null) {
            iconPart = lookup.apply("body");
         }

         if (iconPart == null) {
            iconPart = root;
         }

         TargetElement.FaceIcon icon = findLargestFrontFace(texture, iconPart);
         return icon == null && iconPart != root ? findLargestFrontFace(texture, root) : icon;
      } else {
         return null;
      }
   }

   private static TargetElement.FaceIcon findLargestFrontFace(class_2960 texture, class_630 part) {
      TargetElement.FaceIcon result = null;
      float largestArea = 0.0F;

      for (class_630 candidate : part.method_32088()) {
         for (class_628 cube : ((ModelPartAccessor)(Object)candidate).blade$getCubes()) {
            for (class_593 polygon : cube.field_3649) {
               if (!(polygon.comp_3185().z() > -0.9F)) {
                  float minU = Float.POSITIVE_INFINITY;
                  float minV = Float.POSITIVE_INFINITY;
                  float maxU = Float.NEGATIVE_INFINITY;
                  float maxV = Float.NEGATIVE_INFINITY;

                  for (class_618 vertex : polygon.comp_3184()) {
                     minU = Math.min(minU, vertex.comp_3187());
                     minV = Math.min(minV, vertex.comp_3188());
                     maxU = Math.max(maxU, vertex.comp_3187());
                     maxV = Math.max(maxV, vertex.comp_3188());
                  }

                  float area = (maxU - minU) * (maxV - minV);
                  if (area > largestArea) {
                     largestArea = area;
                     result = new TargetElement.FaceIcon(texture, minU, minV, maxU, maxV);
                  }
               }
            }
         }
      }

      return result;
   }

   @Environment(EnvType.CLIENT)
   private record FaceIcon(class_2960 texture, float u0, float v0, float u1, float v1) {
   }

   @Environment(EnvType.CLIENT)
   private record TargetResolution(class_1309 entity, boolean showcase) {
   }
}
