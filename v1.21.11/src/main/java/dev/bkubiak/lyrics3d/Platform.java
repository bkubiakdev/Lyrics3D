package dev.bkubiak.lyrics3d;

import dev.bkubiak.lyrics3d.systems.event.impl.render.Render3DEvent;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import java.util.function.Consumer;

/** Version-specific hooks (1.21.9+): camera and matrices come from the extraction, drawing happens at the end of the main pass. */
public final class Platform {
   private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(Identifier.of("lyrics3d", "main"));
   private static Camera camera;
   private static final Matrix4f view = new Matrix4f(), projection = new Matrix4f();
   private static float tickDelta;

   /** Lyrics drawn before clouds and weather: the fill writes depth so they stay behind it. */
   public static final boolean WORLD_DEPTH_WRITE = true;

   private Platform() { }

   public static KeyBinding registerKey(String name, int key) {
      return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, CATEGORY));
   }

   public static void onWorldRender(Consumer<Render3DEvent> listener) {
      WorldRenderEvents.END_EXTRACTION.register(context -> {
         camera = context.camera();
         view.set(context.viewMatrix());
         projection.set(context.cullProjectionMatrix());
         tickDelta = context.tickCounter().getTickProgress(false);
      });
      WorldRenderEvents.END_MAIN.register(context -> {
         if (camera == null) return;
         MatrixStack matrices = new MatrixStack();
         matrices.multiplyPositionMatrix(view);
         listener.accept(new Render3DEvent(matrices, new Matrix4f(view), new Matrix4f(projection), camera.getCameraPos(), camera.getRotation(), tickDelta));
      });
   }

   public static void screenshot(MinecraftClient mc, String file) {
      ScreenshotRecorder.saveScreenshot(mc.runDirectory, file, mc.getFramebuffer(), 1, text -> { });
   }

   public static void openLink(Screen parent, String url) {
      ConfirmLinkScreen.open(parent, url);
   }

   /** Clickable chat link. */
   public static net.minecraft.text.MutableText link(String url) {
      return net.minecraft.text.Text.literal(url).styled(s -> s.withUnderline(true).withColor(0xB57CFF)
         .withClickEvent(new net.minecraft.text.ClickEvent.OpenUrl(java.net.URI.create(url))));
   }
}
