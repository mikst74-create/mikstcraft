package ru.mikst74.mikstcraft.model.font;

import lombok.Getter;
import lombok.SneakyThrows;
import org.joml.Vector2f;
import ru.mikst74.mikstcraft.texture.TextureInfo;
import ru.mikst74.mikstcraft.texture.TextureLoader;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 *
 * https://learnopengl.com/In-Practice/Text-Rendering
 *
 */
@Getter
public class Font {
    private final TextureInfo textureInfo;
    private final Map<Character, Letter> letters;
    private final Vector2f letterSize;
    private final Letter unknownLetter;

    @SneakyThrows
    public Font(String fileName, List<String> letterSet) {
        textureInfo = TextureLoader.loadTexture(fileName);
        this.letters = new HashMap<>();
        int lettersInRow = letterSet.stream().map(String::length).max(Integer::compareTo).orElseGet(() -> 0);
        int rowCount = letterSet.size();
        if (lettersInRow == 0 || rowCount == 0) {
            throw new RuntimeException("Can't load Font, letterSet must not be empty");
        }

        float letterW = 1.0f / lettersInRow;
        float letterH = 1.0f / rowCount;

        letterSize = new Vector2f(letterW, letterH);
        unknownLetter = new Letter(' ', 0, 0);
        int y = 0;
        for (String l : letterSet) {
            int x = 0;
            for (Character c : l.toCharArray()) {
                letters.put(c, new Letter(c, x, y));
                x++;
            }
            y++;
        }
    }

    public Letter get(Character c) {
        return letters.getOrDefault(c, letters.getOrDefault(' ', unknownLetter));
    }
}
