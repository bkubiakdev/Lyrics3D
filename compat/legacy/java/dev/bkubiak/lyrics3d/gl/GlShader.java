package dev.bkubiak.lyrics3d.gl;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import org.lwjgl.opengl.GL20;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Program built from the mod's shader json (vertex, fragment, uniforms with defaults, samplers). */
public final class GlShader {
   private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*#moj_import\\s*<([^>]+)>\\s*$");
   private final int program;
   private final GlFormat format;
   private final Map<String, GlUniform> uniforms = new HashMap<>();
   final List<String> samplers = new ArrayList<>();

   GlShader(GlShaderKey key) {
      this.format = key.format();
      int vs = 0, fs = 0, p = 0;
      try {
         JsonObject json = JsonParser.parseString(read(key.id().withPath("shaders/" + key.id().getPath() + ".json"))).getAsJsonObject();
         vs = compile(GL20.GL_VERTEX_SHADER, source(key.id(), json.get("vertex").getAsString(), ".vsh"));
         fs = compile(GL20.GL_FRAGMENT_SHADER, source(key.id(), json.get("fragment").getAsString(), ".fsh"));
         p = GL20.glCreateProgram();
         GL20.glAttachShader(p, vs);
         GL20.glAttachShader(p, fs);
         var names = key.format().names;
         for (int i = 0; i < names.length; i++) GL20.glBindAttribLocation(p, i, names[i]);
         GL20.glLinkProgram(p);
         if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == 0) throw new IllegalStateException(GL20.glGetProgramInfoLog(p));
         program = p;
         use();
         if (json.has("uniforms")) for (var value : json.getAsJsonArray("uniforms")) {
            var u = value.getAsJsonObject();
            GlUniform uniform = getUniform(u.get("name").getAsString());
            if (uniform == null || !u.has("values")) continue;
            var values = u.getAsJsonArray("values");
            if (u.get("type").getAsString().equals("int")) uniform.set(values.get(0).getAsInt());
            else {
               float[] f = new float[values.size()];
               for (int i = 0; i < f.length; i++) f[i] = values.get(i).getAsFloat();
               uniform.set(f);
            }
         }
         if (json.has("samplers")) for (var value : json.getAsJsonArray("samplers")) {
            String name = value.getAsJsonObject().get("name").getAsString();
            GlUniform sampler = getUniform(name);
            if (sampler != null) sampler.set(samplers.size());
            samplers.add(name);
         }
      } catch (Exception error) {
         if (p != 0) GL20.glDeleteProgram(p);
         throw new IllegalStateException("Cannot build shader " + key.id(), error);
      } finally {
         if (vs != 0) GL20.glDeleteShader(vs);
         if (fs != 0) GL20.glDeleteShader(fs);
      }
   }

   private static String read(Identifier id) throws Exception {
      try (var input = MinecraftClient.getInstance().getResourceManager().open(id)) {
         return new String(input.readAllBytes(), StandardCharsets.UTF_8);
      }
   }

   private static String source(Identifier owner, String name, String suffix) throws Exception {
      Identifier id = name.contains(":") ? Identifier.of(name) : Identifier.of(owner.getNamespace(), name);
      return includes(read(id.withPath("shaders/" + id.getPath() + suffix)), 0);
   }

   private static String includes(String text, int depth) throws Exception {
      if (depth > 8) throw new IllegalStateException("Shader include depth");
      Matcher m = IMPORT.matcher(text);
      StringBuilder out = new StringBuilder();
      while (m.find()) {
         Identifier include = Identifier.of(m.group(1));
         String body = includes(read(include.withPath("shaders/include/" + include.getPath())), depth + 1);
         m.appendReplacement(out, Matcher.quoteReplacement(body));
      }
      m.appendTail(out);
      return out.toString();
   }

   private static int compile(int kind, String source) {
      int shader = GL20.glCreateShader(kind);
      GL20.glShaderSource(shader, source);
      GL20.glCompileShader(shader);
      if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == 0) {
         String error = GL20.glGetShaderInfoLog(shader);
         GL20.glDeleteShader(shader);
         throw new IllegalStateException(error);
      }
      return shader;
   }

   void use() { GlStateManager._glUseProgram(program); }
   GlFormat format() { return format; }

   /** Null when the program has no such (active) uniform. */
   public GlUniform getUniform(String name) {
      GlUniform uniform = uniforms.computeIfAbsent(name, n -> {
         int location = GL20.glGetUniformLocation(program, n);
         return location < 0 ? GlUniform.MISSING : new GlUniform(this, location);
      });
      return uniform == GlUniform.MISSING ? null : uniform;
   }

   public GlUniform getUniformOrDefault(String name) {
      GlUniform uniform = getUniform(name);
      return uniform == null ? GlUniform.MISSING : uniform;
   }

   public int getGlRef() { return program; }

   void close() { GL20.glDeleteProgram(program); }
}
