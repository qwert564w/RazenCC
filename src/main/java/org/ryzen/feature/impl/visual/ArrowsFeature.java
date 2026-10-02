package org.ryzen.feature.impl.visual;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1304;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_4184;
import net.minecraft.class_437;
import net.minecraft.class_465;
import org.joml.Matrix3x2fStack;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorMode;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.mixin.accessor.AbstractContainerScreenAccessor;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.math.Animation;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.TextAlign;
import org.ryzen.utils.render.gui.UiFontStyle;

@Environment(EnvType.CLIENT)
public final class ArrowsFeature extends Feature implements MinecraftContext {
   private static final int FRIEND_COLOR = -10099342;
   private static final float RADIUS_TO_GUI_PIXELS = 7.0F;
   private static final float BASE_ARROW_SIZE = 32.0F;
   private static final float GENERIC_SCREEN_EXPANSION = 32.0F;
   private static final long SCREEN_OPEN_MILLIS = 360L;
   private static final long SCREEN_CLOSE_MILLIS = 300L;
   private static final long RADIUS_ADJUST_MILLIS = 180L;
   public final NumberSetting radius = this.register(new NumberSetting("Radius", 10.0, 8.0, 15.0, 1.0, "").configKey("render.arrows.radius"));
   public final NumberSetting scale = this.register(new NumberSetting("Scale", 1.0, 0.4, 1.0, 0.1, "x").configKey("render.arrows.scale"));
   public final BooleanSetting showDistance = this.register(new BooleanSetting("Show Distance", false).configKey("render.arrows.showDistance"));
   public final BooleanSetting onlyFriends = this.register(new BooleanSetting("Only Friends", false).configKey("render.arrows.onlyFriend"));
   public final BooleanSetting filled = this.register(new BooleanSetting("Filled", false).configKey("render.arrows.filled"));
   public final BooleanSetting ignoreNaked = this.register(new BooleanSetting("Ignore Naked", false).configKey("render.arrows.ignoreNaked"));
   public final ModeSetting colorMode = this.register(ColorMode.setting().configKey("render.arrows.colorMode"));
   public final ColorSetting color = this.register(
      new ColorSetting("Color", -1).configKey("render.arrows.color").visibleWhen(() -> ColorMode.isCustom(this.colorMode))
   );
   public final BooleanSetting armorColorEnabled = this.register(new BooleanSetting("Armor Color", false).configKey("render.arrows.armorColorEnabled"));
   public final ColorSetting armorColor = this.register(
      new ColorSetting("Armor Color Value", -49088).configKey("render.arrows.armorColor").visibleWhen(() -> this.armorColorEnabled.getValue())
   );
   private final Animation radiusAnimation = new Animation(0L, Animation.Easing.EASE_OUT_CUBIC);
   private final Map<UUID, ArrowsFeature.ArrowState> arrowStates = new HashMap<>();
   private class_437 lastScreen;
   private float lastRadiusTarget = Float.NaN;
   private long lastFrameNanos;
   private boolean radiusInitialized;

