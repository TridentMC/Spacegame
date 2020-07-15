package com.tridevmc.spacegame.world.scene;

import com.tridevmc.spacegame.gl.shader.ShaderProgram;

public interface IObject {
    void update();
    void render(ShaderProgram s);
}
