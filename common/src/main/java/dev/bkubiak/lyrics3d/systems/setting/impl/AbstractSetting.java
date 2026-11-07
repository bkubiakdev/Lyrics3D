package dev.bkubiak.lyrics3d.systems.setting.impl;

import java.util.function.BooleanSupplier;
import lombok.Generated;
import dev.bkubiak.lyrics3d.systems.setting.Setting;
import dev.bkubiak.lyrics3d.systems.setting.SettingsContainer;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractSetting implements Setting {
   private final String name;
   @NotNull
   private final BooleanSupplier hideCondition;

   public static volatile boolean suppressSave;

   public static void saveConfig() {
      if (suppressSave) return;
      if (dev.bkubiak.lyrics3d.systems.setting.GuiSaveBatch.defer(AbstractSetting.class, AbstractSetting::saveConfig)) return;
      var client = dev.bkubiak.lyrics3d.Lyrics3DClient.getInstance();
      if (client != null) client.saveConfig();
   }

   public AbstractSetting(@NotNull SettingsContainer parent, String name, @NotNull BooleanSupplier hideCondition) {
      this.name = name;
      this.hideCondition = hideCondition;
      this.register(parent);
   }

   public AbstractSetting(@NotNull SettingsContainer parent, String name) {
      this(parent, name, () -> false);
   }

   @Override
   public void register(SettingsContainer parent) {
      parent.getSettings().add(this);
   }

   @Override
   public String getDescription() {
      return this.getName() + ".description";
   }

   @Generated
   @Override
   public String getName() {
      return this.name;
   }

   @NotNull
   @Generated
   @Override
   public BooleanSupplier getHideCondition() {
      return this.hideCondition;
   }
}
