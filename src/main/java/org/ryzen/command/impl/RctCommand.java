package org.ryzen.command.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_642;
import org.ryzen.command.ClientCommand;
import org.ryzen.context.MinecraftContext;
import org.ryzen.mixin.accessor.PlayerTabOverlayAccessor;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class RctCommand extends ClientCommand implements MinecraftContext {
   private static final Pattern ANARCHY_PATTERN = Pattern.compile("анархия-(\\d+)");
   private static final Pattern GRIEF_PATTERN = Pattern.compile("гриф-(\\d+)");
   private static final long REJOIN_DELAY_MS = 1500L;

   public RctCommand() {
      super("rct", "Rejoins the current anarchy (FunTime/SpookyTime)", ":arrows_counterclockwise:");
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> this.execute());
   }

   private int execute() {
      if (this.player() == null) {
         return 0;
      } else if (!this.isOnSupportedServer()) {
         ChatUtil.error("Rct only works on FunTime and SpookyTime");
         return 0;
      } else {
         String header = this.tabHeader();
         String rejoinCommand = this.resolveRejoinCommand(header);
         if (rejoinCommand == null) {
            ChatUtil.error("Failed to detect the anarchy number from the tab list");
            return 0;
         } else {
            this.player().field_3944.method_45730("hub");
            CompletableFuture.delayedExecutor(1500L, TimeUnit.MILLISECONDS).execute(() -> mc.execute(() -> {
               if (this.player() != null) {
                  this.player().field_3944.method_45730(rejoinCommand);
               }
            }));
            ChatUtil.info("Reconnecting via /" + rejoinCommand + "...");
            return 1;
         }
      }
   }

   private boolean isOnSupportedServer() {
      class_642 server = mc.method_1558();
      if (server != null && server.field_3761 != null) {
         String ip = server.field_3761.toLowerCase(Locale.ROOT);
         return ip.contains("funtime") || ip.contains("spookytime");
      } else {
         return false;
      }
   }

   private String tabHeader() {
      class_2561 header = ((PlayerTabOverlayAccessor)mc.field_1705.method_1750()).getHeader();
      return header == null ? "" : header.getString().toLowerCase(Locale.ROOT).replaceAll("§.", "");
   }

   private String resolveRejoinCommand(String header) {
      Matcher anarchy = ANARCHY_PATTERN.matcher(header);
      if (anarchy.find()) {
         return "an" + anarchy.group(1);
      }

      Matcher grief = GRIEF_PATTERN.matcher(header);
      return grief.find() ? "grief" + grief.group(1) : null;
   }
}
