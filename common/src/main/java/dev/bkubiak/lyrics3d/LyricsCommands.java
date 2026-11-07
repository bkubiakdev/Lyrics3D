package dev.bkubiak.lyrics3d;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.bkubiak.lyrics3d.systems.localization.Localizator;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

/**
 * /lyrics [on|off|toggle|editor|status|preview on|off|preset classic|neon|scattered|offset &lt;ms&gt;].
 * Client-side only; works on any server.
 */
public final class LyricsCommands {
   private LyricsCommands() { }

   public static void register() {
      ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(literal("lyrics")
         .executes(LyricsCommands::help)
         .then(literal("on").executes(c -> enabled(c, true)))
         .then(literal("off").executes(c -> enabled(c, false)))
         .then(literal("toggle").executes(c -> enabled(c, !client().getModule().isEnabled())))
         .then(literal("editor").executes(LyricsCommands::editor))
         .then(literal("status").executes(LyricsCommands::status))
         .then(literal("donate").executes(LyricsCommands::donate))
         .then(literal("language")
            .then(literal("pl").executes(c -> language(c, "pl_pl")))
            .then(literal("en").executes(c -> language(c, "en_us")))
            .then(literal("auto").executes(c -> language(c, null))))
         .then(literal("pl").executes(c -> language(c, "pl_pl")))
         .then(literal("en").executes(c -> language(c, "en_us")))
         .then(literal("preview")
            .then(literal("on").executes(c -> preview(c, true)))
            .then(literal("off").executes(c -> preview(c, false))))
         .then(literal("preset")
            .then(literal("classic").executes(c -> preset(c, "classic")))
            .then(literal("neon").executes(c -> preset(c, "neon")))
            .then(literal("scattered").executes(c -> preset(c, "scattered"))))
         .then(literal("offset")
            .executes(c -> offset(c, null))
            .then(argument("ms", IntegerArgumentType.integer(-30000, 30000)).executes(c -> offset(c, IntegerArgumentType.getInteger(c, "ms")))))));
   }

   private static Lyrics3DClient client() { return Lyrics3DClient.getInstance(); }
   private static String tr(String key, Object... args) { return Localizator.translate(key, args); }
   private static int reply(CommandContext<FabricClientCommandSource> c, String key, Object... args) {
      c.getSource().sendFeedback(Text.literal("[Lyrics3D] ").styled(s -> s.withColor(0xB57CFF)).append(Text.literal(tr(key, args)).styled(s -> s.withColor(0xFFFFFF))));
      return 1;
   }

   private static int help(CommandContext<FabricClientCommandSource> c) {
      reply(c, "lyrics.command.state", tr(client().getModule().isEnabled() ? "lyrics.command.on" : "lyrics.command.off"));
      reply(c, "lyrics.command.help");
      return donate(c);
   }

   private static int language(CommandContext<FabricClientCommandSource> c, String code) {
      client().setLanguage(code);
      return reply(c, code == null ? "lyrics.command.language_auto" : "lyrics.command.language");
   }

   private static int donate(CommandContext<FabricClientCommandSource> c) {
      c.getSource().sendFeedback(Text.literal("[Lyrics3D] ").styled(s -> s.withColor(0xB57CFF))
         .append(Text.literal(tr("lyrics.command.donate") + " ").styled(s -> s.withColor(0xFFFFFF)))
         .append(Platform.link(Lyrics3DClient.DONATE_URL)));
      return 1;
   }

   private static int enabled(CommandContext<FabricClientCommandSource> c, boolean value) {
      client().getModule().setEnabled(value);
      client().saveConfig();
      return reply(c, "lyrics.command.state", tr(value ? "lyrics.command.on" : "lyrics.command.off"));
   }

   private static int editor(CommandContext<FabricClientCommandSource> c) {
      // The chat screen closes after the command; open the editor on the next frame.
      MinecraftClient.getInstance().send(() -> client().openEditor(null));
      return 1;
   }

   private static int status(CommandContext<FabricClientCommandSource> c) {
      var tracker = client().getMusicTracker();
      String track = tracker == null ? "" : tracker.getLastTrack();
      if (track == null || track.isEmpty()) return reply(c, "lyrics.command.no_track");
      int lines = tracker.getSyncedLyrics().getLines().size();
      String source = tracker.getLyricsSource();
      return reply(c, "lyrics.command.status", track, tracker.getLyricsStatus(), source == null || source.isEmpty() ? "-" : source, lines);
   }

   private static int preview(CommandContext<FabricClientCommandSource> c, boolean value) {
      var module = client().getModule();
      module.options.preview.enabled(value);
      module.replay();
      client().saveConfig();
      return reply(c, value ? "lyrics.command.preview_on" : "lyrics.command.preview_off");
   }

   private static int preset(CommandContext<FabricClientCommandSource> c, String name) {
      var module = client().getModule();
      module.options.preset(name);
      module.replay();
      client().saveConfig();
      return reply(c, "lyrics.command.preset", tr("modules.settings.lyrics_3d." + (name.equals("scattered") ? "preset_scattered" : name)));
   }

   private static int offset(CommandContext<FabricClientCommandSource> c, Integer ms) {
      var offset = client().getModule().options.offset;
      if (ms != null) {
         offset.setCurrentValue(ms);
         client().saveConfig();
      }
      return reply(c, "lyrics.command.offset", Math.round(offset.getCurrentValue()));
   }

}
