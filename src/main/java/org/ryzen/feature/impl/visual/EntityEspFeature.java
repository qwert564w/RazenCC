package org.ryzen.feature.impl.visual;

import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1303;
import net.minecraft.class_1309;
import net.minecraft.class_1311;
import net.minecraft.class_1542;
import net.minecraft.class_1657;
import net.minecraft.class_1669;
import net.minecraft.class_1672;
import net.minecraft.class_1676;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.HurtUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.gui.Render2DUtil;

@Environment(EnvType.CLIENT)
public final class EntityEspFeature extends Feature {
   private static final int BASE_COLOR = -14188801;
   private static final float ESP_ALPHA = 0.92F;
   private static final float FADE_START_DISTANCE = 0.4F;
   private static final float FADE_FULL_DISTANCE = 2.0F;
   private static final float MIN_RENDER_ALPHA = 0.05F;
   private static final float BOX_LINE_WIDTH = 1.0F;
   private final MultiSelectSetting targets = this.register(
      new MultiSelectSetting("Targets", Set.of("Players", "Hostile"), "Players", "Hostile", "Passive", "Items", "Projectiles")
   );
   private final ModeSetting boxMode = this.register(new ModeSetting("Box Mode", "Corners", "Corners", "Box"));
   private final BooleanSetting rounded = this.register(new BooleanSetting("Rounded", false));
   private final BooleanSetting healthBar = this.register(new BooleanSetting("Health Bar", true));
   private final BooleanSetting box = this.register(new BooleanSetting("Box", true));
   private final NumberSetting distance = this.register(new NumberSetting("Distance", 64.0, 5.0, 128.0, 1.0, " blocks"));
   private final ModeSetting colorMode = this.register(ColorMode.setting());
   private final ColorSetting espColor = this.register(
      new ColorSetting("Color", -14188801).configKey("Glow Color").visibleWhen(() -> ColorMode.isCustom(this.colorMode))
   );

   public EntityEspFeature() {
      super("EntityESP", "Draws boxes and glow around selected entities.", FeatureCategory.VISUAL, 71);
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      class_310 mc = event.getClient();
      if (mc != null && mc.field_1687 != null && mc.field_1724 != null) {
         float tickDelta = event.getDeltaTracker().method_60637(false);

         for (class_1297 entity : mc.field_1687.method_18112()) {
            if (this.isValidOverlayTarget(mc, entity)) {
               float alpha = this.overlayAlpha(mc.field_1724, entity);
               if (!(alpha < 0.05F)) {
                  Render3DUtil.ScreenBounds bounds = this.projectEntityBounds(mc, entity, tickDelta);
                  if (bounds != null && !(bounds.width() < 2.0F) && !(bounds.height() < 2.0F)) {
                     int color = HurtUtil.blend(this.resolveColor(), entity, alpha);
                     if (this.box.getValue()) {
                        if (this.boxMode.is("Corners")) {
                           if (this.rounded.getValue()) {
                              this.drawRoundedCornerBox(bounds, color);
                           } else {
                              this.drawCornerBox(bounds, color);
                           }
                        } else if (this.rounded.getValue()) {
                           this.drawRoundedBox(bounds, color);
                        } else {
                           this.drawBox(bounds, color);
                        }
                     }

                     if (this.healthBar.getValue() && entity instanceof class_1309 livingEntity) {
                        this.drawHealthBar(bounds, livingEntity, alpha);
                     }
                  }
               }
            }
         }
      }
   }

   private boolean isValidOverlayTarget(class_310 mc, class_1297 entity) {
      if (entity == null || entity.method_31481() || !entity.method_5805()) {
         return false;
      } else if (entity == mc.field_1724 && mc.field_1690.method_31044().method_31034()) {
         return false;
      } else {
         return !this.isWithinRenderDistance(mc.field_1724, entity) ? false : this.isTarget(entity);
      }
   }

   private boolean isWithinRenderDistance(class_1657 viewer, class_1297 entity) {
      if (viewer == null) {
         return false;
      }

      double maxDistance = this.distance.getValue();
      return viewer.method_5858(entity) <= maxDistance * maxDistance;
   }

   private boolean isTarget(class_1297 entity) {
      if (entity instanceof class_1657) {
         return this.targets.isSelected("Players");
      }

      if (entity instanceof class_1542 || entity instanceof class_1303) {
         return this.targets.isSelected("Items");
      }

      if (!(entity instanceof class_1676) && !(entity instanceof class_1672) && !(entity instanceof class_1669)) {
         class_1311 category = entity.method_5864().method_5891();
         if (category == class_1311.field_6302) {
            return this.targets.isSelected("Hostile");
         } else {
            return category != class_1311.field_6294
                  && category != class_1311.field_34447
                  && category != class_1311.field_6303
                  && category != class_1311.field_30092
                  && category != class_1311.field_6300
                  && category != class_1311.field_24460
               ? false
               : this.targets.isSelected("Passive");
         }
      } else {
         return this.targets.isSelected("Projectiles");
      }
   }

   private float overlayAlpha(class_1657 viewer, class_1297 entity) {
      float distance = viewer.method_5739(entity);
      float progress = (distance - 0.4F) / 1.6F;
      return 0.92F * class_3532.method_15363(progress, 0.0F, 1.0F);
   }

   private int resolveColor() {
      return ColorMode.resolve(this.colorMode, this.espColor);
   }

