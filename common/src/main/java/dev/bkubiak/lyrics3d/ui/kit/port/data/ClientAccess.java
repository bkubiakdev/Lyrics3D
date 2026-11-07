package dev.bkubiak.lyrics3d.ui.kit.port.data;

import dev.bkubiak.lyrics3d.ui.kit.port.concurrent.ConfigManager;
import dev.bkubiak.lyrics3d.ui.kit.port.concurrent.ModuleRegistry;
import dev.bkubiak.lyrics3d.ui.kit.port.concurrent.SystemClient;

public final class ClientAccess {
   private ClientAccess() {
   }

   public static ConfigManager getConfigManager() {
      return SystemClient.getInstance().getConfigManager();
   }

   public static ModuleRegistry getModuleRegistry() {
      return SystemClient.getInstance().getModuleRegistry();
   }
}
