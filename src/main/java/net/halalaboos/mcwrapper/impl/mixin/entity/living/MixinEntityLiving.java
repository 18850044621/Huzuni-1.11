package net.halalaboos.mcwrapper.impl.mixin.entity.living;

import net.halalaboos.mcwrapper.api.entity.living.data.HealthData;
import net.halalaboos.mcwrapper.api.entity.living.Living;
import net.halalaboos.mcwrapper.api.entity.living.player.Hand;
import net.halalaboos.mcwrapper.api.item.ItemStack;
import net.halalaboos.mcwrapper.api.potion.PotionEffect;
import net.halalaboos.mcwrapper.impl.Convert;
import net.halalaboos.mcwrapper.impl.mixin.entity.MixinEntity;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.potion.Potion;
import net.minecraft.util.EnumHand;
import org.spongepowered.asm.mixin.*;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

@Mixin(net.minecraft.entity.EntityLivingBase.class)
@Implements(@Interface(iface = Living.class, prefix = "api$"))
public abstract class MixinEntityLiving extends MixinEntity implements Living {

	@Shadow public abstract IAttributeInstance getEntityAttribute(IAttribute attribute);

	@Shadow public abstract boolean isPotionActive(Potion potionIn);

	@Shadow public abstract float getHealth();
	@Shadow public abstract float getMaxHealth();
	@Shadow public abstract boolean isOnLadder();
	@Shadow protected abstract void shadow$jump();
	@Shadow public abstract net.minecraft.item.ItemStack getHeldItem(EnumHand hand);
	@Shadow public int maxHurtResistantTime;
	@Shadow public float jumpMovementFactor;
	@Shadow protected int activeItemStackUseCount;
	@Shadow @Final private Map<Potion, net.minecraft.potion.PotionEffect> activePotionsMap;
	@Shadow public abstract float getAbsorptionAmount();

	@Shadow
	public abstract int getTotalArmorValue();

	@Shadow
	public abstract void addPotionEffect(net.minecraft.potion.PotionEffect potioneffectIn);

	@Shadow
	public abstract void removePotionEffect(Potion potionIn);

	@Override
	public HealthData getHealthData() {
		return new HealthData(getHealth(), getMaxHealth(), getAbsorptionAmount());
	}

	@Intrinsic
	public void api$jump() {
		shadow$jump();
	}

	@Override
	public boolean isClimbing() {
		return isOnLadder();
	}

	@Override
	public Optional<ItemStack> getHeldItem(Hand hand) {
		if (getHeldItem(Convert.to(hand)).isEmpty()) {
			return Optional.empty();
		}
		return Optional.of((Convert.from(getHeldItem(Convert.to(hand)))));
	}

	@Override
	public int getMaxHurtResistantTime() {
		return maxHurtResistantTime;
	}

	@Override
	public float getJumpMovementFactor() {
		return jumpMovementFactor;
	}

	@Override
	public void setJumpMovementFactor(float movementFactor) {
		this.jumpMovementFactor = movementFactor;
	}

	@Override
	public int getItemUseTicks() {
		return activeItemStackUseCount;
	}

	@Override
	public Collection<PotionEffect> getEffects() {
		return ((Collection<PotionEffect>)(Object) activePotionsMap.values());
	}

	@Override
	public int getTotalArmor() {
		return getTotalArmorValue();
	}

	@Override
	public void addEffect(PotionEffect effect) {
		addPotionEffect(Convert.to(effect));
	}

	@Override
	public void removeEffect(net.halalaboos.mcwrapper.api.potion.Potion potion) {
		removePotionEffect(Convert.to(potion));
	}

	@Shadow
	public boolean canBePushed() {return true;}

	@Shadow protected boolean dead;
	@Shadow @Final protected static DataParameter<Byte> HAND_STATES;

	@Override
	public boolean isDead() {
		return dead;
	}

	@Override
	public Hand getCurrentHand() {
		return (this.dataManager.get(HAND_STATES) & 2) > 0 ? Hand.OFF : Hand.MAIN;
	}
}
