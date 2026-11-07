package dev.bkubiak.lyrics3d.ui.kit.port.data;

import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.ActionRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.BooleanRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.ChoiceRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.ColorPickerRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.DropdownRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.FilterDropdown;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.FilterMenuRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.PanelWidget;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.SettingRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.SliderRow;
import dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.TextInputRow;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.ActionSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.BooleanSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.ChoiceSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.ColorSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.CompactGroupSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.FilterMenuSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.HotkeySetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.InputSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.KeybindSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.ListSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.MultilistSetting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.Setting;
import dev.bkubiak.lyrics3d.ui.kit.port.setting.SliderSetting;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

public class SettingWidgetFactory {
   private static final Map<Class<? extends Setting>, Function<Setting, PanelWidget>> map = new LinkedHashMap<>();

   private SettingWidgetFactory() {
   }

   static {
      onClassFunction(ChoiceSetting.class, var0 -> new ChoiceRow((ChoiceSetting)var0));
      onClassFunction(FilterMenuSetting.class, var0 -> new FilterMenuRow((FilterMenuSetting)var0));
      onClassFunction(BooleanSetting.class, var0 -> new BooleanRow((BooleanSetting)var0));
      onClassFunction(dev.bkubiak.lyrics3d.ui.kit.port.setting.RangeSliderSetting.class, value -> new dev.bkubiak.lyrics3d.ui.kit.port.gui.widget.RangeSliderRow((dev.bkubiak.lyrics3d.ui.kit.port.setting.RangeSliderSetting)value));
      onClassFunction(SliderSetting.class, var0 -> new SliderRow((SliderSetting)var0));
      onClassFunction(MultilistSetting.class, var0 -> new FilterDropdown((MultilistSetting)var0));
      onClassFunction(ListSetting.class, var0 -> new FilterDropdown((ListSetting)var0));
      onClassFunction(ColorSetting.class, var0 -> new ColorPickerRow((ColorSetting)var0));
      onClassFunction(InputSetting.class, var0 -> new TextInputRow((InputSetting)var0));
      onClassFunction(KeybindSetting.class, var0 -> new DropdownRow((Setting)var0));
      onClassFunction(HotkeySetting.class, var0 -> new DropdownRow((Setting)var0));
      onClassFunction(ActionSetting.class, var0 -> new ActionRow((ActionSetting)var0));
      onClassFunction(CompactGroupSetting.class, var0 -> new SettingRow((CompactGroupSetting)var0));
   }

   public static boolean supports(Setting setting) {
      return setting != null && map.keySet().stream().anyMatch(type -> type.isInstance(setting));
   }

   public static PanelWidget getPanelWidgetBySetting10(Setting setting2) {
      if (setting2 == null) {
         return null;
      } else {
         for (Entry entry : map.entrySet()) {
            if (((Class)entry.getKey()).isInstance(setting2)) {
               return (PanelWidget)((Function)entry.getValue()).apply(setting2);
            }
         }

         return null;
      }
   }

   public static void onClassFunction(Class value, Function function2) {
      map.put(value, function2);
   }
}
