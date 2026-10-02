package org.ryzen.feature.impl.pve;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;
import net.minecraft.class_1531;
import net.minecraft.class_1799;
import net.minecraft.class_2846;
import net.minecraft.class_310;
import net.minecraft.class_746;
import net.minecraft.class_2846.class_2847;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.game.AttackEvent;
import org.ryzen.event.events.game.GameTickEvent;
import org.ryzen.event.events.input.PlayerInputEvent;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketSendEvent;
import org.ryzen.feature.setting.MultiSelectSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.feature.setting.TextSetting;
import org.ryzen.pve.AutomationPriority;
import org.ryzen.pve.AutomationResource;
import org.ryzen.pve.PveFeature;
import org.ryzen.pve.mining.MineTimer;
import org.ryzen.pve.mining.MiningInventory;
import org.ryzen.pve.mining.MiningParsers;
import org.ryzen.pve.mining.MiningServerAdapter;
import org.ryzen.pve.mining.MiningServerAdapters;
import org.ryzen.pve.mining.MiningToolProfile;
import org.ryzen.pve.navigation.BaritoneNavigator;
import org.ryzen.utils.inventory.DropAllInventoryController;
import org.ryzen.utils.inventory.InventorySwap;
import org.ryzen.utils.inventory.InventoryUtil;

@Environment(EnvType.CLIENT)
public final class MineHelperFeature extends PveFeature {
   private static final String NEXT_MINE = "Next Mine";
   private static final String CLEAN_INVENTORY = "Clean Inventory";
   private static final String SAVE_PICKAXE = "Save Pickaxe";
   private static final int TIMER_SCAN_INTERVAL_TICKS = 60;
   public final MultiSelectSetting helpers = this.register(
      new MultiSelectSetting("Helpers", Set.of("Next Mine", "Clean Inventory", "Save Pickaxe"), "Next Mine", "Clean Inventory", "Save Pickaxe")
   );
   public final NumberSetting cleanupAtFreeSlots = this.register(new NumberSetting("Cleanup At Free Slots", 2.0, 1.0, 12.0, 1.0, ""));
   public final NumberSetting dropInterval = this.register(
      new NumberSetting("Drop Interval", 1.0, 1.0, 60.0, 1.0, " s").visibleWhen(() -> this.helpers.isSelected("Clean Inventory"))
   );
   public final TextSetting trashItems = this.register(
      new TextSetting("Trash Items", "cobblestone,cobbled_deepslate,dirt,gravel,andesite,diorite,granite,tuff,netherrack,deepslate,stone", 512)
   );
   private MineTimer currentTimer;
   private long tick;
   private long lastTrashActionTick;
   private boolean pickaxeProtected;
   private final Deque<Integer> trashDropSlots = new ArrayDeque<>();
   private Set<String> activeTrashItems = Set.of();
   private MineHelperFeature.TrashDropState trashDropState = MineHelperFeature.TrashDropState.IDLE;

   public MineHelperFeature() {
      super("MineHelper", "Shows mine timers, removes configured trash and protects pickaxes", -1, AutomationPriority.BACKGROUND);
   }

   @Override
   protected void onPveEnable() {
      this.resetRuntime();
   }

   @Override
   protected void onPveDisable() {
      this.cancelTrashDrop();
      this.resetRuntime();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.cancelTrashDrop();
      this.resetRuntime();
   }

   @EventTarget
   public void onTick(GameTickEvent event) {
      class_310 client = event.getClient();
      class_746 player = client.field_1724;
      this.tick++;
      if (player != null && client.field_1687 != null && client.field_1761 != null) {
         if (this.trashDropState != MineHelperFeature.TrashDropState.IDLE) {
            this.tickTrashDrop(client, player);
         }

         if (this.helpers.isSelected("Next Mine") && (this.currentTimer == null || this.tick % 60L == 0L)) {
            this.updateMineTimer(client);
         }

         if (this.currentTimer != null && this.currentTimer.isExpired(System.currentTimeMillis())) {
            this.currentTimer = null;
         }

         this.pickaxeProtected = this.helpers.isSelected("Save Pickaxe") && this.shouldProtect(player.method_6047());
         if (this.pickaxeProtected) {
            client.field_1761.method_2925();
         }

         if (this.trashDropState == MineHelperFeature.TrashDropState.IDLE
            && this.helpers.isSelected("Clean Inventory")
            && MiningInventory.freeSlots(player) <= this.cleanupAtFreeSlots.getValue().intValue()
            && this.tick - this.lastTrashActionTick >= this.dropIntervalTicks()) {
            this.tryClearTrash(client, player);
         }
      } else {
         this.cancelTrashDrop();
         this.pickaxeProtected = false;
      }
   }

   @EventTarget
   public void onPlayerInput(PlayerInputEvent event) {
      if (this.trashDropState != MineHelperFeature.TrashDropState.IDLE) {
         event.clearMovement(false, true);
      }
   }

   @EventTarget
   public void onAttack(AttackEvent event) {
      class_746 player = event.getClient().field_1724;
      if (this.helpers.isSelected("Save Pickaxe") && player != null && this.shouldProtect(player.method_6047())) {
         this.pickaxeProtected = true;
         event.cancel();
         if (event.getClient().field_1761 != null) {
            event.getClient().field_1761.method_2925();
         }
      }
   }

   @EventTarget
   public void onPacketSend(PacketSendEvent event) {
      if (event.getPhase() == PacketSendEvent.Phase.PRE && this.helpers.isSelected("Save Pickaxe") && event.getPacket() instanceof class_2846 packet) {
         class_746 player = class_310.method_1551().field_1724;
         if (player != null && this.shouldProtect(player.method_6047())) {
            class_2847 action = packet.method_12363();
            if (action == class_2847.field_12968 || action == class_2847.field_12973) {
               this.pickaxeProtected = true;
               event.cancel();
            }
         }
      }
   }

