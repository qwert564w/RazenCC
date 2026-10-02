package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.utils.bots.BotService;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class BotCommand extends ClientCommand {
   public BotCommand() {
      super("bot", "Manage bots", ":robot:");
   }

   @Override
   public List<String> aliases() {
      return List.of("bots");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         this.usage();
         return 1;
      });
      builder.then(LiteralArgumentBuilder.literal("list").executes(context -> {
         this.list();
         return 1;
      }));
      builder.then(
         LiteralArgumentBuilder.literal("connect")
            .then(
               RequiredArgumentBuilder.argument("name", StringArgumentType.word())
                  .then(RequiredArgumentBuilder.argument("address", StringArgumentType.greedyString()).executes(this::connect))
            )
      );
      builder.then(
         LiteralArgumentBuilder.literal("disconnect").then(RequiredArgumentBuilder.argument("name", StringArgumentType.word()).executes(this::disconnect))
      );
      builder.then(LiteralArgumentBuilder.literal("stopall").executes(context -> {
         BotService.INSTANCE.stopAll();
         ChatUtil.success("All bots stopped");
         return 1;
      }));
   }

   private int connect(CommandContext<Object> context) {
      BotService.INSTANCE.connect(StringArgumentType.getString(context, "name"), StringArgumentType.getString(context, "address"));
      return 1;
   }

   private int disconnect(CommandContext<Object> context) {
      BotService.INSTANCE.disconnect(StringArgumentType.getString(context, "name"));
      return 1;
   }

   private void list() {
      if (BotService.INSTANCE.count() == 0) {
         ChatUtil.info("The bot list is empty");
      } else {
         ChatUtil.header("Bots (" + BotService.INSTANCE.count() + ")");

         for (BotService.BotEntry entry : BotService.INSTANCE.bots()) {
            ChatUtil.info("- " + entry.name() + " @ " + entry.address());
         }
      }
   }

   private void usage() {
      ChatUtil.header("Bot commands");
      ChatUtil.info(".bot list    show registered bots");
      ChatUtil.info(".bot connect <name> <address>    register a bot");
      ChatUtil.info(".bot disconnect <name>    remove a bot");
      ChatUtil.info(".bot stopall    remove every bot");
   }
}
