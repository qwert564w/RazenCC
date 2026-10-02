package org.ryzen.feature.impl.combat;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12175;
import net.minecraft.class_12177;
import net.minecraft.class_12180;
import net.minecraft.class_1297;
import net.minecraft.class_1309;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_3532;
import net.minecraft.class_12180.class_12181;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.render.Render3DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class HitBoxesFeature extends Feature implements MinecraftContext {
   private static final double MAX_RENDER_DISTANCE_SQR = 4096.0;
   private static final int HITBOX_COLOR = -16711809;
   private static final int EYE_COLOR = -43691;
   private static final float LINE_WIDTH = 1.5F;
   public final NumberSetting size = this.register(new NumberSetting("Size", 3.0, 1.0, 10.0, 0.5, ""));
   public final BooleanSetting withAura = this.register(new BooleanSetting("With Aura", false));
   public final BooleanSetting showSize = this.register(new BooleanSetting("Show Size", false));
   public final BooleanSetting yModification = this.register(new BooleanSetting("Y Size", false));

   public HitBoxesFeature() {
      super("HitBoxes", "Expands the collision size of surrounding entities for easier hits", FeatureCategory.COMBAT, 0);
   }

   public static HitBoxesFeature getInstance() {
      return FeatureManager.INSTANCE.getFeature(HitBoxesFeature.class);
   }

   public static HitBoxesFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(HitBoxesFeature.class);
   }

   public double getHorizontalExpansion() {
      return this.size.getValue() / 10.0;
   }

   public double getVerticalExpansion() {
      return this.yModification.getValue() ? this.getHorizontalExpansion() : 0.0;
   }

   public double getAuraExpansion() {
      return this.isEnabled() && this.withAura.getValue() ? this.getHorizontalExpansion() : 0.0;
   }

   public class_238 expandBoundingBox(class_238 boundingBox) {
      double horizontal = this.getHorizontalExpansion();
      return boundingBox.method_1009(horizontal, this.getVerticalExpansion(), horizontal);
   }

   public boolean appliesTo(class_1297 entity) {
      return this.isEnabled() && entity instanceof class_1309 livingEntity && livingEntity.method_5805();
   }

   @EventTarget
   public void onRender3D(Render3DEvent event) {
      if (this.isEnabled() && this.showSize.getValue() && mc.field_1724 != null && mc.field_1687 != null && event.getClient().field_1769 != null) {
         float partialTick = event.getDeltaTracker().method_60637(false);
         class_12181 ignored = event.getClient().field_1769.method_75414();

         try {
            for (class_1297 entity : mc.field_1687.method_18112()) {
               if (entity instanceof class_1309 livingEntity
                  && livingEntity.method_5805()
                  && entity != mc.field_1724
                  && !(mc.field_1724.method_5858(entity) > 4096.0)) {
                  class_238 box = this.interpolatedBox(livingEntity, partialTick);
                  class_12180.method_75553(new HitBoxesFeature.BoundingBoxGizmo(box, -16711809, 1.5F)).method_75533();
                  double eyeY = class_3532.method_16436(partialTick, livingEntity.field_5971, livingEntity.method_23318()) + livingEntity.method_5751();
                  class_238 eyeBox = new class_238(box.field_1323, eyeY - 0.01, box.field_1321, box.field_1320, eyeY + 0.01, box.field_1324);
                  class_12180.method_75553(new HitBoxesFeature.BoundingBoxGizmo(eyeBox, -43691, 1.5F)).method_75533();
               }
            }
         } catch (Throwable var12) {
            if (ignored != null) {
               try {
                  ignored.close();
               } catch (Throwable var11) {
                  var12.addSuppressed(var11);
               }
            }

            throw var12;
         }

         if (ignored != null) {
            ignored.close();
         }
      }
   }

   private class_238 interpolatedBox(class_1309 entity, float partialTick) {
      class_238 box = this.expandBoundingBox(entity.method_5829());
      double renderX = class_3532.method_16436(partialTick, entity.field_6038, entity.method_23317());
      double renderY = class_3532.method_16436(partialTick, entity.field_5971, entity.method_23318());
      double renderZ = class_3532.method_16436(partialTick, entity.field_5989, entity.method_23321());
      return box.method_989(renderX - entity.method_23317(), renderY - entity.method_23318(), renderZ - entity.method_23321());
   }

   @Environment(EnvType.CLIENT)
   private record BoundingBoxGizmo(class_238 box, int color, float lineWidth) implements class_12175 {
      public void method_75531(class_12177 primitives, float alpha) {
         class_243 minMinMin = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1321);
         class_243 minMinMax = new class_243(this.box.field_1323, this.box.field_1322, this.box.field_1324);
         class_243 minMaxMin = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1321);
         class_243 minMaxMax = new class_243(this.box.field_1323, this.box.field_1325, this.box.field_1324);
         class_243 maxMinMin = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1321);
         class_243 maxMinMax = new class_243(this.box.field_1320, this.box.field_1322, this.box.field_1324);
         class_243 maxMaxMin = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1321);
         class_243 maxMaxMax = new class_243(this.box.field_1320, this.box.field_1325, this.box.field_1324);
         primitives.method_75473(minMinMin, minMinMax, this.color, this.lineWidth);
         primitives.method_75473(minMinMin, minMaxMin, this.color, this.lineWidth);
         primitives.method_75473(minMinMin, maxMinMin, this.color, this.lineWidth);
         primitives.method_75473(minMinMax, minMaxMax, this.color, this.lineWidth);
         primitives.method_75473(minMinMax, maxMinMax, this.color, this.lineWidth);
         primitives.method_75473(minMaxMin, minMaxMax, this.color, this.lineWidth);
         primitives.method_75473(minMaxMin, maxMaxMin, this.color, this.lineWidth);
         primitives.method_75473(maxMinMin, maxMinMax, this.color, this.lineWidth);
         primitives.method_75473(maxMinMin, maxMaxMin, this.color, this.lineWidth);
         primitives.method_75473(maxMinMax, maxMaxMax, this.color, this.lineWidth);
         primitives.method_75473(minMaxMax, maxMaxMax, this.color, this.lineWidth);
         primitives.method_75473(maxMaxMin, maxMaxMax, this.color, this.lineWidth);
      }
   }
}
