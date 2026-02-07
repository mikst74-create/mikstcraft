package ru.mikst74.mikstcraft.model;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.coo.WorldCoo;

@Getter
public class SelectedVoxel {
    private final WorldCoo     coo;
    private       boolean      hasSelection;
    private       NeighborCode selectedFace;

    public SelectedVoxel() {
        this.coo = new WorldCoo();
    }

    @Override
    public String toString() {
        return "SelectedVoxel{" +
                "coo=" + coo +
                ", hasSelection=" + hasSelection +
                ", selectedFace=" + selectedFace +
                '}';
    }

    public void setSelectedVoxel(WorldCoo selectedCoo, NeighborCode selectedFace) {
        coo.assign(selectedCoo);
        hasSelection      = true;
        this.selectedFace = selectedFace;
    }

    public void unselectVoxel() {
        coo.assign(0, 0, 0);
        hasSelection = false;
    }
}
