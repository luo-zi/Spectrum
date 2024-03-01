package de.dafuqs.spectrum.progression.advancement;

import com.google.gson.*;
import de.dafuqs.spectrum.*;
import de.dafuqs.spectrum.api.item.*;
import net.minecraft.advancement.criterion.*;
import net.minecraft.entity.effect.*;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.predicate.*;
import net.minecraft.predicate.entity.*;
import net.minecraft.predicate.item.*;
import net.minecraft.server.network.*;
import net.minecraft.util.*;

import java.util.*;

public class PotionWorkshopBrewingCriterion extends AbstractCriterion<PotionWorkshopBrewingCriterion.Conditions> {
	
	static final Identifier ID = SpectrumCommon.locate("potion_workshop_brewing");
	
	public static PotionWorkshopBrewingCriterion.Conditions create(ItemPredicate itemPredicate, EntityEffectPredicate effectsPredicate, NumberRange.IntRange brewedCountRange, NumberRange.IntRange maxAmplifierRange, NumberRange.IntRange maxDurationRange, NumberRange.IntRange effectCountRange, NumberRange.IntRange uniqueEffectCountRange) {
		return new PotionWorkshopBrewingCriterion.Conditions(LootContextPredicate.EMPTY, itemPredicate, effectsPredicate, brewedCountRange, maxAmplifierRange, maxDurationRange, effectCountRange, uniqueEffectCountRange);
	}
	
	@Override
	public Identifier getId() {
		return ID;
	}
	
	@Override
	public PotionWorkshopBrewingCriterion.Conditions conditionsFromJson(JsonObject jsonObject, LootContextPredicate extended, AdvancementEntityPredicateDeserializer advancementEntityPredicateDeserializer) {
		ItemPredicate itemPredicate = ItemPredicate.fromJson(jsonObject.get("item"));
		EntityEffectPredicate statusEffectsPredicate = EntityEffectPredicate.fromJson(jsonObject.get("effects"));
		NumberRange.IntRange brewedCountRange = NumberRange.IntRange.fromJson(jsonObject.get("brewed_count"));
		NumberRange.IntRange maxAmplifierRange = NumberRange.IntRange.fromJson(jsonObject.get("highest_amplifier"));
		NumberRange.IntRange maxDurationRange = NumberRange.IntRange.fromJson(jsonObject.get("longest_duration"));
		NumberRange.IntRange effectCountRange = NumberRange.IntRange.fromJson(jsonObject.get("effect_count"));
		NumberRange.IntRange uniqueEffectCountRange = NumberRange.IntRange.fromJson(jsonObject.get("unique_effect_count"));
		return new PotionWorkshopBrewingCriterion.Conditions(extended, itemPredicate, statusEffectsPredicate, brewedCountRange, maxAmplifierRange, maxDurationRange, effectCountRange, uniqueEffectCountRange);
	}
	
	@SuppressWarnings("deprecation")
	public void trigger(ServerPlayerEntity player, ItemStack itemStack, int brewedCount) {
		this.trigger(player, conditions -> {
			List<StatusEffectInstance> effects;
			if (itemStack.getItem() instanceof InkPoweredPotionFillable inkPoweredPotionFillable) {
				effects = inkPoweredPotionFillable.getVanillaEffects(itemStack);
			} else {
				effects = PotionUtil.getPotionEffects(itemStack);
			}
			
			int highestAmplifier = 0;
			int longestDuration = 0;
			for (StatusEffectInstance instance : effects) {
				if (instance.getAmplifier() > highestAmplifier) {
					highestAmplifier = instance.getAmplifier();
				}
				if (instance.getDuration() > longestDuration) {
					longestDuration = instance.getDuration();
				}
			}
			
			List<StatusEffect> uniqueEffects = new ArrayList<>();
			for (StatusEffectInstance instance : effects) {
				if (!uniqueEffects.contains(instance.getEffectType())) {
					uniqueEffects.add(instance.getEffectType());
				}
			}
			
			return conditions.matches(itemStack, effects, brewedCount, highestAmplifier, longestDuration, effects.size(), uniqueEffects.size());
		});
	}
	
	public static class Conditions extends AbstractCriterionConditions {
		private final ItemPredicate itemPredicate;
		private final EntityEffectPredicate statusEffectsPredicate;
		private final NumberRange.IntRange brewedCountRange;
		private final NumberRange.IntRange highestEffectAmplifierRange;
		private final NumberRange.IntRange longestEffectDurationRange;
		private final NumberRange.IntRange effectCountRange;
		private final NumberRange.IntRange uniqueEffectCountRange;
		
		public Conditions(LootContextPredicate player, ItemPredicate itemPredicate, EntityEffectPredicate statusEffectsPredicate, NumberRange.IntRange brewedCountRange, NumberRange.IntRange highestEffectAmplifierRange, NumberRange.IntRange longestEffectDurationRange, NumberRange.IntRange effectCountRange, NumberRange.IntRange uniqueEffectCountRange) {
			super(ID, player);
			this.itemPredicate = itemPredicate;
			this.statusEffectsPredicate = statusEffectsPredicate;
			this.brewedCountRange = brewedCountRange;
			this.highestEffectAmplifierRange = highestEffectAmplifierRange;
			this.longestEffectDurationRange = longestEffectDurationRange;
			this.effectCountRange = effectCountRange;
			this.uniqueEffectCountRange = uniqueEffectCountRange;
		}
		
		@Override
		public JsonObject toJson(AdvancementEntityPredicateSerializer predicateSerializer) {
			JsonObject jsonObject = super.toJson(predicateSerializer);
			jsonObject.add("items", this.itemPredicate.toJson());
			jsonObject.add("effects", this.statusEffectsPredicate.toJson());
			jsonObject.add("brewed_count", this.brewedCountRange.toJson());
			jsonObject.add("highest_amplifier", this.highestEffectAmplifierRange.toJson());
			jsonObject.add("longest_duration", this.longestEffectDurationRange.toJson());
			jsonObject.add("effect_count", this.effectCountRange.toJson());
			jsonObject.add("unique_effect_count", this.uniqueEffectCountRange.toJson());
			return jsonObject;
		}
		
		public boolean matches(ItemStack stack, List<StatusEffectInstance> effects, int brewedCount, int maxAmplifier, int maxDuration, int effectCount, int uniqueEffectCount) {
			if (this.brewedCountRange.test(brewedCount) &&
					this.highestEffectAmplifierRange.test(maxAmplifier) &&
					this.longestEffectDurationRange.test(maxDuration) &&
					this.effectCountRange.test(effectCount) &&
					this.uniqueEffectCountRange.test(uniqueEffectCount) &&
					this.itemPredicate.test(stack))
			{
				Map<StatusEffect, StatusEffectInstance> effectMap = new HashMap<>();
				for (StatusEffectInstance instance : effects) {
					if (!effectMap.containsKey(instance.getEffectType())) {
						effectMap.put(instance.getEffectType(), instance);
					}
				}
				
				return this.statusEffectsPredicate.test(effectMap);
			}

			return false;
		}
	}
	
}
