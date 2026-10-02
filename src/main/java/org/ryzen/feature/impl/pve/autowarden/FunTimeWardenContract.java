package org.ryzen.feature.impl.pve.autowarden;

import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.pve.server.ServerAdapter;
import org.ryzen.pve.server.ServerProfile;

@Environment(EnvType.CLIENT)
final class FunTimeWardenContract {
   private final ServerAdapter adapter;

   FunTimeWardenContract(ServerAdapter adapter) {
      this.adapter = adapter;
   }

   boolean supported() {
      return this.adapter.profile() == ServerProfile.FUNTIME;
   }

   Optional<String> switchAnarchy(int number) {
      return this.supported() ? this.adapter.anarchyCommand(number) : Optional.empty();
   }

   Optional<String> home(String homeName) {
      return this.supported() ? this.adapter.homeCommand(homeName) : Optional.empty();
   }

   Optional<String> auction() {
      return this.supported() ? this.adapter.auctionCommand() : Optional.empty();
   }

   Optional<String> sell(long price) {
      return this.supported() && price > 0L ? Optional.of("ah sell " + price) : Optional.empty();
   }

   Optional<String> invest(long amount) {
      return this.supported() && amount > 0L ? Optional.of("clan invest " + amount) : Optional.empty();
   }

   Optional<String> report(String playerName) {
      return this.supported() ? this.adapter.reportCommand(playerName) : Optional.empty();
   }
}
