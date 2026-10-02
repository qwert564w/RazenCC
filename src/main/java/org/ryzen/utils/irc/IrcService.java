package org.ryzen.utils.irc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;

@Environment(EnvType.CLIENT)
public final class IrcService {
   public static final IrcService INSTANCE = new IrcService();
   private static final Pattern ROLE_PREFIX = Pattern.compile("^\\[([A-Za-z]{1,16})]\\s?(.*)$");
   private static final int MAX_HISTORY = 200;
   private static final int SPAM_REPEAT_LIMIT = 3;
   private static final long MUTE_DURATION_MS = 600000L;
   private static final long SEND_COOLDOWN_MS = 900L;
   private final List<IrcService.Message> history = Collections.synchronizedList(new ArrayList<>());
   private final Map<String, IrcService.OnlineUser> online = new LinkedHashMap<>();
   private volatile boolean chatMode;
   private String lastOwnMessage = "";
   private int repeatCount;
   private long mutedUntil;
   private long lastSendTime;

   private IrcService() {
   }

   public void initialize() {
      IrcConfig.INSTANCE.load();
      if (IrcConfig.INSTANCE.isAutoConnect()) {
         IrcConnection.INSTANCE.connect();
      }
   }

   public boolean isConnected() {
      return IrcConnection.INSTANCE.isConnected();
   }

   public void setConnected(boolean connected) {
      if (connected) {
         IrcConnection.INSTANCE.connect();
      } else {
         IrcConnection.INSTANCE.disconnect();
         this.clearRoster();
      }
   }

   public boolean isChatMode() {
      return this.chatMode;
   }

   public void setChatMode(boolean chatMode) {
      this.chatMode = chatMode;
   }

   public long muteRemainingMs() {
      long left = this.mutedUntil - System.currentTimeMillis();
      return Math.max(0L, left);
   }

   public boolean isMuted() {
      return this.muteRemainingMs() > 0L;
   }

   public synchronized IrcService.SendResult send(String message) {
      if (message == null || message.isBlank()) {
         return IrcService.SendResult.EMPTY;
      }

      if (!IrcConnection.INSTANCE.isConnected()) {
         return IrcService.SendResult.DISCONNECTED;
      }

      if (this.isMuted()) {
         return IrcService.SendResult.MUTED;
      }

      String trimmed = message.trim();
      if (this.registerSpam(trimmed)) {
         return IrcService.SendResult.MUTED;
      }

      long now = System.currentTimeMillis();
      if (now - this.lastSendTime < 900L) {
         return IrcService.SendResult.TOO_FAST;
      }

      if (!IrcConnection.INSTANCE.sendMessage(IrcProfile.role(), trimmed)) {
         return IrcService.SendResult.DISCONNECTED;
      }

      this.lastSendTime = now;
      String nick = IrcConnection.INSTANCE.nick();
      this.record(nick.isEmpty() ? IrcProfile.name() : nick, IrcProfile.role(), trimmed);
      return IrcService.SendResult.SENT;
   }

   private boolean registerSpam(String message) {
      if (message.equalsIgnoreCase(this.lastOwnMessage)) {
         this.repeatCount++;
      } else {
         this.lastOwnMessage = message;
         this.repeatCount = 1;
      }

      if (this.repeatCount >= 3) {
         this.mutedUntil = System.currentTimeMillis() + 600000L;
         this.repeatCount = 0;
         this.lastOwnMessage = "";
         return true;
      } else {
         return false;
      }
   }

   public void onConnected(String nick, String channel) {
      this.notice("Connected as " + nick + " in " + channel, false);
   }

   public void onConnectionLost(String reason) {
      this.clearRoster();
      this.notice("Disconnected" + (reason != null && !reason.isBlank() ? ": " + reason : "") + " - reconnecting", true);
   }

   public void onServerNotice(String text) {
      this.notice(text != null && !text.isBlank() ? text : "Server refused the channel", true);
   }

   public void onRoster(List<String> nicks) {
      long now = System.currentTimeMillis();
      synchronized (this.online) {
         Map<String, IrcService.OnlineUser> previous = new LinkedHashMap<>(this.online);
         this.online.clear();

         for (String nick : nicks) {
            String key = nick.toLowerCase(Locale.ROOT);
            IrcService.OnlineUser known = previous.get(key);
            this.online.put(key, known != null ? new IrcService.OnlineUser(nick, known.role(), now) : new IrcService.OnlineUser(nick, IrcRole.MEMBER, now));
         }
      }
   }

   public void onUserJoined(String nick) {
      this.touch(nick, null);
   }

   public void onUserLeft(String nick) {
      synchronized (this.online) {
         this.online.remove(nick.toLowerCase(Locale.ROOT));
      }
   }

   public void onUserRenamed(String from, String to) {
      synchronized (this.online) {
         IrcService.OnlineUser user = this.online.remove(from.toLowerCase(Locale.ROOT));
         IrcRole role = user == null ? IrcRole.MEMBER : user.role();
         this.online.put(to.toLowerCase(Locale.ROOT), new IrcService.OnlineUser(to, role, System.currentTimeMillis()));
      }
   }

   public void onChannelMessage(String nick, String rawText) {
      IrcRole role = IrcRole.MEMBER;
      String text = rawText;
      Matcher matcher = ROLE_PREFIX.matcher(rawText);
      if (matcher.matches()) {
         role = IrcRole.fromName(matcher.group(1));
         text = matcher.group(2);
      }

      if (!text.isBlank()) {
         IrcService.Message message = this.record(nick, role, text);
         class_310.method_1551().execute(() -> IrcChatRouter.display(message));
      }
   }

   private void notice(String text, boolean warning) {
      class_310.method_1551().execute(() -> IrcChatRouter.systemLine(text, warning));
   }

   private IrcService.Message record(String name, IrcRole role, String text) {
      long now = System.currentTimeMillis();
      IrcService.Message message = new IrcService.Message(name, role, text, now);
      synchronized (this.history) {
         this.history.add(message);

         while (this.history.size() > 200) {
            this.history.remove(0);
         }
      }

      this.touch(name, role);
      return message;
   }

   private void touch(String name, IrcRole role) {
      long now = System.currentTimeMillis();
      String key = name.toLowerCase(Locale.ROOT);
      synchronized (this.online) {
         IrcService.OnlineUser known = this.online.get(key);
         IrcRole resolved = role != null ? role : (known == null ? IrcRole.MEMBER : known.role());
         this.online.put(key, new IrcService.OnlineUser(name, resolved, now));
      }
   }

   private void clearRoster() {
      synchronized (this.online) {
         this.online.clear();
      }
   }

   public List<IrcService.Message> history() {
      synchronized (this.history) {
         return new ArrayList<>(this.history);
      }
   }

   public List<IrcService.OnlineUser> onlineUsers() {
      synchronized (this.online) {
         List<IrcService.OnlineUser> list = new ArrayList<>(this.online.values());
         list.sort((a, b) -> Integer.compare(b.role().priority(), a.role().priority()));
         return list;
      }
   }

   public void clear() {
      synchronized (this.history) {
         this.history.clear();
      }

      this.clearRoster();
   }

   @Environment(EnvType.CLIENT)
   public record Message(String name, IrcRole role, String text, long time) {
   }

   @Environment(EnvType.CLIENT)
   public record OnlineUser(String name, IrcRole role, long lastSeen) {
   }

   @Environment(EnvType.CLIENT)
   public enum SendResult {
      SENT,
      EMPTY,
      DISCONNECTED,
      MUTED,
      TOO_FAST;
   }
}
