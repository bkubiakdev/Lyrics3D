package dev.bkubiak.lyrics3d.ui.kit.port.concurrent;

import dev.bkubiak.lyrics3d.ui.kit.port.module.Module;
import dev.bkubiak.lyrics3d.ui.kit.port.module.client.ThemeModule;
import java.util.ArrayList;
import java.util.List;

/** Only the appearance module exists here; the editor builds its own setting adapter. */
public final class ModuleRegistry {
   private final ThemeModule theme = new ThemeModule();
   public List<Module> getList32() { return new ArrayList<>(List.of(theme)); }
   public <T extends Module> T getModuleByClass(Class<T> type) { return type.isInstance(theme) ? type.cast(theme) : null; }
}
