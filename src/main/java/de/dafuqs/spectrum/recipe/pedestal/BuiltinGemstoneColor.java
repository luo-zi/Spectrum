package de.dafuqs.spectrum.recipe.pedestal;

import de.dafuqs.spectrum.api.item.*;
import de.dafuqs.spectrum.registries.*;
import net.minecraft.item.*;
import net.minecraft.util.*;

public enum BuiltinGemstoneColor implements GemstoneColor {
	CYAN(DyeColor.CYAN),
	MAGENTA(DyeColor.MAGENTA),
	YELLOW(DyeColor.YELLOW),
	BLACK(DyeColor.BLACK),
	WHITE(DyeColor.WHITE);
	
	private final DyeColor dyeColor;
	
	BuiltinGemstoneColor(DyeColor dyeColor) {
		this.dyeColor = dyeColor;
	}
	
	@Override
	public DyeColor getDyeColor() {
		return this.dyeColor;
	}
	
	@Override
	public Item getGemstonePowderItem() {
		switch (this) {
			case CYAN -> {
				return SpectrumItems.TOPAZ_POWDER;
			}
			case MAGENTA -> {
				return SpectrumItems.AMETHYST_POWDER;
			}
			case YELLOW -> {
				return SpectrumItems.CITRINE_POWDER;
			}
			case BLACK -> {
				return SpectrumItems.ONYX_POWDER;
			}
			default -> {
				return SpectrumItems.MOONSTONE_POWDER;
			}
		}
	}
	
}