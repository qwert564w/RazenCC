package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.TextSetting;

@Environment(EnvType.CLIENT)
public final class NameProtectFeature extends Feature {
   public final TextSetting name = this.register(new TextSetting("Name", "RyzenUser", 32));

   public NameProtectFeature() {
      super("NameProtect", "Protects your nickname in client UI", FeatureCategory.MISC, -1);
   }
}
