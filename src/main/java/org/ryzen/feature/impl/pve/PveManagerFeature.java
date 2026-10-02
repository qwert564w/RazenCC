package org.ryzen.feature.impl.pve;

import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.navigation.NavigationOptions;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
public final class PveManagerFeature extends Feature {
   public static final PveManagerFeature INSTANCE = new PveManagerFeature();
   public static final String SERVER_AUTO = "Auto";
   public static final String SERVER_GENERIC = "Generic";
   public static final String SERVER_FUNTIME = "FunTime";
   public static final String SERVER_HOLYWORLD = "HolyWorld";
   public static final String SERVER_REALLYWORLD = "ReallyWorld";
   public final BooleanSetting rotate = this.register(new BooleanSetting("Rotate", true));
   public final ModeSetting serverProfile = this.register(new ModeSetting("Server Profile", "Auto", "Auto", "Generic", "FunTime", "HolyWorld", "ReallyWorld"));
   public final TextSetting homeName = this.register(new TextSetting("Home Name", "", 32));
   public final TextSetting clanName = this.register(new TextSetting("Clan Name", "", 32));
   public final NumberSetting anarchy = this.register(new NumberSetting("Anarchy", 1.0, 1.0, 999.0, 1.0, ""));
   public final NumberSetting minimumHealth = this.register(new NumberSetting("Minimum Health", 12.0, 1.0, 20.0, 1.0, ""));
   public final NumberSetting minimumToolDurability = this.register(new NumberSetting("Minimum Tool Durability", 15.0, 1.0, 80.0, 1.0, "%"));
   public final BooleanSetting pauseNearPlayers = this.register(new BooleanSetting("Pause Near Players", true));
   public final NumberSetting playerRadius = this.register(
      new NumberSetting("Player Radius", 16.0, 0.0, 96.0, 1.0, " blocks").visibleWhen(this.pauseNearPlayers::getValue)
   );
   public final BooleanSetting debugLogging = this.register(new BooleanSetting("Debug Logging", true));
   public final BooleanSetting currentState = this.register(new BooleanSetting("Current State", false));

   private PveManagerFeature() {
      super("PveManager", "Shared server, identity, rotation and safety settings for PvE", FeatureCategory.PVE, -1);
   }

   @Override
   public boolean isToggleable() {
      return false;
   }

   @Override
   public boolean supportsBinds() {
      return false;
   }

   public ServerProfile resolveServerProfile(class_310 client) {
      String normalized = this.serverProfile.getValue().trim().toLowerCase(Locale.ROOT);

      return switch (normalized) {
         case "generic" -> ServerProfile.GENERIC;
         case "funtime" -> ServerProfile.FUNTIME;
         case "holyworld" -> ServerProfile.HOLYWORLD;
         case "reallyworld" -> ServerProfile.REALLYWORLD;
         default -> ServerProfile.detect(client);
      };
   }

   public String resolvedHomeName() {
      return this.homeName.getValue().trim();
   }

   public String resolvedClanName() {
      return this.clanName.getValue().trim();
   }

   public int resolvedAnarchy() {
      return this.anarchy.getValue().intValue();
   }

   public NavigationOptions configureNavigation(NavigationOptions options) {
      return options.withViewRotation(this.rotate.getValue());
   }
}
