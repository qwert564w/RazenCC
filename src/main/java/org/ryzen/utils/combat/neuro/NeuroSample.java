package org.ryzen.utils.combat.neuro;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record NeuroSample(float yaw, float pitch, float moveX, float moveZ, boolean sprinting, boolean jumping) {
}
