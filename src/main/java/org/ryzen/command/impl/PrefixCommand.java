package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.command.CommandManager;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class PrefixCommand extends ClientCommand {
   private static final int MAX_LENGTH = 5;
   private final CommandManager manager;

   public PrefixCommand(CommandManager manager) {
      super("prefix", "Shows or changes the command prefix", ":pencil2:");
      this.manager = manager;
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         ChatUtil.info("Current prefix  •  " + this.manager.getPrefix());
         return 1;
      });
      builder.then(RequiredArgumentBuilder.argument("prefix", StringArgumentType.word()).executes(context -> {
         String prefix = StringArgumentType.getString(context, "prefix");
         if (prefix.length() > 5) {
            ChatUtil.error("Prefix is too long  •  Maximum 5 characters");
            return 0;
         } else {
            this.manager.setPrefix(prefix);
            ChatUtil.success("Prefix changed to  •  " + this.manager.getPrefix());
            return 1;
         }
      }));
   }
}
