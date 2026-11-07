package dev.bkubiak.lyrics3d.gl;

import net.minecraft.client.gl.ShaderProgram;
import java.util.HashMap;
import java.util.Map;

/** A linked shader program with cached uniform handles. */
public final class GlShader {
   private final ShaderProgram program;
   private final Map<String, GlUniform> uniforms = new HashMap<>();

   GlShader(ShaderProgram program) {
      this.program = program;
   }

   ShaderProgram program() { return program; }

   /** Null when the program has no such uniform. */
   public GlUniform getUniform(String name) {
      GlUniform uniform = uniforms.computeIfAbsent(name, n -> {
         var raw = program.getUniform(n);
         return raw == null ? GlUniform.MISSING : new GlUniform(raw);
      });
      return uniform == GlUniform.MISSING ? null : uniform;
   }

   /** Never null: writes to a missing uniform are ignored. */
   public GlUniform getUniformOrDefault(String name) {
      GlUniform uniform = getUniform(name);
      return uniform == null ? GlUniform.MISSING : uniform;
   }

   public int getGlRef() { return program.getGlRef(); }
}
