package dev.bkubiak.lyrics3d.ui.kit;

import dev.bkubiak.lyrics3d.ui.kit.port.enums.SoundEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import java.util.Locale;

/** Editor click samples at the default interface volume (20 %). */
public final class KitSounds {
    private static final float VOLUME = .2F;
    private static long lastTick;
    private KitSounds() {}
    private static void play(String file, float volume, float pitch) {
        var mc = MinecraftClient.getInstance();
        if (mc == null || mc.getSoundManager() == null || mc.getOverlay() != null || volume <= 0) return;
        mc.getSoundManager().play(new PositionedSoundInstance(Identifier.of("lyrics3d", "ui/" + file), SoundCategory.MASTER, volume, pitch,
            SoundInstance.createRandom(), false, 0, SoundInstance.AttenuationType.NONE, 0, 0, 0, true));
    }
    public static void play(SoundEvent event) {
        play(event.getText().toLowerCase(Locale.ROOT).replace(".wav", ""), VOLUME, 1);
    }
    public static void scroll() { tick(.18F, 1.2F); }
    public static void slider() { tick(.14F, 1.45F); }
    private static void tick(float gain, float pitch) {
        long now = System.nanoTime();
        if (now - lastTick < 65_000_000L) return;
        lastTick = now;
        play("6", VOLUME * gain, pitch);
    }
}
