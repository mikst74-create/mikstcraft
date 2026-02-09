package ru.mikst74.mikstcraft.dictionary;

import ru.mikst74.mikstcraft.texture.TextureArrayLoader;
import ru.mikst74.mikstcraft.texture.TextureInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TextureDictionary {
    public static final String SOLID_BLOCK_TEXTURE_ARRAY = "SolidBlockTextureArray";
    public static TextureInfo DEFAULT_TEXTURE;
    private final Map<String, TextureInfo> allTextures;

    private static final TextureDictionary INSTANCE = new TextureDictionary();

    // Public static method to provide a global point of access
    public static TextureDictionary getInstance() {
        return INSTANCE;
    }

    private TextureDictionary() {
        allTextures = new HashMap<>();

    }

    public void init() {
        Map<String, String> loadMap = new HashMap<>();

        loadMap.put("DEFAULT", "DEFAULT.png");
        loadMap.put("Grass", "grass_block_top.png");
        loadMap.put("Acacia", "Acacia_Planks_(texture)_JE3_BE2.png");
        loadMap.put("Andesite", "Andesite_(texture)_JE3_BE2.png");
        loadMap.put("Basalt", "Basalt_(top_texture)_JE1_BE1.png");
        loadMap.put("Bedrock", "Bedrock_(texture)_JE2_BE2.png");
        loadMap.put("Birch", "Birch_Planks_(texture)_JE3_BE2.png");
        loadMap.put("Blackstone", "Blackstone_(top_texture)_JE1_BE1.png");
        loadMap.put("Iron", "Block_of_Raw_Iron_(texture)_JE3_BE2.png");
        loadMap.put("Bricks", "Bricks_(texture)_JE5_BE3.png");
        loadMap.put("Coral", "Brain_Coral_Block_(texture)_JE2_BE1.png");
        loadMap.put("Blue_Ice", "Blue_Ice_(texture)_JE1_BE1.png");
        addTextures(SOLID_BLOCK_TEXTURE_ARRAY,TextureArrayLoader.loadTextures(loadMap, 16, 16));

        DEFAULT_TEXTURE = allTextures.get("DEFAULT");
    }

    public void addTexture(TextureInfo ti) {
        allTextures.put(ti.getName(), ti);
    }

    public void addTextures(String arrayName, List<TextureInfo> til) {
        TextureInfo tiAr = TextureInfo.builder()
                .name(arrayName)
                .textureId(til.get(0).getTextureId())
                .textureArrayId(-1)
                .size(til.get(0).getSize())
                .isArray(true)
                .build();
        addTexture(tiAr);
        til.forEach(this::addTexture);
    }

    public static TextureInfo getTextureInfo(String name) {
        return getInstance().allTextures.get(name);
    }
}
