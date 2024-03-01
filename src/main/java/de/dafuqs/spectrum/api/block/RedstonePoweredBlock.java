package de.dafuqs.spectrum.api.block;

import net.minecraft.state.property.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public interface RedstonePoweredBlock {
	
	BooleanProperty POWERED = BooleanProperty.of("powered");
	
	default boolean checkGettingPowered(World world, BlockPos pos) {
		Direction[] var4 = Direction.values();
		int var5 = var4.length;
		
		int var6;
		for (var6 = 0; var6 < var5; ++var6) {
			Direction direction = var4[var6];
			if (world.isEmittingRedstonePower(pos.offset(direction), direction)) {
				return true;
			}
		}
		
		if (world.isEmittingRedstonePower(pos, Direction.DOWN)) {
			return true;
		} else {
			BlockPos blockPos = pos.up();
			Direction[] var10 = Direction.values();
			var6 = var10.length;
			
			for (int var11 = 0; var11 < var6; ++var11) {
				Direction direction2 = var10[var11];
				if (direction2 != Direction.DOWN && world.isEmittingRedstonePower(blockPos.offset(direction2), direction2)) {
					return true;
				}
			}
			return false;
		}
	}
	
	default void power(World world, BlockPos pos) {
		world.setBlockState(pos, world.getBlockState(pos).with(POWERED, true));
	}
	
	default void unPower(World world, BlockPos pos) {
		world.setBlockState(pos, world.getBlockState(pos).with(POWERED, false));
	}
	
}
