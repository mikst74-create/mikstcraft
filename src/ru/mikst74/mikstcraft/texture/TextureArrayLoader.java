package ru.mikst74.mikstcraft.texture;

import org.joml.Vector2i;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.mikst74.mikstcraft.util.IOUtils.ioResourceToByteBuffer;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11C.*;
import static org.lwjgl.opengl.GL12C.glTexSubImage3D;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL13C.GL_TEXTURE0;
import static org.lwjgl.opengl.GL30C.GL_TEXTURE_2D_ARRAY;
import static org.lwjgl.opengl.GL42C.glTexStorage3D;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class TextureArrayLoader {

    public static List<TextureInfo> loadTextures(Map<String, String> textureList, int width, int height) {
        try {
            try (MemoryStack frame = MemoryStack.stackPush()) {
                List<TextureInfo> res = new ArrayList<>();
                int id = glGenTextures();
                glActiveTexture(GL_TEXTURE0);

                glBindTexture(GL_TEXTURE_2D_ARRAY, id);

                int mipLevelCount = 1; // Or more if using mipmaps
                int layerCount = textureList.size();
                glTexStorage3D(GL_TEXTURE_2D_ARRAY, mipLevelCount, GL_RGBA8, width, height, layerCount);

                int arraySpot = 0;
                IntBuffer widthBuff = frame.mallocInt(1);
                IntBuffer heightBuff = frame.mallocInt(1);
                IntBuffer componentsBuff = frame.mallocInt(1);
                for (Map.Entry<String, String> item : textureList.entrySet()) {

                    ByteBuffer data = stbi_load_from_memory(ioResourceToByteBuffer("texture/" + item.getValue(), 1024)
                            , widthBuff, heightBuff, componentsBuff, 4);
                    if (width != widthBuff.get(0) || height != heightBuff.get(0)) {
                        throw new RuntimeException("Texture " + item.getValue() + " has size " + widthBuff.get(0) + "x" + heightBuff.get(0) + ", but need " + width + "x" + height);
                    }
                    data.flip();
                    // zoffset is the layer index, depth is 1 for a single layer upload
                    glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, arraySpot, width, height, 1, GL_RGBA, GL_UNSIGNED_BYTE, data);
                    stbi_image_free(data);

                    res.add(TextureInfo.builder()
                            .name(item.getKey())
                            .textureId(id)
                            .size(new Vector2i(width, height))
                            .fileName(item.getValue())
                            .textureArrayId(arraySpot)
                            .build());
                    arraySpot++;
                }

                // GL_LINEAR - будет размытие
//                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
//                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

                // GL_NEAREST - пиксельная текстура без замыливания
                glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
                glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//                glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
//                glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
//                glTexParameteri(GL_TEXTURE_2D, GL_GENERATE_MIPMAP, GL_TRUE);

                glBindTexture(GL_TEXTURE_2D_ARRAY, 0);

                return res;
            }
        } catch (IOException e) {
            throw new RuntimeException("no file " + textureList);
        }

    }
}
