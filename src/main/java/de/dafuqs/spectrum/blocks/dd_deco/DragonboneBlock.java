package de.dafuqs.spectrum.blocks.dd_deco;

import de.dafuqs.spectrum.api.block.*;
import de.dafuqs.spectrum.registries.*;
import net.minecraft.block.*;
import net.minecraft.entity.*;
import net.minecraft.sound.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

public class DragonboneBlock extends CrackedDragonboneBlock implements MoonstoneStrikeableBlock {
	
	public DragonboneBlock(Settings settings) {
		super(settings);
	}
	
	@Override
	public void onMoonstoneStrike(World world, BlockPos pos, @Nullable LivingEntity striker) {
		crack(world, pos);
	}
	
	public void crack(World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		if (state.getBlock() instanceof DragonboneBlock) {
			world.setBlockState(pos, SpectrumBlocks.CRACKED_DRAGONBONE.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)));
			if (world.isClient) {
				world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_CRACK, SoundCategory.BLOCKS, 1.0F, MathHelper.nextBetween(world.random, 0.8F, 1.2F));
			}
		}
	}
	
	@Override
	public BlockState getStateForExplosion(World world, BlockPos blockPos, BlockState stateAtPos) {
		if (stateAtPos.getBlock() instanceof PillarBlock) {
			return SpectrumBlocks.CRACKED_DRAGONBONE.getDefaultState().with(PillarBlock.AXIS, stateAtPos.get(PillarBlock.AXIS));
		}
		return Blocks.AIR.getDefaultState();
	}
	
}
