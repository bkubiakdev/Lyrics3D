package dev.bkubiak.lyrics3d;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.bkubiak.lyrics3d.systems.localization.Language;
import dev.bkubiak.lyrics3d.systems.localization.Localizator;
import dev.bkubiak.lyrics3d.systems.modules.modules.visuals.Lyrics3D;
import dev.bkubiak.lyrics3d.ui.menu.lyrics.LyricsEditorScreen;
import dev.bkubiak.lyrics3d.utility.sounds.MusicTracker;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.text.Text;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class Lyrics3DClient implements ClientModInitializer {
   public static final String MOD_ID = "lyrics3d";
   public static final Logger LOGGER = LoggerFactory.getLogger("Lyrics3D");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
   private static Lyrics3DClient instance;
   private MusicTracker musicTracker;
   private Lyrics3D module;
   public static final String DONATE_URL = "https://donate.bkubiak.dev";
   private boolean loading;
   private boolean donatePrompted;
   private String language = "";
   private KeyBinding editorKey, toggleKey;

   public static Identifier id(String path) { return Identifier.of(MOD_ID, path); }
   public static Lyrics3DClient getInstance() { return instance; }
   public MusicTracker getMusicTracker() { return musicTracker; }
   public Lyrics3D getModule() { return module; }

   @Override
   public void onInitializeClient() {
      instance = this;
      Localizator.loadTranslations();
      module = new Lyrics3D();
      loadConfig();
      musicTracker = new MusicTracker();
      LyricsCommands.register();
      editorKey = Platform.registerKey("key.lyrics3d.editor", GLFW.GLFW_KEY_RIGHT_SHIFT);
      toggleKey = Platform.registerKey("key.lyrics3d.toggle", GLFW.GLFW_KEY_UNKNOWN);
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         syncLanguage(client);
         while (editorKey.wasPressed()) if (client.currentScreen == null) openEditor(null);
         while (toggleKey.wasPressed()) { module.toggle(); saveConfig(); }
      });
      Platform.onWorldRender(event -> {
         if (!module.isEnabled()) return;
         try (var target = dev.bkubiak.lyrics3d.gl.GlTargets.world(event.getProjectionMatrix())) {
            module.render(event);
         } catch (Exception error) {
            LOGGER.warn("Lyrics render pass failed", error);
         }
      });
      ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
         if (donatePrompted || !(screen instanceof TitleScreen) || Boolean.getBoolean("lyrics3d.smoke")) return;
         donatePrompted = true;
         saveConfig();
         client.execute(() -> showDonatePrompt(client, screen));
      });
      if (Boolean.getBoolean("lyrics3d.smoke")) {
         try { Class.forName("dev.bkubiak.lyrics3d.dev.SmokeTest").getMethod("start").invoke(null); }
         catch (ReflectiveOperationException error) { LOGGER.warn("Smoke test unavailable", error); }
      }
      ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
         saveConfig();
         if (musicTracker != null) musicTracker.shutdown();
      });
   }

   public void openEditor(Screen parent) {
      MinecraftClient.getInstance().setScreen(new LyricsEditorScreen(module, parent));
   }

   /** First start: a short note that the mod is free and can be supported. */
   private void showDonatePrompt(MinecraftClient client, Screen parent) {
      syncLanguage(client);
      client.setScreen(new ConfirmScreen(open -> {
         if (open) openDonate(parent); else client.setScreen(parent);
      }, Text.literal(Localizator.translate("lyrics.donate.title")), Text.literal(Localizator.translate("lyrics.donate.message")),
         Text.literal(Localizator.translate("lyrics.donate.open")), Text.literal(Localizator.translate("lyrics.donate.later"))));
   }

   public static void openDonate(Screen parent) {
      Platform.openLink(parent, DONATE_URL);
   }

   private void syncLanguage(MinecraftClient client) {
      String code = client.options == null ? "en_us" : client.options.language;
      if (code.equals(language)) return;
      language = code;
      Language next = switch (code) {
         case "pl_pl" -> Language.PL_PL;
         default -> Language.EN_US;
      };
      Localizator.setLanguage(next);
   }

   private static Path configPath() { return FabricLoader.getInstance().getConfigDir().resolve("lyrics3d.json"); }

   private void loadConfig() {
      Path path = configPath();
      loading = true;
      try {
         JsonObject json = Files.exists(path) ? JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject() : new JsonObject();
         module.setEnabled(!json.has("enabled") || json.get("enabled").getAsBoolean());
         donatePrompted = json.has("donatePrompted") && json.get("donatePrompted").getAsBoolean();
         if (json.has("settings")) module.loadSettings(json.getAsJsonObject("settings"));
      } catch (Exception error) {
         LOGGER.warn("Cannot read {}, using defaults", path, error);
         module.setEnabled(true);
      } finally { loading = false; }
   }

   public void saveConfig() {
      if (loading || module == null) return;
      JsonObject json = new JsonObject();
      json.addProperty("enabled", module.isEnabled());
      json.addProperty("donatePrompted", donatePrompted);
      json.add("settings", module.saveSettings());
      Path path = configPath();
      try {
         Files.createDirectories(path.getParent());
         Path temp = path.resolveSibling("lyrics3d.json.tmp");
         Files.writeString(temp, GSON.toJson(json), StandardCharsets.UTF_8);
         Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (Exception error) {
         LOGGER.warn("Cannot save {}", path, error);
      }
   }
}
