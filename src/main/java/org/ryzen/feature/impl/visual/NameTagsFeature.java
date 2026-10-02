package org.ryzen.feature.impl.visual;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_124;
import net.minecraft.class_1304;
import net.minecraft.class_1799;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_268;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_5251;
import net.minecraft.class_742;
import org.joml.Matrix3x2fStack;
import org.ryzen.event.EventTarget;
import org.ryzen.event.events.render.Render2DEvent;
import org.ryzen.feature.Feature;
import org.ryzen.feature.FeatureCategory;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.setting.BooleanSetting;
import org.ryzen.feature.setting.NumberSetting;
import org.ryzen.utils.ColorUtil;
import org.ryzen.utils.render.Render3DUtil;
import org.ryzen.utils.render.Textures;
import org.ryzen.utils.render.Theme;
import org.ryzen.utils.render.gui.MsdfFont;
import org.ryzen.utils.render.gui.Render2DUtil;
import org.ryzen.utils.render.gui.UiFontStyle;
import org.ryzen.utils.render.gui.UiFonts;

@Environment(EnvType.CLIENT)
public final class NameTagsFeature extends Feature {
   private static final int DIVIDER_COLOR = ColorUtil.rgba(255, 255, 255, 20);
   private static final float SCALE = 1.1F;
   private static final float PILL_HEIGHT = 40.0F;
   private static final float RADIUS = 16.0F;
   private static final float PADDING = 10.0F;
   private static final float GAP = 8.0F;
   private static final float HEAD_SIZE = 20.0F;
   private static final float ICON_SIZE = 16.0F;
   private static final float ITEM_SIZE = 16.0F;
   private static final float DIVIDER_HEIGHT = 12.0F;
   private static final float TEXT_SIZE = 12.0F;
   private static final float HEAD_OFFSET = 0.25F;
   private static final class_1304[] EQUIPMENT_ORDER = new class_1304[]{
      class_1304.field_6169, class_1304.field_6174, class_1304.field_6172, class_1304.field_6166, class_1304.field_6173, class_1304.field_6171
   };
   public final NumberSetting scale = this.register(new NumberSetting("Scale", 1.0, 0.95, 1.2, 0.05, "x"));
   public final BooleanSetting privilege = this.register(new BooleanSetting("Privilege", true));
   public final BooleanSetting health = this.register(new BooleanSetting("Health", true));
   public final BooleanSetting items = this.register(new BooleanSetting("Items", true));
   public final NumberSetting distance = this.register(new NumberSetting("Distance", 64.0, 16.0, 128.0, 8.0, " blocks"));

   public NameTagsFeature() {
      super("NameTags", "Draws styled nametags above players", FeatureCategory.VISUAL, -1);
   }

   public static boolean shouldHideVanillaTag() {
      return FeatureManager.INSTANCE.getEnabled(NameTagsFeature.class) != null;
   }

   @EventTarget
   public void onRender2D(Render2DEvent event) {
      class_310 mc = event.getClient();
      if (mc != null && mc.field_1687 != null && mc.field_1724 != null) {
         float tickDelta = event.getDeltaTracker().method_60637(false);
         float unit = (float)(1.1F * this.scale.getValue() / mc.method_22683().method_4495());
         double maxDistanceSqr = this.distance.getValue() * this.distance.getValue();

         for (class_742 player : mc.field_1687.method_18456()) {
            if ((player != mc.field_1724 || !mc.field_1690.method_31044().method_31034())
               && !player.method_31481()
               && player.method_5805()
               && !player.method_7325()
               && !(mc.field_1724.method_5858(player) > maxDistanceSqr)) {
               this.drawTag(event, player, tickDelta, unit);
            }
         }
      }
   }

