package dev.bkubiak.lyrics3d.gl;

import org.joml.Matrix4f;

/** Uniform handle; a missing uniform ignores writes. */
public final class GlUniform {
   static final GlUniform MISSING = new GlUniform(null);
   private final net.minecraft.client.gl.GlUniform uniform;

   GlUniform(net.minecraft.client.gl.GlUniform uniform) {
      this.uniform = uniform;
   }

   public void set(float value) { if (uniform != null) uniform.set(value); }
   public void set(int value) { if (uniform != null) uniform.set(value); }
   public void set(float a, float b) { if (uniform != null) uniform.set(a, b); }
   public void set(float a, float b, float c) { if (uniform != null) uniform.set(a, b, c); }
   public void set(float a, float b, float c, float d) { if (uniform != null) uniform.set(a, b, c, d); }
   public void set(float[] values) { if (uniform != null) uniform.set(values); }
   public void set(Matrix4f matrix) { if (uniform != null) uniform.set(matrix); }
}
