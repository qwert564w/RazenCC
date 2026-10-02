package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10055;
import net.minecraft.class_1268;
import net.minecraft.class_1306;
import net.minecraft.class_1657;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_4050;
import net.minecraft.class_5602;
import net.minecraft.class_591;
import net.minecraft.class_630;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.world.WorldMeshRenderer;

@Environment(EnvType.CLIENT)
public final class SkeletonEspFeature extends Feature implements MinecraftContext {
   private static final float HEAD_BONE = -0.32F;
   private static final float BODY_BONE = 0.52F;
   private static final float LIMB_BONE = 0.52F;
   private static final float MODEL_SCALE = 0.0625F;
   private static final float MODEL_OFFSET_Y = -1.501F;
   public final ColorSetting playerColor = this.register(new ColorSetting("Player Color", -1));
   public final ColorSetting friendColor = this.register(new ColorSetting("Friend Color", -16711816));
   public final BooleanSetting throughWalls = this.register(new BooleanSetting("Through Walls", true));
   public final NumberSetting range = this.register(new NumberSetting("Range", 96.0, 16.0, 192.0, 4.0, " blocks"));
   private class_591 model;
   private class_10055 renderState;

   public SkeletonEspFeature() {
      super("SkeletonESP", "Draws a stick figure over nearby players", FeatureCategory.VISUAL, -1);
   }

   public static SkeletonEspFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(SkeletonEspFeature.class);
   }

   @Override
   protected void onDisable() {
      this.release();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.release();
   }

   private void release() {
      this.model = null;
      this.renderState = null;
   }

   public void renderWorld(float tickDelta) {
      if (this.inGame()) {
         this.ensureModel();
         if (this.model != null) {
            double maxDistance = this.range.getValue();
            double maxDistanceSquared = maxDistance * maxDistance;
            List<WorldMeshRenderer.Line> lines = new ArrayList<>();

            for (class_1657 other : this.level().method_18456()) {
               if (this.isTarget(other, maxDistanceSquared)) {
                  this.appendSkeleton(lines, other, tickDelta);
               }
            }

            if (!lines.isEmpty()) {
               WorldMeshRenderer.render(new WorldMeshRenderer.WorldMesh(lines, List.of(), List.of()), this.throughWalls.getValue());
            }
         }
      }
   }

   private boolean isTarget(class_1657 other, double maxDistanceSquared) {
      return other != null
         && other != this.player()
         && other.method_5805()
         && !other.method_7325()
         && !other.method_5767()
         && this.player().method_5858(other) <= maxDistanceSquared;
   }

   private int colorOf(class_1657 other) {
      return FriendManager.INSTANCE.isFriend(other.method_5477().getString()) ? this.friendColor.getValue() : this.playerColor.getValue();
   }

   private void ensureModel() {
      if (this.model == null) {
         if (mc.method_31974() != null) {
            this.model = new class_591(mc.method_31974().method_32072(class_5602.field_27577), false);
            this.renderState = new class_10055();
         }
      }
   }

   private void appendSkeleton(List<WorldMeshRenderer.Line> lines, class_1657 other, float tickDelta) {
      this.poseModel(other, tickDelta);
      class_243 position = Render3DUtil.interpolatedPosition(other, tickDelta);
      Matrix4f pose = new Matrix4f()
         .translate((float)position.field_1352, (float)position.field_1351, (float)position.field_1350)
         .rotateY((float)Math.toRadians(180.0F - this.renderState.field_53446))
         .scale(-1.0F, -1.0F, 1.0F)
         .translate(0.0F, -1.501F, 0.0F);
      int color = this.colorOf(other);
      this.appendBone(lines, pose, this.model.field_3398, -0.32F, color);
      this.appendBone(lines, pose, this.model.field_3391, 0.52F, color);
      this.appendBone(lines, pose, this.model.field_3401, 0.52F, color);
      this.appendBone(lines, pose, this.model.field_27433, 0.52F, color);
      this.appendBone(lines, pose, this.model.field_3392, 0.52F, color);
      this.appendBone(lines, pose, this.model.field_3397, 0.52F, color);
   }

   private void poseModel(class_1657 other, float tickDelta) {
      class_10055 state = this.renderState;
      state.field_53446 = class_3532.method_17821(tickDelta, other.field_6220, other.field_6283);
      state.field_53447 = class_3532.method_17821(tickDelta, other.field_6259, other.field_6241) - state.field_53446;
      state.field_53448 = class_3532.method_16439(tickDelta, other.field_6004, other.method_36455());
      state.field_53450 = other.field_42108.method_48572(tickDelta);
      state.field_53451 = other.field_42108.method_48570(tickDelta);
      state.field_53410 = other.method_18276();
      state.field_53411 = other.method_6128();
      state.field_53412 = other.method_20232();
      state.field_53413 = other.method_5765();
      state.field_53414 = false;
      state.field_53408 = class_1306.field_6183;
      state.field_53409 = class_1268.field_5808;
      state.field_53465 = other.method_18376() == null ? class_4050.field_18076 : other.method_18376();
      state.field_55309 = class_1799.field_8037;
      state.field_53418 = class_1799.field_8037;
      state.field_53419 = class_1799.field_8037;
      state.field_53420 = class_1799.field_8037;
      state.field_53454 = 1.0F;
      state.field_53453 = 1.0F;
      this.model.method_62110(state);
   }

   private void appendBone(List<WorldMeshRenderer.Line> lines, Matrix4f pose, class_630 part, float length, int color) {
      Matrix4f bone = new Matrix4f(pose)
         .translate(part.field_3657 * 0.0625F, part.field_3656 * 0.0625F, part.field_3655 * 0.0625F)
         .rotateZ(part.field_3674)
         .rotateY(part.field_3675)
         .rotateX(part.field_3654);
      lines.add(new WorldMeshRenderer.Line(transform(bone, 0.0F, 0.0F, 0.0F), transform(bone, 0.0F, length, 0.0F), color));
   }

   private static class_243 transform(Matrix4f matrix, float x, float y, float z) {
      Vector4f point = matrix.transform(new Vector4f(x, y, z, 1.0F));
      return new class_243(point.x, point.y, point.z);
   }
}
