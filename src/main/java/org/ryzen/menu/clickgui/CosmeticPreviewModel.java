package org.ryzen.menu.clickgui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10017;
import net.minecraft.class_10042;
import net.minecraft.class_1309;
import net.minecraft.class_332;
import net.minecraft.class_4050;
import net.minecraft.class_897;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.ryzen.context.MinecraftContext;
import org.ryzen.utils.math.MathUtil;

@Environment(EnvType.CLIENT)
public final class CosmeticPreviewModel {
   private static final float DEG_TO_RAD = (float) (Math.PI / 180.0);
   private static final float DEFAULT_ZOOM = 1.0F;
   private static final float MIN_ZOOM = 0.55F;
   private static final float MAX_ZOOM = 2.4F;
   private static final float MAX_PITCH = 45.0F;
   private static final float DRAG_DEGREES_PER_PIXEL = 0.85F;
   private static final float IDLE_SPIN = 11.0F;
   private static final float SETTLE_PER_SECOND = 9.0F;
   private static final float Y_OFFSET = 0.0625F;
   private static final float BOX_SCALE = 0.42F;
   private float yaw;
   private float pitch;
   private float zoom = 1.0F;
   private float shownYaw;
   private float shownPitch;
   private float shownZoom = 1.0F;
   private boolean dragging;
   private boolean touched;
   private double lastMouseX;
   private double lastMouseY;

   public void beginDrag(double mouseX, double mouseY) {
      this.dragging = true;
      this.touched = true;
      this.lastMouseX = mouseX;
      this.lastMouseY = mouseY;
   }

   public void drag(double mouseX, double mouseY) {
      if (this.dragging) {
         this.yaw = this.yaw + (float)(mouseX - this.lastMouseX) * 0.85F;
         this.pitch = MathUtil.clamp(this.pitch + (float)(mouseY - this.lastMouseY) * 0.85F, -45.0F, 45.0F);
         this.lastMouseX = mouseX;
         this.lastMouseY = mouseY;
      }
   }

   public void release() {
      this.dragging = false;
   }

   public void zoom(double amount) {
      this.touched = true;
      this.zoom = MathUtil.clamp(this.zoom + (float)amount * 0.12F, 0.55F, 2.4F);
   }

   public void reset() {
      this.yaw = 0.0F;
      this.pitch = 0.0F;
      this.zoom = 1.0F;
      this.touched = false;
   }

   public boolean isTouched() {
      return this.touched;
   }

   public void tick(float deltaSeconds) {
      if (!this.touched && !this.dragging) {
         this.yaw += 11.0F * deltaSeconds;
      }

      float settle = 1.0F - (float)Math.exp(-9.0F * deltaSeconds);
      this.shownYaw = this.shownYaw + (this.yaw - this.shownYaw) * settle;
      this.shownPitch = this.shownPitch + (this.pitch - this.shownPitch) * settle;
      this.shownZoom = this.shownZoom + (this.zoom - this.shownZoom) * settle;
   }

   public boolean render(class_332 graphics, class_1309 entity, int left, int top, int right, int bottom) {
      if (graphics != null && entity != null && right - left >= 8 && bottom - top >= 8) {
         try {
            class_10017 state = extractRenderState(entity);
            if (state == null) {
               return false;
            }

            if (state instanceof class_10042 living) {
               living.field_53446 = 180.0F + this.shownYaw;
               living.field_53447 = 0.0F;
               living.field_53448 = living.field_53465 == class_4050.field_18077 ? 0.0F : -this.shownPitch;
               living.field_53451 = 0.0F;
               if (living.field_53453 != 0.0F) {
                  living.field_53329 = living.field_53329 / living.field_53453;
                  living.field_53330 = living.field_53330 / living.field_53453;
                  living.field_53453 = 1.0F;
               }
            }

            int scale = Math.max(8, Math.round((bottom - top) * 0.42F * this.shownZoom));
            Vector3f translation = new Vector3f(0.0F, state.field_53330 / 2.0F + 0.0625F, 0.0F);
            Quaternionf tilt = new Quaternionf().rotateX(this.shownPitch * (float) (Math.PI / 180.0));
            Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI).mul(tilt);
            graphics.method_70856(state, scale, translation, rotation, tilt, left, top, right, bottom);
            return true;
         } catch (Exception ignored) {
            return false;
         }
      } else {
         return false;
      }
   }

   private static class_10017 extractRenderState(class_1309 entity) {
      class_897 renderer = MinecraftContext.mc.method_1561().method_3953(entity);
      if (renderer == null) {
         return null;
      }

      class_10017 state = renderer.method_62425(entity, 1.0F);
      state.field_61820 = 15728880;
      state.field_61823.clear();
      state.field_61821 = 0;
      state.field_53337 = null;
      return state;
   }
}
