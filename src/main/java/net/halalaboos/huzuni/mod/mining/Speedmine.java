package net.halalaboos.huzuni.mod.mining;

import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.node.Toggleable;
import net.halalaboos.huzuni.api.node.Value;
import net.halalaboos.mcwrapper.api.event.player.BlockDigEvent;
import net.halalaboos.mcwrapper.api.event.player.PreMotionUpdateEvent;
import net.halalaboos.mcwrapper.api.util.enums.DigAction;
import org.lwjgl.input.Keyboard;

import static net.halalaboos.mcwrapper.api.MCWrapper.getController;
import static net.halalaboos.mcwrapper.api.MCWrapper.getMinecraft;
import static net.halalaboos.mcwrapper.api.MCWrapper.getPlayer;

/**
 * Adjusts the player's mining speed, as well as give the option to modify the wait time between breaking blocks,
 * and disable the slowdown from mining while jumping/falling.
 */
public class Speedmine extends BasicMod {

	private final Value speed = new Value("Mine speed", 1F, 1F, 2F, "Mine speed modifier");
	private final Value breakPercent = new Value("Break Percent", 0F, 97F, 100F, 1F, "Block damage percent to break at");
	private final Value hitDelay = new Value("Hit Delay", 0F, 0F, 5F, 1F, "The delay between breaking blocks.");
	private final Toggleable noSlow = new Toggleable("No Slowdown", "Allows you to dig under yourself quicker.");

	public Speedmine() {
		super("Speedmine", "Mines blocks at a faster rate", Keyboard.KEY_V);
		this.setCategory(Category.MINING);
		setAuthor("Halalaboos");
		this.addChildren(speed, breakPercent, hitDelay, noSlow);
		//For adjusting the mining speed
		subscribe(BlockDigEvent.class, event -> {
			//If no slowdown is enabled, then we will multiply the dig speed by 5 if we aren't on the ground
			float multiplier = noSlow.isEnabled() && getPlayer().getFallDistance() <= 1F
					&& getPlayer().getFallDistance() > 0 ? 5F : 1F;
			//Set the event dig multiplier to the no slowdown multiplier, and multiply that by our speed value
			event.multiplier = multiplier * speed.getValue();
			//If the current block break progress is greater than or equal to our break percent value...
			if (event.progress >= breakPercent.getValue() / 100F) {
				//Do the client-side block destroying
				getController().onBlockDestroy(event.position);
				//And tell the server we broke the block too
				getMinecraft().getNetworkHandler().sendDigging(DigAction.COMPLETE, event.position, event.faceOrdinal);
			}
		});
		//For applying the hit delay
		subscribe(PreMotionUpdateEvent.class, event -> {
			//If our current block hit delay is greater than the hitdelay value...
			if (getController().getHitDelay() > hitDelay.getValue()) {
				//Change it to the hit delay value.
				getController().setHitDelay(((int) hitDelay.getValue()));
			}
		});
	}
}
