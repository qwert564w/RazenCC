package org.ryzen.utils.bots;

import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class BotService {
   public static final BotService INSTANCE = new BotService();
   private final Map<String, BotService.BotEntry> bots = new ConcurrentHashMap<>();

   private BotService() {
   }

   public boolean connect(String name, String address) {
      if (name == null || name.isBlank()) {
         ChatUtil.error("Bot name cannot be empty");
         return false;
      }

      if (address != null && !address.isBlank()) {
         if (this.bots.containsKey(name.toLowerCase(Locale.ROOT))) {
            ChatUtil.error("A bot with that name is already active");
            return false;
         } else {
            this.bots.put(name.toLowerCase(Locale.ROOT), new BotService.BotEntry(name, address, BotService.BotMode.RENDERABLE));
            ChatUtil.success("Bot " + name + " registered @ " + address);
            return true;
         }
      } else {
         ChatUtil.error("Server address cannot be empty");
         return false;
      }
   }

   public boolean disconnect(String name) {
      if (name == null) {
         return false;
      } else {
         BotService.BotEntry removed = this.bots.remove(name.toLowerCase(Locale.ROOT));
         if (removed == null) {
            ChatUtil.error("Bot not found: " + name);
            return false;
         } else {
            ChatUtil.info("Bot " + removed.name() + " disconnected");
            return true;
         }
      }
   }

   public boolean isConnected(String name) {
      return name != null && this.bots.containsKey(name.toLowerCase(Locale.ROOT));
   }

   public Collection<BotService.BotEntry> bots() {
      return this.bots.values();
   }

   public int count() {
      return this.bots.size();
   }

   public void stopAll() {
      this.bots.clear();
   }

   @Environment(EnvType.CLIENT)
   public record BotEntry(String name, String address, BotService.BotMode mode) {
   }

   @Environment(EnvType.CLIENT)
   public enum BotMode {
      RENDERABLE,
      HEADLESS;
   }
}
