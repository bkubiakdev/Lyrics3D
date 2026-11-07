package dev.bkubiak.lyrics3d.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Off-screen layer for screens drawn by the mod (1.21.6+ defers GUI drawing): the mod draws into a private
 * framebuffer, the result (straight alpha) is a registered texture the screen hands to Minecraft's GUI.
 */
public final class GuiSurface {
   private static final Identifier ID = Identifier.of("lyrics3d", "runtime/gui_surface");
   private static NativeImageBackedTexture texture;
   private static int fbo, inputFbo, inputTexture, width, height, resolveProgram, resolveVao;
   private static int previousDraw, previousRead;
   private static final int[] viewport = new int[4];
   private static boolean scissor;
   private static Matrix4f previousProjection;
   private static final Matrix4f previousModelView = new Matrix4f();

   private GuiSurface() { }

   /** Binds a cleared layer and the GUI projection (scaled pixels, like Minecraft's GUI). */
   public static void begin() {
      var mc = MinecraftClient.getInstance();
      var window = mc.getWindow();
      previousDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
      previousRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
      GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
      scissor = GL11.glIsEnabled(GL11.GL_SCISSOR_TEST);
      previousProjection = new Matrix4f(Gl.getProjectionMatrix());
      previousModelView.set(Gl.getModelViewMatrix());
      resize(window.getFramebufferWidth(), window.getFramebufferHeight());
      GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, inputFbo);
      GlStateManager._viewport(0, 0, width, height);
      GlStateManager._disableScissorTest();
      GL11.glColorMask(true, true, true, true);
      GL11.glClearColor(0, 0, 0, 0);
      GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
      double scale = window.getScaleFactor();
      Gl.setProjectionMatrix(new Matrix4f().setOrtho(0, (float)(width / scale), (float)(height / scale), 0, 1000, 21000));
      Gl.getModelViewMatrix().translation(0, 0, -11000);
      GlStateManager._disableDepthTest();
   }

   /** Resolves the layer; returns the texture to draw over the whole framebuffer (flipped vertically). */
   public static Identifier end() {
      resolve();
      GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
      GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
      GlStateManager._viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
      if (scissor) GlStateManager._enableScissorTest(); else GlStateManager._disableScissorTest();
      Gl.setProjectionMatrix(previousProjection);
      Gl.getModelViewMatrix().set(previousModelView);
      return ID;
   }

   public static int width() { return width; }
   public static int height() { return height; }

   private static void resize(int w, int h) {
      if (texture != null && width == w && height == h) return;
      var mc = MinecraftClient.getInstance();
      if (fbo != 0) {
         GL30.glDeleteFramebuffers(fbo);
         GL30.glDeleteFramebuffers(inputFbo);
         GL11.glDeleteTextures(inputTexture);
         mc.getTextureManager().destroyTexture(ID);
      }
      width = w;
      height = h;
      texture = new NativeImageBackedTexture(() -> "Lyrics3D GUI", w, h, false);
      mc.getTextureManager().registerTexture(ID, texture);
      int output = texture.getGlTexture() instanceof GlTexture gl ? gl.getGlId() : 0;
      fbo = attach(output);
      inputTexture = GL11.glGenTextures();
      GlStateManager._bindTexture(inputTexture);
      GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, 0L);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
      GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
      GlStateManager._bindTexture(0);
      inputFbo = attach(inputTexture);
   }

   private static int attach(int tex) {
      int f = GL30.glGenFramebuffers();
      GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, f);
      GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, tex, 0);
      if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) throw new IllegalStateException("Incomplete GUI framebuffer");
      return f;
   }

   /** Blending into a cleared layer leaves premultiplied colour; Minecraft draws GUI textures with straight alpha. */
   private static void resolve() {
      if (resolveProgram == 0) {
         int v = compile(GL20.GL_VERTEX_SHADER, "#version 150\nout vec2 uv;void main(){vec2 p=vec2((gl_VertexID<<1)&2,gl_VertexID&2);uv=p;gl_Position=vec4(p*2.0-1.0,0,1);}");
         int f = compile(GL20.GL_FRAGMENT_SHADER, "#version 150\nuniform sampler2D image;in vec2 uv;out vec4 color;void main(){vec4 p=texture(image,uv);color=vec4(p.a>0.00001?p.rgb/p.a:vec3(0),p.a);}");
         resolveProgram = GL20.glCreateProgram();
         GL20.glAttachShader(resolveProgram, v);
         GL20.glAttachShader(resolveProgram, f);
         GL20.glLinkProgram(resolveProgram);
         GL20.glDeleteShader(v);
         GL20.glDeleteShader(f);
         if (GL20.glGetProgrami(resolveProgram, GL20.GL_LINK_STATUS) == 0) throw new IllegalStateException(GL20.glGetProgramInfoLog(resolveProgram));
         resolveVao = GL30.glGenVertexArrays();
      }
      int vao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING), program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
      int active = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
      boolean blend = GL11.glIsEnabled(GL11.GL_BLEND), cull = GL11.glIsEnabled(GL11.GL_CULL_FACE), depth = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
      GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, fbo);
      GlStateManager._disableBlend();
      GlStateManager._disableDepthTest();
      GlStateManager._disableCull();
      GlStateManager._disableScissorTest();
      GlStateManager._glUseProgram(resolveProgram);
      GlStateManager._activeTexture(GL13.GL_TEXTURE0);
      int bound = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
      GlStateManager._bindTexture(inputTexture);
      org.lwjgl.opengl.GL33.glBindSampler(0, 0);
      GL20.glUniform1i(GL20.glGetUniformLocation(resolveProgram, "image"), 0);
      GlStateManager._glBindVertexArray(resolveVao);
      GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
      GlStateManager._glBindVertexArray(vao);
      GlStateManager._bindTexture(bound);
      GlStateManager._activeTexture(active);
      GlStateManager._glUseProgram(program);
      if (blend) GlStateManager._enableBlend();
      if (depth) GlStateManager._enableDepthTest();
      if (cull) GlStateManager._enableCull();
   }

   private static int compile(int type, String source) {
      int s = GL20.glCreateShader(type);
      GL20.glShaderSource(s, source);
      GL20.glCompileShader(s);
      if (GL20.glGetShaderi(s, GL20.GL_COMPILE_STATUS) == 0) throw new IllegalStateException(GL20.glGetShaderInfoLog(s));
      return s;
   }
}
