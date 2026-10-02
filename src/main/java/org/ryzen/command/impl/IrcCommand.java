package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.utils.irc.IrcChatRouter;
import org.ryzen.utils.irc.IrcConfig;
import org.ryzen.utils.irc.IrcConnection;
import org.ryzen.utils.irc.IrcProfile;
import org.ryzen.utils.irc.IrcService;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class IrcCommand extends ClientCommand {
   public IrcCommand() {
      super("irc", "In-client chat for Ryzen users", ":speech_balloon:");
   }

   @Override
   public List<String> aliases() {
      return List.of();
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         this.help();
         return 1;
      });
      builder.then(LiteralArgumentBuilder.literal("help").executes(context -> {
         this.help();
         return 1;
      }));
      builder.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("chat")
                  .then(LiteralArgumentBuilder.literal("on").executes(context -> this.setChat(true))))
               .then(LiteralArgumentBuilder.literal("off").executes(context -> this.setChat(false))))
            .executes(context -> {
               ChatUtil.info("Chat mode is " + (IrcService.INSTANCE.isChatMode() ? "on" : "off") + ". Use .irc chat on/off");
               return 1;
            })
      );
      builder.then(LiteralArgumentBuilder.literal("connect").executes(context -> {
         IrcService.INSTANCE.setConnected(true);
         ChatUtil.info("Connecting to " + address() + " ...");
         return 1;
      }));
      builder.then(LiteralArgumentBuilder.literal("disconnect").executes(context -> {
         IrcService.INSTANCE.setConnected(false);
         ChatUtil.error("Disconnected from IRC");
         return 1;
      }));
      builder.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("server").executes(context -> {
               ChatUtil.info("Server: " + address() + (IrcConfig.INSTANCE.isTls() ? " (TLS)" : ""));
               return 1;
            }))
            .then(
               ((RequiredArgumentBuilder)RequiredArgumentBuilder.argument("host", StringArgumentType.word())
                     .executes(context -> this.setServer(StringArgumentType.getString(context, "host"), 0)))
                  .then(
                     RequiredArgumentBuilder.argument("port", IntegerArgumentType.integer(1, 65535))
                        .executes(context -> this.setServer(StringArgumentType.getString(context, "host"), IntegerArgumentType.getInteger(context, "port")))
                  )
            )
      );
      builder.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("tls")
                  .then(LiteralArgumentBuilder.literal("on").executes(context -> this.setTls(true))))
               .then(LiteralArgumentBuilder.literal("off").executes(context -> this.setTls(false))))
            .executes(context -> {
               ChatUtil.info("TLS is " + (IrcConfig.INSTANCE.isTls() ? "on" : "off"));
               return 1;
            })
      );
      builder.then(
         ((LiteralArgumentBuilder)LiteralArgumentBuilder.literal("channel").executes(context -> {
               ChatUtil.info("Channel: " + IrcConfig.INSTANCE.getChannel());
               return 1;
            }))
            .then(
               RequiredArgumentBuilder.argument("name", StringArgumentType.greedyString())
                  .executes(context -> this.setChannel(StringArgumentType.getString(context, "name")))
            )
      );
      builder.then(LiteralArgumentBuilder.literal("info").executes(context -> {
         this.info();
         return 1;
      }));
      builder.then(RequiredArgumentBuilder.argument("message", StringArgumentType.greedyString()).executes(this::sendMessage));
   }

   private int sendMessage(CommandContext<Object> context) {
      String message = StringArgumentType.getString(context, "message");
      switch (IrcService.INSTANCE.send(message)) {
         case SENT:
            IrcChatRouter.display(this.latest());
            break;
         case MUTED:
            long minutes = (IrcService.INSTANCE.muteRemainingMs() + 59999L) / 60000L;
            ChatUtil.error("You are muted for spam. Wait " + minutes + " min.");
            break;
         case TOO_FAST:
            ChatUtil.error("Slow down - one message per second.");
            break;
         case DISCONNECTED:
            ChatUtil.error("IRC is disconnected. Use .irc connect");
            break;
         case EMPTY:
            ChatUtil.error("Message is empty");
      }

      return 1;
   }

   private int setChat(boolean on) {
      IrcService.INSTANCE.setChatMode(on);
      if (on) {
         ChatUtil.success("IRC chat mode ON - every message goes to IRC. Turn off with .irc chat off");
      } else {
         ChatUtil.info("IRC chat mode OFF - messages go to the server again");
      }

      return 1;
   }

   private int setServer(String host, int port) {
      IrcConfig config = IrcConfig.INSTANCE;
      config.setHost(host);
      if (port > 0) {
         config.setPort(port);
      }

      config.save();
      ChatUtil.success("IRC server set to " + address() + " - reconnecting");
      IrcConnection.INSTANCE.reconnect();
      return 1;
   }

   private int setTls(boolean on) {
      IrcConfig.INSTANCE.setTls(on);
      IrcConfig.INSTANCE.save();
      ChatUtil.success("TLS " + (on ? "on" : "off") + " - reconnecting");
      IrcConnection.INSTANCE.reconnect();
      return 1;
   }

   private int setChannel(String name) {
      String channel = IrcConfig.normalizeChannel(name);
      IrcConfig.INSTANCE.setChannel(channel);
      IrcConfig.INSTANCE.save();
      ChatUtil.success("IRC channel set to " + channel + " - reconnecting");
      IrcConnection.INSTANCE.reconnect();
      return 1;
   }

   private void info() {
      ChatUtil.header("IRC info");
      ChatUtil.info("You: " + IrcProfile.name() + " [" + IrcProfile.role().displayName() + "]");
      ChatUtil.info("Server: " + address() + (IrcConfig.INSTANCE.isTls() ? " (TLS)" : ""));
      ChatUtil.info("Channel: " + IrcConfig.INSTANCE.getChannel());
      ChatUtil.info("Connected: " + (IrcService.INSTANCE.isConnected() ? "yes" : "no"));
      if (IrcService.INSTANCE.isConnected()) {
         ChatUtil.info("Nick: " + IrcConnection.INSTANCE.nick());
      }

      ChatUtil.info("Chat mode: " + (IrcService.INSTANCE.isChatMode() ? "on" : "off"));
      ChatUtil.info("Online: " + IrcService.INSTANCE.onlineUsers().size());
      if (IrcService.INSTANCE.isMuted()) {
         long minutes = (IrcService.INSTANCE.muteRemainingMs() + 59999L) / 60000L;
         ChatUtil.error("Muted for " + minutes + " more min.");
      }
   }

   private void help() {
      ChatUtil.header("IRC help");
      ChatUtil.info(".irc <message>    send one message to IRC");
      ChatUtil.info(".irc chat on    route every chat line into IRC");
      ChatUtil.info(".irc chat off    send chat to the server again");
      ChatUtil.info(".irc connect    join the IRC channel");
      ChatUtil.info(".irc disconnect    leave the IRC channel");
      ChatUtil.info(".irc server <host> [port]    point the client at another IRC server");
      ChatUtil.info(".irc tls on/off    TLS for the IRC socket (6697 on, 6667 off)");
      ChatUtil.info(".irc channel <#name>    switch channel");
      ChatUtil.info(".irc info    your name, role and channel status");
      ChatUtil.info(".irc help    this list");
      ChatUtil.info("Roles: Member (grey), BETA (blue), YT (red), Admin (maroon), Owner (orange)");
      ChatUtil.info("Anti-spam: 3 identical messages = 10 min mute");
   }

   private static String address() {
      return IrcConfig.INSTANCE.getHost() + ":" + IrcConfig.INSTANCE.getPort();
   }

   private IrcService.Message latest() {
      List<IrcService.Message> history = IrcService.INSTANCE.history();
      return history.isEmpty() ? null : history.get(history.size() - 1);
   }
}
