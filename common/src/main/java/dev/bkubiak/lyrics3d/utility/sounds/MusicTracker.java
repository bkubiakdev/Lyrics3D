package dev.bkubiak.lyrics3d.utility.sounds;

import dev.redstones.mediaplayerinfo.IMediaSession;
import dev.redstones.mediaplayerinfo.MediaInfo;
import dev.redstones.mediaplayerinfo.MediaPlayerInfo;
import dev.bkubiak.lyrics3d.Lyrics3DClient;
import java.util.List;

/** Windows media sessions (Spotify and others) and timed lyrics for the current track. */
public final class MusicTracker {
   private final Thread thread;
   private volatile boolean running = true;
   private volatile IMediaSession session;
   private final java.util.concurrent.ThreadPoolExecutor fetchExecutor = new java.util.concurrent.ThreadPoolExecutor(
      1, 1, 30, java.util.concurrent.TimeUnit.SECONDS, new java.util.concurrent.ArrayBlockingQueue<>(1), task -> {
         Thread worker = new Thread(task, "Lyrics3D Lyrics Fetcher"); worker.setDaemon(true); return worker;
      }, new java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy());
   private volatile String lyrics = "";
   private volatile SyncedLyrics syncedLyrics = SyncedLyrics.EMPTY;
   private volatile boolean fetchingLyrics = false;
   private volatile boolean approximateLyrics = true;
   private volatile boolean lyricsRefresh;
   private volatile String lyricsStatus = "idle";
   private volatile String lyricsSource = "";
   private volatile long lyricsRecordingSeconds;

   public synchronized void setApproximateLyrics(boolean allowed) {
      if (approximateLyrics == allowed) return;
      approximateLyrics = allowed;
      ++trackGeneration; // Any in-flight result for the old matching policy is stale.
      syncedLyrics = SyncedLyrics.EMPTY;
      lyricsStatus = "loading";
      lyricsRefresh = true;
   }
   public String getLyricsStatus() { return lyricsStatus; }
   public String getLyricsSource() { return lyricsSource; }
   public long getLyricsRecordingSeconds() { return lyricsRecordingSeconds; }

   private volatile String lastTrack = "";
   private volatile long lastPositionSeconds = -1L;
   private volatile long lastPositionTimestamp = 0L;
   private volatile boolean wasPlaying = false;
   private volatile long trackGeneration;

   public MusicTracker() {
      this.thread = new Thread(() -> {
         try {
            while (this.running) {
               try {
                  Thread.sleep(500L);
                  if (this.running) this.onScheduleTask();
               } catch (InterruptedException var2) {
                  if (!this.running) {
                     break;
                  }
               }
            }
         } finally {
            try {
               MediaPlayerInfo.INSTANCE.close();
               Lyrics3DClient.LOGGER.info("[MusicTracker] Native media manager released");
            } catch (Throwable failure) {
               Lyrics3DClient.LOGGER.error("[MusicTracker] Native media cleanup failed", failure);
            }
         }
      }, "Lyrics3D Music Tracker");
      this.thread.setDaemon(true);
      this.thread.start();
   }

   private volatile long lastFetchTime = 0L;
   private volatile long lastSessionLog = 0L;

