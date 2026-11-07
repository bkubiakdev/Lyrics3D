package dev.bkubiak.lyrics3d;

import dev.bkubiak.lyrics3d.systems.event.impl.render.Render3DEvent;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import java.util.function.Consumer;

/** Version-specific hooks (1.21.5 - 1.21.8). */
public final class Platform {
   /** Lyrics drawn before clouds and weather: the fill writes depth so they stay behind it. */
   public static final boolean WORLD_DEPTH_WRITE = false;

   private Platform() { }

   public static KeyBinding registerKey(String name, int key) {
      return KeyBindingHelper.registerKeyBinding(new KeyBinding(name, InputUtil.Type.KEYSYM, key, "key.categories.lyrics3d"));
   }

   /** After the whole world pass, before the hand: the same point as the end of WorldRenderer.render. */
   public static void onWorldRender(Consumer<Render3DEvent> listener) {
      WorldRenderEvents.END.register(context -> {
         MatrixStack matrices = new MatrixStack();
         matrices.multiplyPositionMatrix(context.positionMatrix());
         listener.accept(new Render3DEvent(matrices, context.positionMatrix(), context.projectionMatrix(), context.camera().getPos(), context.camera().getRotation(),
            context.tickCounter().getTickProgress(false)));
      });
   }

   public static void screenshot(net.minecraft.client.MinecraftClient mc, String file) {
      net.minecraft.client.util.ScreenshotRecorder.saveScreenshot(mc.runDirectory, file, mc.getFramebuffer(), 1, text -> { });
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
