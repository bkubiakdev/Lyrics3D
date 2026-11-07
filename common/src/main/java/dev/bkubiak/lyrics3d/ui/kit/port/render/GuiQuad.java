package dev.bkubiak.lyrics3d.ui.kit.port.render;

import dev.bkubiak.lyrics3d.gl.Gl;
import dev.bkubiak.lyrics3d.gl.GlFormat;
import org.joml.Matrix4f;

/** Unit quad (position + UV) drawn with the current shader; the matrix places it. */
public final class GuiQuad {
   private GuiQuad() { }

   public static void draw(Matrix4f matrix, float x, float y, float width, float height) {
      Gl.assertOnRenderThread();
      Matrix4f m = new Matrix4f(matrix).translate(x, y, 0).scale(width, height, 1);
      var vertices = Gl.begin(GlFormat.POSITION_TEXTURE);
      vertices.vertex(m, 0, 0, 0).texture(0, 0);
      vertices.vertex(m, 0, 1, 0).texture(0, 1);
      vertices.vertex(m, 1, 1, 0).texture(1, 1);
      vertices.vertex(m, 1, 0, 0).texture(1, 0);
      Gl.draw(vertices.end());
   }

   public static void close() {
   }
}
