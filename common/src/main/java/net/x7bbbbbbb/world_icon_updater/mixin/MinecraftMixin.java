package net.x7bbbbbbb.world_icon_updater.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.util.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    private IntegratedServer singleplayerServer;

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"), require = 0)
    private void onDisconnectWithTransfer(Screen disconnectionScreen, boolean transferring, CallbackInfo ci) {
        world_icon_updater$saveWorldIconOnClose();
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;)V", at = @At("HEAD"), require = 0)
    private void onDisconnect(Screen disconnectionScreen, CallbackInfo ci) {
        world_icon_updater$saveWorldIconOnClose();
    }

    @Unique
    private void world_icon_updater$saveWorldIconOnClose() {
        Minecraft client = (Minecraft) (Object) this;

        if (this.singleplayerServer != null && client.level != null && client.getMainRenderTarget() != null) {
            this.singleplayerServer.getWorldScreenshotFile().ifPresent(path -> {
                client.gameRenderer.setRenderHand(false);
                client.gameRenderer.setRenderBlockOutline(false);

                client.gameRenderer.renderLevel();

                client.gameRenderer.setRenderHand(true);
                client.gameRenderer.setRenderBlockOutline(true);

                NativeImage nativeImage = Screenshot.takeScreenshot(client.getMainRenderTarget());
                Util.ioPool().execute(() -> {
                    int width = nativeImage.getWidth();
                    int height = nativeImage.getHeight();
                    int startX = 0;
                    int startY = 0;

                    if (width > height) {
                        startX = (width - height) / 2;
                        width = height;
                    } else {
                        startY = (height - width) / 2;
                        height = width;
                    }

                    try (NativeImage croppedImage = new NativeImage(64, 64, false)) {
                        nativeImage.resizeSubRectTo(startX, startY, width, height, croppedImage);
                        croppedImage.writeToFile(path);
                    } catch (IOException e) {
                        LOGGER.warn("Couldn't save world icon on close", e);
                    } finally {
                        nativeImage.close();
                    }
                });
            });
        }
    }
}