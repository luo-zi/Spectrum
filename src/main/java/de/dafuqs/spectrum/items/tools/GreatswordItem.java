package de.dafuqs.spectrum.items.tools;

import com.google.common.collect.*;
import com.jamieswhiteshirt.reachentityattributes.*;
import de.dafuqs.spectrum.api.item.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.attribute.*;
import net.minecraft.item.*;

import java.util.*;

/**
 * A sword with additional reach
 */
public class GreatswordItem extends SwordItem implements Preenchanted {

	protected static final UUID REACH_MODIFIER_ID = UUID.fromString("3b9a13c8-a9a7-4545-8c32-e60baf25823e");

	// shadowing SwordItem's properties in a way, since those are private final
	private final float attackDamage;
	private final Multimap<EntityAttribute, EntityAttributeModifier> attributeModifiers;

	public GreatswordItem(ToolMaterial material, int attackDamage, float attackSpeed, float extraReach, Settings settings) {
		super(material, attackDamage, attackSpeed, settings);

		this.attackDamage = (float) attackDamage + material.getAttackDamage();
		ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ATTACK_DAMAGE_MODIFIER_ID, "Weapon modifier", this.attackDamage, EntityAttributeModifier.Operation.ADDITION));
		builder.put(EntityAttributes.GENERIC_ATTACK_SPEED, new EntityAttributeModifier(ATTACK_SPEED_MODIFIER_ID, "Weapon modifier", attackSpeed, EntityAttributeModifier.Operation.ADDITION));
		builder.put(ReachEntityAttributes.ATTACK_RANGE, new EntityAttributeModifier(REACH_MODIFIER_ID, "Weapon modifier", extraReach, EntityAttributeModifier.Operation.ADDITION));
		this.attributeModifiers = builder.build();
	}

	@Override
	public float getAttackDamage() {
		return this.attackDamage;
	}

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getAttributeModifiers(EquipmentSlot slot) {
		return slot == EquipmentSlot.MAINHAND ? attributeModifiers : super.getAttributeModifiers(slot);
	}

	@Override
	public Map<Enchantment, Integer> getDefaultEnchantments() {
		return Map.of(Enchantments.SWEEPING, 4);
	}

	@Override
	public ItemStack getDefaultStack() {
		return getDefaultEnchantedStack(this);
	}

}
