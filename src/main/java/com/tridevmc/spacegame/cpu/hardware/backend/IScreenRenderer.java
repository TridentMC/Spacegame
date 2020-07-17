package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;

public interface IScreenRenderer {
    void init(ShaderProgram s, I2DScreen screen);
    void bind(I2DScreen screen);
    void render(ShaderProgram s, ViewProjection proj, I2DScreen screen);

    default void render(ShaderProgram s, I2DScreen screen) {
        render(s, null, screen);
    }

}
