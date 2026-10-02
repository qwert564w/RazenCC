package org.ryzen.feature.impl.misc;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;

@Environment(EnvType.CLIENT)
public final class BotsFeature extends Feature {
   public final TextSetting nickname = this.register(new TextSetting("Nickname", "Rickstone"));
   public final NumberSetting delay = this.register(new NumberSetting("Connect Delay", 500.0, 0.0, 8000.0, 50.0, "ms"));
   public final NumberSetting activeLimit = this.register(new NumberSetting("Active Limit", 6.0, 1.0, 32.0, 1.0, ""));
   public final BooleanSetting chat = this.register(new BooleanSetting("Chat", true));
   public final BooleanSetting bossBar = this.register(new BooleanSetting("Boss Bar", true));

   public BotsFeature() {
      super("Bots", "Bot farm settings", FeatureCategory.MISC, -1);
   }

   public long connectDelayMs() {
      return Math.max(0L, Math.round(this.delay.getValue()));
   }

   public int activeBotLimit() {
      return Math.max(1, this.activeLimit.getValue().intValue());
   }
}
