package org.ryzen.utils.text;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2561;
import net.minecraft.class_2583;
import net.minecraft.class_310;
import net.minecraft.class_5348;
import net.minecraft.class_5481;
import org.ryzen.feature.FeatureManager;
import org.ryzen.feature.impl.misc.NameProtectFeature;

@Environment(EnvType.CLIENT)
public final class NameProtectUtil {
   private static final String FEATURE_NAME = "NameProtect";

   private NameProtectUtil() {
   }

   public static String protect(String text) {
      if (text != null && !text.isEmpty()) {
         String realName = realName();
         String fakeName = fakeName();
         return !realName.isEmpty() && !fakeName.isEmpty() && !realName.equals(fakeName) && text.contains(realName) ? text.replace(realName, fakeName) : text;
      } else {
         return text;
      }
   }

   public static class_2561 protect(class_2561 component) {
      if (component == null) {
         return null;
      }

      String protectedText = protect(component.getString());
      return (class_2561)(protectedText.equals(component.getString())
         ? component
         : class_2561.method_43470(protectedText).method_27696(component.method_10866()));
   }

   public static class_5348 protect(class_5348 text) {
      if (text == null) {
         return null;
      } else if (text instanceof class_2561 component) {
         return protect(component);
      } else {
         String protectedText = protect(text.getString());
         return protectedText.equals(text.getString()) ? text : class_5348.method_29430(protectedText);
      }
   }

   public static class_5481 protect(class_5481 sequence) {
      if (sequence == null) {
         return null;
      }

      StringBuilder text = new StringBuilder();
      sequence.accept((index, style, codePoint) -> {
         text.appendCodePoint(codePoint);
         return true;
      });
      String original = text.toString();
      String protectedText = protect(original);
      return protectedText.equals(original) ? sequence : class_5481.method_30747(protectedText, class_2583.field_24360);
   }

   private static String realName() {
      class_310 minecraft = class_310.method_1551();
      if (minecraft == null) {
         return "";
      } else {
         return minecraft.method_1548() != null && minecraft.method_1548().method_1676() != null ? minecraft.method_1548().method_1676() : "";
      }
   }

   private static String fakeName() {
      if (FeatureManager.INSTANCE.getFeature("NameProtect") instanceof NameProtectFeature nameProtect && nameProtect.isEnabled()) {
         String value = nameProtect.name.getValue();
         return value == null ? "" : value.trim();
      } else {
         return "";
      }
   }
}
