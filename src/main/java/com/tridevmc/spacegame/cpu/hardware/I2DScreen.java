package com.tridevmc.spacegame.cpu.hardware;

import org.joml.Vector3f;

public interface I2DScreen {
    int[] getScreenBuffer();
    int getWidth();
    int getHeight();
    void beginRender();
    void endRender();
    Vector3f getAverage();
}
