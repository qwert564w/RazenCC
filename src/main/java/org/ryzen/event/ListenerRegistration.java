package org.ryzen.event;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
record ListenerRegistration(Class<? extends Event> eventType, RegisteredListener listener) {
}
