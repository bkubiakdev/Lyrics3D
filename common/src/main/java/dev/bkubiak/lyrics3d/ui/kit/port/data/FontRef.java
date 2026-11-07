package dev.bkubiak.lyrics3d.ui.kit.port.data;

import dev.bkubiak.lyrics3d.ui.kit.port.render.IconAtlas;

public record FontRef(IconAtlas font, float weight) {
   public float getWeight() {
      return this.weight;
   }

   public IconAtlas getFont() {
      return this.font;
   }
}
