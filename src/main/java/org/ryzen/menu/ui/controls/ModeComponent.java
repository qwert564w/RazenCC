package org.ryzen.menu.ui.controls;

import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ModeComponent extends DropdownComponent {
   public ModeComponent(Supplier<String> valueSupplier) {
      super(valueSupplier);
   }
}
