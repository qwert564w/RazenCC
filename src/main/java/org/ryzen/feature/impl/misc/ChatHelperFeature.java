package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class ChatHelperFeature extends Feature {
   public final BooleanSetting antiSpam = this.register(new BooleanSetting("Anti Spam", true));
   public final NumberSetting window = this.register(new NumberSetting("Repeat Window", 10.0, 1.0, 50.0, 1.0, " lines").visibleWhen(this.antiSpam::getValue));
   public final BooleanSetting keepHistory = this.register(new BooleanSetting("Keep History", true));

   public ChatHelperFeature() {
      super("ChatHelper", "Collapses repeated messages and keeps chat across worlds", FeatureCategory.MISC, -1);
   }

   public static ChatHelperFeature getEnabled() {
      return FeatureManager.INSTANCE.getEnabled(ChatHelperFeature.class);
   }

   public static int antiSpamWindow() {
      ChatHelperFeature feature = getEnabled();
      return feature != null && feature.antiSpam.getValue() ? feature.window.getValue().intValue() : 0;
   }

   public static boolean shouldKeepHistory() {
      ChatHelperFeature feature = getEnabled();
      return feature != null && feature.keepHistory.getValue();
   }

   public static class_2561 withRepeatCounter(class_2561 message, int count) {
      return class_2561.method_43473().method_10852(message).method_10852(class_2561.method_43470(" (x" + count + ")"));
   }
}
