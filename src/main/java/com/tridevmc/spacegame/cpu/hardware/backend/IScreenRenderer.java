package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.render.shader.Shader;
import com.tridevmc.spacegame.render.shader.ShaderProgram;

public interface IScreenRenderer {
    void setup(ShaderProgram s);
    void bind(I2DScreen screen);
    void pre(ShaderProgram s, ViewProjection proj, I2DScreen screen);
    void render(ShaderProgram s, ViewProjection proj, I2DScreen screen);

    default void render(ShaderProgram s, I2DScreen screen) {
        render(s, null, screen);
    }

}
