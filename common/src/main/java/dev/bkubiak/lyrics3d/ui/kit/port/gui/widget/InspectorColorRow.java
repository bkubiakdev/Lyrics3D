package dev.bkubiak.lyrics3d.ui.kit.port.gui.widget;

import dev.bkubiak.lyrics3d.ui.kit.port.setting.ColorSetting;

/** Full palette and editable HEX, with the popup facing into the available canvas. */
final class InspectorColorRow extends ColorPickerRow {
   InspectorColorRow(ColorSetting color) { super(color); }
   @Override protected float[] popupPosition() {
      return popupPosition(true);
   }
}
