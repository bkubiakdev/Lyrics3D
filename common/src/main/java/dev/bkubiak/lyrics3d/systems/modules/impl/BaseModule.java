package dev.bkubiak.lyrics3d.systems.modules.impl;

import com.google.gson.JsonObject;
import dev.bkubiak.lyrics3d.systems.setting.Setting;
import dev.bkubiak.lyrics3d.systems.setting.SettingsContainer;
import net.minecraft.client.MinecraftClient;
import java.util.ArrayList;
import java.util.List;

/** The single feature of this mod: a settings container with an on/off state. */
public abstract class BaseModule implements SettingsContainer {
   protected static final MinecraftClient mc = MinecraftClient.getInstance();
   private final String name;
   private final String description;
   private final List<Setting> settings = new ArrayList<>();
   private boolean enabled;

   protected BaseModule(String name, String description) {
      this.name = name;
      this.description = description;
   }
   public String getName() { return name; }
   public String getDescription() { return description; }
   @Override public List<Setting> getSettings() { return settings; }
   public List<Setting> getDisplaySettings() { return settings; }
   public boolean isEnabled() { return enabled; }
   public void setEnabled(boolean value) {
      if (enabled == value) return;
      enabled = value;
      if (value) onEnable(); else onDisable();
   }
   public void toggle() { setEnabled(!enabled); }
   public void onEnable() { }
   public void onDisable() { }

   public JsonObject saveSettings() {
      JsonObject json = new JsonObject();
      for (Setting setting : settings) {
         var value = setting.save();
         if (value != null) json.add(setting.getName(), value);
      }
      return json;
   }

   public void loadSettings(JsonObject json) {
      for (Setting setting : settings) {
         if (!json.has(setting.getName())) continue;
         try { setting.load(json.get(setting.getName())); }
         catch (RuntimeException ignored) { }
      }
   }
}
