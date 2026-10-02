package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ButtonSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.InputBindSetting;
import org.ryzen.feature.setting.ModeSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.Setting;
import org.ryzen.feature.setting.TextSetting;

@Environment(EnvType.CLIENT)
public final class SettingRows {
   private SettingRows() {
   }

   public static SettingRow create(String featureName, Setting<?> setting) {
      SettingRow row = switch (setting) {
         case BooleanSetting booleanSetting -> new ToggleRow(booleanSetting);
         case ButtonSetting buttonSetting -> new ButtonRow(buttonSetting);
         case NumberSetting numberSetting -> new SliderRow(numberSetting);
         case ModeSetting modeSetting -> new DropdownRow(modeSetting);
         case MultiSelectSetting multiSelectSetting -> new DropdownRow(multiSelectSetting);
         case ColorSetting colorSetting -> new ColorRow(colorSetting);
         case TextSetting textSetting -> new TextRow(textSetting);
         case InputBindSetting inputBindSetting -> new BindChipRow(inputBindSetting);
         default -> null;
      };
      return row == null ? null : row.context(featureName);
   }
}
