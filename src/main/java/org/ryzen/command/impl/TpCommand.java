package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_2828.class_2829;
import net.minecraft.class_2828.class_5911;
import org.ryzen.command.ClientCommand;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class TpCommand extends ClientCommand {
   private static final int SETTLE_PACKETS = 3;

   public TpCommand() {
      super("tp", "Teleports to a player by name", ":round_pushpin:");
   }

   @Override
   public List<String> aliases() {
      return List.of("teleport");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> this.showUsage());
      builder.then(
         RequiredArgumentBuilder.argument("name", StringArgumentType.word())
            .suggests((context, suggestions) -> suggestPlayers(suggestions))
            .executes(this::teleport)
      );
   }

   private int showUsage() {
      ChatUtil.usage("tp <name>  •  teleports to a player in your world");
      return 1;
   }

   private int teleport(CommandContext<Object> context) {
      class_310 mc = class_310.method_1551();
      class_746 player = mc.field_1724;
      if (player != null && mc.field_1687 != null) {
         String name = StringArgumentType.getString(context, "name");
         class_1657 target = findPlayer(mc, name);
         if (target == null) {
            ChatUtil.error("Player not found  •  " + name);
            return 0;
         }

         if (target == player) {
            ChatUtil.error("That is you");
            return 0;
         }

         class_243 position = target.method_73189();

         for (int index = 0; index < 3; index++) {
            player.field_3944.method_52787(new class_5911(player.method_24828(), player.field_5976));
         }

         player.field_3944.method_52787(new class_2829(position.field_1352, position.field_1351, position.field_1350, false, player.field_5976));
         player.method_5814(position.field_1352, position.field_1351, position.field_1350);
         ChatUtil.success(
            String.format(
               Locale.ROOT, "Teleported to %s  •  %.1f %.1f %.1f", target.method_7334().name(), position.field_1352, position.field_1351, position.field_1350
            )
         );
         return 1;
      } else {
         ChatUtil.error("Not in a world");
         return 0;
      }
   }

   private static class_1657 findPlayer(class_310 mc, String name) {
      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != null && player.method_7334().name().equalsIgnoreCase(name)) {
            return player;
         }
      }

      return null;
   }

   private static CompletableFuture<Suggestions> suggestPlayers(SuggestionsBuilder builder) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1687 == null) {
         return builder.buildFuture();
      }

      String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
      List<String> names = new ArrayList<>();

      for (class_1657 player : mc.field_1687.method_18456()) {
         if (player != null && player != mc.field_1724) {
            names.add(player.method_7334().name());
         }
      }

      names.stream().filter(name -> name.toLowerCase(Locale.ROOT).startsWith(remaining)).forEach(builder::suggest);
      return builder.buildFuture();
   }
}
