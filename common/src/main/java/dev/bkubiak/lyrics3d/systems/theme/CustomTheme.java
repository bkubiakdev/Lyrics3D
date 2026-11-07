package dev.bkubiak.lyrics3d.systems.theme;

import dev.bkubiak.lyrics3d.utility.colors.ColorRGBA;

public interface CustomTheme {
   ColorRGBA getTextColor();

   ColorRGBA getBackgroundColor();

   ColorRGBA getAdditionalColor();

   ColorRGBA getOutlineColor();

   ColorRGBA getFlatColor();

   ColorRGBA getAccentColor();

   ColorRGBA getIconsColor();

   ColorRGBA getEnabledModulesColor();

   boolean isDark();

   boolean isSeparators();

   float getBlurStrength();

   float getGlassOpacity();

   float getEnabledOffset();

   float getBlurOffset();

   float getHudAlpha();

   float getDisableAlphaGlass();

   float getGlassPower();

   float getGlassStrength();

   float getPadding();

   float getSplitters();

   float getAlbumColor();
}
