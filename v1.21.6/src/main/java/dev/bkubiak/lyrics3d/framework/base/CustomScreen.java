package dev.bkubiak.lyrics3d.framework.base;

import dev.bkubiak.lyrics3d.framework.objects.MouseButton;
import dev.bkubiak.lyrics3d.gl.GuiSurface;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** Screen drawn by the mod's own renderer, with version-neutral input hooks (1.21.6 - 1.21.8), drawn into an off-screen layer. */
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
   public final boolean mouseClicked(double mouseX, double mouseY, int button) {
      this.onMouseClicked(mouseX, mouseY, MouseButton.fromButtonIndex(button));
      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public final boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.onMouseReleased(mouseX, mouseY, MouseButton.fromButtonIndex(button));
      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public final boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      this.onMouseDragged(mouseX, mouseY, MouseButton.fromButtonIndex(button), deltaX, deltaY);
      return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }

   @Override
   public final boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      return this.onMouseScrolled(mouseX, mouseY, horizontal, vertical);
   }

   @Override
   public final boolean keyPressed(int key, int scan, int modifiers) {
      return this.onKeyPressed(key, scan, modifiers);
   }

   @Override
   public final boolean keyReleased(int key, int scan, int modifiers) {
      return this.onKeyReleased(key, scan, modifiers);
   }

   @Override
   public final boolean charTyped(char chr, int modifiers) {
      return this.onCharTyped(chr, modifiers);
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
