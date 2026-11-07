package dev.bkubiak.lyrics3d.gl;

import com.mojang.blaze3d.textures.GpuTextureView;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.EmptyGlyphRect;
import net.minecraft.client.font.TextDrawable;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.texture.GlTextureView;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import java.util.LinkedHashMap;
import java.util.Map;

/** Minecraft-font glyphs captured as textured quads and drawn by the mod's own pass (1.21.9+). */
public final class VanillaGlyphs implements AutoCloseable {
   private static final GlShaderKey SHADER = new GlShaderKey(Identifier.of("lyrics3d", "core/vanilla_text"), GlFormat.POSITION_TEXTURE_COLOR);
   private final Map<Integer, BufferAllocator> allocators = new LinkedHashMap<>();
   private final Map<Integer, BufferBuilder> pending = new LinkedHashMap<>();

   private BufferBuilder builder(GpuTextureView view) {
      int id = view instanceof GlTextureView gl ? gl.texture().getGlId() : 0;
      return pending.computeIfAbsent(id, key -> Gl.newBuilder(allocators.computeIfAbsent(key, k -> new BufferAllocator(1 << 14)), GlFormat.POSITION_TEXTURE_COLOR));
   }

   public void glyph(String text, float x, float y, int color, Matrix4f matrix, boolean seeThrough) {
      var drawable = MinecraftClient.getInstance().textRenderer.prepare(Text.literal(text).asOrderedText(), x, y, color, false, false, 0);
      drawable.draw(new TextRenderer.GlyphDrawer() {
         @Override public void drawGlyph(TextDrawable.DrawnGlyphRect glyph) { glyph.render(matrix, builder(glyph.textureView()), 0xF000F0, false); }
         @Override public void drawRectangle(TextDrawable rectangle) { rectangle.render(matrix, builder(rectangle.textureView()), 0xF000F0, false); }
         @Override public void drawEmptyGlyphRect(EmptyGlyphRect rect) { }
      });
   }

   public void flush() {
      if (pending.isEmpty()) return;
      GlShader previous = Gl.getShader();
      Gl.setShader(SHADER);
      for (var entry : pending.entrySet()) {
         var built = entry.getValue().endNullable();
         if (built == null) continue;
         int texture = entry.getKey();
         Gl.setShaderTexture(0, texture);
         Gl.setFilter(texture, false);
         Gl.draw(built);
      }
      pending.clear();
      Gl.restoreShader(previous);
   }

   @Override
   public void close() {
      pending.clear();
      allocators.values().forEach(BufferAllocator::close);
      allocators.clear();
   }
}
