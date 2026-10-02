package org.ryzen.menu.ui.rows;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface RowHost {
   void closeOtherRows(SettingRow var1);

   int popupViewportMaxY();
}
