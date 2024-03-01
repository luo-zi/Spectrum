package de.dafuqs.spectrum.api.item;

import de.dafuqs.spectrum.helpers.*;
import net.minecraft.entity.player.*;
import net.minecraft.item.*;
import net.minecraft.util.hit.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public interface AoEBreakingTool {
	
	default void onTryBreakBlock(ItemStack stack, BlockPos pos, PlayerEntity player) {
		World world = player.getWorld();
		BlockHitResult hitResult = (BlockHitResult) player.raycast(10, 1, false);
		if (!world.isClient && hitResult.getType() == HitResult.Type.BLOCK) {
			Direction side = hitResult.getSide();
			if (canUseAoE(player, stack)) {
				AoEHelper.doAoEBlockBreaking(player, stack, pos, side, getAoERange(stack));
			}
		}
	}
	
	/**
	 * Called when breaking a block to check if the stack can use it's AoE ability
	 * Return false if AoE ability disabled / player can't pay energy for AoE mining, ...
	 *
	 * @param stack the stack blocks get broken with
	 * @return true to do AoE mining, false to skip AoE mining
	 */
	boolean canUseAoE(PlayerEntity player, ItemStack stack);
	
	/**
	 * The range this tool breaks blocks via AoE
	 *
	 * @param stack the AoEBreakingTool stack
	 * @return max square radius of block breaking
	 */
	int getAoERange(ItemStack stack);
	
}
