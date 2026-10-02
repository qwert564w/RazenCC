package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.command.ClientCommand;
import org.ryzen.utils.StaffManager;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class StaffCommand extends ClientCommand {
   public StaffCommand() {
      super("staff", "Manages the staff list of the current server", ":shield:");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> this.showUsage());
      builder.then(
         LiteralArgumentBuilder.literal("add")
            .then(
               RequiredArgumentBuilder.argument("name", StringArgumentType.word())
                  .suggests((context, suggestions) -> this.suggestOnlinePlayers(suggestions))
                  .executes(this::add)
            )
      );
      builder.then(
         LiteralArgumentBuilder.literal("remove")
            .then(
               RequiredArgumentBuilder.argument("name", StringArgumentType.word())
                  .suggests((context, suggestions) -> this.suggestStaff(suggestions))
                  .executes(this::remove)
            )
      );
      builder.then(LiteralArgumentBuilder.literal("list").executes(context -> this.list()));
   }

   private int showUsage() {
      ChatUtil.usage("staff add <name>  •  remove <name>  •  list");
      return 1;
   }

   private int add(CommandContext<Object> context) {
      String server = StaffManager.INSTANCE.currentServer();
      if (server.isEmpty()) {
         ChatUtil.error("Join a server first  •  the staff list is per server");
         return 0;
      } else {
         String name = StringArgumentType.getString(context, "name");
         if (!StaffManager.isValidName(name)) {
            ChatUtil.error("Invalid player name  •  " + name);
            return 0;
         } else if (!StaffManager.INSTANCE.add(server, name)) {
            ChatUtil.error(name + " is already staff on " + server);
            return 0;
         } else {
            ChatUtil.success(name + " added to staff of " + server);
            return 1;
         }
      }
   }

   private int remove(CommandContext<Object> context) {
      String server = StaffManager.INSTANCE.currentServer();
      String name = StringArgumentType.getString(context, "name");
      if (!server.isEmpty() && StaffManager.INSTANCE.remove(server, name)) {
         ChatUtil.success(name + " removed from staff of " + server);
         return 1;
      } else {
         ChatUtil.error("Staff not found  •  " + name);
         return 0;
      }
   }

   private int list() {
      String server = StaffManager.INSTANCE.currentServer();
      if (server.isEmpty()) {
         return this.listEverything();
      } else {
         List<String> names = StaffManager.INSTANCE.names(server);
         if (names.isEmpty()) {
            ChatUtil.info("No staff on record for " + server);
            return 1;
         } else {
            ChatUtil.header("Staff  •  " + server + "  •  " + names.size());
            names.forEach(name -> ChatUtil.entry(":shield:", name, null));
            return 1;
         }
      }
   }

   private int listEverything() {
      Map<String, List<String>> all = StaffManager.INSTANCE.all();
      if (all.isEmpty()) {
         ChatUtil.info("Staff list is empty");
         return 1;
      } else {
         ChatUtil.header("Staff  •  " + all.size() + " servers");
         all.forEach((server, names) -> ChatUtil.entry(":shield:", server, String.join(", ", names)));
         return 1;
      }
   }

   private CompletableFuture<Suggestions> suggestOnlinePlayers(SuggestionsBuilder builder) {
      class_310 minecraft = class_310.method_1551();
      return minecraft.method_1562() == null
         ? builder.buildFuture()
         : suggest(
            builder,
            minecraft.method_1562().method_2880().stream().map(info -> info.method_2966().name()).filter(name -> !StaffManager.INSTANCE.isStaff(name)).toList()
         );
   }

   private CompletableFuture<Suggestions> suggestStaff(SuggestionsBuilder builder) {
      return suggest(builder, StaffManager.INSTANCE.names(StaffManager.INSTANCE.currentServer()));
   }

   private static CompletableFuture<Suggestions> suggest(SuggestionsBuilder builder, Iterable<String> values) {
      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

      for (String value : values) {
         if (value.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(value);
         }
      }

      return builder.buildFuture();
   }
}