   public Optional<MineTimer> getCurrentTimer() {
      return Optional.ofNullable(this.currentTimer);
   }

   public boolean isMineTimerSelected() {
      return this.helpers.isSelected("Next Mine");
   }

   public boolean isPickaxeProtected() {
      return this.pickaxeProtected;
   }

   public boolean tryClearTrash() {
      class_310 client = class_310.method_1551();
      class_746 player = client.field_1724;
      return player != null && this.tryClearTrash(client, player);
   }

   private void updateMineTimer(class_310 client) {
      MiningServerAdapter adapter = MiningServerAdapters.forProfile(PveManagerFeature.INSTANCE.resolveServerProfile(client));
      List<String> lines = this.hologramLines(client);
      adapter.parseMineTimer(lines, System.currentTimeMillis()).ifPresent(timer -> this.currentTimer = timer);
   }

   private List<String> hologramLines(class_310 client) {
      if (client.field_1687 == null) {
         return List.of();
      }

      List<class_1531> stands = new ArrayList<>();

      for (class_1297 entity : client.field_1687.method_18112()) {
         if (entity instanceof class_1531 stand && stand.method_16914() && stand.method_5797() != null) {
            stands.add(stand);
         }
      }

      stands.sort(Comparator.<class_1531>comparingDouble(standx -> standx.method_23318()).reversed());
      return stands.stream().map(standx -> standx.method_5797().getString()).toList();
   }

   private boolean tryClearTrash(class_310 client, class_746 player) {
      if (player.field_7512 == player.field_7498
         && player.field_7498.method_34255().method_7960()
         && client.field_1755 == null
         && this.trashDropState == MineHelperFeature.TrashDropState.IDLE
         && !InventorySwap.isBusy()
         && !DropAllInventoryController.blocksInventoryOperations()
         && this.claim(AutomationResource.INVENTORY)) {
         this.activeTrashItems = MiningParsers.identifiers(this.trashItems.getValue());
         this.trashDropSlots.clear();

         for (int slot = 9; slot < 45; slot++) {
            if (MiningInventory.isTrash(player.field_7498.method_7611(slot).method_7677(), this.activeTrashItems)) {
               this.trashDropSlots.addLast(slot);
            }
         }

         if (MiningInventory.isTrash(player.field_7498.method_7611(45).method_7677(), this.activeTrashItems)) {
            this.trashDropSlots.addLast(45);
         }

         if (this.trashDropSlots.isEmpty()) {
            this.lastTrashActionTick = this.tick;
            this.release(AutomationResource.INVENTORY);
            return false;
         } else {
            BaritoneNavigator.INSTANCE.cancel();
            client.field_1761.method_2925();
            this.trashDropState = MineHelperFeature.TrashDropState.PREPARING;
            return true;
         }
      } else {
         return false;
      }
   }

   private void tickTrashDrop(class_310 client, class_746 player) {
      if (player.field_7512 == player.field_7498 && player.field_7498.method_34255().method_7960() && client.field_1755 == null) {
         client.field_1761.method_2925();
         switch (this.trashDropState) {
            case PREPARING:
               this.trashDropState = MineHelperFeature.TrashDropState.DROPPING;
               break;
            case DROPPING:
               while (!this.trashDropSlots.isEmpty()) {
                  int slot = this.trashDropSlots.removeFirst();
                  if (player.field_7498.method_40442(slot) && MiningInventory.isTrash(player.field_7498.method_7611(slot).method_7677(), this.activeTrashItems)
                     )
                   {
                     InventoryUtil.dropPlayerStack(player, slot);
                     return;
                  }
               }

               this.trashDropState = MineHelperFeature.TrashDropState.SETTLING;
               break;
            case SETTLING:
               this.finishTrashDrop();
         }
      } else {
         this.cancelTrashDrop();
      }
   }

   private void finishTrashDrop() {
      if (this.trashDropState != MineHelperFeature.TrashDropState.IDLE) {
         this.trashDropSlots.clear();
         this.activeTrashItems = Set.of();
         this.trashDropState = MineHelperFeature.TrashDropState.IDLE;
         this.lastTrashActionTick = this.tick;
         this.release(AutomationResource.INVENTORY);
      }
   }

   private void cancelTrashDrop() {
      this.finishTrashDrop();
   }

   private boolean shouldProtect(class_1799 stack) {
      MiningToolProfile profile = MiningToolProfile.detect(PveManagerFeature.INSTANCE.resolveServerProfile(class_310.method_1551()), stack);
      return MiningInventory.isPickaxe(stack)
         && stack.method_7963()
         && MiningInventory.remainingDurability(stack) <= profile.durabilityReserve(stack, PveManagerFeature.INSTANCE.minimumToolDurability.getValue());
   }

   private long dropIntervalTicks() {
      return Math.max(20L, Math.round(this.dropInterval.getValue() * 20.0));
   }

   private void resetRuntime() {
      this.currentTimer = null;
      this.tick = 0L;
      this.lastTrashActionTick = -4611686018427387904L;
      this.pickaxeProtected = false;
      this.trashDropSlots.clear();
      this.activeTrashItems = Set.of();
      this.trashDropState = MineHelperFeature.TrashDropState.IDLE;
   }

   @Environment(EnvType.CLIENT)
   private enum TrashDropState {
      IDLE,
      PREPARING,
      DROPPING,
      SETTLING;
   }
}
