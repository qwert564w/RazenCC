package org.ryzen.command.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_4076;
import net.minecraft.class_7439;
import net.minecraft.class_746;
import net.minecraft.class_2902.class_2903;
import org.ryzen.command.ClientCommand;
import org.ryzen.context.MinecraftContext;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.packet.PacketReceiveEvent;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.menu.core.MenuConfigStore;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;
import org.ryzen.utils.text.ChatUtil;

@Environment(EnvType.CLIENT)
public final class GpsCommand extends ClientCommand implements MinecraftContext {
   private static final int DIVIDER_COLOR = ColorUtil.rgba(255, 255, 255, 25);
   private static final String[] DIRECTIONS = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
   private static final Pattern COORD_PATTERN = Pattern.compile("\\[([-\\d]+)\\s+([-\\d]+)\\s+([-\\d]+)\\]");
   private static final long EVENT_TIMEOUT_MS = 3000L;
   private static final float SCALE = 1.2F;
   private static final float PILL_HEIGHT = 36.0F;
   private static final float PADDING = 10.0F;
   private static final float ICON_SIZE = 16.0F;
   private static final float GAP = 8.0F;
   private static final float DIVIDER_HEIGHT = 16.0F;
   private static final float NAME_SIZE = 12.0F;
   private static final float DISTANCE_SIZE = 10.0F;
   private static final float DOT_SIZE = 6.0F;
   private static final float DOT_GAP = 8.0F;
   private String markName;
   private Double targetX;
   private Double targetY;
   private Double targetZ;
   private boolean waitingForEvent;
   private boolean receivedEventHeader;
   private long eventRequestAt;

   public GpsCommand() {
      super("gps", "GPS marker: add <name> <x> <y> <z> | set <x> <z> | clear | info", ":round_pushpin:");
      this.loadMarker();
   }

