package net.halalaboos.mcwrapper.impl.mixin.entity.living.player;

import net.halalaboos.huzuni.Huzuni;
import net.halalaboos.huzuni.mod.movement.Freecam;
import net.halalaboos.mcwrapper.api.MCWrapper;
import net.halalaboos.mcwrapper.api.client.ClientPlayer;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.entity.living.player.Player;
import net.halalaboos.mcwrapper.api.event.PostMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.event.PreMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.network.PlayerInfo;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.util.EnumHand;
import net.minecraft.util.MovementInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(net.minecraft.client.entity.EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends MixinAbstractClientPlayer implements ClientPlayer {

	@Shadow public abstract void swingArm(EnumHand hand);
	@Shadow public abstract void closeScreen();
	@Shadow public MovementInput movementInput;
	@Shadow private String serverBrand;
	@Shadow @Final public NetHandlerPlayClient connection;
	@Shadow public abstract boolean isHandActive();

	@Shadow
	public abstract void onUpdateWalkingPlayer();

	private PreMotionUpdateEvent preMotion = new PreMotionUpdateEvent();
	private PostMotionUpdateEvent postMotion = new PostMotionUpdateEvent();
	private Huzuni huzuni = Huzuni.INSTANCE;

	@Inject(method = "onUpdate", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/client/entity/EntityPlayerSP;onUpdateWalkingPlayer()V",
			shift = At.Shift.BEFORE), cancellable = true)
	private void dispatchUpdateEvents(CallbackInfo ci) {
		preMotion.setCancelled(false);
		MCWrapper.getEventManager().publish(preMotion);
		if (preMotion.isCancelled()) {
			huzuni.lookManager.cancelTask();
			ci.cancel();
		}
		huzuni.clickManager.onPreUpdate(preMotion);
		huzuni.hotbarManager.onPreUpdate(preMotion);
		huzuni.lookManager.onPreUpdate(preMotion);
		if (!Freecam.INSTANCE.isEnabled()) {
			onUpdateWalkingPlayer();
		}
		huzuni.lookManager.onPostUpdate();
		huzuni.hotbarManager.onPostUpdate();
		huzuni.clickManager.onPostUpdate();
		MCWrapper.getEventManager().publish(postMotion);
		ci.cancel();
	}

	@Override
	public void swingItem(Hand hand) {
		swingArm(EnumHand.values()[hand.ordinal()]);
	}

	@Override
	public void closeWindow() {
		closeScreen();
	}

	@Override
	public boolean isFlying() {
		return capabilities.isFlying;
	}

	@Override
	public void setFlying(boolean flying) {
		capabilities.isFlying = flying;
	}

	@Override
	public float getForwardMovement() {
		return movementInput.moveForward;
	}

	@Override
	public void setSneak(boolean sneak) {
		this.movementInput.sneak = sneak;
	}

	@Override
	public String getBrand() {
		return serverBrand;
	}

	@Override
	public void sendMessage(String message) {
		connection.sendPacket(new CPacketChatMessage(message));
	}

	@Override
	public PlayerInfo getInfo(Player player) {
		return ((PlayerInfo) connection.getPlayerInfo(player.getUUID()));
	}

	@Override
	public boolean isNPC() {
		return false;
	}

	@Override
	public boolean isUsingItem() {
		return isHandActive();
	}

	@Override
	public void setItemUseSlowdown(boolean slowdown) {
		this.itemSlowdown = slowdown;
	}

	@Override
	public boolean getItemUseSlowdown() {
		return itemSlowdown;
	}

	private boolean itemSlowdown = true;
}