   public ArrowsFeature() {
      super("Arrows", "Points toward players outside the visible screen", FeatureCategory.VISUAL, -1);
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (mc.field_1687 != null && mc.field_1724 != null && mc.field_1773 != null) {
         float delta = this.frameDelta();
         float tickDelta = event.getDeltaTracker().method_60637(false);
         float width = event.getGuiGraphicsExtractor().method_51421();
         float height = event.getGuiGraphicsExtractor().method_51443();
         float centerX = width * 0.5F;
         float centerY = height * 0.5F;
         float arrowSize = 32.0F * this.scale.getValue().floatValue();
         class_437 screen = mc.field_1755;
         float maxRadius = maximumRadius(width, height, arrowSize);
         float radiusTarget = this.targetRadius(screen, width, height, arrowSize, maxRadius);
         float ringRadius = Math.clamp(this.animateRadius(radiusTarget, screen), 8.0F, maxRadius);
         class_4184 camera = mc.field_1773.method_19418();
         class_243 cameraPos = camera.method_71156();
         double yaw = Math.toRadians(camera.method_19330());
         double sinYaw = Math.sin(yaw);
         double cosYaw = Math.cos(yaw);
         int normalColor = ColorMode.resolve(this.colorMode, this.color);
         Set<UUID> touched = new HashSet<>();

         for (class_1657 player : mc.field_1687.method_18456()) {
            if (this.eligible(player)) {
               class_243 target = Render3DUtil.interpolatedPosition(player, tickDelta);
               double dx = target.field_1352 - cameraPos.field_1352;
               double dz = target.field_1350 - cameraPos.field_1350;
               double right = dx * cosYaw - dz * sinYaw;
               double forward = -dx * sinYaw + dz * cosYaw;
               if (!(right * right + forward * forward < 1.0E-4)) {
                  boolean visible = this.isOffscreen(player, width, height, arrowSize, tickDelta);
                  UUID id = player.method_5667();
                  ArrowsFeature.ArrowState state = this.arrowStates.get(id);
                  if (state != null || visible) {
                     float targetAngle = (float)Math.atan2(-forward, right);
                     if (state == null) {
                        state = new ArrowsFeature.ArrowState(targetAngle);
                        this.arrowStates.put(id, state);
                     }

                     state.targetAngle = targetAngle;
                     state.targetVisible = visible;
                     state.color = this.resolveColor(player, normalColor);
                     state.distance = Math.max(1, Math.round(mc.field_1724.method_5739(player)));
                     touched.add(id);
                  }
               }
            }
         }

         this.arrowStates.forEach((idx, statex) -> {
            if (!touched.contains(idx)) {
               statex.targetVisible = false;
            }
         });
         Iterator<ArrowsFeature.ArrowState> iterator = this.arrowStates.values().iterator();

         while (iterator.hasNext()) {
            ArrowsFeature.ArrowState state = iterator.next();
            this.updateState(state, delta);
            if (!state.targetVisible && state.alpha < 0.01F) {
               iterator.remove();
            } else {
               float angle = state.angle;
               float arrowX = centerX + (float)Math.cos(angle) * ringRadius;
               float arrowY = centerY + (float)Math.sin(angle) * ringRadius;
               float animatedSize = arrowSize * state.scale;
               this.drawArrow(event, arrowX, arrowY, animatedSize, angle, state.color, state.alpha);
               if (this.showDistance.getValue()) {
                  this.drawDistance(event, state.distance, arrowX, arrowY, animatedSize, state.alpha);
               }
            }
         }
      } else {
         this.clearAnimations();
      }
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.clearAnimations();
   }

   @Override
   protected void onEnable() {
      this.clearAnimations();
   }

   @Override
   protected void onDisable() {
      this.clearAnimations();
   }

   private boolean eligible(class_1657 player) {
      if (player != mc.field_1724 && !player.method_31481() && player.method_5805() && !player.method_7325()) {
         boolean friend = FriendManager.INSTANCE.isFriend(player.method_7334().name());
         return this.onlyFriends.getValue() && !friend ? false : !this.ignoreNaked.getValue() || hasArmor(player);
      } else {
         return false;
      }
   }

   private boolean isOffscreen(class_1657 player, float width, float height, float arrowSize, float tickDelta) {
      class_243 anchor = Render3DUtil.interpolatedPosition(player, tickDelta).method_1031(0.0, player.method_17682() * 0.5, 0.0);
      Render3DUtil.ScreenPoint point = Render3DUtil.projectToScreen(mc, anchor);
      float margin = arrowSize * 0.35F;
      return point == null || point.x() < margin || point.x() > width - margin || point.y() < margin || point.y() > height - margin;
   }

   private float targetRadius(class_437 screen, float width, float height, float arrowSize, float maxRadius) {
      float baseRadius = this.radius.getValue().floatValue() * 7.0F;
      if (screen == null) {
         return Math.min(baseRadius, maxRadius);
      }

      float expanded = baseRadius + 32.0F;
      if (screen instanceof class_465<?> container) {
         AbstractContainerScreenAccessor accessor = (AbstractContainerScreenAccessor)container;
         float left = accessor.getLeftPos();
         float top = accessor.getTopPos();
         float right = left + accessor.getImageWidth();
         float bottom = top + accessor.getImageHeight();
         float centerX = width * 0.5F;
         float centerY = height * 0.5F;
         float farthestCorner = Math.max(
            Math.max(distance(centerX, centerY, left, top), distance(centerX, centerY, right, top)),
            Math.max(distance(centerX, centerY, left, bottom), distance(centerX, centerY, right, bottom))
         );
         expanded = Math.max(expanded, farthestCorner + arrowSize * 0.6F + 10.0F);
      }

      return Math.min(expanded, maxRadius);
   }

