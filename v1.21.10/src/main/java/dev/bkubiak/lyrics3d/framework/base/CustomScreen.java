package dev.bkubiak.lyrics3d.framework.base;

import dev.bkubiak.lyrics3d.framework.objects.MouseButton;
import dev.bkubiak.lyrics3d.gl.GuiSurface;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

/** Screen drawn by the mod's own renderer into an off-screen layer, with version-neutral input hooks (1.21.9+). */
public abstract class CustomScreen extends Screen {
   protected CustomScreen() {
      super(Text.empty());
   }

   public abstract void render(UIContext context);

   @Override
   public final void render(DrawContext context, int mouseX, int mouseY, float delta) {
      super.render(context, mouseX, mouseY, delta);
      GuiSurface.begin();
      var id = net.minecraft.util.Identifier.of("lyrics3d", "runtime/gui_surface");
      try {
         this.render(UIContext.of(context, mouseX, mouseY, delta));
      } finally {
         id = GuiSurface.end();
      }
      var window = this.client.getWindow();
      int w = GuiSurface.width(), h = GuiSurface.height();
      context.drawTexture(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0, h, window.getScaledWidth(), window.getScaledHeight(), w, -h, w, h);
   }

   @Override
   public final boolean mouseClicked(Click click, boolean doubled) {
      this.onMouseClicked(click.x(), click.y(), MouseButton.fromButtonIndex(click.button()));
      return super.mouseClicked(click, doubled);
   }

   @Override
   public final boolean mouseReleased(Click click) {
      this.onMouseReleased(click.x(), click.y(), MouseButton.fromButtonIndex(click.button()));
      return super.mouseReleased(click);
   }

   @Override
   public final boolean mouseDragged(Click click, double deltaX, double deltaY) {
      this.onMouseDragged(click.x(), click.y(), MouseButton.fromButtonIndex(click.button()), deltaX, deltaY);
      return super.mouseDragged(click, deltaX, deltaY);
   }

   @Override
   public final boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      return this.onMouseScrolled(mouseX, mouseY, horizontal, vertical);
   }

   @Override
   public final boolean keyPressed(KeyInput input) {
      return this.onKeyPressed(input.key(), input.scancode(), input.modifiers());
   }

   @Override
   public final boolean keyReleased(KeyInput input) {
      return this.onKeyReleased(input.key(), input.scancode(), input.modifiers());
   }

   @Override
   public final boolean charTyped(CharInput input) {
      boolean handled = false;
      for (char c : Character.toChars(input.codepoint())) handled |= this.onCharTyped(c, input.modifiers());
      return handled;
   }

   /** Escape closes the screen. */
   protected boolean defaultKeyPressed(int key, int scan, int modifiers) {
      if (key == 256 && this.shouldCloseOnEsc()) {
         this.close();
         return true;
      }
      return false;
   }

   public void onMouseClicked(double mouseX, double mouseY, MouseButton button) { }
   public void onMouseReleased(double mouseX, double mouseY, MouseButton button) { }
   public void onMouseDragged(double mouseX, double mouseY, MouseButton button, double deltaX, double deltaY) { }
   public boolean onMouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) { return false; }
   public boolean onKeyPressed(int key, int scan, int modifiers) { return defaultKeyPressed(key, scan, modifiers); }
   public boolean onKeyReleased(int key, int scan, int modifiers) { return false; }
   public boolean onCharTyped(char chr, int modifiers) { return false; }
}
