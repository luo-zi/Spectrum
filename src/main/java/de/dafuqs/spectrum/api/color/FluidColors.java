package de.dafuqs.spectrum.api.color;

import net.minecraft.fluid.*;
import net.minecraft.util.*;

import java.util.*;

public class FluidColors extends ColorRegistry<Fluid> {
	
	private static final HashMap<Fluid, DyeColor> COLORS = new HashMap<>() {{
		put(Fluids.WATER, DyeColor.BLUE);
		put(Fluids.LAVA, DyeColor.ORANGE);
	}};

	@Override
	public void registerColorMapping(Fluid fluid, DyeColor dyeColor) {
		COLORS.put(fluid, dyeColor);
	}
	
	@Override
	public Optional<DyeColor> getMapping(Fluid fluid) {
		if (COLORS.containsKey(fluid)) {
			return Optional.of(COLORS.get(fluid));
		} else {
			return Optional.empty();
		}
	}
	
}
