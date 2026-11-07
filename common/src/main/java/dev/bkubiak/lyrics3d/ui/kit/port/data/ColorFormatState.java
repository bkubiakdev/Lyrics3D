package dev.bkubiak.lyrics3d.ui.kit.port.data;

import dev.bkubiak.lyrics3d.ui.kit.port.concurrent.ConfigManager;
import dev.bkubiak.lyrics3d.ui.kit.port.enums.ColorFormat;

public final class ColorFormatState {
   private ColorFormatState() {
   }

   public static ColorFormat getColorFormat() {
      try {
         ConfigManager configmanager = ClientAccess.getConfigManager();
         String s = configmanager == null ? null : configmanager.getColorFormat();
         if (s != null) return ColorFormat.getColorFormatByString(s);
      } catch (Exception exception) {
      }
      return ColorFormat.RGB;
   }

   public static void onColorFormat(ColorFormat colorFormat) {
      ConfigManager configmanager = ClientAccess.getConfigManager();
      if (configmanager != null) configmanager.setColorFormat(colorFormat.name());
   }
}
