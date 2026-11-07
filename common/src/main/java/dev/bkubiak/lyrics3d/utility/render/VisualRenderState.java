package dev.bkubiak.lyrics3d.utility.render;

import dev.bkubiak.lyrics3d.gl.*;
import org.lwjgl.opengl.*;

/** Scope for offscreen visual passes; never leave state behind for terrain, hands or HUD. */
public final class VisualRenderState implements AutoCloseable {
    private final boolean blend=GL11.glIsEnabled(GL11.GL_BLEND), depth=GL11.glIsEnabled(GL11.GL_DEPTH_TEST), cull=GL11.glIsEnabled(GL11.GL_CULL_FACE);
    private final boolean depthWrite=GL11.glGetBoolean(GL11.GL_DEPTH_WRITEMASK);
    private final int src=GL11.glGetInteger(GL14.GL_BLEND_SRC_RGB), dst=GL11.glGetInteger(GL14.GL_BLEND_DST_RGB), srcA=GL11.glGetInteger(GL14.GL_BLEND_SRC_ALPHA), dstA=GL11.glGetInteger(GL14.GL_BLEND_DST_ALPHA);
    private final int read=GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING), draw=GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING), programId=GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
    private final int texture=Gl.getShaderTexture(0);
    private final int[] viewport=new int[4];
    private final float[] color=Gl.getShaderColor().clone();
    private final GlShader program=Gl.getShader();
    public VisualRenderState(){GL11.glGetIntegerv(GL11.GL_VIEWPORT,viewport);}
    @Override public void close(){
        GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER,read);GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER,draw);
        Gl.viewport(viewport[0],viewport[1],viewport[2],viewport[3]);
        Gl.restoreShader(program); GL20.glUseProgram(programId);
        Gl.setShaderTexture(0,texture); Gl.setShaderColor(color[0],color[1],color[2],color[3]);
        Gl.depthMask(depthWrite);
        if(depth)Gl.enableDepthTest();else Gl.disableDepthTest();
        if(cull)Gl.enableCull();else Gl.disableCull();
        Gl.blendFuncSeparate(src,dst,srcA,dstA);
        if(blend)Gl.enableBlend();else Gl.disableBlend();
    }
}
