package dev.bkubiak.lyrics3d.gl;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

/** Render state facade (1.21.2 - 1.21.4): delegates to Minecraft's RenderSystem. */
public final class Gl {
   private static final Map<GlShaderKey, ShaderProgramKey> KEYS = new HashMap<>();
   private static final Map<ShaderProgram, GlShader> SHADERS = new WeakHashMap<>();

   private Gl() { }

   private static GlShader wrap(ShaderProgram program) {
      return program == null ? null : SHADERS.computeIfAbsent(program, GlShader::new);
   }

   public static GlShader setShader(GlShaderKey key) {
      ShaderProgramKey vanilla = KEYS.computeIfAbsent(key, k -> new ShaderProgramKey(k.id(), k.format().format, Defines.EMPTY));
      return wrap(RenderSystem.setShader(vanilla));
   }
   /** Compiles (if needed) without binding; throws when the shader cannot be loaded. */
   public static GlShader loadShader(GlShaderKey key) throws Exception {
      ShaderProgramKey vanilla = KEYS.computeIfAbsent(key, k -> new ShaderProgramKey(k.id(), k.format().format, Defines.EMPTY));
      return wrap(MinecraftClient.getInstance().getShaderLoader().getOrCreateProgram(vanilla));
   }
   public static void onResourceReload() { }
   public static GlShader setPositionColorShader() { return wrap(RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR)); }
   public static GlShader getShader() { return wrap(RenderSystem.getShader()); }
   public static void restoreShader(GlShader shader) { RenderSystem.setShader(shader == null ? null : shader.program()); }

   public static BufferBuilder begin(GlFormat format) { return Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, format.format); }
   public static BufferBuilder newBuilder(BufferAllocator allocator, GlFormat format) { return new BufferBuilder(allocator, VertexFormat.DrawMode.QUADS, format.format); }
   public static void draw(BuiltBuffer buffer) { BufferRenderer.drawWithGlobalProgram(buffer); }
   public static void resetBuffers() { BufferRenderer.reset(); BufferRenderer.resetCurrentVertexBuffer(); }

   public static void enableBlend() { RenderSystem.enableBlend(); }
   public static void disableBlend() { RenderSystem.disableBlend(); }
   public static void defaultBlendFunc() { RenderSystem.defaultBlendFunc(); }
   public static void blendFuncSeparate(int srcRgb, int dstRgb, int srcAlpha, int dstAlpha) { RenderSystem.blendFuncSeparate(srcRgb, dstRgb, srcAlpha, dstAlpha); }
   public static void enableCull() { RenderSystem.enableCull(); }
   public static void disableCull() { RenderSystem.disableCull(); }
   public static void enableDepthTest() { RenderSystem.enableDepthTest(); }
   public static void disableDepthTest() { RenderSystem.disableDepthTest(); }
   public static void depthMask(boolean mask) { RenderSystem.depthMask(mask); }
   public static void depthFunc(int func) { RenderSystem.depthFunc(func); }
   public static void enableScissor(int x, int y, int width, int height) { RenderSystem.enableScissor(x, y, width, height); }
   public static void disableScissor() { RenderSystem.disableScissor(); }
   public static void viewport(int x, int y, int width, int height) { RenderSystem.viewport(x, y, width, height); }
   public static void setShaderColor(float r, float g, float b, float a) { RenderSystem.setShaderColor(r, g, b, a); }
   public static float[] getShaderColor() { return RenderSystem.getShaderColor(); }
   public static void activeTexture(int unit) { RenderSystem.activeTexture(unit); }
   public static void bindTexture(int texture) { RenderSystem.bindTexture(texture); }
   public static void setShaderTexture(int unit, int texture) { RenderSystem.setShaderTexture(unit, texture); }
   public static int getShaderTexture(int unit) { return RenderSystem.getShaderTexture(unit); }
   public static void assertOnRenderThread() { RenderSystem.assertOnRenderThread(); }
   public static void recordRenderCall(Runnable call) { RenderSystem.recordRenderCall(call::run); }
   public static Matrix4f getProjectionMatrix() { return RenderSystem.getProjectionMatrix(); }
   public static Matrix4f getModelViewMatrix() { return RenderSystem.getModelViewMatrix(); }

   public static void useProgram(int program) { GlStateManager._glUseProgram(program); }
   public static void bindFramebuffer(int target, int framebuffer) { GlStateManager._glBindFramebuffer(target, framebuffer); }
   public static void texParameter(int target, int name, int value) { GlStateManager._texParameter(target, name, value); }

   public static int textureId(AbstractTexture texture) { return texture == null ? 0 : texture.getGlId(); }
   public static int textureId(Identifier id) { return textureId(MinecraftClient.getInstance().getTextureManager().getTexture(id)); }
   /** Linear (true) or nearest filtering of a registered texture. */
   public static void setFilter(AbstractTexture texture, boolean linear) { texture.setFilter(linear, false); }

   /** Main framebuffer size in pixels. */
   public static net.minecraft.client.texture.NativeImageBackedTexture newTexture(String name, net.minecraft.client.texture.NativeImage image) {
      return new net.minecraft.client.texture.NativeImageBackedTexture(image);
   }

   public static int framebufferWidth() { return MinecraftClient.getInstance().getFramebuffer().textureWidth; }
   public static int framebufferHeight() { return MinecraftClient.getInstance().getFramebuffer().textureHeight; }

   public static int getArgb(net.minecraft.client.texture.NativeImage image, int x, int y) { return image.getColorArgb(x, y); }
   public static void setArgb(net.minecraft.client.texture.NativeImage image, int x, int y, int argb) { image.setColorArgb(x, y, argb); }
}
