package ru.fiw.proxyserver;

import com.google.gson.annotations.SerializedName;
import java.net.InetSocketAddress;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class Proxy {
   @SerializedName("IP:PORT")
   public String ipPort = "";
   public Proxy.ProxyType type = Proxy.ProxyType.SOCKS5;
   public String username = "";
   public String password = "";

   public Proxy() {
   }

   public Proxy(boolean socks4, String ipPort, String username, String password) {
      this.type = socks4 ? Proxy.ProxyType.SOCKS4 : Proxy.ProxyType.SOCKS5;
      this.ipPort = safe(ipPort).trim();
      this.username = safe(username);
      this.password = safe(password);
   }

   public Proxy copy() {
      Proxy copy = new Proxy();
      copy.ipPort = safe(this.ipPort).trim();
      copy.type = this.type == null ? Proxy.ProxyType.SOCKS5 : this.type;
      copy.username = safe(this.username);
      copy.password = safe(this.password);
      return copy;
   }

   public boolean isUsable() {
      try {
         this.endpoint();
         return true;
      } catch (IllegalArgumentException ignored) {
         return false;
      }
   }

   public String getIp() {
      return this.endpoint().host();
   }

   public int getPort() {
      return this.endpoint().port();
   }

   public InetSocketAddress socketAddress() {
      Proxy.Endpoint endpoint = this.endpoint();
      return new InetSocketAddress(endpoint.host(), endpoint.port());
   }

   private Proxy.Endpoint endpoint() {
      String value = safe(this.ipPort).trim();
      String host;
      String portText;
      if (value.startsWith("[")) {
         int close = value.indexOf(93);
         if (close < 2 || close + 1 >= value.length() || value.charAt(close + 1) != ':') {
            throw new IllegalArgumentException("Use [IPv6]:port");
         }

         host = value.substring(1, close).trim();
         portText = value.substring(close + 2).trim();
      } else {
         int colon = value.lastIndexOf(58);
         if (colon <= 0 || colon == value.length() - 1) {
            throw new IllegalArgumentException("Use host:port");
         }

         host = value.substring(0, colon).trim();
         portText = value.substring(colon + 1).trim();
      }

      if (host.isEmpty()) {
         throw new IllegalArgumentException("Proxy host is empty");
      }

      int port;
      try {
         port = Integer.parseInt(portText);
      } catch (NumberFormatException exception) {
         throw new IllegalArgumentException("Proxy port is not a number", exception);
      }

      if (port >= 1 && port <= 65535) {
         return new Proxy.Endpoint(host, port);
      } else {
         throw new IllegalArgumentException("Proxy port must be between 1 and 65535");
      }
   }

   private static String safe(String value) {
      return value == null ? "" : value;
   }

   @Environment(EnvType.CLIENT)
   private record Endpoint(String host, int port) {
   }

   @Environment(EnvType.CLIENT)
   public enum ProxyType {
      SOCKS4,
      SOCKS5;
   }
}
