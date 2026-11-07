package dev.bkubiak.lyrics3d.systems.event.impl.render;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** World pass after terrain, entities and translucency; before the hand. */
public final class Render3DEvent {
   private final MatrixStack matrices;
   private final Matrix4f positionMatrix;
   private final Matrix4f projectionMatrix;
   private final Vec3d cameraPos;
   private final Quaternionf cameraRotation;
   private final float tickDelta;

   public Render3DEvent(MatrixStack matrices, Matrix4f positionMatrix, Matrix4f projectionMatrix, Vec3d cameraPos, Quaternionf cameraRotation, float tickDelta) {
      this.matrices = matrices;
      this.positionMatrix = positionMatrix;
      this.projectionMatrix = projectionMatrix;
      this.cameraPos = cameraPos;
      this.cameraRotation = new Quaternionf(cameraRotation);
      this.tickDelta = tickDelta;
   }
   public MatrixStack getMatrices() { return matrices; }
   public Matrix4f getPositionMatrix() { return positionMatrix; }
   public Matrix4f getProjectionMatrix() { return projectionMatrix; }
   public Vec3d getCameraPos() { return cameraPos; }
   public Quaternionf getCameraRotation() { return cameraRotation; }
   public float getTickDelta() { return tickDelta; }
}
