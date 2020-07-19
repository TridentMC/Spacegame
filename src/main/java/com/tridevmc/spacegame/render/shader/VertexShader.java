package com.tridevmc.spacegame.render.shader;

import org.lwjgl.opengl.GL33;

import java.io.File;
import java.io.IOException;

public class VertexShader extends Shader {
    public VertexShader(File file) throws IOException {
        super(GL33.GL_VERTEX_SHADER, file);
    }
}
