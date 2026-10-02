package org.ryzen.utils.irc;

import com.google.gson.JsonObject;
import java.nio.file.Path;
import lombok.Generated;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.utils.ConfigIO;

@Environment(EnvType.CLIENT)
public final class IrcConfig {
   public static final IrcConfig INSTANCE = new IrcConfig();
   private static final String DEFAULT_HOST = "irc.libera.chat";
   private static final int DEFAULT_PORT = 6697;
   private static final String DEFAULT_CHANNEL = "#ryzen-client";
   private Path path;
   private boolean loaded;
   private volatile String host = "irc.libera.chat";
   private volatile int port = 6697;
   private volatile boolean tls = true;
   private volatile String channel = "#ryzen-client";
   private volatile boolean autoConnect = true;

   private IrcConfig() {
   }

   public synchronized void load() {
      if (!this.loaded) {
         this.loaded = true;
         JsonObject root = ConfigIO.read(this.path());
         if (root != null) {
            this.host = string(root, "host", "irc.libera.chat");
            this.port = root.has("port") ? root.get("port").getAsInt() : 6697;
            this.tls = !root.has("tls") || root.get("tls").getAsBoolean();
            this.channel = normalizeChannel(string(root, "channel", "#ryzen-client"));
            this.autoConnect = !root.has("autoConnect") || root.get("autoConnect").getAsBoolean();
            if (this.host.isBlank()) {
               this.host = "irc.libera.chat";
            }

            if (this.port <= 0 || this.port > 65535) {
               this.port = 6697;
            }
         }
      }
   }

   public synchronized void save() {
      JsonObject root = new JsonObject();
      root.addProperty("host", this.host);
      root.addProperty("port", this.port);
      root.addProperty("tls", this.tls);
      root.addProperty("channel", this.channel);
      root.addProperty("autoConnect", this.autoConnect);
      ConfigIO.write(this.path(), root);
   }

   public static String normalizeChannel(String value) {
      if (value != null && !value.isBlank()) {
         String trimmed = value.trim();
         return !trimmed.startsWith("#") && !trimmed.startsWith("&") ? "#" + trimmed : trimmed;
      } else {
         return "#ryzen-client";
      }
   }

   private static String string(JsonObject root, String key, String fallback) {
      return root.has(key) ? root.get(key).getAsString() : fallback;
   }

   private Path path() {
      if (this.path == null) {
         this.path = ConfigIO.resolve("irc.json");
      }

      return this.path;
   }

   @Generated
   public String getHost() {
      return this.host;
   }

   @Generated
   public void setHost(String host) {
      this.host = host;
   }

   @Generated
   public int getPort() {
      return this.port;
   }

   @Generated
   public void setPort(int port) {
      this.port = port;
   }

   @Generated
   public boolean isTls() {
      return this.tls;
   }

   @Generated
   public void setTls(boolean tls) {
      this.tls = tls;
   }

   @Generated
   public String getChannel() {
      return this.channel;
   }

   @Generated
   public void setChannel(String channel) {
      this.channel = channel;
   }

   @Generated
   public boolean isAutoConnect() {
      return this.autoConnect;
   }

   @Generated
   public void setAutoConnect(boolean autoConnect) {
      this.autoConnect = autoConnect;
   }
}
