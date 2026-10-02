package ru.fiw.proxyserver;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public final class ProxyServer {
   public static boolean proxyEnabled;
   public static Proxy proxy = new Proxy();
   public static Proxy lastUsedProxy = new Proxy();

   private ProxyServer() {
   }

   public static void initialize() {
      Config.loadConfig();
   }

   public static String getLastUsedProxyIp() {
      if (lastUsedProxy != null && lastUsedProxy.ipPort != null && !lastUsedProxy.ipPort.isBlank()) {
         try {
            return lastUsedProxy.getIp();
         } catch (IllegalArgumentException ignored) {
            return "none";
         }
      } else {
         return "none";
      }
   }
}
