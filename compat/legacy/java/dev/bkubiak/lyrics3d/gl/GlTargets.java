package dev.bkubiak.lyrics3d.gl;

import org.joml.Matrix4f;

/** Minecraft's world pass already has the main target and the world projection bound. */
public final class GlTargets {
   private GlTargets() { }

   public static AutoCloseable world(Matrix4f projectionMatrix) {
      return () -> { };
   }
}
