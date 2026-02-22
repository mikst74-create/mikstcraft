package ru.mikst74.mikstcraft.dictionary;

import ru.mikst74.mikstcraft.model.FaceMaterial;

import java.util.ArrayList;
import java.util.List;

public class MaterialDictionary {
    private final List<FaceMaterial> faceMaterialList = new ArrayList<>();

    public MaterialDictionary() {
//        faceMaterialList.addAll(
//                Arrays.asList(
//                        new FaceMaterial(rgb(0, 0, 0)),
//                        new FaceMaterial(rgb(46, 213, 64)),
//                        new FaceMaterial(rgb(255, 0, 0)),
//                        new FaceMaterial(rgb(255, 115, 0)),
//                        new FaceMaterial(rgb(252, 252, 0)),
//                        new FaceMaterial(rgb(0, 255, 255)),
//                        new FaceMaterial(rgb(63, 0, 255)),
//                        new FaceMaterial(rgb(76, 50, 176)),
//                        new FaceMaterial(rgb(2, 3, 3)),
//                        new FaceMaterial(rgb(255, 255, 255)),
//                        new FaceMaterial(rgb(42, 45, 46)),
//                        new FaceMaterial(rgb(157, 12, 205)),
//                        new FaceMaterial(rgb(71, 28, 19)),
//                        new FaceMaterial(rgb(30, 166, 154)),
//                        new FaceMaterial(rgb(8, 45, 61)),
//                        new FaceMaterial(rgb(11, 61, 3)),
//                        new FaceMaterial(rgb(75, 75, 124)),
//                        new FaceMaterial(rgb(28, 180, 133))));
    }

    public FaceMaterial get(int id) {
        return faceMaterialList.get(id);
    }

    public int count(){return faceMaterialList.size();}
}
