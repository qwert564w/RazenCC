package org.ryzen.pve.mining;

import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface MiningServerAdapter {
   default Optional<MineTimer> parseMineTimer(List<String> hologramLines, long nowMillis) {
      return Optional.empty();
   }

   default Optional<String> warpMineCommand() {
      return Optional.empty();
   }

   default Optional<String> homeCommand(String homeName) {
      return Optional.empty();
   }

   default Optional<String> anarchyCommand(int number) {
      return Optional.empty();
   }

   default Optional<String> randomTeleportCommand(String size) {
      return Optional.empty();
   }
}
