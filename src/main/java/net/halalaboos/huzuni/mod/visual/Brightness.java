package net.halalaboos.huzuni.mod.visual;

import net.halalaboos.huzuni.api.event.EventManager.EventMethod;
import net.halalaboos.huzuni.api.event.UpdateEvent;
import net.halalaboos.huzuni.api.mod.BasicMod;
import net.halalaboos.huzuni.api.mod.Category;
import net.halalaboos.mcwrapper.api.potion.Potion;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import org.lwjgl.input.Keyboard;

import static net.halalaboos.mcwrapper.api.MCWrapper.getAdapter;
import static net.halalaboos.mcwrapper.api.MCWrapper.getPlayer;

/**
 * Applies the night vision potion effect with a duration of 1,000,000 to the player.
 * */
public class Brightness extends BasicMod {
		
	private Potion nightVision;
	
	public Brightness() {
		super("Brightness", "Light up your world as you recieve the night vision potion effect", Keyboard.KEY_C);
		setAuthor("brudin");
		this.setCategory(Category.VISUAL);
	}
	
	@Override
	public void onEnable() {
		huzuni.eventManager.addListener(this);
	}
	
	@Override
	public void onDisable() {
		huzuni.eventManager.removeListener(this);
		if (nightVision != null) {
			getPlayer().removeEffect(nightVision);
		}
	}

	@EventMethod
	public void onUpdate(UpdateEvent event) {
		int duration = 1000000;
		if (nightVision == null) {
			nightVision = getAdapter().getPotionRegistry().getPotion("night_vision");
		}
		PotionEffect effect = PotionEffect.from(nightVision, duration, 1, true, false);
		getPlayer().addEffect(effect);
	}

}
