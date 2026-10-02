package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface EventInvoker {
   void invoke(Object var1, Event var2);
}
