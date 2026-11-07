package dev.bkubiak.lyrics3d.gl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.GlTexture;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL33;
import java.util.HashMap;
import java.util.Map;

/**
 * Render state facade (1.21.5+): Minecraft renders through its own pipeline objects, so the mod keeps its own
 * shader, matrices and textures and draws with OpenGL through GlStateManager (keeping Minecraft's state cache valid).
 */
public final class Gl {
   private static final Map<GlShaderKey, GlShader> SHADERS = new HashMap<>();
   private static final GlShaderKey POSITION_COLOR = new GlShaderKey(Identifier.of("lyrics3d", "core/position_color_plain"), GlFormat.POSITION_COLOR);
   private static final float[] COLOR = {1, 1, 1, 1};
   private static final int[] TEXTURES = new int[8];
   private static final Matrix4fStack MODEL_VIEW = new Matrix4fStack(16);
   private static Matrix4f projection = new Matrix4f();
   private static GlShader current;
   private static int vao, vbo, ebo, indexCapacity, linearSampler, nearestSampler;
   /** Filtering per texture: bound as a sampler object, so Minecraft's own sampler state does not matter. */
   private static final Map<Integer, Boolean> FILTERS = new HashMap<>();

   private static int sampler(int texture) {
      if (linearSampler == 0) {
         linearSampler = GL33.glGenSamplers();
         nearestSampler = GL33.glGenSamplers();
         for (int s : new int[]{linearSampler, nearestSampler}) {
            int filter = s == linearSampler ? GL11.GL_LINEAR : GL11.GL_NEAREST;
            GL33.glSamplerParameteri(s, GL11.GL_TEXTURE_MIN_FILTER, filter);
            GL33.glSamplerParameteri(s, GL11.GL_TEXTURE_MAG_FILTER, filter);
            GL33.glSamplerParameteri(s, GL11.GL_TEXTURE_WRAP_S, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
            GL33.glSamplerParameteri(s, GL11.GL_TEXTURE_WRAP_T, org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE);
         }
      }
      return FILTERS.getOrDefault(texture, Boolean.TRUE) ? linearSampler : nearestSampler;
   }

   private Gl() { }

   /** Programs and buffers are rebuilt after a resource reload. */
   public static void onResourceReload() {
      SHADERS.values().forEach(GlShader::close);
      SHADERS.clear();
      current = null;
   }

   public static GlShader loadShader(GlShaderKey key) {
      return SHADERS.computeIfAbsent(key, GlShader::new);
   }
   public static GlShader setShader(GlShaderKey key) {
      current = loadShader(key);
      current.use();
      return current;
   }
   public static GlShader setPositionColorShader() { return setShader(POSITION_COLOR); }
   public static GlShader getShader() { return current; }
   public static void restoreShader(GlShader shader) {
      current = shader;
      if (shader != null) shader.use();
   }

   public static BufferBuilder begin(GlFormat format) { return Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, format.format); }
   public static BufferBuilder newBuilder(BufferAllocator allocator, GlFormat format) { return new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, format.format); }

   /** Draws the built quads with the current shader, matrices, colour and textures. */
   public static void draw(BuiltBuffer buffer) {
      try (buffer) {
         GlShader shader = current;
         if (shader == null) return;
         var params = buffer.getDrawParameters();
         var format = params.format();
         shader.use();
         var proj = shader.getUniform("ProjMat");
         if (proj != null) proj.set(projection);
         var view = shader.getUniform("ModelViewMat");
         if (view != null) view.set(MODEL_VIEW);
         var color = shader.getUniform("ColorModulator");
         if (color != null) color.set(COLOR[0], COLOR[1], COLOR[2], COLOR[3]);
         int active = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
         int samplers = shader.samplers.size();
         int[] previous = new int[samplers];
         for (int i = 0; i < samplers; i++) {
            GlStateManager._activeTexture(GL13.GL_TEXTURE0 + i);
            previous[i] = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
            GlStateManager._bindTexture(TEXTURES[i]);
            GL33.glBindSampler(i, sampler(TEXTURES[i]));
         }
         int previousVao = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
         int previousVbo = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
         try {
            if (vao == 0) { vao = GL30.glGenVertexArrays(); vbo = GL15.glGenBuffers(); ebo = GL15.glGenBuffers(); }
            GlStateManager._glBindVertexArray(vao);
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer.getBuffer(), GL15.GL_STREAM_DRAW);
            var elements = format.getElements();
            for (int i = 0; i < elements.size(); i++) {
               var e = elements.get(i);
               boolean ubyte = format.getElementName(e).equals("Color");
               GL20.glEnableVertexAttribArray(i);
               GL20.glVertexAttribPointer(i, e.count(), ubyte ? GL11.GL_UNSIGNED_BYTE : GL11.GL_FLOAT, ubyte, format.getVertexSize(), format.getOffset(e));
            }
            int count = params.vertexCount() / 4 * 6;
            GlStateManager._glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, ebo);
            if (count > indexCapacity) {
               int[] indices = new int[count];
               for (int q = 0; q < params.vertexCount() / 4; q++) {
                  int b = q * 4, j = q * 6;
                  indices[j] = b; indices[j + 1] = b + 1; indices[j + 2] = b + 2;
                  indices[j + 3] = b + 2; indices[j + 4] = b + 3; indices[j + 5] = b;
               }
               GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indices, GL15.GL_DYNAMIC_DRAW);
               indexCapacity = count;
            }
            GL11.glDrawElements(GL11.GL_TRIANGLES, count, GL11.GL_UNSIGNED_INT, 0L);
         } finally {
            GlStateManager._glBindVertexArray(previousVao);
            GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, previousVbo);
            for (int i = 0; i < samplers; i++) {
               GlStateManager._activeTexture(GL13.GL_TEXTURE0 + i);
               GlStateManager._bindTexture(previous[i]);
               GL33.glBindSampler(i, 0);
            }
            GlStateManager._activeTexture(active);
         }
      }
   }
   public static void resetBuffers() { }

   public static void enableBlend() { GlStateManager._enableBlend(); }
   public static void disableBlend() { GlStateManager._disableBlend(); }
   public static void defaultBlendFunc() { GlStateManager._blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA); }
   public static void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) { GlStateManager._blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha); }
   public static void enableCull() { GlStateManager._enableCull(); }
   public static void disableCull() { GlStateManager._disableCull(); }
   public static void enableDepthTest() { GlStateManager._enableDepthTest(); }
   public static void disableDepthTest() { GlStateManager._disableDepthTest(); }
   public static void depthMask(boolean mask) { GlStateManager._depthMask(mask); }
   public static void depthFunc(int func) { GlStateManager._depthFunc(func); }
   public static void enableScissor(int x, int y, int width, int height) { GlStateManager._enableScissorTest(); GlStateManager._scissorBox(x, y, width, height); }
   public static void disableScissor() { GlStateManager._disableScissorTest(); }
   public static void viewport(int x, int y, int width, int height) { GlStateManager._viewport(x, y, width, height); }
   public static void setShaderColor(float r, float g, float b, float a) { COLOR[0] = r; COLOR[1] = g; COLOR[2] = b; COLOR[3] = a; }
   public static float[] getShaderColor() { return COLOR; }
   public static void activeTexture(int unit) { GlStateManager._activeTexture(unit); }
   public static void bindTexture(int texture) { GlStateManager._bindTexture(texture); }
   public static void setShaderTexture(int unit, int texture) { if (unit >= 0 && unit < TEXTURES.length) TEXTURES[unit] = texture; }
   public static int getShaderTexture(int unit) { return unit >= 0 && unit < TEXTURES.length ? TEXTURES[unit] : 0; }
   public static void assertOnRenderThread() { }
   public static void recordRenderCall(Runnable call) {
      var mc = MinecraftClient.getInstance();
      if (mc.isOnThread()) call.run(); else mc.execute(call);
   }
   public static Matrix4f getProjectionMatrix() { return projection; }
   public static void setProjectionMatrix(Matrix4f matrix) { projection = new Matrix4f(matrix); }
   public static Matrix4fStack getModelViewMatrix() { return MODEL_VIEW; }

   public static void useProgram(int program) { GlStateManager._glUseProgram(program); }
   public static void bindFramebuffer(int target, int framebuffer) { GlStateManager._glBindFramebuffer(target, framebuffer); }
   public static void texParameter(int target, int name, int value) { GlStateManager._texParameter(target, name, value); }

   public static int textureId(AbstractTexture texture) {
      if (texture == null) return 0;
      return texture.getGlTexture() instanceof GlTexture gl ? gl.getGlId() : 0;
   }
   public static int textureId(Identifier id) { return textureId(MinecraftClient.getInstance().getTextureManager().getTexture(id)); }
   /** Linear (true) or nearest filtering of a registered texture. */
   public static void setFilter(AbstractTexture texture, boolean linear) {
      setFilter(textureId(texture), linear);
   }
   public static void setFilter(int id, boolean linear) {
      if (id == 0) return;
      FILTERS.put(id, linear);
      int bound = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
      GlStateManager._bindTexture(id);
      int filter = linear ? GL11.GL_LINEAR : GL11.GL_NEAREST;
      GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, filter);
      GlStateManager._texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, filter);
      GlStateManager._bindTexture(bound);
   }

   public static net.minecraft.client.texture.NativeImageBackedTexture newTexture(String name, net.minecraft.client.texture.NativeImage image) {
      return new net.minecraft.client.texture.NativeImageBackedTexture(() -> name, image);
   }

   public static int framebufferWidth() { return MinecraftClient.getInstance().getFramebuffer().textureWidth; }
   public static int framebufferHeight() { return MinecraftClient.getInstance().getFramebuffer().textureHeight; }

   public static int getArgb(net.minecraft.client.texture.NativeImage image, int x, int y) { return image.getColorArgb(x, y); }
   public static void setArgb(net.minecraft.client.texture.NativeImage image, int x, int y, int argb) { image.setColorArgb(x, y, argb); }
}
