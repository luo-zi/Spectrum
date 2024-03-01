package de.dafuqs.spectrum.api.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.loot.*;
import net.minecraft.loot.context.*;
import net.minecraft.server.world.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * A block that drops additional items from a loot table when stripped
 * Call checkAndDropStrippedLoot() in the blocks onStateReplaced()
 */
public interface StrippableDrop {
	
	Block getStrippedBlock();
	
	Identifier getStrippingLootTableIdentifier();
	
	default boolean checkAndDropStrippedLoot(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
		if (!moved && newState.isOf(getStrippedBlock())) {
			// we sadly don't have the entity or hand stack here, but oh well
			List<ItemStack> harvestedStacks = getStrippedStacks(state, (ServerWorld) world, pos, world.getBlockEntity(pos), null, ItemStack.EMPTY, getStrippingLootTableIdentifier());
			for (ItemStack harvestedStack : harvestedStacks) {
				ItemScatterer.spawn(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, harvestedStack);
			}
			return true;
		}
		return false;
	}
	
	static List<ItemStack> getStrippedStacks(BlockState state, ServerWorld world, BlockPos pos, @Nullable BlockEntity blockEntity, @Nullable Entity entity, ItemStack stack, Identifier lootTableIdentifier) {
		var builder = (new LootContextParameterSet.Builder(world))
				.add(LootContextParameters.BLOCK_STATE, state)
				.add(LootContextParameters.ORIGIN, Vec3d.ofCenter(pos))
				.add(LootContextParameters.TOOL, stack)
				.addOptional(LootContextParameters.THIS_ENTITY, entity)
				.addOptional(LootContextParameters.BLOCK_ENTITY, blockEntity);
		
		LootTable lootTable = world.getServer().getLootManager().getLootTable(lootTableIdentifier);
		return lootTable.generateLoot(builder.build(LootContextTypes.BLOCK));
	}
	
}
