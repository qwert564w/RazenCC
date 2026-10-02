package org.ryzen.feature.impl.misc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureEnableRejectedException;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class FunDeliverFeature extends Feature {
   private static final Pattern NICKNAME = Pattern.compile("[A-Za-z0-9_]{3,16}");
   public final TextSetting goldenKey = this.register(new TextSetting("Golden Key", "", 256).secret());
   public final NumberSetting triggerPrice = this.register(new NumberSetting("Unit Price", 1.0, 0.5, 10.0, 0.01, ""));
   public final NumberSetting minimumMillions = this.register(new NumberSetting("Minimum Order", 10.0, 1.0, 50.0, 1.0, " kk"));
   private final ConcurrentLinkedQueue<FunDeliverFeature.Delivery> deliveries = new ConcurrentLinkedQueue<>();
   private volatile ScheduledExecutorService worker;
   private volatile FunDeliverFeature.FunPaySession session;

   public FunDeliverFeature() {
      super("FunDeliver", "Automatically delivers FunPay currency orders", FeatureCategory.MISC, -1);
      this.renamedFrom("Fun Deliver");
   }

   @Override
   protected void onEnable() {
      if (this.goldenKey.getValue().isBlank()) {
         throw new FeatureEnableRejectedException("Golden Key is empty");
      }

      ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
         Thread thread = new Thread(runnable, "Ryzen Fun Deliver");
         thread.setDaemon(true);
         return thread;
      });
      this.worker = executor;
      executor.execute(this::connect);
   }

   @Override
   protected void onDisable() {
      ScheduledExecutorService executor = this.worker;
      this.worker = null;
      if (executor != null) {
         executor.shutdownNow();
      }

      this.session = null;
      this.deliveries.clear();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (event.getClient().field_1724 != null && event.getClient().field_1724.field_3944 != null) {
         FunDeliverFeature.Delivery delivery = this.deliveries.poll();
         if (delivery != null) {
            long amount = Math.max(1L, delivery.millions()) * 1000000L;
            event.getClient().field_1724.field_3944.method_45730("pay " + delivery.nickname() + " " + amount);
            FunDeliverFeature.FunPaySession current = this.session;
            if (current != null) {
               ScheduledExecutorService executor = this.worker;
               if (executor != null && !executor.isShutdown()) {
                  executor.execute(() -> current.sendMessage(delivery.roomId(), "✅ Валюта отправлена на ник " + delivery.nickname() + ". Спасибо за покупку!"));
               }
            }

            ChatUtil.success("Fun Deliver: sent " + delivery.millions() + "кк to " + delivery.nickname());
         }
      }
   }

   private void connect() {
      try {
         FunDeliverFeature.FunPaySession connected = new FunDeliverFeature.FunPaySession(this.goldenKey.getValue());
         if (!connected.login()) {
            this.fail("FunPay rejected the Golden Key");
            return;
         }

         this.session = connected;
         class_310.method_1551().execute(() -> ChatUtil.success("Fun Deliver connected as " + connected.username));
         ScheduledExecutorService executor = this.worker;
         if (executor != null && !executor.isShutdown()) {
            executor.scheduleWithFixedDelay(this::pollSafely, 0L, 3L, TimeUnit.SECONDS);
         }
      } catch (Throwable throwable) {
         this.fail("FunPay connection failed: " + throwable.getClass().getSimpleName());
      }
   }

   private void pollSafely() {
      FunDeliverFeature.FunPaySession current = this.session;
      if (current != null && this.isEnabled()) {
         try {
            current.pollOrders(this.triggerPrice.getValue(), this.minimumMillions.getValue().intValue());
            current.pollChats(this.deliveries);
         } catch (Throwable var3) {
         }
      }
   }

   private void fail(String message) {
      class_310.method_1551().execute(() -> {
         ChatUtil.error(message);
         if (this.isEnabled()) {
            this.setEnabled(false);
         }
      });
   }

   @Environment(EnvType.CLIENT)
   private record Delivery(String roomId, String nickname, int millions) {
   }

   @Environment(EnvType.CLIENT)
   private static final class FunPaySession {
      private final String goldenKey;
      private final Map<String, String> cookies = new HashMap<>();
      private final Map<String, FunDeliverFeature.RoomState> rooms = new HashMap<>();
      private final Map<String, FunDeliverFeature.Order> ordersByBuyer = new HashMap<>();
      private final Set<String> seenOrders = new HashSet<>();
      private String userId;
      private String csrf;
      private String username;
      private String bookmarkTag = "00000000";

      private FunPaySession(String goldenKey) {
         this.goldenKey = goldenKey;
      }

      private boolean login() throws Exception {
         Response response = Jsoup.connect("https://funpay.com/").cookie("golden_key", this.goldenKey).timeout(7500).execute();
         this.cookies.putAll(response.cookies());
         this.cookies.put("golden_key", this.goldenKey);
         Document document = response.parse();
         Element user = document.selectFirst(".user-link-name");
         Element appData = document.selectFirst("[data-app-data]");
         if (user != null && appData != null) {
            JsonObject data = JsonParser.parseString(appData.attr("data-app-data")).getAsJsonObject();
            this.userId = data.get("userId").getAsString();
            this.csrf = data.get("csrf-token").getAsString();
            this.username = user.text();
            return true;
         } else {
            return false;
         }
      }

      private void pollOrders(double expectedUnitPrice, int minimumMillions) throws Exception {
         Document document = this.post("https://funpay.com/orders/trade", Map.of()).parse();
         Map<String, FunDeliverFeature.Order> active = new HashMap<>();

         for (Element row : document.select(".tc-item")) {
            Element status = row.selectFirst(".tc-status");
            Element category = row.selectFirst(".order-desc .text-muted");
            Element buyer = row.selectFirst(".media-user-name > span");
            Element id = row.selectFirst(".tc-order");
            Element description = row.selectFirst(".order-desc > div");
            Element price = row.selectFirst(".tc-price");
            if (status != null
               && category != null
               && buyer != null
               && id != null
               && description != null
               && price != null
               && status.text().equalsIgnoreCase("Оплачен")
               && category.text().toLowerCase(Locale.ROOT).contains("валют")) {
               int count = parseCount(description.text());

               double paid;
               try {
                  paid = Double.parseDouble(price.ownText().replace(',', '.').replaceAll("[^0-9.]", ""));
               } catch (NumberFormatException ignored) {
                  continue;
               }

               if (!(Math.abs(paid / Math.max(1, count) - expectedUnitPrice) > 0.011)) {
                  FunDeliverFeature.Order order = new FunDeliverFeature.Order(id.text(), buyer.text(), count, minimumMillions);
                  active.put(order.buyer().toLowerCase(Locale.ROOT), order);
                  if (this.seenOrders.add(order.id())) {
                     this.rooms
                        .values()
                        .stream()
                        .filter(room -> room.buyer().equalsIgnoreCase(order.buyer()))
                        .findFirst()
                        .ifPresent(room -> this.beginOrder(room, order));
                  }
               }
            }
         }

         this.ordersByBuyer.clear();
         this.ordersByBuyer.putAll(active);
      }

      private void pollChats(ConcurrentLinkedQueue<FunDeliverFeature.Delivery> deliveries) throws Exception {
         JsonObject bookmark = new JsonObject();
         bookmark.addProperty("type", "chat_bookmarks");
         bookmark.addProperty("id", this.userId);
         bookmark.addProperty("tag", this.bookmarkTag);
         bookmark.addProperty("data", false);
         JsonArray objects = new JsonArray();
         objects.add(bookmark);
         JsonObject response = JsonParser.parseString(this.runner(objects.toString(), "false")).getAsJsonObject();

         for (JsonElement element : response.getAsJsonArray("objects")) {
            JsonObject object = element.getAsJsonObject();
            if ("chat_bookmarks".equalsIgnoreCase(object.get("type").getAsString())) {
               this.bookmarkTag = object.get("tag").getAsString();
               Document contacts = Jsoup.parse(object.getAsJsonObject("data").get("html").getAsString());

               for (Element contact : contacts.select(".contact-item")) {
                  String roomId = contact.attr("data-id");
                  long lastId = parseLong(contact.attr("data-node-msg"));
                  String buyer = contact.select(".media-user-name").text();
                  FunDeliverFeature.RoomState room = this.rooms.computeIfAbsent(roomId, ignored -> new FunDeliverFeature.RoomState(roomId, buyer, lastId));
                  FunDeliverFeature.Order order = this.ordersByBuyer.get(buyer.toLowerCase(Locale.ROOT));
                  if (order != null && room.stage == FunDeliverFeature.Stage.IDLE && this.seenOrders.contains(order.id())) {
                     this.beginOrder(room, order);
                  }

                  if (lastId > room.lastMessageId) {
                     this.pollHistory(room, order, deliveries);
                     room.lastMessageId = lastId;
                  }
               }
            }
         }
      }

      private void beginOrder(FunDeliverFeature.RoomState room, FunDeliverFeature.Order order) {
         if (order.millions() < order.minimum()) {
            this.sendMessage(room.roomId(), "❌ Минимальная сумма заказа — " + order.minimum() + "кк.");
            room.stage = FunDeliverFeature.Stage.IDLE;
         } else {
            room.order = order;
            room.stage = FunDeliverFeature.Stage.NICKNAME;
            this.sendMessage(room.roomId(), "\ud83e\uddf8 Укажите ваш игровой никнейм. Для отмены напишите «Отмена».");
         }
      }

      private void pollHistory(FunDeliverFeature.RoomState room, FunDeliverFeature.Order order, ConcurrentLinkedQueue<FunDeliverFeature.Delivery> deliveries) throws Exception {
         JsonObject history = JsonParser.parseString(this.chatNode(room.roomId())).getAsJsonObject();

         for (JsonElement objectElement : history.getAsJsonArray("objects")) {
            JsonObject object = objectElement.getAsJsonObject();
            if ("chat_node".equalsIgnoreCase(object.get("type").getAsString())) {
               for (JsonElement messageElement : object.getAsJsonObject("data").getAsJsonArray("messages")) {
                  JsonObject message = messageElement.getAsJsonObject();
                  long id = message.get("id").getAsLong();
                  if (id > room.lastProcessedId && !message.get("author").getAsString().equals(this.userId) && !message.get("author").getAsString().equals("0")
                     )
                   {
                     room.lastProcessedId = id;
                     String content = Jsoup.parse(message.get("html").getAsString()).select(".chat-msg-text").text();
                     this.handleMessage(room, order, content, deliveries);
                  }
               }
            }
         }
      }

      private void handleMessage(
         FunDeliverFeature.RoomState room, FunDeliverFeature.Order currentOrder, String content, ConcurrentLinkedQueue<FunDeliverFeature.Delivery> deliveries
      ) {
         String answer = content.replaceAll("[^A-Za-zА-Яа-я+]", "").toLowerCase(Locale.ROOT);
         if (!answer.equals("отмена") && !answer.equals("otmena")) {
            FunDeliverFeature.Order order = currentOrder != null ? currentOrder : room.order;
            if (order != null) {
               if (room.stage == FunDeliverFeature.Stage.NICKNAME) {
                  Matcher matcher = FunDeliverFeature.NICKNAME.matcher(content);
                  if (!matcher.find()) {
                     this.sendMessage(room.roomId(), "❌ Ник должен содержать 3–16 латинских букв, цифр или _. Попробуйте ещё раз.");
                  } else {
                     room.nickname = matcher.group();
                     room.stage = FunDeliverFeature.Stage.CONFIRM;
                     this.sendMessage(
                        room.roomId(),
                        "\ud83d\udcdd Ник: " + room.nickname + "\n\ud83d\udce6 Количество: " + order.millions() + "кк\n\nВсё верно? Напишите «Да» или «Нет»."
                     );
                  }
               } else {
                  if (room.stage == FunDeliverFeature.Stage.CONFIRM) {
                     if (answer.equals("нет") || answer.equals("net")) {
                        room.stage = FunDeliverFeature.Stage.NICKNAME;
                        this.sendMessage(room.roomId(), "\ud83e\uddf8 Укажите правильный никнейм:");
                     } else if (answer.equals("да") || answer.equals("da") || answer.equals("+")) {
                        room.stage = FunDeliverFeature.Stage.DELIVERED;
                        deliveries.add(new FunDeliverFeature.Delivery(room.roomId(), room.nickname, order.millions()));
                        this.sendMessage(room.roomId(), "✅ Данные подтверждены. Валюта будет отправлена в игре.");
                     }
                  }
               }
            }
         } else {
            room.stage = FunDeliverFeature.Stage.IDLE;
            this.sendMessage(room.roomId(), "↩️ Выдача отменена. Для возврата обратитесь к продавцу.");
         }
      }

      private String chatNode(String roomId) throws Exception {
         JsonObject object = new JsonObject();
         object.addProperty("type", "chat_node");
         object.addProperty("id", roomId);
         object.addProperty("tag", "00000000");
         JsonObject data = new JsonObject();
         data.addProperty("node", roomId);
         data.addProperty("last_message", -1);
         data.addProperty("content", "");
         object.add("data", data);
         JsonArray objects = new JsonArray();
         objects.add(object);
         return this.runner(objects.toString(), "false");
      }

      private void sendMessage(String roomId, String content) {
         try {
            JsonObject request = new JsonObject();
            request.addProperty("action", "chat_message");
            JsonObject data = new JsonObject();
            data.addProperty("node", roomId);
            data.addProperty("last_message", -1);
            data.addProperty("content", content);
            request.add("data", data);
            JsonObject object = new JsonObject();
            object.addProperty("type", "chat_node");
            object.addProperty("id", roomId);
            object.addProperty("tag", "00000000");
            JsonObject nodeData = new JsonObject();
            nodeData.addProperty("node", roomId);
            nodeData.addProperty("last_message", -1);
            nodeData.addProperty("content", "");
            object.add("data", nodeData);
            JsonArray objects = new JsonArray();
            objects.add(object);
            this.runner(objects.toString(), request.toString());
         } catch (Throwable var8) {
         }
      }

      private String runner(String objects, String request) throws Exception {
         return this.post("https://funpay.com/runner/", Map.of("objects", objects, "request", request, "csrf_token", this.csrf == null ? "" : this.csrf))
            .body();
      }

      private Response post(String url, Map<String, String> data) throws Exception {
         Connection connection = Jsoup.connect(url)
            .cookies(this.cookies)
            .header("X-Requested-With", "XMLHttpRequest")
            .method(Method.POST)
            .ignoreContentType(true)
            .timeout(7500);
         if (!data.isEmpty()) {
            connection.data(data);
         }

         return connection.execute();
      }

      private static int parseCount(String description) {
         Matcher matcher = Pattern.compile("(?iu)(\\d+)\\s*шт").matcher(description);
         return matcher.find() ? Math.max(1, Integer.parseInt(matcher.group(1))) : 1;
      }

      private static long parseLong(String value) {
         try {
            return Long.parseLong(value);
         } catch (NumberFormatException ignored) {
            return 0L;
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private record Order(String id, String buyer, int millions, int minimum) {
   }

   @Environment(EnvType.CLIENT)
   private static final class RoomState {
      private final String roomId;
      private final String buyer;
      private long lastMessageId;
      private long lastProcessedId;
      private FunDeliverFeature.Stage stage = FunDeliverFeature.Stage.IDLE;
      private String nickname = "";
      private FunDeliverFeature.Order order;

      private RoomState(String roomId, String buyer, long lastMessageId) {
         this.roomId = roomId;
         this.buyer = buyer;
         this.lastMessageId = lastMessageId;
         this.lastProcessedId = lastMessageId;
      }

      private String roomId() {
         return this.roomId;
      }

      private String buyer() {
         return this.buyer;
      }
   }

   @Environment(EnvType.CLIENT)
   private enum Stage {
      IDLE,
      NICKNAME,
      CONFIRM,
      DELIVERED;
   }
}
