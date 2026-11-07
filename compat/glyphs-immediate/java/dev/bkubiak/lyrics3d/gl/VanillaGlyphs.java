package dev.bkubiak.lyrics3d.gl;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

/** Minecraft-font glyphs through Minecraft's own immediate buffers (up to 1.21.5). */
public final class VanillaGlyphs implements AutoCloseable {
   private BufferAllocator allocator;
   private VertexConsumerProvider.Immediate consumers;

   public void glyph(String text, float x, float y, int color, Matrix4f matrix, boolean seeThrough) {
      if (allocator == null) {
         allocator = new BufferAllocator(1 << 16);
         consumers = VertexConsumerProvider.immediate(allocator);
      }
      MinecraftClient.getInstance().textRenderer.draw(Text.literal(text).asOrderedText(), x, y, color, false, matrix, consumers,
         seeThrough ? TextRenderer.TextLayerType.SEE_THROUGH : TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
   }

   public void flush() {
      if (consumers != null) consumers.draw();
   }

   @Override
   public void close() {
      consumers = null;
      if (allocator != null) {
         allocator.close();
         allocator = null;
      }
   }
}
