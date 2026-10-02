package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
record ListenerDefinition(Class<? extends Event> eventType, int priority, String methodName, EventInvoker invoker) {
}
