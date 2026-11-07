package dev.bkubiak.lyrics3d.framework.objects.gradient.impl;

import dev.bkubiak.lyrics3d.framework.objects.gradient.Gradient;
import dev.bkubiak.lyrics3d.utility.colors.ColorRGBA;

public class HorizontalGradient extends Gradient {
   public HorizontalGradient(ColorRGBA startColor, ColorRGBA endColor) {
      super(startColor, startColor, endColor, endColor);
   }

   public HorizontalGradient rotate() {
      return new HorizontalGradient(this.bottomRightColor, this.topLeftColor);
   }
}
