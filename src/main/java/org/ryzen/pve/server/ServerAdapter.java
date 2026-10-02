package org.ryzen.pve.server;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_746;

@Environment(EnvType.CLIENT)
public interface ServerAdapter {
   ServerProfile profile();

   default void sendCommand(class_746 player, String command) {
      if (player != null && command != null && !command.isBlank()) {
         String normalized = command.charAt(0) == '/' ? command.substring(1) : command;
         player.field_3944.method_45730(normalized);
      }
   }

   default Optional<String> anarchyCommand(int number) {
      return Optional.empty();
   }

   default Optional<String> homeCommand(String home) {
      return Optional.empty();
   }

   default Optional<String> hubCommand() {
      return Optional.empty();
   }

   default Optional<String> auctionCommand() {
      return Optional.empty();
   }

   default Optional<String> reportCommand(String playerName) {
      return Optional.empty();
   }
}