   @Override
   public void build(LiteralArgumentBuilder<Object> builder) {
      builder.executes(context -> {
         ChatUtil.usage("gps add <name> <x> <y> <z>  •  set <x> <z>  •  clear  •  info");
         return 1;
      });
      builder.then(
         LiteralArgumentBuilder.literal("add")
            .then(
               RequiredArgumentBuilder.argument("name", StringArgumentType.string())
                  .then(
                     RequiredArgumentBuilder.argument("x", DoubleArgumentType.doubleArg())
                        .then(
                           RequiredArgumentBuilder.argument("y", DoubleArgumentType.doubleArg())
                              .then(
                                 RequiredArgumentBuilder.argument("z", DoubleArgumentType.doubleArg())
                                    .executes(
                                       context -> this.setMarker(
                                          StringArgumentType.getString(context, "name"),
                                          DoubleArgumentType.getDouble(context, "x"),
                                          DoubleArgumentType.getDouble(context, "y"),
                                          DoubleArgumentType.getDouble(context, "z")
                                       )
                                    )
                              )
                        )
                  )
            )
      );
      builder.then(
         ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)LiteralArgumentBuilder.literal(
                              "set"
                           )
                           .then(
                              RequiredArgumentBuilder.argument("x", DoubleArgumentType.doubleArg())
                                 .then(
                                    RequiredArgumentBuilder.argument("z", DoubleArgumentType.doubleArg())
                                       .executes(
                                          context -> this.setMarker(
                                             "GPS", DoubleArgumentType.getDouble(context, "x"), DoubleArgumentType.getDouble(context, "z")
                                          )
                                       )
                                 )
                           ))
                        .then(LiteralArgumentBuilder.literal("w").executes(context -> this.setMarker("Warden", 2000.0, 2000.0))))
                     .then(LiteralArgumentBuilder.literal("m").executes(context -> this.setMarker("Copper", -2000.0, -2000.0))))
                  .then(LiteralArgumentBuilder.literal("z").executes(context -> this.setMarker("Zamok", 0.0, 0.0))))
               .then(LiteralArgumentBuilder.literal("e").executes(context -> this.requestEvent())))
            .then(LiteralArgumentBuilder.literal("r").executes(context -> this.setRandom()))
      );
      builder.then(LiteralArgumentBuilder.literal("clear").executes(context -> this.clearMarker()));
      builder.then(LiteralArgumentBuilder.literal("info").executes(context -> this.showInfo()));
      builder.then(LiteralArgumentBuilder.literal("zamok").executes(context -> this.setZamokMarker()));
   }

   private int setZamokMarker() {
      this.markName = "Замок";
      this.targetX = 0.0;
      this.targetY = 100.0;
      this.targetZ = 0.0;
      this.persistMarker();
      ChatUtil.success("Метка замка поставлена: 0, 100, 0");
      return 1;
   }

   private int setRandom() {
      double x = (Math.random() * 700.0 + 1800.0) * (Math.random() < 0.5 ? 1 : -1);
      double z = (Math.random() * 700.0 + 1800.0) * (Math.random() < 0.5 ? 1 : -1);
      return this.setMarker("Random", Math.round(x), Math.round(z));
   }

   private int requestEvent() {
      if (this.player() == null) {
         return 0;
      }

      this.waitingForEvent = true;
      this.receivedEventHeader = false;
      this.eventRequestAt = System.currentTimeMillis();
      this.player().field_3944.method_45730("event delay");
      return 1;
   }

   @EventTarget
   public void onPacketReceive(PacketReceiveEvent event) {
      if (this.waitingForEvent && mc.field_1724 != null && mc.field_1687 != null) {
         if (event.getPhase() == PacketReceiveEvent.Phase.PRE && event.getPacket() instanceof class_7439 chatPacket) {
            if (System.currentTimeMillis() - this.eventRequestAt > 3000L) {
               this.waitingForEvent = false;
               this.receivedEventHeader = false;
               mc.execute(() -> ChatUtil.error("Event not found  •  GPS not changed"));
            } else {
               String content = chatPacket.comp_763().getString();
               if (content.contains("[Ивенты]")) {
                  this.receivedEventHeader = true;
               } else if (this.receivedEventHeader) {
                  Matcher matcher = COORD_PATTERN.matcher(content);
                  if (matcher.find()) {
                     double x = Double.parseDouble(matcher.group(1));
                     double y = Double.parseDouble(matcher.group(2));
                     double z = Double.parseDouble(matcher.group(3));
                     this.waitingForEvent = false;
                     this.receivedEventHeader = false;
                     mc.execute(() -> this.setMarker("Event", x, y, z));
                  } else if (content.contains("следующего ивента")) {
                     this.waitingForEvent = false;
                     this.receivedEventHeader = false;
                     mc.execute(() -> ChatUtil.error("Event has not started yet  •  GPS not changed"));
                  }
               }
            }
         }
      }
   }

   private int setMarker(String name, double x, double z) {
      return this.setMarker(name, x, null, z);
   }

   private int setMarker(String name, double x, double y, double z) {
      return this.setMarker(name, x, Double.valueOf(y), z);
   }

   private int setMarker(String name, double x, Double y, double z) {
      String normalizedName = name == null ? "" : name.trim();
      if (normalizedName.isEmpty()) {
         ChatUtil.error("GPS marker name cannot be empty");
         return 0;
      }

      if (normalizedName.length() > 48) {
         ChatUtil.error("GPS marker name is too long (maximum 48 characters)");
         return 0;
      }

      this.markName = normalizedName;
      this.targetX = x;
      this.targetY = y;
      this.targetZ = z;
      this.persistMarker();
      String coordinates = "X " + format(x) + (y == null ? "" : "  •  Y " + format(y)) + "  •  Z " + format(z);
      ChatUtil.success("GPS marker " + normalizedName + "  •  " + coordinates);
      if (this.player() != null) {
         ChatUtil.info("Distance  •  " + format(this.distanceTo(this.player())) + " blocks");
      }

      return 1;
   }

   private int clearMarker() {
      if (!this.hasMarker()) {
         ChatUtil.error("No GPS marker set");
         return 0;
      } else {
         this.markName = null;
         this.targetX = null;
         this.targetY = null;
         this.targetZ = null;
         this.persistMarker();
         ChatUtil.success("GPS marker cleared");
         return 1;
      }
   }

   private int showInfo() {
      if (!this.hasMarker()) {
         ChatUtil.error("No GPS marker set");
         return 0;
      }

      ChatUtil.header("GPS marker  •  " + this.markName);
      ChatUtil.entry(
         ":round_pushpin:",
         "Coordinates",
         "X " + format(this.targetX) + (this.targetY == null ? "" : "  •  Y " + format(this.targetY)) + "  •  Z " + format(this.targetZ)
      );
      if (this.player() != null) {
         ChatUtil.entry(":compass:", "Navigation", format(this.distanceTo(this.player())) + " blocks  •  " + this.directionTo(this.player()));
      }

      return 1;
   }

   private boolean hasMarker() {
      return this.targetX != null && this.targetZ != null;
   }

   private double distanceTo(class_746 player) {
      double dx = this.targetX - player.method_23317();
      double dy = this.targetY == null ? 0.0 : this.targetY - player.method_23318();
      double dz = this.targetZ - player.method_23321();
      return Math.sqrt(dx * dx + dy * dy + dz * dz);
   }

   private String directionTo(class_746 player) {
      double dx = this.targetX - player.method_23317();
      double dz = this.targetZ - player.method_23321();
      double bearing = (Math.toDegrees(Math.atan2(dx, -dz)) + 360.0) % 360.0;
      return DIRECTIONS[(int)Math.round(bearing / 45.0) % DIRECTIONS.length];
   }

   private static String format(double value) {
      return String.format(Locale.ROOT, "%.1f", value);
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      if (this.hasMarker() && mc.field_1724 != null && mc.field_1687 != null) {
         Render3DUtil.ScreenPoint anchor = Render3DUtil.projectToScreen(mc, this.anchorPos());
         if (anchor != null) {
            float unit = 1.2F / mc.method_22683().method_4495();
            MsdfFont font = UiFonts.sfProDisplay();
            float nameSize = 12.0F * unit;
            float distanceSize = 10.0F * unit;
            String distance = formatDistance(this.distanceTo(mc.field_1724));
            float nameWidth = font.measureWidth(this.markName, nameSize, nameSize * UiFontStyle.MEDIUM.letterSpacingEm());
            float distanceWidth = font.measureWidth(distance, distanceSize, distanceSize * UiFontStyle.REGULAR.letterSpacingEm());
            float dividerWidth = Math.max(0.5F, 0.5F * unit);
            float pillWidth = 34.0F * unit + nameWidth + 8.0F * unit + dividerWidth + 8.0F * unit + distanceWidth + 10.0F * unit;
            float pillHeight = 36.0F * unit;
            float dotSize = 6.0F * unit;
            float dotX = anchor.x() - dotSize / 2.0F;
            float dotY = anchor.y() - dotSize / 2.0F;
            float pillX = anchor.x() - pillWidth / 2.0F;
            float pillY = dotY - 8.0F * unit - pillHeight;
            Render2DUtil.rect(dotX, dotY, dotSize, dotSize).color(Theme.getAccent()).radius(dotSize / 2.0F).draw();
            Render2DUtil.rect(pillX, pillY, pillWidth, pillHeight)
               .color(Theme.Colors.BACKGROUND_PRIMARY_50)
               .radius(pillHeight / 2.0F)
               .border(Math.max(0.5F, 0.5F * unit), Theme.Colors.OUTLINES_SMALL)
               .blur(8.0F * unit)
               .draw();
            float centerY = pillY + pillHeight / 2.0F;
            float cursor = pillX + 10.0F * unit;
            float iconSize = 16.0F * unit;
            Render2DUtil.texture(cursor, centerY - iconSize / 2.0F, iconSize, iconSize, Textures.Icons.GIFT).color(Theme.getAccent()).draw();
            cursor += iconSize + 8.0F * unit;
            Render2DUtil.text(cursor, font.centeredTextY(centerY, nameSize), nameSize, this.markName)
               .style(UiFontStyle.MEDIUM)
               .color(Theme.Colors.TEXT_TEXT)
               .draw();
            cursor += nameWidth + 8.0F * unit;
            Render2DUtil.rect(cursor, centerY - 16.0F * unit / 2.0F, dividerWidth, 16.0F * unit).color(DIVIDER_COLOR).draw();
            cursor += dividerWidth + 8.0F * unit;
            Render2DUtil.text(cursor, font.centeredTextY(centerY, distanceSize), distanceSize, distance)
               .style(UiFontStyle.REGULAR)
               .color(Theme.Colors.TEXT_TEXT)
               .draw();
         }
      }
   }

   private class_243 anchorPos() {
      int blockX = (int)Math.floor(this.targetX);
      int blockZ = (int)Math.floor(this.targetZ);
      if (this.targetY != null) {
         return new class_243(this.targetX, this.targetY, this.targetZ);
      } else {
         return mc.field_1687.method_2935().method_12123(class_4076.method_18675(blockX), class_4076.method_18675(blockZ))
            ? new class_243(this.targetX, mc.field_1687.method_8624(class_2903.field_13197, blockX, blockZ), this.targetZ)
            : new class_243(this.targetX, mc.field_1724.method_23320(), this.targetZ);
      }
   }

   private static String formatDistance(double blocks) {
      return blocks >= 1000.0 ? String.format(Locale.ROOT, "%.1fkm", blocks / 1000.0) : Math.round(blocks) + "m";
   }

   private void loadMarker() {
      String name = MenuConfigStore.getString("gps.name", "");
      float x = MenuConfigStore.getFloat("gps.x", Float.NaN);
      float y = MenuConfigStore.getFloat("gps.y", Float.NaN);
      float z = MenuConfigStore.getFloat("gps.z", Float.NaN);
      if (!name.isEmpty() && !Float.isNaN(x) && !Float.isNaN(z)) {
         this.markName = name;
         this.targetX = (double)x;
         this.targetY = Float.isNaN(y) ? null : (double)y;
         this.targetZ = (double)z;
      }
   }

   private void persistMarker() {
      String name = this.markName == null ? "" : this.markName;
      double x = this.targetX == null ? Double.NaN : this.targetX;
      double y = this.targetY == null ? Double.NaN : this.targetY;
      double z = this.targetZ == null ? Double.NaN : this.targetZ;
      MenuConfigStore.save(data -> {
         if (!name.isEmpty() && !Double.isNaN(x) && !Double.isNaN(z)) {
            data.addProperty("gps.name", name);
            data.addProperty("gps.x", x);
            if (!Double.isNaN(y)) {
               data.addProperty("gps.y", y);
            } else {
               data.remove("gps.y");
            }

            data.addProperty("gps.z", z);
         } else {
            data.remove("gps.name");
            data.remove("gps.x");
            data.remove("gps.y");
            data.remove("gps.z");
         }
      });
   }
}
