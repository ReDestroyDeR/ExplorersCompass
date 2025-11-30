package com.chaosthedude.explorerscompass.workers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.chaosthedude.explorerscompass.config.ExplorersCompassConfig;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.apache.commons.lang3.RandomStringUtils;

import com.chaosthedude.explorerscompass.util.StructureUtils;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.chunk.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.gen.chunk.placement.StructurePlacement;
import net.minecraft.world.gen.structure.Structure;

public class SearchWorkerManager {

    private final String id = RandomStringUtils.random(8, "0123456789abcdef");

    private List<StructureSearchWorker<?>> workers;

    public SearchWorkerManager() {
        workers = new ArrayList<>();
    }

    public void createWorkers(
      ServerWorld world,
      PlayerEntity player,
      ItemStack stack,
      List<Pair<Identifier, Structure>> structures,
      BlockPos startPos
    ) {
        workers.clear();

        Map<StructurePlacement, List<Structure>> placementToStructuresMap = new Object2ObjectArrayMap<>();

        for (Pair<Identifier, Structure> s : structures) {
            var id = s.getLeft();
            var structure = s.getRight();

            var override = ExplorersCompassConfig.structureOverrides.get(id.toString());
            if (override != null) {
                workers.add(new OverrideSearchWorker(world, structure, override, stack));
                continue;
            }

            for (
              StructurePlacement structureplacement :
              world.getChunkManager()
                .getStructurePlacementCalculator()
                .getPlacements(StructureUtils.getEntryForStructure(world, structure))
            ) {
                placementToStructuresMap.computeIfAbsent(
                  structureplacement,
                  holderSet -> new ObjectArrayList<>()
                ).add(structure);
            }
        }

        for (Map.Entry<StructurePlacement, List<Structure>> entry : placementToStructuresMap.entrySet()) {
            StructurePlacement placement = entry.getKey();
            if (placement instanceof ConcentricRingsStructurePlacement) {
                workers.add(new ConcentricRingsSearchWorker(world, player, stack, startPos, (ConcentricRingsStructurePlacement) placement, entry.getValue(), id));
            } else if (placement instanceof RandomSpreadStructurePlacement) {
                workers.add(new RandomSpreadSearchWorker(world, player, stack, startPos, (RandomSpreadStructurePlacement) placement, entry.getValue(), id));
            } else {
                workers.add(new GenericSearchWorker(world, player, stack, startPos, placement, entry.getValue(), id));
            }
        }
    }

    // Returns true if a worker starts, false otherwise
    public boolean start() {
        if (!workers.isEmpty()) {
            workers.get(0).start();
            return true;
        }
        return false;
    }

    public void pop() {
        if (!workers.isEmpty()) {
            workers.remove(0);
        }
    }

    public void stop() {
        for (StructureSearchWorker<?> worker : workers) {
            worker.stop();
        }
    }

    public void clear() {
        workers.clear();
    }

}
