package org.ryzen.feature.impl.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1657;
import net.minecraft.class_1703;
import net.minecraft.class_1713;
import net.minecraft.class_1735;
import net.minecraft.class_1802;
import net.minecraft.class_465;
import net.minecraft.class_640;
import net.minecraft.class_7439;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;

@Environment(EnvType.CLIENT)
public final class AutoDuelFeature extends Feature implements MinecraftContext {
   private static final String[] KITS = new String[]{"Shield", "Spikes", "Bow", "Totems", "Heal", "Balls", "Classic", "Cheaters", "Nether"};
   private static final String KIT_SCREEN_TITLE = "выбор набора";
   private static final String DUEL_COMMAND = "duel ";
   private static final String ROUND_START = "начало";
   private static final String ROUND_IN = "через";
   private static final String ROUND_SECONDS = "секунд!";
   private static final String IN_MATCH = "дуэли » во время поединка запрещено использовать команды";
   private static final Pattern NICKNAME = Pattern.compile("^[a-zA-Z0-9_]{3,16}$");
   private static final Pattern COLOUR_CODES = Pattern.compile("§.");
   private static final long CHALLENGE_INTERVAL = 1000L;
   private static final long MATCH_BACKOFF = 1100L;
   private static final long TARGET_COOLDOWN = 1000L;
   private static final float SLOT_DELAY_SCALE = 0.05F;
   public final MultiSelectSetting kits = this.register(new MultiSelectSetting("Kits", Set.of("Classic"), KITS));
   public final BooleanSetting money = this.register(new BooleanSetting("Bet Money", false));
   public final NumberSetting moneyAmount = this.register(new NumberSetting("Bet Amount", 50.0, 0.0, 500.0, 50.0, "").visibleWhen(this.money::getValue));
   public final NumberSetting slotDelay = this.register(new NumberSetting("Slot Delay", 20.0, 20.0, 400.0, 20.0, " ms"));
   private final Map<String, Long> recentTargets = new HashMap<>();
   private long challengeAt;
   private long slotClickAt;
   private long kitClickAt;
   private boolean inMatch;
   private int targetRotation;

   public AutoDuelFeature() {
      super("AutoDuel", "Automatically challenges players to duels", FeatureCategory.MISC, -1);
   }

   @Override
   protected void onEnable() {
      this.inMatch = false;
      this.targetRotation = 0;
      this.recentTargets.clear();
      long now = System.currentTimeMillis();
      this.challengeAt = now;
      this.slotClickAt = now;
      this.kitClickAt = now;
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.PRE && this.inGame()) {
         if (event.getPacket() instanceof class_7439 packet) {
            String text = strip(packet.comp_763().getString());
            if (text.contains("дуэли » во время поединка запрещено использовать команды")) {
               this.inMatch = true;
               this.challengeAt = System.currentTimeMillis() + 1100L;
            } else {
               if (text.contains("начало") || text.contains("через") && text.contains("секунд!")) {
                  this.inMatch = true;
               }
            }
         }
      }
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      if (this.inGame() && this.gameMode() != null) {
         if (this.screen() instanceof class_465<?> container) {
            this.pickKit(container);
         } else if (!this.inMatch && this.screen() == null) {
            long now = System.currentTimeMillis();
            if (now >= this.challengeAt) {
               List<String> targets = this.collectTargets();
               if (!targets.isEmpty()) {
                  if (this.sendChallenge(this.rotateTargets(targets), now)) {
                     this.challengeAt = now + 1000L;
                  }
               }
            }
         }
      }
   }

   private List<String> collectTargets() {
      Set<String> names = new LinkedHashSet<>();
      if (mc.method_1562() != null) {
         for (class_640 entry : mc.method_1562().method_2880()) {
            if (entry.method_2966() != null
               && entry.method_2966().name() != null
               && (entry.method_2966().id() == null || !entry.method_2966().id().equals(this.player().method_5667()))) {
               String name = entry.method_2966().name();
               if (NICKNAME.matcher(name).matches()) {
                  names.add(name);
               }
            }
         }
      }

      for (class_1657 other : this.level().method_18456()) {
         if (other != this.player()) {
            String name = other.method_5477().getString();
            if (NICKNAME.matcher(name).matches()) {
               names.add(name);
            }
         }
      }

      return new ArrayList<>(names);
   }

   private List<String> rotateTargets(List<String> targets) {
      int size = targets.size();
      int offset = Math.floorMod(this.targetRotation, size);
      List<String> rotated = new ArrayList<>(size);

      for (int index = 0; index < size; index++) {
         rotated.add(targets.get((offset + index) % size));
      }

      this.targetRotation++;
      return rotated;
   }

   private boolean sendChallenge(List<String> targets, long now) {
      String self = this.sessionName();

      for (String target : targets) {
         if (!target.equalsIgnoreCase(self)) {
            String key = target.toLowerCase(Locale.ROOT);
            Long last = this.recentTargets.get(key);
            if (last == null || now - last >= 1000L) {
               String command = "duel " + target;
               if (this.money.getValue()) {
                  command = command + " " + this.moneyAmount.getValue().intValue();
               }

               this.player().field_3944.method_45730(command);
               this.recentTargets.put(key, now);
               return true;
            }
         }
      }

      return false;
   }

   private void pickKit(class_465<?> screen) {
      class_1703 menu = screen.method_17577();
      if (menu.field_7763 != 0) {
         long now = System.currentTimeMillis();
         long delay = (long)(this.slotDelay.getValue().floatValue() * 0.05F * 1000.0F);
         String title = strip(screen.method_25440().getString());
         if (title.contains("выбор набора")) {
            if (now >= this.kitClickAt) {
               List<Integer> enabledSlots = new ArrayList<>();

               for (int index = 0; index < KITS.length; index++) {
                  if (this.kits.isSelected(KITS[index])) {
                     enabledSlots.add(index);
                  }
               }

               if (!enabledSlots.isEmpty()) {
                  Collections.shuffle(enabledSlots);
                  int slot = (Integer)enabledSlots.getFirst();
                  if (slot < menu.field_7761.size()) {
                     this.clickSlot(menu, slot);
                     this.kitClickAt = now + delay;
                  }
               }
            }
         } else if (now >= this.slotClickAt) {
            for (class_1735 slot : menu.field_7761) {
               if (slot.method_7677().method_31574(class_1802.field_8581)) {
                  this.clickSlot(menu, slot.field_7874);
                  this.slotClickAt = now + delay;
                  return;
               }
            }
         }
      }
   }

   private void clickSlot(class_1703 menu, int slot) {
      this.gameMode().method_2906(menu.field_7763, slot, 0, class_1713.field_7790, this.player());
   }

   private String sessionName() {
      return mc.method_1548() != null && mc.method_1548().method_1676() != null ? mc.method_1548().method_1676() : this.player().method_5477().getString();
   }

   private static String strip(String text) {
      return text == null ? "" : COLOUR_CODES.matcher(text).replaceAll("").toLowerCase(Locale.ROOT);
   }
}
