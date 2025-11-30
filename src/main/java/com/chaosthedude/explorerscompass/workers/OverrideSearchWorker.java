package com.chaosthedude.explorerscompass.workers;

import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;

public class OverrideSearchWorker extends StructureSearchWorker<StructurePlacement> {

    public OverrideSearchWorker(
      ServerWorld world,
      Structure structure,
      BlockPos override,
      ItemStack stack
    ) {
        super(world, null, stack, null, null, null, null);

        this.succeed(override, structure);
    }


    @Override
    protected String getName() {
        return "OverrideSearchWorker";
    }

    @Override
    protected boolean shouldLogRadius() {
        return false;
    }

    @Override
    public boolean doWork() {
        return true;
    }
}
