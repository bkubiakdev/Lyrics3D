package dev.bkubiak.lyrics3d.gl;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

/** Uniform of the bound program; a missing location (-1) ignores writes. Values are written while the program is in use. */
public final class GlUniform {
   static final GlUniform MISSING = new GlUniform(null, -1);
   private final GlShader shader;
   private final int location;

   GlUniform(GlShader shader, int location) {
      this.shader = shader;
      this.location = location;
   }

   private boolean use() {
      if (location < 0) return false;
      shader.use();
      return true;
   }

   public void set(float value) { if (use()) GL20.glUniform1f(location, value); }
   public void set(int value) { if (use()) GL20.glUniform1i(location, value); }
   public void set(float a, float b) { if (use()) GL20.glUniform2f(location, a, b); }
   public void set(float a, float b, float c) { if (use()) GL20.glUniform3f(location, a, b, c); }
   public void set(float a, float b, float c, float d) { if (use()) GL20.glUniform4f(location, a, b, c, d); }
   public void set(float[] v) {
      if (!use()) return;
      switch (v.length) {
         case 1 -> GL20.glUniform1f(location, v[0]);
         case 2 -> GL20.glUniform2f(location, v[0], v[1]);
         case 3 -> GL20.glUniform3f(location, v[0], v[1], v[2]);
         case 4 -> GL20.glUniform4f(location, v[0], v[1], v[2], v[3]);
         case 16 -> GL20.glUniformMatrix4fv(location, false, v);
         default -> GL20.glUniform1fv(location, v);
      }
   }
   public void set(Matrix4f matrix) {
      if (!use()) return;
      try (MemoryStack stack = MemoryStack.stackPush()) {
         GL20.glUniformMatrix4fv(location, false, matrix.get(stack.mallocFloat(16)));
      }
   }
}
