package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.ryzen.command.ClientCommand;
import org.ryzen.utils.FriendManager;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class FriendCommand extends ClientCommand {
   public FriendCommand() {
      super("friend", "Manages the friend list", ":busts_in_silhouette:");
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
      builder.then(this.friendRemoval("delete"));
      builder.then(this.friendRemoval("remove"));
      builder.then(LiteralArgumentBuilder.literal("list").executes(context -> this.list()));
      builder.then(LiteralArgumentBuilder.literal("clear").executes(context -> this.clear()));
   }

   private int showUsage() {
      ChatUtil.usage("friend add <name>  •  delete <name>  •  list  •  clear");
      return 1;
   }

   private int add(CommandContext<Object> context) {
      String name = StringArgumentType.getString(context, "name");
      if (!FriendManager.isValidName(name)) {
         ChatUtil.error("Invalid player name  •  " + name);
         return 0;
      } else if (!FriendManager.INSTANCE.add(name)) {
         ChatUtil.error(name + " is already a friend");
         return 0;
      } else {
         ChatUtil.success(name + " added to friends");
         return 1;
      }
   }

   private int remove(CommandContext<Object> context) {
      String name = StringArgumentType.getString(context, "name");
      if (!FriendManager.INSTANCE.remove(name)) {
         ChatUtil.error("Friend not found  •  " + name);
         return 0;
      } else {
         ChatUtil.success(name + " removed from friends");
         return 1;
      }
   }

   private int list() {
      Collection<String> friends = FriendManager.INSTANCE.getFriends();
      if (friends.isEmpty()) {
         ChatUtil.info("Friend list is empty");
         return 1;
      } else {
         ChatUtil.header("Friends  •  " + friends.size());
         friends.forEach(name -> ChatUtil.entry(":bust_in_silhouette:", name, null));
         return 1;
      }
   }

   private int clear() {
      if (!FriendManager.INSTANCE.clear()) {
         ChatUtil.info("Friend list is already empty");
         return 1;
      } else {
         ChatUtil.success("Friend list cleared");
         return 1;
      }
   }

   private LiteralArgumentBuilder<Object> friendRemoval(String literal) {
      return (LiteralArgumentBuilder<Object>)LiteralArgumentBuilder.literal(literal)
         .then(
            RequiredArgumentBuilder.argument("name", StringArgumentType.word())
               .suggests((context, suggestions) -> suggest(suggestions, FriendManager.INSTANCE.getFriends()))
               .executes(this::remove)
         );
   }

   private CompletableFuture<Suggestions> suggestOnlinePlayers(SuggestionsBuilder builder) {
      class_310 minecraft = class_310.method_1551();
      return minecraft.method_1562() == null
         ? builder.buildFuture()
         : suggest(
            builder,
            minecraft.method_1562()
               .method_2880()
               .stream()
               .map(info -> info.method_2966().name())
               .filter(name -> !FriendManager.INSTANCE.isFriend(name))
               .toList()
         );
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
