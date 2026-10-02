package org.ryzen.command;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public record Macro(String name, int bindCode, String text) {
}
