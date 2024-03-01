package de.dafuqs.spectrum.api.color;

import net.minecraft.util.*;

import java.util.*;

public abstract class ColorRegistry<T> {
	
	public static ItemColors ITEM_COLORS;
	public static FluidColors FLUID_COLORS;
	
	public static void registerColorRegistries() {
		ITEM_COLORS = new ItemColors();
		FLUID_COLORS = new FluidColors();
	}
	
	public abstract void registerColorMapping(T object, DyeColor dyeColor);
	
	public abstract Optional<DyeColor> getMapping(T element);
	
}
