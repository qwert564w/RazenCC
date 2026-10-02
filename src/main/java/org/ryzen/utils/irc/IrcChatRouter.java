package org.ryzen.utils.irc;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5250;

@Environment(EnvType.CLIENT)
public final class IrcChatRouter {
   private static final int IRC_TAG_COLOR = -11141121;
   private static final int NAME_COLOR = -1;
   private static final int TEXT_COLOR = -2236963;
   private static final int INFO_COLOR = -6643546;
   private static final int WARN_COLOR = -43691;

   private IrcChatRouter() {
   }

   public static boolean routeOutgoing(String message) {
      IrcService irc = IrcService.INSTANCE;
      if (irc.isChatMode() && message != null && !message.isBlank()) {
         switch (irc.send(message)) {
            case SENT:
               List<IrcService.Message> history = irc.history();
               if (!history.isEmpty()) {
                  display(history.get(history.size() - 1));
               }

               return true;
            case MUTED:
               long minutes = (irc.muteRemainingMs() + 59999L) / 60000L;
               systemLine("You are muted for spam. Wait " + minutes + " min.", true);
               return true;
            case TOO_FAST:
               systemLine("Slow down - one message per second.", true);
               return true;
            case DISCONNECTED:
               systemLine("IRC is disconnected. Use .irc connect", true);
               return true;
            default:
               return false;
         }
      } else {
         return false;
      }
   }

   public static void display(IrcService.Message message) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1705 != null && message != null) {
         mc.field_1705.method_1743().method_1812(build(message));
      }
   }

   public static class_5250 build(IrcService.Message message) {
      class_5250 line = class_2561.method_43473();
      line.method_10852(colored("IRC ", -11141121));
      line.method_10852(colored("[" + message.role().displayName() + "] ", message.role().color()));
      line.method_10852(colored(message.name() + ": ", -1));
      line.method_10852(colored(message.text(), -2236963));
      return line;
   }

   public static void systemLine(String text, boolean warning) {
      class_310 mc = class_310.method_1551();
      if (mc.field_1705 != null && text != null && !text.isBlank()) {
         mc.field_1705.method_1743().method_1812(colored("IRC ", -11141121).method_10852(colored(text, warning ? -43691 : -6643546)));
      }
   }

   private static class_5250 colored(String text, int argb) {
      return class_2561.method_43470(text).method_27696(class_2583.field_24360.method_36139(argb & 16777215));
   }
}
