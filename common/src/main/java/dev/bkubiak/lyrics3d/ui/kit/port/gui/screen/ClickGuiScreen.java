package dev.bkubiak.lyrics3d.ui.kit.port.gui.screen;

import dev.bkubiak.lyrics3d.ui.menu.lyrics.LyricsEditorScreen;
import net.minecraft.client.MinecraftClient;

/** Widget scale of the screen hosting the settings widgets. */
public final class ClickGuiScreen {
   private ClickGuiScreen() { }
   public static float getValue235() {
      return MinecraftClient.getInstance().currentScreen instanceof LyricsEditorScreen editor ? editor.widgetScale() : 1.0F;
   }
   public static boolean isFlag() { return false; }
}
