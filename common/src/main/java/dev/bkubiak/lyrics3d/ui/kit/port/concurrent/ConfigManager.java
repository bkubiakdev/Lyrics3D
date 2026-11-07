package dev.bkubiak.lyrics3d.ui.kit.port.concurrent;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Editor UI state: colour picker format and saved swatches. */
public final class ConfigManager {
   private final Path path = FabricLoader.getInstance().getConfigDir().resolve("lyrics3d-ui.json");
   private String colorFormat;
   private List<Integer> colors = new ArrayList<>();

   public ConfigManager() {
      try {
         if (Files.isRegularFile(path)) {
            JsonObject json = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            if (json.has("colorFormat")) colorFormat = json.get("colorFormat").getAsString();
            if (json.has("colors")) for (var value : json.getAsJsonArray("colors")) colors.add(value.getAsInt());
         }
      } catch (Exception error) {
         dev.bkubiak.lyrics3d.Lyrics3DClient.LOGGER.warn("Could not load editor state", error);
      }
   }
   public String getColorFormat() { return colorFormat; }
   public void setColorFormat(String format) { colorFormat = format; save(); }
   public List<Integer> getList3() { return colors; }
   public void onList2(List<Integer> list) { colors = new ArrayList<>(list); save(); }

   public void save() {
      try {
         JsonObject json = new JsonObject();
         if (colorFormat != null) json.addProperty("colorFormat", colorFormat);
         JsonArray array = new JsonArray();
         colors.forEach(array::add);
         json.add("colors", array);
         Files.createDirectories(path.getParent());
         Files.writeString(path, json.toString(), StandardCharsets.UTF_8);
      } catch (Exception error) {
         dev.bkubiak.lyrics3d.Lyrics3DClient.LOGGER.warn("Could not save editor state", error);
      }
   }
}
