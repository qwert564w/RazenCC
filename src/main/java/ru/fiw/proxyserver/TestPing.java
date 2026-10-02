package ru.fiw.proxyserver;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;

@Environment(EnvType.CLIENT)
public final class TestPing {
   private final AtomicLong generation = new AtomicLong();
   public volatile String state = "";

   public void run(String targetIp, int targetPort, Proxy proxy) {
      long attempt = this.generation.incrementAndGet();
      this.state = class_124.field_1054 + "Connecting...";
      Proxy snapshot = proxy.copy();
      CompletableFuture.runAsync(() -> this.test(attempt, targetIp, targetPort, snapshot));
   }

   private void test(long attempt, String targetIp, int targetPort, Proxy proxy) {
      EventLoopGroup group = new NioEventLoopGroup(1);

      try {
         final InetSocketAddress proxyAddress = proxy.socketAddress();
         Bootstrap bootstrap = (Bootstrap)((Bootstrap)((Bootstrap)((Bootstrap)new Bootstrap().group(group)).channel(NioSocketChannel.class))
               .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000))
            .handler(new ChannelInitializer<Channel>() {
               protected void initChannel(Channel channel) {
                  ProxyHandler handler = TestPing.handler(proxy, proxyAddress);
                  handler.setConnectTimeoutMillis(5000L);
                  channel.pipeline().addLast("proxyserver_test", handler);
               }
            });
         Channel channel = bootstrap.connect(targetIp, targetPort).sync().channel();
         this.setState(attempt, class_124.field_1060 + "Success!");
         channel.close().syncUninterruptibly();
      } catch (Exception exception) {
         String detail = exception.getMessage();
         this.setState(attempt, class_124.field_1061 + "Failed: " + (detail != null && !detail.isBlank() ? detail : exception.getClass().getSimpleName()));
      } finally {
         group.shutdownGracefully().syncUninterruptibly();
      }
   }

   private void setState(long attempt, String state) {
      if (this.generation.get() == attempt) {
         this.state = state;
      }
   }

   private static ProxyHandler handler(Proxy proxy, InetSocketAddress address) {
      String user = proxy.username == null ? "" : proxy.username;
      String password = proxy.password == null ? "" : proxy.password;
      if (proxy.type == Proxy.ProxyType.SOCKS4) {
         return user.isBlank() ? new Socks4ProxyHandler(address) : new Socks4ProxyHandler(address, user);
      } else {
         return user.isBlank() ? new Socks5ProxyHandler(address) : new Socks5ProxyHandler(address, user, password);
      }
   }
}
