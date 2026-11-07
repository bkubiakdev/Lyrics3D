package dev.bkubiak.lyrics3d.utility.render.batching.impl;

import dev.bkubiak.lyrics3d.gl.*;
import dev.bkubiak.lyrics3d.framework.msdf.MsdfFont;
import dev.bkubiak.lyrics3d.framework.msdf.MsdfRenderer;
import dev.bkubiak.lyrics3d.utility.render.batching.Batching;

public class FontBatching extends Batching {
   protected MsdfFont font;

   public FontBatching(GlFormat vertexFormat, MsdfFont font) {
      super(vertexFormat);
      this.font = font;
   }

   public MsdfFont getFont() {
      return this.font;
   }

   @Override
   public void draw() {
      float thickness = 0.05F;
      float smoothness = 0.5F;
      float spacing = 0.0F;
      Gl.enableBlend();
      Gl.defaultBlendFunc();
      Gl.disableCull();
      Gl.setShaderTexture(0, this.font.getTextureId());
      GlShader shader = Gl.setShader(MsdfRenderer.MSDF_FONT_SHADER_KEY);
      shader.getUniform("Range").set(this.font.getAtlas().range());
      shader.getUniform("Thickness").set(thickness);
      shader.getUniform("Smoothness").set(smoothness);
      shader.getUniform("EnableFadeout").set(0);
      this.build();
      Gl.setShaderTexture(0, 0);
      Gl.enableCull();
      Gl.disableBlend();
      if (active == this) {
         active = null;
      }
   }
}
