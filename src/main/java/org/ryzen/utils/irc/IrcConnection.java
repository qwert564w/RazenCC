package org.ryzen.utils.irc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(EnvType.CLIENT)
public final class IrcConnection {
   public static final IrcConnection INSTANCE = new IrcConnection();
   private static final Logger LOGGER = LoggerFactory.getLogger(IrcConnection.class);
   private static final int CONNECT_TIMEOUT_MS = 10000;
   private static final int READ_TIMEOUT_MS = 300000;
   private static final long RECONNECT_MIN_MS = 5000L;
   private static final long RECONNECT_MAX_MS = 60000L;
   private static final int MAX_TEXT_LENGTH = 380;
   private static final String REAL_NAME = "Ryzen";
   private static final char CTCP_MARKER = '\u0001';
   private static final char NUL = '\u0000';
   private final Object writeLock = new Object();
   private volatile Thread worker;
   private volatile boolean wantConnected;
   private volatile boolean registered;
   private volatile Socket socket;
   private volatile BufferedWriter writer;
   private volatile String nick = "";
   private volatile String joinedChannel = "";
   private final List<String> pendingNames = new ArrayList<>();

   private IrcConnection() {
   }

   public boolean isConnected() {
      return this.registered;
   }

   public String nick() {
      return this.nick;
   }

   public synchronized void connect() {
      this.wantConnected = true;
      if (this.worker == null || !this.worker.isAlive()) {
         Thread thread = new Thread(this::runLoop, "Ryzen-IRC");
         thread.setDaemon(true);
         this.worker = thread;
         thread.start();
      }
   }

   public synchronized void disconnect() {
      this.wantConnected = false;
      this.writeLine("QUIT :Ryzen");
      this.closeSocket();
      Thread thread = this.worker;
      if (thread != null) {
         thread.interrupt();
      }

      this.worker = null;
   }

   public void reconnect() {
      this.closeSocket();
      this.connect();
   }

   public boolean sendMessage(IrcRole role, String text) {
      if (this.registered && text != null && !text.isBlank()) {
         String channel = this.joinedChannel;
         if (channel.isEmpty()) {
            return false;
         }

         String body = sanitize(text);
         return body.isEmpty() ? false : this.writeLine("PRIVMSG " + channel + " :[" + role.displayName() + "] " + body);
      } else {
         return false;
      }
   }

   private void runLoop() {
      long backoff = 5000L;

      while (this.wantConnected && this.worker == Thread.currentThread()) {
         String reason = null;

         try {
            this.runSession();
         } catch (IOException exception) {
            reason = exception.getMessage();
         } catch (RuntimeException exception) {
            LOGGER.error("IRC session failed", exception);
            reason = exception.getMessage();
         }

         boolean wasRegistered = this.registered;
         this.registered = false;
         this.joinedChannel = "";
         this.closeSocket();
         if (!this.wantConnected || this.worker != Thread.currentThread()) {
            break;
         }

         IrcService.INSTANCE.onConnectionLost(reason);
         backoff = wasRegistered ? 5000L : Math.min(60000L, backoff * 2L);

         try {
            Thread.sleep(backoff);
         } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            break;
         }
      }

