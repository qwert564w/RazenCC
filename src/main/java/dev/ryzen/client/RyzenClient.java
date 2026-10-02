package dev.ryzen.client;

import dev.ryzen.client.account.AccountSwitcher;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.ClientStarted;
import ru.fiw.proxyserver.ProxyServer;

@Environment(EnvType.CLIENT)
public final class RyzenClient implements ClientModInitializer {
   public void onInitializeClient() {
      ProxyServer.initialize();
      ClientLifecycleEvents.CLIENT_STARTED.register((ClientStarted)client -> AccountSwitcher.applyStartupAccount());
   }
}
