package dev.bkubiak.lyrics3d.ui.menu.clickgui;

import dev.bkubiak.lyrics3d.utility.colors.ColorRGBA;

/** Render-scoped V2 palette. Closing the scope restores the HUD/other screens' palette. */
public final class ClickGuiV2Theme implements AutoCloseable {
    private static final ThreadLocal<ClickGuiV2Theme> ACTIVE = new ThreadLocal<>();
    private final ClickGuiV2Theme previous;
    public final boolean dark;
    public final boolean liquid;
    public final ColorRGBA window, surface, field, hover, text, muted, border, accent, selection;

    private ClickGuiV2Theme(boolean dark, ColorRGBA accent, boolean liquid) {
        this.previous = ACTIVE.get();
        this.dark = dark;
        this.liquid = liquid;
        this.window = liquid ? dev.bkubiak.lyrics3d.utility.colors.Colors.getBackgroundColor() : dark ? new ColorRGBA(20, 22, 26) : new ColorRGBA(240, 241, 242);
        this.surface = liquid ? dev.bkubiak.lyrics3d.utility.colors.Colors.getBackgroundColor() : dark ? new ColorRGBA(30, 33, 39) : ColorRGBA.WHITE;
        this.field = liquid ? dev.bkubiak.lyrics3d.utility.colors.Colors.getAdditionalColor() : dark ? new ColorRGBA(43, 47, 55) : new ColorRGBA(230, 233, 238);
        this.hover = liquid ? dev.bkubiak.lyrics3d.utility.colors.Colors.getAdditionalColor() : dark ? new ColorRGBA(38, 42, 49) : new ColorRGBA(247, 250, 252);
        this.text = liquid ? dev.bkubiak.lyrics3d.utility.colors.Colors.getTextColor() : dark ? new ColorRGBA(239, 241, 245) : ColorRGBA.BLACK;
        this.muted = liquid ? this.text.mulAlpha(0.62F) : dark ? new ColorRGBA(165, 172, 184) : new ColorRGBA(91, 98, 109);
        this.border = liquid ? dev.bkubiak.lyrics3d.utility.colors.Colors.getOutlineColor() : dark ? new ColorRGBA(61, 66, 77) : new ColorRGBA(212, 217, 224);
        this.accent = accent.withAlpha(255);
        this.selection = this.surface.mix(this.accent, dark ? 0.28F : 0.17F);
        ACTIVE.set(this);
    }

    public static ClickGuiV2Theme open(boolean dark, ColorRGBA accent) { return new ClickGuiV2Theme(dark, accent, false); }
    /** Reuse the flat component rendering with the current UI kit palette. */
    public static ClickGuiV2Theme openKit() {
        return new ClickGuiV2Theme(dev.bkubiak.lyrics3d.ui.kit.port.data.ThemeConfig.getGuiPalette());
    }

    private ClickGuiV2Theme(dev.bkubiak.lyrics3d.ui.kit.port.data.Palette palette) {
        this.previous = ACTIVE.get();
        this.liquid = false;
        this.window = ColorRGBA.fromInt(palette.getValue());
        this.surface = ColorRGBA.fromInt(palette.getValue2());
        this.field = ColorRGBA.fromInt(palette.getValue3());
        this.border = ColorRGBA.fromInt(dev.bkubiak.lyrics3d.ui.kit.KitMaterial.panelBorder(
            palette.getValue2(), palette.getValue4()));
        this.muted = ColorRGBA.fromInt(palette.getValue5());
        this.accent = ColorRGBA.fromInt(palette.getValue6());
        this.text = ColorRGBA.fromInt(palette.getValue7());
        this.dark = this.window.getRed() < 128;
        this.hover = this.field;
        this.selection = this.surface.mix(this.accent, this.dark ? 0.18F : 0.1F);
        ACTIVE.set(this);
    }
    public static ClickGuiV2Theme openLiquid() {
        return new ClickGuiV2Theme(true, dev.bkubiak.lyrics3d.utility.colors.Colors.getAccent(), true);
    }
    public static ClickGuiV2Theme current() { return ACTIVE.get(); }
    public static boolean active() { return current() != null && !current().liquid; }
    @Override public void close() {
        if (this.previous == null) ACTIVE.remove(); else ACTIVE.set(this.previous);
    }
}
