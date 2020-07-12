package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import org.joml.Matrix4f;

public interface IScreenRenderer {
    void init(I2DScreen screen);
    void bind(I2DScreen screen);
    void render(Matrix4f proj, Matrix4f view, I2DScreen screen);

    default void render(I2DScreen screen) {
        render(new Matrix4f(), new Matrix4f(), screen);
    }

}