   private void drawTag(Render2DEvent event, class_742 player, float tickDelta, float unit) {
      class_310 mc = event.getClient();
      class_243 position = Render3DUtil.interpolatedPosition(player, tickDelta).method_1031(0.0, player.method_17682() + 0.25F, 0.0);
      Render3DUtil.ScreenPoint anchor = Render3DUtil.projectToScreen(mc, position);
      if (anchor != null) {
         MsdfFont font = UiFonts.sfProDisplay();
         float textSize = 12.0F * unit;
         float letterSpacing = textSize * UiFontStyle.MEDIUM.letterSpacingEm();
         float headSize = 20.0F * unit;
         float iconSize = 16.0F * unit;
         float itemSize = 16.0F * unit;
         float gap = 8.0F * unit;
         float dividerWidth = Math.max(0.5F, 0.5F * unit);
         String name = player.method_7334().name();
         NameTagsFeature.Privilege donat = this.privilege.getValue() ? resolvePrivilege(player) : null;
         String healthText = this.health.getValue() ? (int)Math.ceil(player.method_6032() + player.method_6067()) + "HP" : null;
         List<class_1799> equipment = this.items.getValue() ? equipment(player) : List.of();
         float width = 10.0F * unit + headSize + gap + font.measureWidth(name, textSize, letterSpacing);
         if (donat != null) {
            width += gap + dividerWidth + gap + iconSize + gap + font.measureWidth(donat.name(), textSize, letterSpacing);
         }

         if (healthText != null) {
            width += gap + dividerWidth + gap + iconSize + gap + font.measureWidth(healthText, textSize, letterSpacing);
         }

         if (!equipment.isEmpty()) {
            width += gap + dividerWidth + gap + equipment.size() * itemSize + (equipment.size() - 1) * gap;
         }

         width += 10.0F * unit;
         float pillHeight = 40.0F * unit;
         float pillX = anchor.x() - width / 2.0F;
         float pillY = anchor.y() - pillHeight;
         float centerY = pillY + pillHeight / 2.0F;
         float textY = font.centeredTextY(centerY, textSize);
         Render2DUtil.rect(pillX, pillY, width, pillHeight)
            .color(Theme.Colors.BACKGROUND_PRIMARY_50)
            .radius(16.0F * unit)
            .border(Math.max(0.5F, 0.5F * unit), Theme.Colors.OUTLINES_MEDIUM)
            .blur(8.0F * unit)
            .draw();
         float cursor = pillX + 10.0F * unit;
         drawHead(player, cursor, centerY - headSize / 2.0F, headSize);
         cursor += headSize + gap;
         Render2DUtil.text(cursor, textY, textSize, name).style(UiFontStyle.MEDIUM).color(-1).draw();
         cursor += font.measureWidth(name, textSize, letterSpacing);
         if (donat != null) {
            cursor = this.drawDivider(cursor, centerY, dividerWidth, gap, unit);
            Render2DUtil.texture(cursor, centerY - iconSize / 2.0F, iconSize, iconSize, Textures.Icons.SPARKLES).color(Theme.Colors.ICON_GHOST).draw();
            cursor += iconSize + gap;
            Render2DUtil.text(cursor, textY, textSize, donat.name()).style(UiFontStyle.MEDIUM).color(donat.color()).draw();
            cursor += font.measureWidth(donat.name(), textSize, letterSpacing);
         }

         if (healthText != null) {
            cursor = this.drawDivider(cursor, centerY, dividerWidth, gap, unit);
            Render2DUtil.texture(cursor, centerY - iconSize / 2.0F, iconSize, iconSize, Textures.Icons.SCAN_HEART).color(Theme.Colors.ICON_GHOST).draw();
            cursor += iconSize + gap;
            Render2DUtil.text(cursor, textY, textSize, healthText).style(UiFontStyle.MEDIUM).color(-1).draw();
            cursor += font.measureWidth(healthText, textSize, letterSpacing);
         }

         if (!equipment.isEmpty()) {
            cursor = this.drawDivider(cursor, centerY, dividerWidth, gap, unit);
            Render2DUtil.flush();
            Matrix3x2fStack pose = event.getGuiGraphicsExtractor().method_51448();
            float guiScale = mc.method_22683().method_4495();
            float scale = itemSize / 16.0F;
            float itemY = Math.round((centerY - itemSize / 2.0F) * guiScale) / guiScale;

            for (class_1799 stack : equipment) {
               float itemX = Math.round(cursor * guiScale) / guiScale;
               pose.pushMatrix();
               pose.translate(itemX, itemY);
               pose.scale(scale);
               event.getGuiGraphicsExtractor().method_51427(stack, 0, 0);
               pose.popMatrix();
               cursor += itemSize + gap;
            }
         }
      }
   }

