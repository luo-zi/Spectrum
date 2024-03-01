package de.dafuqs.spectrum.items.magic_items;

import de.dafuqs.spectrum.api.item.*;
import de.dafuqs.spectrum.registries.*;
import net.minecraft.block.entity.*;
import net.minecraft.client.item.*;
import net.minecraft.enchantment.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.registry.entry.*;
import net.minecraft.server.network.*;
import net.minecraft.sound.*;
import net.minecraft.text.*;
import net.minecraft.util.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class KnowledgeGemItem extends Item implements ExperienceStorageItem, ExtendedEnchantable, LoomPatternProvider {
	
	private final int maxStorageBase;
	
	// these are copies from the item model file
	// and specify the sprite used for its texture
	protected final int[] displayTiers = {1, 10, 25, 50, 100, 250, 500, 1000, 2500, 5000};
	
	public KnowledgeGemItem(Settings settings, int maxStorageBase) {
		super(settings);
		this.maxStorageBase = maxStorageBase;
	}
	
	public static ItemStack getKnowledgeDropStackWithXP(int experience, boolean noStoreTooltip) {
		ItemStack stack = new ItemStack(SpectrumItems.KNOWLEDGE_GEM);
		NbtCompound compound = new NbtCompound();
		compound.putInt("stored_experience", experience);
		if (noStoreTooltip) {
			compound.putBoolean("do_not_display_store_tooltip", true);
		}
		stack.setNbt(compound);
		return stack;
	}
	
	@Override
	public int getMaxStoredExperience(ItemStack itemStack) {
		int efficiencyLevel = EnchantmentHelper.getLevel(Enchantments.EFFICIENCY, itemStack);
		return maxStorageBase * (int) Math.pow(10, Math.min(5, efficiencyLevel)); // to not exceed int max
	}
	
	@Override
	public UseAction getUseAction(ItemStack stack) {
		return UseAction.BOW;
	}
	
	public int getTransferableExperiencePerTick(ItemStack itemStack) {
		int quickChargeLevel = EnchantmentHelper.getLevel(Enchantments.QUICK_CHARGE, itemStack);
		return (int) (2 * Math.pow(2, Math.min(10, quickChargeLevel)));
	}
	
	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		return ItemUsage.consumeHeldItem(world, user, hand);
	}
	
	@Override
	public int getMaxUseTime(ItemStack stack) {
		return Integer.MAX_VALUE;
	}
	
	@Override
	public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
		super.usageTick(world, user, stack, remainingUseTicks);
		if (user instanceof ServerPlayerEntity serverPlayerEntity) {
			
			int playerExperience = serverPlayerEntity.totalExperience;
			int itemExperience = ExperienceStorageItem.getStoredExperience(stack);
			int transferableExperience = getTransferableExperiencePerTick(stack);
			
			if (serverPlayerEntity.isSneaking()) {
				int maxStorage = getMaxStoredExperience(stack);
				int experienceToTransfer = serverPlayerEntity.isCreative() ? Math.min(transferableExperience, maxStorage - itemExperience) : Math.min(Math.min(transferableExperience, playerExperience), maxStorage - itemExperience);
				
				// store experience in gem; drain from player
				if (experienceToTransfer > 0 && itemExperience < maxStorage && removePlayerExperience(serverPlayerEntity, experienceToTransfer)) {
					ExperienceStorageItem.addStoredExperience(stack, experienceToTransfer);
					
					if (remainingUseTicks % 4 == 0) {
						world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3F, 0.8F + world.getRandom().nextFloat() * 0.4F);
					}
				}
			} else {
				// drain experience from gem; give to player
				if (itemExperience > 0 && playerExperience != Integer.MAX_VALUE) {
					int experienceToTransfer = Math.min(Math.min(transferableExperience, itemExperience), Integer.MAX_VALUE - playerExperience);
					
					if (experienceToTransfer > 0) {
						if (!serverPlayerEntity.isCreative()) {
							serverPlayerEntity.addExperience(experienceToTransfer);
						}
						ExperienceStorageItem.removeStoredExperience(stack, experienceToTransfer);
						
						if (remainingUseTicks % 4 == 0) {
							world.playSound(null, user.getBlockPos(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.3F, 0.8F + world.getRandom().nextFloat() * 0.4F);
						}
					}
				}
			}
		}
	}
	
	@Override
	public void appendTooltip(ItemStack itemStack, World world, List<Text> tooltip, TooltipContext tooltipContext) {
		super.appendTooltip(itemStack, world, tooltip, tooltipContext);
		
		int maxExperience = getMaxStoredExperience(itemStack);
		int storedExperience = ExperienceStorageItem.getStoredExperience(itemStack);
		if (storedExperience == 0) {
			tooltip.add(Text.literal("0 ").formatted(Formatting.DARK_GRAY).append(Text.translatable("item.spectrum.knowledge_gem.tooltip.stored_experience", maxExperience).formatted(Formatting.GRAY)));
		} else {
			tooltip.add(Text.literal(storedExperience + " ").formatted(Formatting.GREEN).append(Text.translatable("item.spectrum.knowledge_gem.tooltip.stored_experience", maxExperience).formatted(Formatting.GRAY)));
		}
		if (shouldDisplayUsageTooltip(itemStack)) {
			tooltip.add(Text.translatable("item.spectrum.knowledge_gem.tooltip.use", getTransferableExperiencePerTick(itemStack)).formatted(Formatting.GRAY));
			addBannerPatternProviderTooltip(tooltip);
		}
	}
	
	public boolean shouldDisplayUsageTooltip(ItemStack itemStack) {
		NbtCompound nbtCompound = itemStack.getNbt();
		return nbtCompound == null || !nbtCompound.getBoolean("do_not_display_store_tooltip");
	}
	
	public boolean removePlayerExperience(@NotNull PlayerEntity playerEntity, int experience) {
		if (playerEntity.isCreative()) {
			return true;
		} else if (playerEntity.totalExperience < experience) {
			return false;
		} else {
			playerEntity.totalExperience -= experience;
			
			// recalculate levels & level progress
			playerEntity.experienceProgress -= (float) experience / (float) playerEntity.getNextLevelExperience();
			while (playerEntity.experienceProgress < 0.0F) {
				float f = playerEntity.experienceProgress * (float) playerEntity.getNextLevelExperience();
				if (playerEntity.experienceLevel > 0) {
					playerEntity.addExperienceLevels(-1);
					playerEntity.experienceProgress = 1.0F + f / (float) playerEntity.getNextLevelExperience();
				} else {
					playerEntity.addExperienceLevels(-1);
					playerEntity.experienceProgress = 0.0F;
				}
			}
			return true;
		}
	}
	
	public boolean changedDisplayTier(int currentStoredExperience, int destinationStoredExperience) {
		return getDisplayTierForExperience(currentStoredExperience) != getDisplayTierForExperience(destinationStoredExperience);
	}
	
	public int getDisplayTierForExperience(int experience) {
		for (int i = 0; i < displayTiers.length; i++) {
			if (experience < displayTiers[i]) {
				return i;
			}
		}
		return displayTiers.length;
	}
	
	@Override
	public RegistryEntry<BannerPattern> getPattern() {
		return SpectrumBannerPatterns.KNOWLEDGE_GEM;
	}
	
	@Override
	public boolean isEnchantable(ItemStack stack) {
		return stack.getCount() == 1;
	}
	
	@Override
	public boolean acceptsEnchantment(Enchantment enchantment) {
		return enchantment == Enchantments.EFFICIENCY || enchantment == Enchantments.QUICK_CHARGE;
	}
	
	@Override
	public int getEnchantability() {
		return 5;
	}

//	@Override
//	public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
//		super.appendStacks(group, stacks);
//		if (this.isIn(group)) {
//			ItemStack stack = getDefaultStack();
//			ExperienceStorageItem.addStoredExperience(stack, getMaxStoredExperience(stack));
//			stacks.add(stack);
//
//			ItemStack enchantedStack = SpectrumEnchantmentHelper.getMaxEnchantedStack(this);
//			ExperienceStorageItem.addStoredExperience(enchantedStack, getMaxStoredExperience(stack));
//			stacks.add(enchantedStack);
//		}
//	}

}
