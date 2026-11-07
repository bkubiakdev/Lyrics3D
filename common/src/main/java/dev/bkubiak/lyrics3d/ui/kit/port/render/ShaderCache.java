package dev.bkubiak.lyrics3d.ui.kit.port.render;

import dev.bkubiak.lyrics3d.gl.*;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ShaderCache {
   private static final ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
   private static final Gson gson = new Gson();

   public static GlShaderKey getShaderProgramKeyByStringVertexFormatDefines(String text, GlFormat vertexFormat) {
      return new GlShaderKey(Identifier.of("lyrics3d", "core/ui/" + text), vertexFormat);
   }

   public static String getStringByStringIdentifier(String text, Identifier identifier) {
      try {
         String s;
         try (
            InputStream inputstream = resourceManager.open(identifier);
            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(inputstream));
         ) {
            s = bufferedreader.lines().collect(Collectors.joining(text));
         }

         return s;
      } catch (IOException ioexception) {
         throw new RuntimeException(ioexception);
      }
   }

   public static Object getObjectByStringClass(String text, Class value) {
      try {
         return gson.fromJson(text, value);
      } catch (Exception exception) {
         return null;
      }
   }
}
