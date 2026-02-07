package ru.mikst74.mikstcraft.model.font;

import lombok.Getter;

@Getter
public class Letter {
    private final char c;
    private final int pos; // encoded position letter on texture grid as 16bit mask YYYY.YYYY.XXXX.XXXX. max 256*256
    private final int x;
    private final int y;

    public Letter(char c, int x, int y) {
        this.c = c;
        this.pos = x & 0xFF | (y & 0xFF) << 8;
        this.x = x;
        this.y = y;
    }
}
