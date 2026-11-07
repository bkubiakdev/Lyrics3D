package dev.bkubiak.lyrics3d.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.bkubiak.lyrics3d.Lyrics3DClient;
import dev.bkubiak.lyrics3d.ui.menu.lyrics.LyricsEditorScreen;

/** Mod Menu "Configure" opens the lyrics editor. */
public final class ModMenuEntry implements ModMenuApi {
   @Override
   public ConfigScreenFactory<?> getModConfigScreenFactory() {
      return parent -> new LyricsEditorScreen(Lyrics3DClient.getInstance().getModule(), parent);
   }
}
