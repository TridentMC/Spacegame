package com.tridevmc.spacegame.world;

import com.tridevmc.spacegame.gl.shader.ShaderProgram;

public interface IObject {
    void update();
    void render(ShaderProgram s);
}
