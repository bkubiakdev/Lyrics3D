package dev.bkubiak.lyrics3d.framework.base;

import dev.bkubiak.lyrics3d.gl.*;
import dev.bkubiak.lyrics3d.framework.msdf.Font;
import dev.bkubiak.lyrics3d.framework.msdf.MsdfRenderer;
import dev.bkubiak.lyrics3d.framework.objects.BorderRadius;
import dev.bkubiak.lyrics3d.utility.colors.ColorRGBA;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;

/** The drawing primitives of the editor window: rectangles, rounded rectangles, shadows and MSDF text. */
public class CustomDrawContext {
   private static final GlShaderKey RECTANGLE = new GlShaderKey(Identifier.of("lyrics3d", "core/rectangle/data"), GlFormat.POSITION_COLOR);
   private final DrawContext originalContext;
   private final MatrixStack matrices = new MatrixStack();

   protected CustomDrawContext(DrawContext originalContext) {
      this.originalContext = originalContext;
   }

   public MatrixStack getMatrices() {
      return this.matrices;
   }

   /** The mod draws immediately; nothing is batched. */
   public void draw() {
   }

   protected DrawContext getOriginalContext() {
      return this.originalContext;
   }

   public void drawRect(float x, float y, float width, float height, ColorRGBA color) {
      Matrix4f m = this.getMatrices().peek().getPositionMatrix();
      Gl.setPositionColorShader();
      Gl.enableBlend();
      Gl.defaultBlendFunc();
      BufferBuilder builder = Gl.begin(GlFormat.POSITION_COLOR);
      builder.vertex(m, x, y + height, 0.0F).color(color.getRGB());
      builder.vertex(m, x + width, y + height, 0.0F).color(color.getRGB());
      builder.vertex(m, x + width, y, 0.0F).color(color.getRGB());
      builder.vertex(m, x, y, 0.0F).color(color.getRGB());
      Gl.draw(builder.end());
      Gl.disableBlend();
   }

   public void drawRoundedRect(float x, float y, float width, float height, BorderRadius radius, ColorRGBA color) {
      float smoothness = 0.5F;
      if ((color.getRGB() >>> 24) == 0) return;
      float horizontalPadding = -smoothness / 2.0F + smoothness * 2.0F;
      float verticalPadding = smoothness / 2.0F + smoothness;
      rounded(x - horizontalPadding / 2.0F, y - verticalPadding / 2.0F, width + horizontalPadding, height + verticalPadding,
         width, height, radius.topLeftRadius(), radius.bottomLeftRadius(), radius.topRightRadius(), radius.bottomRightRadius(), smoothness, color);
   }

   public void drawShadow(float x, float y, float width, float height, float softness, BorderRadius radius, ColorRGBA color) {
      float padding = softness * 3.0F;
      rounded(x - padding / 2.0F, y - padding / 2.0F, width + padding, height + padding,
         width, height, radius.topLeftRadius() * 3.0F, radius.bottomLeftRadius() * 3.0F, radius.topRightRadius() * 3.0F, radius.bottomRightRadius() * 3.0F,
         softness, color);
   }

   private void rounded(float ax, float ay, float aw, float ah, float width, float height, float r1, float r2, float r3, float r4, float smoothness, ColorRGBA color) {
      Matrix4f m = this.getMatrices().peek().getPositionMatrix();
      GlShader shader = Gl.setShader(RECTANGLE);
      if (shader == null) return;
      shader.getUniformOrDefault("Size").set(width, height);
      shader.getUniformOrDefault("Radius").set(r1, r2, r3, r4);
      shader.getUniformOrDefault("Smoothness").set(smoothness);
      Gl.enableBlend();
      Gl.defaultBlendFunc();
      BufferBuilder builder = Gl.begin(GlFormat.POSITION_COLOR);
      builder.vertex(m, ax, ay, 0.0F).color(color.getRGB());
      builder.vertex(m, ax, ay + ah, 0.0F).color(color.getRGB());
      builder.vertex(m, ax + aw, ay + ah, 0.0F).color(color.getRGB());
      builder.vertex(m, ax + aw, ay, 0.0F).color(color.getRGB());
      Gl.draw(builder.end());
      Gl.disableBlend();
   }

   public void drawText(Font font, String text, float x, float y, ColorRGBA color) {
      MsdfRenderer.renderText(font.getFont(), text, font.getSize(), color.getRGB(), this.getMatrices().peek().getPositionMatrix(), x, y, 0.0F);
   }

   public void drawCenteredText(Font font, String text, float x, float y, ColorRGBA color) {
      this.drawText(font, text, x - font.getFont().getWidth(text, font.getSize()) / 2.0F, y, color);
   }
}
