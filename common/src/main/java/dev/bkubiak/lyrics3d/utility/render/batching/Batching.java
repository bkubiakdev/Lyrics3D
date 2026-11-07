package dev.bkubiak.lyrics3d.utility.render.batching;

import dev.bkubiak.lyrics3d.gl.*;
import lombok.Generated;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;

public abstract class Batching {
   protected static Batching active;
   protected BufferBuilder builder;

   public Batching(GlFormat vertexFormat) {
      this.builder = Gl.begin(vertexFormat);
      active = this;
   }

   protected void build() {
      BuiltBuffer builtBuffer = this.builder.endNullable();
      if (builtBuffer != null) {
         Gl.draw(builtBuffer);
      }
   }

   public abstract void draw();

   @Generated
   public BufferBuilder getBuilder() {
      return this.builder;
   }

   @Generated
   public static Batching getActive() {
      return active;
   }
}