   private void onScheduleTask() {
      try {
         List<IMediaSession> sessions = MediaPlayerInfo.INSTANCE.getMediaSessions();
         if (sessions == null) {
            return;
         }

         long now = System.currentTimeMillis();
         if (Lyrics3DClient.LOGGER.isDebugEnabled() && now - lastSessionLog > 4000L) {
            lastSessionLog = now;
            if (sessions.isEmpty()) {
               Lyrics3DClient.LOGGER.debug("[MusicTracker] Polling Windows media sessions... (0 active sessions found)");
            } else {
               Lyrics3DClient.LOGGER.debug("[MusicTracker] Found {} active Windows session(s):", sessions.size());
               for (IMediaSession s : sessions) {
                  MediaInfo m = s != null ? s.getMedia() : null;
                  Lyrics3DClient.LOGGER.debug("   -> [{}] Title='{}', Artist='{}', Playing={}",
                     (s != null ? s.getOwner() : "null"),
                     (m != null ? m.getTitle() : "null"),
                     (m != null ? m.getArtist() : "null"),
                     (m != null && m.isPlaying()));
               }
            }
         }

         IMediaSession playing = sessions.stream()
            .filter(s -> s != null && s.getMedia() != null && s.getMedia().getTitle() != null && !s.getMedia().getTitle().isEmpty() && s.getMedia().isPlaying())
            .findFirst()
            .orElse(null);

         IMediaSession found = playing != null ? playing : sessions.stream()
            .filter(s -> s != null && s.getMedia() != null && s.getMedia().getTitle() != null && !s.getMedia().getTitle().isEmpty())
            .findFirst()
            .orElse(null);

         if (found != null && found.getMedia() != null) {
            this.session = found;
            String artist = nullToEmpty(found.getMedia().getArtist());
            String title = nullToEmpty(found.getMedia().getTitle());
            String trackId = artist + " - " + title;
            boolean changed = !trackId.equals(this.lastTrack);
            if (changed) {
               synchronized (this) {
                  this.lastTrack = trackId;
                  ++this.trackGeneration;
                  this.syncedLyrics = SyncedLyrics.EMPTY;
                  this.lyricsStatus = "loading";
                  this.lastPositionSeconds = -1L;
               }
            }
            // A network failure is retried after 10 s; "not found" and rate limits keep the 60 s pause.
            long retryMs = "unavailable".equals(lyricsStatus) ? 10000L : 60000L;
            if (changed || lyricsRefresh || ((!this.syncedLyrics.isSynced() || this.syncedLyrics.isEmpty()) && !this.fetchingLyrics && now - lastFetchTime > retryMs)) {
               lyricsRefresh = false;
               final boolean allowApproximate = approximateLyrics;
               final long generation = trackGeneration;
               final long duration = found.getMedia().getDuration();
               fetchingLyrics = true;
               lastFetchTime = now;
               // At most one running request and one newest pending track. No native images in this queue.
               fetchExecutor.execute(() -> {
                  try {
                     if (!running || generation != trackGeneration) return;
                     var result = LyricsFetcher.fetchLyricsResult(artist, title, duration, allowApproximate);
                     synchronized (this) {
                        if (running && generation == trackGeneration && result != null) {
                           syncedLyrics = result.lyrics();
                           if (!lyricsStatus.equals(result.status()) || !lyricsSource.equals(result.source()))
                              Lyrics3DClient.LOGGER.info("[Lyrics] status={}, source={}, recording={}s, playback={}s", result.status(), result.source(), result.recordingSeconds(), duration);
                           lyricsSource = result.source();
                           lyricsRecordingSeconds = result.recordingSeconds();
                           lyricsStatus = result.status();
                        }
                     }
                  } catch (Exception failure) {
                     Lyrics3DClient.LOGGER.warn("Media metadata request failed: {}", failure.getClass().getSimpleName());
                     if (generation == trackGeneration) lyricsStatus = "unavailable";
                  } finally {
                     synchronized (this) {
                        if (generation == trackGeneration) fetchingLyrics = false;
                     }
                  }
               });
            }
         } else {
            synchronized (this) {
               if (this.session != null) ++this.trackGeneration;
               this.session = null;
               this.lastTrack = "";
               this.lyricsStatus = "idle";
               this.syncedLyrics = SyncedLyrics.EMPTY;
               this.fetchingLyrics = false;
               this.lastPositionSeconds = -1L;
            }
         }
      } catch (Throwable t) {
         // Silently ignore or print once
      }
   }

   public void shutdown() {
      this.running = false;
      this.thread.interrupt();
      this.fetchExecutor.shutdownNow();
      synchronized (this) { ++trackGeneration; }
      this.session = null;
      try {
         this.thread.join(2000L);
         if (this.thread.isAlive()) Lyrics3DClient.LOGGER.warn("[MusicTracker] Native media call still pending during shutdown");
      } catch (InterruptedException interrupted) {
         Thread.currentThread().interrupt();
      }
   }

   private static String nullToEmpty(String value) { return value == null ? "" : value; }

   public boolean haveActiveSession() {
      return this.session != null;
   }

   public Thread getThread() {
      return this.thread;
   }

   public IMediaSession getSession() {
      return this.session;
   }

   public String getLyrics() {
      return this.lyrics;
   }

   public String getLastTrack() {
      return this.lastTrack;
   }

   public SyncedLyrics getSyncedLyrics() {
      return this.syncedLyrics;
   }

   public boolean isFetchingLyrics() {
      return this.fetchingLyrics;
   }

   public record LyricsPlayback(String track, SyncedLyrics lyrics, long positionMs, long durationMs, boolean playing) { }

   /** One track-consistent snapshot for world lyrics; native polling and downloads stay on their workers. */
   public synchronized LyricsPlayback getLyricsPlayback() {
      IMediaSession current = this.session;
      MediaInfo media = current == null ? null : current.getMedia();
      if (media == null) return new LyricsPlayback("", SyncedLyrics.EMPTY, 0, 0, false);
      String key = nullToEmpty(media.getArtist()) + " - " + nullToEmpty(media.getTitle());
      if (!key.equals(this.lastTrack)) return new LyricsPlayback("", SyncedLyrics.EMPTY, 0, 0, false);
      return new LyricsPlayback(key, this.syncedLyrics, smoothPositionMs(media),
          Math.max(0L, media.getDuration() * 1000L), media.isPlaying());
   }

   public synchronized long getSmoothPositionMs() {
      IMediaSession s = this.session;
      if (s == null || s.getMedia() == null) {
         return 0L;
      }
      return smoothPositionMs(s.getMedia());
   }

   private long smoothPositionMs(MediaInfo media) {
      long posSec = Math.max(0L, media.getPosition());
      boolean playing = media.isPlaying();
      long now = System.currentTimeMillis();

      if (posSec != this.lastPositionSeconds || playing != this.wasPlaying) {
         this.lastPositionSeconds = posSec;
         this.lastPositionTimestamp = now;
         this.wasPlaying = playing;
      }

      long durationMs = Math.max(0L, media.getDuration() * 1000L);
      if (playing) {
         long elapsed = Math.min(1000L, Math.max(0L, now - this.lastPositionTimestamp));
         long calculated = posSec * 1000L + elapsed;
         return durationMs > 0L ? Math.min(durationMs, calculated) : calculated;
      } else {
         return posSec * 1000L;
      }
   }
}
