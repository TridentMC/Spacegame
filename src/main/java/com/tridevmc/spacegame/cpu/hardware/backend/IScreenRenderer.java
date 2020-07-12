package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.cpu.hardware.I2DScreen;

public interface IScreenRenderer {
    void init(I2DScreen screen);
    void bind(I2DScreen screen);
    void render(I2DScreen screen);
}
