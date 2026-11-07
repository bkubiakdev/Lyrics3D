package dev.bkubiak.lyrics3d.ui.kit.port.audio;
import dev.bkubiak.lyrics3d.ui.kit.port.enums.SoundEvent;
public final class SoundEngine {
 private static final SoundEngine INSTANCE=new SoundEngine();public static SoundEngine getInstance(){return INSTANCE;}
 public void onSoundEvent(SoundEvent event){dev.bkubiak.lyrics3d.ui.kit.KitSounds.play(event);}
}