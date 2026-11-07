package dev.bkubiak.lyrics3d.ui.kit;

import dev.bkubiak.lyrics3d.framework.base.UIContext;
import dev.bkubiak.lyrics3d.framework.objects.BorderRadius;
import dev.bkubiak.lyrics3d.ui.kit.port.module.client.ThemeModule;
import dev.bkubiak.lyrics3d.ui.kit.port.render.ShapeShader;
import dev.bkubiak.lyrics3d.utility.colors.ColorRGBA;
import org.joml.Matrix4f;

/** Render-pass scope and shared surfaces of the settings widgets. */
public final class KitMaterial {
   private static UIContext context;
   private static final java.util.Deque<UIContext> contexts = new java.util.ArrayDeque<>();
   private static long cursorTime;
   private static float cursorX, cursorY;
   public static float cursorX() { return cursorX; }
   public static float cursorY() { return cursorY; }
   private KitMaterial() { }
   public static boolean enabled() { return false; }
   public static void begin(UIContext ctx) {
      contexts.push(ctx); context = ctx;
      if (contexts.size() > 1) return;
      var mc = net.minecraft.client.MinecraftClient.getInstance();
      var theme = ThemeModule.getThemeModule();
      long now = System.nanoTime();
      float x = (float)(mc.mouse.getX() * mc.getFramebuffer().textureWidth / Math.max(1,mc.getWindow().getWidth()));
      float y = (float)((1-mc.mouse.getY()/Math.max(1,mc.getWindow().getHeight())) * mc.getFramebuffer().textureHeight);
      float dt = cursorTime == 0 ? 1 : Math.min(1,(now-cursorTime)/1_000_000_000f);
      float t = theme != null && theme.effect("Cursor inertia") ? 1-(float)Math.exp(-dt*12) : 1;
      cursorX += (x-cursorX)*t; cursorY += (y-cursorY)*t; cursorTime=now;
   }
   public static void end() {
      if(!contexts.isEmpty()) contexts.pop();
      context=contexts.peek();
   }
   public static UIContext context() { return context; }
   /** Shared focus treatment for native fields and the embedded market editor. */
   public static void inputField(Matrix4f matrix, float x, float y, float width, float height, float focus, float alpha) {
      int fill = dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.background();
      int border = dev.bkubiak.lyrics3d.ui.kit.port.data.AnimatedInt.getIntByIntFloatInt(
         dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.primary(), focus,
         panelBorder(fill, dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.border()));
      ShapeShader.onFloatFloatFloatMatrix4fFloatIntFloatFloatIntFloat(
         1, width, alpha, matrix, y, fill, 8, x, border, height);
   }
   /** Thin monochrome track and outlined thumb matching the reference ClickGUI. */
   public static void slider(Matrix4f matrix, float x, float y, float width, float progress, float alpha) {
      if (width <= 0) return;
      float h = 6, kw = Math.min(width, 12), kh = 8;
      float fill = width * Math.clamp(progress, 0, 1);
      int rail = dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.elevated();
      int foreground = dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.foreground();
      ShapeShader.onFloatFloatIntMatrix4fFloatFloatFloatFloat(3, x, rail, matrix, h, width, alpha, y);
      if (fill > 0) ShapeShader.onFloatFloatIntMatrix4fFloatFloatFloatFloat(Math.min(3, fill/2), x, foreground, matrix, h, fill, alpha * .7F, y);
      float tip = x + Math.clamp(fill, kw/2, width-kw/2);
      ShapeShader.onFloatFloatFloatMatrix4fFloatIntFloatFloatIntFloat(1, kw, alpha, matrix, y+h/2-kh/2,
         dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.background(), 4, tip-kw/2, foreground, kh);
   }
   public static void rangeSlider(Matrix4f matrix, float x, float y, float width, float low, float high, float alpha) {
      if (width <= 0) return;
      float h = 6, kw = Math.min(width, 12), kh = 8;
      float start = width * Math.clamp(low, 0, 1), end = width * Math.clamp(high, 0, 1);
      int rail = dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.elevated();
      int accent = dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.foreground();
      ShapeShader.onFloatFloatIntMatrix4fFloatFloatFloatFloat(h/2, x, rail, matrix, h, width, alpha, y);
      if (end > start) ShapeShader.onFloatFloatIntMatrix4fFloatFloatFloatFloat(Math.min(h/2, (end-start)/2), x+start, accent, matrix, h, end-start, alpha * .7F, y);
      for (float position : new float[]{start,end}) {
         float tip = x + Math.clamp(position, kw/2, width-kw/2);
         ShapeShader.onFloatFloatFloatMatrix4fFloatIntFloatFloatIntFloat(1, kw, alpha, matrix, y+h/2-kh/2,
            dev.bkubiak.lyrics3d.ui.kit.port.api.Theme.background(), 4, tip-kw/2, accent, kh);
      }
   }
   public static int layer(int color, float opacity) {
      return enabled() ? ShapeShader.getIntByIntFloat(color, opacity) : color;
   }
   /** Dark windows need only a quiet edge; keep light surfaces unchanged. */
   public static int panelBorder(int fill, int border) {
      ColorRGBA surface = ColorRGBA.fromInt(fill);
      float luminance = surface.getRed() * .2126F + surface.getGreen() * .7152F + surface.getBlue() * .0722F;
      if (luminance >= 128) return border;
      ColorRGBA edge = ColorRGBA.fromInt(border);
      return surface.mix(edge, .25F).withAlpha(edge.getAlpha()).getRGB();
   }
   public static void panel(Matrix4f matrix, float x, float y, float w, float h, float radius, int fill, int border, float alpha) {
      border = panelBorder(fill, border);
      ColorRGBA surface = ColorRGBA.fromInt(fill);
      boolean dark = surface.getRed() * .2126F + surface.getGreen() * .7152F + surface.getBlue() * .0722F < 128;
      ShapeShader.onFloatFloatFloatMatrix4fFloatIntFloatFloatIntFloat(dark ? .5F : 1, w, alpha, matrix, y, fill, radius, x, border, h);
   }
}
