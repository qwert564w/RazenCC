package org.ryzen.menu.ui.controls;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.menu.i18n.MenuText;
import org.ryzen.utils.text.StringUtil;

@Environment(EnvType.CLIENT)
public final class MultiSelectComponent extends DropdownComponent {
   public MultiSelectComponent(MultiSelectSetting setting) {
      super(() -> selectedOptions(setting));
   }

   @Override
   protected String displayValue() {
      return this.value();
   }

   private static String selectedOptions(MultiSelectSetting setting) {
      List<String> selected = setting.getOptions().stream().filter(setting::isSelected).map(MenuText::option).toList();
      return selected.isEmpty() ? MenuText.option("Nothing selected") : StringUtil.joinLimited(selected, 2);
   }
}
