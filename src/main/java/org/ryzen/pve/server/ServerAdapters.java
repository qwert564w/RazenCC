package org.ryzen.pve.server;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.feature.impl.pve.PveManagerFeature;

@Environment(EnvType.CLIENT)
public final class ServerAdapters {
   private static final Map<ServerProfile, ServerAdapter> ADAPTERS = new EnumMap<>(ServerProfile.class);

   private ServerAdapters() {
   }

   public static ServerAdapter current() {
      return forProfile(PveManagerFeature.INSTANCE.resolveServerProfile(class_310.method_1551()));
   }

   public static ServerAdapter forProfile(ServerProfile profile) {
      return ADAPTERS.getOrDefault(profile, ADAPTERS.get(ServerProfile.GENERIC));
   }

   static {
      ADAPTERS.put(ServerProfile.GENERIC, new ServerAdapters.GenericAdapter());
      ADAPTERS.put(ServerProfile.FUNTIME, new ServerAdapters.FunTimeAdapter());
      ADAPTERS.put(ServerProfile.HOLYWORLD, new ServerAdapters.HolyWorldAdapter());
      ADAPTERS.put(ServerProfile.REALLYWORLD, new ServerAdapters.ReallyWorldAdapter());
   }

   @Environment(EnvType.CLIENT)
   private static final class FunTimeAdapter extends ServerAdapters.GenericAdapter {
      @Override
      public ServerProfile profile() {
         return ServerProfile.FUNTIME;
      }

      @Override
      public Optional<String> anarchyCommand(int number) {
         return number > 0 ? Optional.of("an " + number) : Optional.empty();
      }

      @Override
      public Optional<String> homeCommand(String home) {
         return home != null && !home.isBlank() ? Optional.of("home " + home.trim()) : Optional.of("home");
      }

      @Override
      public Optional<String> hubCommand() {
         return Optional.of("hub");
      }

      @Override
      public Optional<String> auctionCommand() {
         return Optional.of("ah");
      }

      @Override
      public Optional<String> reportCommand(String playerName) {
         return playerName != null && !playerName.isBlank() ? Optional.of("report " + playerName.trim()) : Optional.empty();
      }
   }

   @Environment(EnvType.CLIENT)
   private static class GenericAdapter implements ServerAdapter {
      @Override
      public ServerProfile profile() {
         return ServerProfile.GENERIC;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class HolyWorldAdapter extends ServerAdapters.GenericAdapter {
      @Override
      public ServerProfile profile() {
         return ServerProfile.HOLYWORLD;
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class ReallyWorldAdapter extends ServerAdapters.GenericAdapter {
      @Override
      public ServerProfile profile() {
         return ServerProfile.REALLYWORLD;
      }

      @Override
      public Optional<String> hubCommand() {
         return Optional.of("hub");
      }

      @Override
      public Optional<String> reportCommand(String playerName) {
         return playerName != null && !playerName.isBlank() ? Optional.of("report " + playerName.trim()) : Optional.empty();
      }
   }
}
