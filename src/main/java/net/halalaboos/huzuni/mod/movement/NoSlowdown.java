package net.halalaboos.huzuni.mod.movement;

import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.huzuni.api.node.Toggleable;
import net.halalaboos.huzuni.api.node.Value;
import net.halalaboos.mcwrapper.api.block.BlockTypes;
import net.halalaboos.mcwrapper.api.event.PostMotionUpdateEvent;

import static net.halalaboos.mcwrapper.api.MCWrapper.getPlayer;

public class NoSlowdown extends BasicMod {

	private final Toggleable itemUse = new Toggleable("Item Use", "Move at normal speeds using items.");
	private final Toggleable fastIce = new Toggleable("Fast Ice", "Move on ice at normal (or fast) speeds.");
	private final Value iceSpeed = new Value("Ice Slipperiness", 0.3F, 0.45F, 0.98F, 0.1F, "The lower, the faster.");

	public NoSlowdown() {
		super("No slowdown", "Prevents various things from slowing you down.");
		setAuthor("brudin");
		setCategory(Category.MOVEMENT);
		itemUse.setEnabled(true);
		fastIce.setEnabled(true);
		addChildren(itemUse, fastIce, iceSpeed);
		subscribe(PostMotionUpdateEvent.class, event -> {
			setIceSpeed(fastIce.isEnabled());
			getPlayer().setItemUseSlowdown(!itemUse.isEnabled());
		});
	}

	@Override
	protected void onDisable() {
		getPlayer().setItemUseSlowdown(true);
		setIceSpeed(false);
	}

	private void setIceSpeed(boolean enabled) {
		float speed = enabled ? iceSpeed.getValue() : 0.98F;
		BlockTypes.ICE.setSlipperiness(speed);
		BlockTypes.PACKED_ICE.setSlipperiness(speed);
		BlockTypes.FROSTED_ICE.setSlipperiness(speed);
	}
}
