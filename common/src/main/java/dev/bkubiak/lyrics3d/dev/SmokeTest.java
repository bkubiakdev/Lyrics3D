package dev.bkubiak.lyrics3d.dev;

import dev.bkubiak.lyrics3d.Lyrics3DClient;
import dev.bkubiak.lyrics3d.ui.menu.lyrics.LyricsEditorScreen;
import dev.bkubiak.lyrics3d.utility.render.lyrics.LyricsOptions;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

/** Development-only scripted run (-Dlyrics3d.smoke=true): modes, editor and in-game screenshots. Not shipped. */
public final class SmokeTest {
   private static int tick = -1;
   private static final String PREFIX = System.getProperty("lyrics3d.smoke.prefix", "smoke");

   public static void start() {
      ClientTickEvents.END_CLIENT_TICK.register(SmokeTest::onTick);
   }

   private static void shot(MinecraftClient mc, String name) {
      dev.bkubiak.lyrics3d.Platform.screenshot(mc, PREFIX + "_" + name + ".png");
      Lyrics3DClient.LOGGER.info("SMOKE shot {}", name);
   }

   private static void preset(String name) {
      var module = Lyrics3DClient.getInstance().getModule();
      module.options.preset(name);
      module.replay();
   }

   private static final boolean LIVE = Boolean.getBoolean("lyrics3d.smoke.live");
   private static int readyAt = -1;

   /** Real media session: waits for a playing track with timed lyrics, then shoots Classic, Neon and Scattered. */
   private static void live(MinecraftClient mc) {
      var module = Lyrics3DClient.getInstance().getModule();
      var tracker = Lyrics3DClient.getInstance().getMusicTracker();
      if (tick == 20) {
         mc.player.networkHandler.sendChatCommand("time set 6000");
         mc.player.networkHandler.sendChatCommand("weather clear");
         mc.inGameHud.getChatHud().clear(false);
         module.setEnabled(true);
         module.options.preview.enabled(false);
         module.options.follow.enabled(false);
         preset("classic");
      }
      if (tick % 100 == 0) {
         var playback = tracker.getLyricsPlayback();
         Lyrics3DClient.LOGGER.info("SMOKE live track='{}' status={} source={} lines={} playing={} position={}ms", tracker.getLastTrack(), tracker.getLyricsStatus(),
            tracker.getLyricsSource(), tracker.getSyncedLyrics().getLines().size(), playback.playing(), playback.positionMs());
      }
      if (readyAt < 0 && tick > 20 && !tracker.getSyncedLyrics().getLines().isEmpty() && tracker.getLyricsPlayback().playing()) readyAt = tick;
      if (readyAt >= 0) {
         int t = tick - readyAt;
         if (t == 100) shot(mc, "live_classic");
         if (t == 110) preset("neon");
         if (t == 230) shot(mc, "live_neon");
         if (t == 240) preset("scattered");
         if (t == 400) shot(mc, "live_scattered");
         if (t == 410) { Lyrics3DClient.LOGGER.info("SMOKE done"); mc.scheduleStop(); }
      } else if (tick == 3600) {
         Lyrics3DClient.LOGGER.info("SMOKE timeout: no playing track with timed lyrics");
         mc.scheduleStop();
      }
   }

   private static void onTick(MinecraftClient mc) {
      if (mc.player == null || mc.world == null) return;
      tick++;
      if (LIVE) { live(mc); return; }
      var module = Lyrics3DClient.getInstance().getModule();
      var o = module.options;
      switch (tick) {
         case 20 -> {
            mc.player.networkHandler.sendChatCommand("time set 6000");
            mc.player.networkHandler.sendChatCommand("weather clear");
            mc.player.networkHandler.sendChatCommand("gamerule doDaylightCycle false");
            module.setEnabled(true);
            o.preview.enabled(true);
            o.follow.enabled(false);
            preset("classic");
         }
         case 140 -> {
            mc.inGameHud.getChatHud().clear(false);
            mc.player.networkHandler.sendChatCommand("lyrics status");
            mc.player.networkHandler.sendChatCommand("lyrics offset 0");
            mc.player.networkHandler.sendChatCommand("lyrics");
         }
         case 150 -> shot(mc, "01_classic");
         case 160 -> preset("neon");
         case 270 -> shot(mc, "02_neon");
         case 280 -> preset("scattered");
         case 420 -> shot(mc, "03_scattered");
         case 430 -> { preset("classic"); o.follow.enabled(true); }
         case 560 -> shot(mc, "04_follow");
         case 570 -> { LyricsOptions.select(o.section, "appearance"); mc.setScreen(new LyricsEditorScreen(module, null)); }
         case 630 -> shot(mc, "05_editor");
         case 640 -> { mc.setScreen(null); LyricsOptions.select(o.section, "animations"); mc.setScreen(new LyricsEditorScreen(module, null)); }
         case 700 -> shot(mc, "06_editor_animations");
         case 710 -> { mc.setScreen(null); o.follow.enabled(false); o.preview.enabled(false); module.replay(); }
         case 940 -> {
            shot(mc, "07_live");
            var tracker = Lyrics3DClient.getInstance().getMusicTracker();
            Lyrics3DClient.LOGGER.info("SMOKE live track='{}' status={} source={} lines={}", tracker.getLastTrack(), tracker.getLyricsStatus(),
               tracker.getLyricsSource(), tracker.getSyncedLyrics().getLines().size());
         }
         case 960 -> { Lyrics3DClient.LOGGER.info("SMOKE done"); mc.scheduleStop(); }
         default -> { }
      }
      // Turn slowly while the follow preset is active, so following the camera is visible.
      if (tick > 430 && tick < 560) mc.player.setYaw(mc.player.getYaw() + 1.2F);
   }
}
