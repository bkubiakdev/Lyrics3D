package dev.bkubiak.lyrics3d.gl;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

/** Vertex layouts used by the mod's shaders, with their OpenGL attributes (1.21.1). */
public enum GlFormat {
   POSITION(VertexFormats.POSITION, new String[]{"Position"}, new int[]{3}, 12),
   POSITION_COLOR(VertexFormats.POSITION_COLOR, new String[]{"Position", "Color"}, new int[]{3, 4}, 16),
   POSITION_TEXTURE(VertexFormats.POSITION_TEXTURE, new String[]{"Position", "UV0"}, new int[]{3, 2}, 20),
   POSITION_TEXTURE_COLOR(VertexFormats.POSITION_TEXTURE_COLOR, new String[]{"Position", "UV0", "Color"}, new int[]{3, 2, 4}, 24);

   public final VertexFormat format;
   final String[] names;
   final int[] counts;
   final int stride;

   GlFormat(VertexFormat format, String[] names, int[] counts, int stride) {
      this.format = format;
      this.names = names;
      this.counts = counts;
      this.stride = stride;
   }
}
