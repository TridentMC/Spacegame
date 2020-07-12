package com.tridevmc.spacegame.cpu.hardware;

public interface I2DScreen {
    int[] getScreenBuffer();
    int getWidth();
    int getHeight();
    void beginRender();
    void endRender();
}
