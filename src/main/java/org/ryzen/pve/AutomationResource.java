package org.ryzen.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutomationResource {
   MOVEMENT,
   ROTATION,
   INVENTORY,
   SCREEN,
   CHAT,
   NAVIGATION,
   COMBAT;
}