      this.registered = false;
   }

   private void runSession() throws IOException {
      IrcConfig config = IrcConfig.INSTANCE;
      String host = config.getHost();
      int port = config.getPort();
      boolean tls = config.isTls();
      Socket raw = new Socket();
      this.socket = raw;
      raw.connect(new InetSocketAddress(host, port), 10000);
      raw.setSoTimeout(300000);
      Socket active = raw;
      if (tls) {
         SSLSocketFactory factory = (SSLSocketFactory)SSLSocketFactory.getDefault();
         SSLSocket secure = (SSLSocket)factory.createSocket(raw, host, port, true);
         secure.startHandshake();
         active = secure;
      }

      this.socket = active;
      BufferedReader reader = new BufferedReader(new InputStreamReader(active.getInputStream(), StandardCharsets.UTF_8));
      synchronized (this.writeLock) {
         this.writer = new BufferedWriter(new OutputStreamWriter(active.getOutputStream(), StandardCharsets.UTF_8));
      }

      this.nick = IrcConnection.IrcNames.toNick(IrcProfile.name());
      this.writeLine("NICK " + this.nick);
      this.writeLine("USER " + this.nick + " 0 * :Ryzen");

      String line;
      while (this.wantConnected && (line = this.readLine(reader)) != null) {
         this.handleLine(line);
      }
   }

   private String readLine(BufferedReader reader) throws IOException {
      try {
         return reader.readLine();
      } catch (SocketTimeoutException timeout) {
         return null;
      }
   }

   private void closeSocket() {
      Socket current = this.socket;
      this.socket = null;
      synchronized (this.writeLock) {
         this.writer = null;
      }

      if (current != null) {
         try {
            current.close();
         } catch (IOException var4) {
         }
      }
   }

   private boolean writeLine(String line) {
      synchronized (this.writeLock) {
         BufferedWriter target = this.writer;
         if (target == null) {
            return false;
         }

         boolean var10000;
         try {
            target.write(line);
            target.write("\r\n");
            target.flush();
            var10000 = true;
         } catch (IOException exception) {
            return false;
         }

         return var10000;
      }
   }

   private void handleLine(String raw) {
      IrcConnection.IrcMessage message = IrcConnection.IrcMessage.parse(raw);
      if (message != null) {
         switch (message.command()) {
            case "PING":
               this.writeLine("PONG :" + message.lastParam());
               break;
            case "001":
               this.onRegistered();
               break;
            case "433":
            case "436":
               this.onNickTaken();
               break;
            case "353":
               this.collectNames(message.lastParam());
               break;
            case "366":
               this.publishNames();
               break;
            case "JOIN":
               this.onJoin(message);
               break;
            case "PART":
            case "QUIT":
            case "KICK":
               this.onLeave(message);
               break;
            case "NICK":
               this.onNickChange(message);
               break;
            case "PRIVMSG":
               this.onPrivMsg(message);
               break;
            case "ERROR":
               IrcService.INSTANCE.onConnectionLost(message.lastParam());
               break;
            case "464":
            case "465":
            case "471":
            case "473":
            case "474":
            case "475":
               IrcService.INSTANCE.onServerNotice(message.lastParam());
         }
      }
   }

   private void onRegistered() {
      this.registered = true;
      String channel = IrcConfig.normalizeChannel(IrcConfig.INSTANCE.getChannel());
      this.joinedChannel = channel;
      this.writeLine("JOIN " + channel);
      IrcService.INSTANCE.onConnected(this.nick, channel);
   }

   private void onNickTaken() {
      this.nick = IrcConnection.IrcNames.nextCandidate(this.nick);
      this.writeLine("NICK " + this.nick);
   }

   private void collectNames(String names) {
      if (names != null) {
         synchronized (this.pendingNames) {
            for (String entry : names.split(" ")) {
               String cleaned = IrcConnection.IrcNames.stripModePrefix(entry);
               if (!cleaned.isEmpty()) {
                  this.pendingNames.add(cleaned);
               }
            }
         }
      }
   }

   private void publishNames() {
      List<String> snapshot;
      synchronized (this.pendingNames) {
         snapshot = List.copyOf(this.pendingNames);
         this.pendingNames.clear();
      }

      IrcService.INSTANCE.onRoster(snapshot);
   }

   private void onJoin(IrcConnection.IrcMessage message) {
      String who = message.nick();
      if (!who.isEmpty()) {
         if (who.equalsIgnoreCase(this.nick)) {
            this.joinedChannel = message.paramOrTrailing(1);
         } else {
            IrcService.INSTANCE.onUserJoined(who);
         }
      }
   }

   private void onLeave(IrcConnection.IrcMessage message) {
      String who = "KICK".equals(message.command()) ? message.param(2) : message.nick();
      if (!who.isEmpty() && !who.equalsIgnoreCase(this.nick)) {
         IrcService.INSTANCE.onUserLeft(who);
      }
   }

   private void onNickChange(IrcConnection.IrcMessage message) {
      String from = message.nick();
      String to = message.lastParam();
      if (!from.isEmpty() && to != null && !to.isBlank()) {
         if (from.equalsIgnoreCase(this.nick)) {
            this.nick = to;
         } else {
            IrcService.INSTANCE.onUserRenamed(from, to);
         }
      }
   }

   private void onPrivMsg(IrcConnection.IrcMessage message) {
      String target = message.param(1);
      String text = message.lastParam();
      String from = message.nick();
      if (!from.isEmpty() && text != null && !text.isBlank()) {
         if (text.charAt(0) != 1 && !from.equalsIgnoreCase(this.nick)) {
            boolean channelMessage = target.startsWith("#") || target.startsWith("&");
            if (channelMessage) {
               IrcService.INSTANCE.onChannelMessage(from, text);
            }
         }
      }
   }

   private static String sanitize(String text) {
      String cleaned = text.replace('\r', ' ').replace('\n', ' ').replace('\u0000', ' ').trim();
      if (cleaned.length() > 380) {
         cleaned = cleaned.substring(0, 380);
      }

      return cleaned;
   }

   @Environment(EnvType.CLIENT)
   record IrcMessage(String prefix, String command, List<String> params, String trailing) {
      static IrcConnection.IrcMessage parse(String raw) {
         if (raw != null && !raw.isBlank()) {
            String rest = raw;
            String prefix = "";
            if (rest.charAt(0) == ':') {
               int space = rest.indexOf(32);
               if (space < 0) {
                  return null;
               }

               prefix = rest.substring(1, space);
               rest = rest.substring(space + 1);
            }

            String trailing = null;
            if (rest.startsWith(":")) {
               trailing = rest.substring(1);
               rest = "";
            } else {
               int marker = rest.indexOf(" :");
               if (marker >= 0) {
                  trailing = rest.substring(marker + 2);
                  rest = rest.substring(0, marker);
               }
            }

            List<String> params = new ArrayList<>();

            for (String token : rest.split(" ")) {
               if (!token.isEmpty()) {
                  params.add(token);
               }
            }

            return params.isEmpty() ? null : new IrcConnection.IrcMessage(prefix, params.get(0).toUpperCase(Locale.ROOT), params, trailing);
         } else {
            return null;
         }
      }

      String nick() {
         if (this.prefix.isEmpty()) {
            return "";
         }

         int bang = this.prefix.indexOf(33);
         return bang < 0 ? this.prefix : this.prefix.substring(0, bang);
      }

      String param(int index) {
         return index < this.params.size() ? this.params.get(index) : "";
      }

      String lastParam() {
         if (this.trailing != null) {
            return this.trailing;
         } else {
            return this.params.isEmpty() ? "" : this.params.get(this.params.size() - 1);
         }
      }

      String paramOrTrailing(int index) {
         String value = this.param(index);
         return value.isEmpty() ? this.lastParam() : value;
      }
   }

   @Environment(EnvType.CLIENT)
   static final class IrcNames {
      private static final String SPECIAL = "[]\\`_^{|}";
      private static final int MAX_NICK_LENGTH = 30;

      private IrcNames() {
      }

      static String toNick(String name) {
         StringBuilder builder = new StringBuilder();

         for (char character : (name == null ? "" : name).toCharArray()) {
            boolean letter = Character.isLetter(character) && character < 128;
            boolean digit = character >= '0' && character <= '9';
            if (letter || digit || "[]\\`_^{|}".indexOf(character) >= 0 || character == '-') {
               builder.append(character);
            }
         }

         if (builder.isEmpty()) {
            builder.append("Ryzen");
         }

         char first = builder.charAt(0);
         if (first >= '0' && first <= '9' || first == '-') {
            builder.insert(0, '_');
         }

         if (builder.length() > 30) {
            builder.setLength(30);
         }

         return builder.toString();
      }

      static String nextCandidate(String current) {
         String base = current;
         int suffix = 1;
         int underscore = current.lastIndexOf(95);
         if (underscore > 0 && underscore < current.length() - 1) {
            String tail = current.substring(underscore + 1);
            if (tail.chars().allMatch(Character::isDigit)) {
               base = current.substring(0, underscore);
               suffix = Integer.parseInt(tail) + 1;
            }
         }

         String candidate = base + "_" + suffix;
         if (candidate.length() > 30) {
            int overflow = candidate.length() - 30;
            base = base.substring(0, Math.max(1, base.length() - overflow));
            candidate = base + "_" + suffix;
         }

         return candidate;
      }

      static String stripModePrefix(String entry) {
         int index = 0;

         while (index < entry.length() && "~&@%+".indexOf(entry.charAt(index)) >= 0) {
            index++;
         }

         return entry.substring(index).trim();
      }
   }
}
