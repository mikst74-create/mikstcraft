package ru.mikst74.mikstcraft.render.text;

import lombok.Getter;
import ru.mikst74.mikstcraft.model.font.Font;
import ru.mikst74.mikstcraft.model.font.Letter;
import ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem;

import java.util.ArrayList;
import java.util.List;

import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.color;
import static ru.mikst74.mikstcraft.render.shader.texturedquads.TexturedQuadsItem.pos;

@Getter
public class TextAreaRenderer extends LetterQuadsRenderer {
    private float        cols;
    private float        rows;
    private List<String> lines;
//    private int direction =0; // 0

    public TextAreaRenderer(Font font, float cols, float rows) {
        super(font, (int) Math.ceil(cols * rows));
        lines = new ArrayList<>();
        setGrid(cols, rows);
    }

    public void setGrid(float cols, float rows) {
        this.cols = cols;
        this.rows = rows;
    }

    public void setLines(List<String> lines) {
        this.lines = lines;
        applyTextChange();
    }

    public void addLine(String line) {
        lines.add(line);
        if (lines.size() > rows) {
            lines.remove(0);
        }
        applyTextChange();
    }

    public void applyTextChange() {
        float stepX = (float) 2 / cols;
        float stepY = (float) 2 / rows;
        float letterSizeX = getFont().getLetterSize().x;
        float letterSizeY = getFont().getLetterSize().y;
        List<TexturedQuadsItem> items = new ArrayList<>();
        int y = lines.size();
        for (String s0 : lines) {
            y--;
            String s = s0;
            int i = 0;
            for (Character c : s.toCharArray()) {
                Letter l = getFont().get(c);
                if (i >= cols) {
                    break;
                }
                items.add(
                        new TexturedQuadsItem(pos(stepX * i - 1, stepY * (y + 1) - 1, stepX * (i + 1) - 1, stepY * y - 1),
                                pos(letterSizeX * l.getX(), letterSizeY * l.getY(), letterSizeX * (l.getX() + 1), letterSizeY * (l.getY() + 1)),
                                color(0, 0, 1, 0.5f)));
                i++;
            }
//            y++;
        }
        setItems(items);
    }
}