   private float animateRadius(float target, class_437 screen) {
      if (!this.radiusInitialized) {
         this.radiusAnimation.animate(target, target, 0L, Animation.Easing.EASE_OUT_CUBIC);
         this.radiusInitialized = true;
         this.lastScreen = screen;
         this.lastRadiusTarget = target;
         return target;
      }

      boolean screenChanged = screen != this.lastScreen;
      boolean targetChanged = Math.abs(target - this.lastRadiusTarget) > 0.25F;
      if (screenChanged || targetChanged) {
         boolean opening = this.lastScreen == null && screen != null;
         boolean closing = this.lastScreen != null && screen == null;
         long duration = opening ? 360L : (closing ? 300L : 180L);
         Animation.Easing easing = opening ? Animation.Easing.EASE_OUT_BACK : Animation.Easing.EASE_OUT_CUBIC;
         this.radiusAnimation.animate(this.radiusAnimation.getValue(), target, duration, easing);
         this.lastScreen = screen;
         this.lastRadiusTarget = target;
      }

      return this.radiusAnimation.getValue();
   }

   private float frameDelta() {
      long now = System.nanoTime();
      float delta = this.lastFrameNanos == 0L ? 0.016666668F : (float)(now - this.lastFrameNanos) / 1.0E9F;
      this.lastFrameNanos = now;
      return Math.clamp(delta, 0.001F, 0.05F);
   }

   private void updateState(ArrowsFeature.ArrowState state, float delta) {
      float angleBlend = smoothing(18.0F, delta);
      state.angle = state.angle + shortestAngle(state.angle, state.targetAngle) * angleBlend;
      float alphaTarget = state.targetVisible ? 1.0F : 0.0F;
      float alphaSpeed = state.targetVisible ? 13.0F : 10.0F;
      state.alpha = state.alpha + (alphaTarget - state.alpha) * smoothing(alphaSpeed, delta);
      float scaleTarget = state.targetVisible ? 1.0F : 0.78F;
      state.scale = state.scale + (scaleTarget - state.scale) * smoothing(15.0F, delta);
   }

   private int resolveColor(class_1657 player, int normalColor) {
      if (FriendManager.INSTANCE.isFriend(player.method_7334().name())) {
         return -10099342;
      } else {
         return this.armorColorEnabled.getValue() && hasArmor(player) ? this.armorColor.getValue() : normalColor;
      }
   }

   private void drawArrow(Render2DEvent event, float x, float y, float size, float angle, int color, float alpha) {
      Matrix3x2fStack pose = event.getGuiGraphicsExtractor().method_51448();
      pose.pushMatrix();
      pose.translate(x, y);
      pose.rotate(angle);
      Render2DUtil.texture(-size * 0.5F, -size * 0.5F, size, size, this.filled.getValue() ? Textures.Hud.ARROW_FILLED : Textures.Hud.ARROW_OUTLINE)
         .color(ColorUtil.multiplyAlpha(color, 0.95F * alpha))
         .draw();
      pose.popMatrix();
   }

   private void drawDistance(Render2DEvent event, int distance, float x, float y, float arrowSize, float alpha) {
      float textSize = 8.0F + 2.0F * this.scale.getValue().floatValue();
      Render2DUtil.text(x, y + arrowSize * 0.52F, textSize, distance + "m")
         .style(UiFontStyle.SEMIBOLD)
         .align(TextAlign.CENTER)
         .color(ColorUtil.multiplyAlpha(-1, alpha))
         .outline(ColorUtil.multiplyAlpha(-1342177280, alpha), 0.8F)
         .draw();
   }

   private void clearAnimations() {
      this.arrowStates.clear();
      this.lastScreen = null;
      this.lastRadiusTarget = Float.NaN;
      this.lastFrameNanos = 0L;
      this.radiusInitialized = false;
   }

   private static float maximumRadius(float width, float height, float arrowSize) {
      return Math.max(8.0F, Math.min(width, height) * 0.5F - arrowSize * 0.55F - 8.0F);
   }

   private static float distance(float x1, float y1, float x2, float y2) {
      return (float)Math.hypot(x2 - x1, y2 - y1);
   }

   private static float shortestAngle(float from, float to) {
      return (float)Math.atan2(Math.sin(to - from), Math.cos(to - from));
   }

   private static float smoothing(float speed, float delta) {
      return 1.0F - (float)Math.exp(-speed * delta);
   }

   private static boolean hasArmor(class_1657 player) {
      return !player.method_6118(class_1304.field_6169).method_7960()
         || !player.method_6118(class_1304.field_6174).method_7960()
         || !player.method_6118(class_1304.field_6172).method_7960()
         || !player.method_6118(class_1304.field_6166).method_7960();
   }

   @Environment(EnvType.CLIENT)
   private static final class ArrowState {
      float angle;
      float targetAngle;
      float alpha;
      float scale = 0.72F;
      int color;
      int distance;
      boolean targetVisible;

      ArrowState(float angle) {
         this.angle = angle;
         this.targetAngle = angle;
      }
   }
}
