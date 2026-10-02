package org.ryzen.feature.impl.visual;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1306;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ViewModelFeature extends Feature {
   public final NumberSetting rightX = this.register(new NumberSetting("Right X", 0.0, -1.0, 1.0, 0.05, ""));
   public final NumberSetting rightY = this.register(new NumberSetting("Right Y", 0.0, -1.0, 1.0, 0.05, ""));
   public final NumberSetting rightZ = this.register(new NumberSetting("Right Z", 0.0, -1.0, 1.0, 0.05, ""));
   public final NumberSetting leftX = this.register(new NumberSetting("Left X", 0.0, -1.0, 1.0, 0.05, ""));
   public final NumberSetting leftY = this.register(new NumberSetting("Left Y", 0.0, -1.0, 1.0, 0.05, ""));
   public final NumberSetting leftZ = this.register(new NumberSetting("Left Z", 0.0, -1.0, 1.0, 0.05, ""));
   public final ButtonSetting resetPosition = this.register(new ButtonSetting("Reset Position", "Reset", this::resetPosition));

   public ViewModelFeature() {
      super("ViewModel", "Adjusts first person hand positions", FeatureCategory.VISUAL, -1);
   }

   public float offsetX(class_1306 arm) {
      return value(arm == class_1306.field_6183 ? this.rightX : this.leftX);
   }

   public float offsetY(class_1306 arm) {
      return value(arm == class_1306.field_6183 ? this.rightY : this.leftY);
   }

   public float offsetZ(class_1306 arm) {
      return value(arm == class_1306.field_6183 ? this.rightZ : this.leftZ);
   }

   private static float value(NumberSetting setting) {
      return setting.getValue().floatValue();
   }

   private void resetPosition() {
      this.rightX.reset();
      this.rightY.reset();
      this.rightZ.reset();
      this.leftX.reset();
      this.leftY.reset();
      this.leftZ.reset();
   }
}
