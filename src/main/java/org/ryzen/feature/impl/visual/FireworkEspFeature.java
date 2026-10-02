package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_243;
import net.minecraft.class_2767;
import net.minecraft.class_310;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.lifecycle.WorldLeaveEvent;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.ColorSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class FireworkEspFeature extends Feature {
   private static final long LIFETIME_MS = 3000L;
   private static final double MERGE_DISTANCE_SQR = 25.0;
   private static final float TAG_HEIGHT = 11.0F;
   private static final float ICON_SIZE = 8.0F;
   private static final float TEXT_SIZE = 7.0F;
   public final BooleanSetting showTime = this.register(new BooleanSetting("Show Time", true));
   public final NumberSetting scale = this.register(new NumberSetting("Scale", 1.0, 0.5, 2.0, 0.05, "x"));
   public final ColorSetting color = this.register(new ColorSetting("Color", 16777215));
   private final List<FireworkEspFeature.Mark> marks = new ArrayList<>();

   public FireworkEspFeature() {
      super("FireworkESP", "Tags the spot where a firework was launched", FeatureCategory.VISUAL, -1);
   }

   @Override
   protected void onDisable() {
      this.marks.clear();
   }

   @EventTarget
   public void onWorldLeave(WorldLeaveEvent event) {
      this.marks.clear();
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (event.getPhase() == PacketReceiveEvent.Phase.POST && event.getPacket() instanceof class_2767 packet) {
         if (((class_3414)packet.method_11894().comp_349()).equals(class_3417.field_14702)) {
            class_243 position = new class_243(packet.method_11890(), packet.method_11889(), packet.method_11893());
            synchronized (this.marks) {
               for (FireworkEspFeature.Mark mark : this.marks) {
                  if (mark.position.method_1025(position) <= 25.0) {
                     mark.startedAt = System.currentTimeMillis();
                     return;
                  }
               }

               this.marks.add(new FireworkEspFeature.Mark(position, System.currentTimeMillis()));
            }
         }
      }
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      class_310 mc = event.getClient();
      if (mc.field_1687 != null && mc.field_1724 != null) {
         long now = System.currentTimeMillis();
         List<FireworkEspFeature.Mark> snapshot;
         synchronized (this.marks) {
            this.marks.removeIf(markx -> now - markx.startedAt >= 3000L);
            if (this.marks.isEmpty()) {
               return;
            }

            snapshot = List.copyOf(this.marks);
         }

         float unit = (float)(this.scale.getValue() / mc.method_22683().method_4495());
         MsdfFont font = UiFonts.sfProDisplay();
         float textSize = 7.0F * unit;
         float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
         float iconSize = 8.0F * unit;
         float height = 11.0F * unit;
         class_1799 icon = new class_1799(class_1802.field_8639);

         for (FireworkEspFeature.Mark mark : snapshot) {
            Render3DUtil.ScreenPoint anchor = Render3DUtil.projectToScreen(mc, mark.position);
            if (anchor != null) {
               long remaining = Math.max(0L, 3000L - (now - mark.startedAt));
               float fade = (float)remaining / 3000.0F;
               String label = this.showTime.getValue() ? String.format(Locale.ROOT, "%.1fs", (float)remaining / 1000.0F) : "";
               float labelWidth = label.isEmpty() ? 0.0F : font.measureWidth(label, textSize, letterSpacing);
               float width = iconSize + (label.isEmpty() ? 0.0F : labelWidth + 3.0F * unit) + 4.0F * unit;
               float x = anchor.x() - width / 2.0F;
               float y = anchor.y() - height / 2.0F;
               Render2DUtil.rect(x, y, width, height).color(ColorUtil.withAlpha(0, (int)(140.0F * fade))).radius(height / 2.0F).draw();
               if (!label.isEmpty()) {
                  Render2DUtil.text(x + 2.0F * unit + iconSize + 3.0F * unit, y + (height - textSize) / 2.0F, textSize, label)
                     .font(font)
                     .color(ColorUtil.withAlpha(this.color.getValue(), (int)(255.0F * fade)))
                     .draw();
               }

               Render2DUtil.flush();
               event.getGuiGraphicsExtractor().method_51427(icon, Math.round(x + 2.0F * unit), Math.round(y + (height - iconSize) / 2.0F));
            }
         }
      }
   }

   @Environment(EnvType.CLIENT)
   private static final class Mark {
      private final class_243 position;
      private volatile long startedAt;

      private Mark(class_243 position, long startedAt) {
         this.position = position;
         this.startedAt = startedAt;
      }
   }
}
