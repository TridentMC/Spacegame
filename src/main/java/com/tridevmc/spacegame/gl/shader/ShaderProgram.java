package com.tridevmc.spacegame.gl.shader;

import org.lwjgl.opengl.GL33;

public class ShaderProgram {
    private final int _program;

    public ShaderProgram(VertexShader vertexShader, FragmentShader fragmentShader) {
        _program = GL33.glCreateProgram();

        GL33.glAttachShader(_program, vertexShader.getShader());
        GL33.glAttachShader(_program, fragmentShader.getShader());

        GL33.glBindFragDataLocation(_program, 0, "outColor");
        GL33.glLinkProgram(_program);

        use();
    }


    public void use() {
        GL33.glUseProgram(_program);
    }

    public int getProgram() {
        return _program;
    }
}
