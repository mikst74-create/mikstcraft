package ru.mikst74.mikstcraft.storage;

import ru.mikst74.mikstcraft.model.chunk.Chunk;
import ru.mikst74.mikstcraft.world.WorldMap;

import java.io.*;

public class WorldSaver {
    private WorldMap worldMap;

    public WorldSaver(WorldMap worldMap) {
        this.worldMap = worldMap;
    }

    public void save() {
        if (false) {
            System.out.println("Save world ...");
            worldMap.getChunkManager().getAllNotSavedChunk().forEach(this::saveChunk);
            System.out.println("done!");
        } else {
            System.out.println("WARNING!!!!!!!!!!!!!! World NOT BE SAVED!");
        }
    }

    private void saveChunk(Chunk chunk) {
        ObjectOutputStream oos = null;
        try {
            FileOutputStream fOut = new FileOutputStream(getFileName(chunk), false);
            oos = new ObjectOutputStream(fOut);
            oos.writeObject(chunk);
            chunk.markAsSaved();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    //  throw new RuntimeException(e);
                }
            }
        }
    }

    private static String getFileName(Chunk chunk) {
        return "saves/chunk_" + chunk.getCoo().getX() + "_" + chunk.getCoo().getY() + "_" + chunk.getCoo().getZ() + ".dat";
    }

    public static boolean loadChunk(Chunk chunk) {
        ObjectInputStream objectinputstream = null;
        try {
            FileInputStream streamIn = new FileInputStream(getFileName(chunk));
            objectinputstream = new ObjectInputStream(streamIn);
            Chunk loadedChunk = (Chunk) objectinputstream.readObject();
            chunk.setVoxelFieldData(loadedChunk.getVoxelField());
            System.out.println("loaded chunk:" + loadedChunk);
            return true;
        } catch (FileNotFoundException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (objectinputstream != null) {
                try {
                    objectinputstream.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return false;
    }
}
