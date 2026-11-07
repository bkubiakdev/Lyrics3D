package dev.bkubiak.lyrics3d.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.GlTexture;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

/** Render targets for the mod's own OpenGL passes (1.21.5+). */
public final class GlTargets {
   private static int fbo, color, depth;

   private GlTargets() { }

   private static int id(GpuTexture texture) {
      return texture instanceof GlTexture gl ? gl.getGlId() : 0;
   }

   /** Binds the main world framebuffer (colour + depth) with the world projection; close() restores the previous state. */
   public static AutoCloseable world(Matrix4f projectionMatrix) {
      var target = MinecraftClient.getInstance().getFramebuffer();
      int c = id(target.getColorAttachment()), d = target.useDepthAttachment ? id(target.getDepthAttachment()) : 0;
      int previousDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING), previousRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      int[] viewport = new int[4];
      GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
      Matrix4f previousProjection = new Matrix4f(Gl.getProjectionMatrix());
      Matrix4f previousModelView = new Matrix4f(Gl.getModelViewMatrix());
      if (fbo == 0 || c != color || d != depth) {
         if (fbo == 0) fbo = GL30.glGenFramebuffers();
         color = c;
         depth = d;
         GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
         GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, c, 0);
         GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, d, 0);
      }
      GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
      GlStateManager._viewport(0, 0, target.textureWidth, target.textureHeight);
      GL11.glColorMask(true, true, true, true);
      Gl.setProjectionMatrix(projectionMatrix);
      Gl.getModelViewMatrix().identity();
      return () -> {
         Gl.setProjectionMatrix(previousProjection);
         Gl.getModelViewMatrix().set(previousModelView);
         GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
         GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
         GlStateManager._viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
      };
   }

   /** 1.21.5 draws the GUI immediately: the mod draws straight into the main framebuffer with the GUI projection. */
   public static AutoCloseable gui() {
      var mc = MinecraftClient.getInstance();
      var window = mc.getWindow();
      AutoCloseable restore = world(new Matrix4f().setOrtho(0, (float)(window.getFramebufferWidth() / window.getScaleFactor()),
         (float)(window.getFramebufferHeight() / window.getScaleFactor()), 0, 1000, 21000));
      Gl.getModelViewMatrix().translation(0, 0, -11000);
      GlStateManager._disableDepthTest();
      return restore;
   }
}