   private static void drawHead(class_742 player, float x, float y, float size) {
      class_2960 skin = player.method_52814().comp_1626().comp_3627();
      float radius = size * 0.2F;
      Render2DUtil.texture(x, y, size, size, skin).managed().uv(0.125F, 0.125F, 0.25F, 0.25F).radius(radius).draw();
      Render2DUtil.texture(x, y, size, size, skin).managed().uv(0.625F, 0.125F, 0.75F, 0.25F).radius(radius).draw();
   }

   private float drawDivider(float cursor, float centerY, float dividerWidth, float gap, float unit) {
      cursor += gap;
      Render2DUtil.rect(cursor, centerY - 12.0F * unit / 2.0F, dividerWidth, 12.0F * unit).color(DIVIDER_COLOR).draw();
      return cursor + dividerWidth + gap;
   }

   private static List<class_1799> equipment(class_742 player) {
      List<class_1799> stacks = new ArrayList<>(EQUIPMENT_ORDER.length);

      for (class_1304 slot : EQUIPMENT_ORDER) {
         class_1799 stack = player.method_6118(slot);
         if (!stack.method_7960()) {
            stacks.add(stack);
         }
      }

      return stacks;
   }

   private static NameTagsFeature.Privilege resolvePrivilege(class_742 player) {
      class_268 team = player.method_5781();
      if (team == null) {
         return null;
      }

      class_2561 prefix = team.method_1144();
      if (prefix == null) {
         return null;
      }

      String raw = prefix.getString();
      String name = cleanName(raw);
      if (name.isEmpty()) {
         return null;
      }

      Integer color = extractColor(prefix);
      if (color == null) {
         color = legacyColor(raw);
      }

      int rgb = color != null ? ColorUtil.rgba(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, 255) : Theme.getAccent();
      return new NameTagsFeature.Privilege(name, rgb);
   }

   private static String cleanName(String text) {
      String stripped = text.replaceAll("§.", "");
      StringBuilder clean = new StringBuilder(stripped.length());

      for (int i = 0; i < stripped.length(); i++) {
         char character = stripped.charAt(i);
         if (isBasicLetterOrDigit(character) || character == ' ') {
            clean.append(character);
         }
      }

      return clean.toString().replaceAll("\\s+", " ").trim();
   }

   private static boolean isBasicLetterOrDigit(char character) {
      return character >= 'a' && character <= 'z'
         || character >= 'A' && character <= 'Z'
         || character >= '0' && character <= '9'
         || character >= 1072 && character <= 1103
         || character >= 1040 && character <= 1071
         || character == 1105
         || character == 1025;
   }

   private static Integer legacyColor(String text) {
      for (int i = 0; i < text.length() - 1; i++) {
         if (text.charAt(i) == 167) {
            class_124 formatting = class_124.method_544(text.charAt(i + 1));
            if (formatting != null) {
               class_5251 color = class_5251.method_27718(formatting);
               if (color != null) {
                  return color.method_27716();
               }
            }
         }
      }

      return null;
   }

   private static Integer extractColor(class_2561 component) {
      class_5251 color = component.method_10866().method_10973();
      if (color != null) {
         return color.method_27716();
      }

      for (class_2561 sibling : component.method_10855()) {
         Integer siblingColor = extractColor(sibling);
         if (siblingColor != null) {
            return siblingColor;
         }
      }

      return null;
   }

   @Environment(EnvType.CLIENT)
   private record Privilege(String name, int color) {
   }
}