   private Render3DUtil.ScreenBounds projectEntityBounds(class_310 mc, class_1297 entity, float tickDelta) {
      class_243 renderPosition = Render3DUtil.interpolatedPosition(entity, tickDelta);
      double halfWidth = entity.method_17681() / 1.5;
      double height = entity.method_17682() + 0.1 - (entity.method_5715() ? 0.2 : 0.0);
      return Render3DUtil.projectBoxBounds(
         mc,
         renderPosition.field_1352 - halfWidth,
         renderPosition.field_1351,
         renderPosition.field_1350 - halfWidth,
         renderPosition.field_1352 + halfWidth,
         renderPosition.field_1351 + height,
         renderPosition.field_1350 + halfWidth
      );
   }

   private void drawBox(Render3DUtil.ScreenBounds bounds, int color) {
      int minX = Math.round(bounds.minX());
      int minY = Math.round(bounds.minY());
      int maxX = Math.round(bounds.maxX());
      int maxY = Math.round(bounds.maxY());
      int width = maxX - minX;
      int height = maxY - minY;
      if (width > 0 && height > 0) {
         Render2DUtil.rect(minX, minY, width, Math.max(1, Math.round(1.0F))).color(color).draw();
         Render2DUtil.rect(minX, maxY - 1, width, 1.0F).color(color).draw();
         Render2DUtil.rect(minX, minY, 1.0F, height).color(color).draw();
         Render2DUtil.rect(maxX - 1, minY, 1.0F, height).color(color).draw();
      }
   }

   private void drawCornerBox(Render3DUtil.ScreenBounds bounds, int color) {
      int minX = Math.round(bounds.minX());
      int minY = Math.round(bounds.minY());
      int maxX = Math.round(bounds.maxX());
      int maxY = Math.round(bounds.maxY());
      int width = maxX - minX;
      int height = maxY - minY;
      if (width > 0 && height > 0) {
         int cornerWidth = Math.max(2, Math.round(width / 4.0F));
         int cornerHeight = Math.max(2, Math.round(height / 4.0F));
         Render2DUtil.rect(minX, minY, cornerWidth, 1.0F).color(color).draw();
         Render2DUtil.rect(minX, minY, 1.0F, cornerHeight).color(color).draw();
         Render2DUtil.rect(maxX - cornerWidth, minY, cornerWidth, 1.0F).color(color).draw();
         Render2DUtil.rect(maxX - 1, minY, 1.0F, cornerHeight).color(color).draw();
         Render2DUtil.rect(minX, maxY - 1, cornerWidth, 1.0F).color(color).draw();
         Render2DUtil.rect(minX, maxY - cornerHeight, 1.0F, cornerHeight).color(color).draw();
         Render2DUtil.rect(maxX - cornerWidth, maxY - 1, cornerWidth, 1.0F).color(color).draw();
         Render2DUtil.rect(maxX - 1, maxY - cornerHeight, 1.0F, cornerHeight).color(color).draw();
      }
   }

   private void drawRoundedBox(Render3DUtil.ScreenBounds bounds, int color) {
      float minX = Math.round(bounds.minX());
      float minY = Math.round(bounds.minY());
      float width = Math.round(bounds.maxX()) - minX;
      float height = Math.round(bounds.maxY()) - minY;
      if (!(width <= 0.0F) && !(height <= 0.0F)) {
         float radius = Math.min(width, height) * 0.75F / 4.0F;
         Render2DUtil.rect(minX, minY, width, height).color(0).radius(radius).border(1.0F, color).draw();
      }
   }

   private void drawRoundedCornerBox(Render3DUtil.ScreenBounds bounds, int color) {
      float minX = Math.round(bounds.minX());
      float minY = Math.round(bounds.minY());
      float width = Math.round(bounds.maxX()) - minX;
      float height = Math.round(bounds.maxY()) - minY;
      if (!(width <= 0.0F) && !(height <= 0.0F)) {
         float cornerWidth = Math.max(3.0F, width / 4.0F);
         float cornerHeight = Math.max(3.0F, height / 4.0F);
         float radius = Math.min(cornerWidth, cornerHeight) * 0.75F;
         float maxY = minY + height;
         float maxX = minX + width;
         float[][] corners = new float[][]{{minX, minY}, {maxX - cornerWidth, minY}, {minX, maxY - cornerHeight}, {maxX - cornerWidth, maxY - cornerHeight}};

         for (float[] corner : corners) {
            Render2DUtil.pushScissor(corner[0], corner[1], cornerWidth, cornerHeight);
            Render2DUtil.rect(minX, minY, width, height).color(0).radius(radius).border(1.0F, color).draw();
            Render2DUtil.popScissor();
         }
      }
   }

   private void drawHealthBar(Render3DUtil.ScreenBounds bounds, class_1309 entity, float alpha) {
      float health = class_3532.method_15363(entity.method_6032(), 0.0F, entity.method_6063());
      float ratio = entity.method_6063() <= 0.0F ? 0.0F : health / entity.method_6063();
      int barHeight = Math.max(1, Math.round(bounds.height()));
      int filledHeight = Math.max(0, Math.round(barHeight * ratio));
      int x = Math.round(bounds.minX()) - 4;
      int y = Math.round(bounds.minY());
      Render2DUtil.rect(x, y, 2.0F, barHeight).color(ColorUtil.multiplyAlpha(Integer.MIN_VALUE, alpha)).draw();
      if (filledHeight > 0) {
         int fillY = y + (barHeight - filledHeight);
         int fillColor = ColorUtil.multiplyAlpha(ColorUtil.lerp(-65536, -16711936, ratio), alpha);
         Render2DUtil.rect(x, fillY, 2.0F, filledHeight).color(fillColor).draw();
      }
   }
}
