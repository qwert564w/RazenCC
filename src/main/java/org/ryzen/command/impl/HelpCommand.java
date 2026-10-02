package org.ryzen.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.ryzen.command.ClientCommand;
import org.ryzen.command.CommandManager;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class HelpCommand extends ClientCommand {
   private final CommandManager manager;

   public HelpCommand(CommandManager manager) {
      super("help", "Shows all available commands", ":question:");
      this.manager = manager;
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         ChatUtil.header("Ryzen commands");

         for (ClientCommand command : this.manager.getCommands()) {
            ChatUtil.entry(command.emoji(), this.manager.getPrefix() + command.name(), command.description());
         }

         return 1;
      });
   }
}
