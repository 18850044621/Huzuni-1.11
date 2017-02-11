package net.halalaboos.huzuni.mc.mixin;

import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.huzuni.mc.Wrapper;
import net.minecraft.client.multiplayer.WorldClient;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(net.minecraft.client.Minecraft.class) public class MixinMinecraft {

	@Shadow private WorldClient world;

	@Inject(method = "init", at = @At("RETURN"))
	public void inject(CallbackInfo callbackInfo) {
		Huzuni.INSTANCE.start();
	}

	@Inject(method = "runTickKeyboard", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Keyboard;getEventKeyState()Z"))
	public void onKeyPress(CallbackInfo ci) {
		if (Keyboard.getEventKeyState()) {
			int keyCode = Keyboard.getEventKey();
			Wrapper.keyTyped(keyCode);
		}
	}

	@Inject(method = "runTickMouse", at = @At(value = "INVOKE_ASSIGN", target = "Lorg/lwjgl/input/Mouse;getEventButton()I"))
	public void onMouseClick(CallbackInfo ci) {
		if (Mouse.getEventButtonState()) {
			Wrapper.onMouseClicked(Mouse.getEventButton());
		}
	}

	@Inject(method = "loadWorld(Lnet/minecraft/client/multiplayer/WorldClient;Ljava/lang/String;)V", at = @At("HEAD"))
	public void onLoadWorld(@Nullable WorldClient worldClientIn, String loadingMessage, CallbackInfo ci) {
		if (worldClientIn != null) {
			Wrapper.loadWorld(worldClientIn);
		}
	}

	@Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
	public void onShutdown(CallbackInfo ci) {
		Huzuni.INSTANCE.end();
	}
}