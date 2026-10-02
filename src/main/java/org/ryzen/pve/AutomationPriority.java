package org.ryzen.pve;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public enum AutomationPriority {
   BACKGROUND(0),
   FEATURE(100),
   BOT(200),
   EMERGENCY(300);

   private final int weight;

   AutomationPriority(int weight) {
      this.weight = weight;
   }

   public int weight() {
      return this.weight;
   }
}
