package dev.bkubiak.lyrics3d.gl;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

/** Vertex layouts used by the mod's shaders. */
public enum GlFormat {
   POSITION(VertexFormats.POSITION),
   POSITION_COLOR(VertexFormats.POSITION_COLOR),
   POSITION_TEXTURE(VertexFormats.POSITION_TEXTURE),
   POSITION_TEXTURE_COLOR(VertexFormats.POSITION_TEXTURE_COLOR);

   public final VertexFormat format;

   GlFormat(VertexFormat format) {
      this.format = format;
   }
}
