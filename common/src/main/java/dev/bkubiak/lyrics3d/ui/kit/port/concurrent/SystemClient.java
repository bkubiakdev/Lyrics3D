package dev.bkubiak.lyrics3d.ui.kit.port.concurrent;

public final class SystemClient {
   private final ModuleRegistry modules = new ModuleRegistry();
   private final ConfigManager configs = new ConfigManager();
   private static final class Holder { static final SystemClient INSTANCE = new SystemClient(); }
   public static SystemClient getInstance() { return Holder.INSTANCE; }
   public ModuleRegistry getModuleRegistry() { return modules; }
   public ConfigManager getConfigManager() { return configs; }
}
