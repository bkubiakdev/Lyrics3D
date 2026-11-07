package dev.bkubiak.lyrics3d.gl;

import net.minecraft.util.Identifier;

/** A core shader of this mod: assets/&lt;namespace&gt;/shaders/&lt;path&gt;.json with its vertex layout. */
public record GlShaderKey(Identifier id, GlFormat format) {
}
