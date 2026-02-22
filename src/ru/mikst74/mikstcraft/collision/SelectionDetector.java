package ru.mikst74.mikstcraft.collision;

import lombok.Getter;
import org.joml.Vector3f;
import ru.mikst74.mikstcraft.model.SelectedVoxel;
import ru.mikst74.mikstcraft.util.raytrace.RaytraceVoxelSearch;
import ru.mikst74.mikstcraft.world.WorldMap;

public class SelectionDetector {
    private       WorldMap      worldMap;
    @Getter
    private final SelectedVoxel selectedVoxel;

    public SelectionDetector( WorldMap worldMap) {
        this.worldMap = worldMap;
        selectedVoxel = new SelectedVoxel();
    }

    public void determineSelectedVoxel(Vector3f fromPosition, Vector3f toDirection) {
        if (!RaytraceVoxelSearch.
                traverseFromAI(fromPosition, toDirection, worldMap::getVoxel, selectedVoxel::setSelectedVoxel)) {
            selectedVoxel.unselectVoxel();
        }
    }
}
