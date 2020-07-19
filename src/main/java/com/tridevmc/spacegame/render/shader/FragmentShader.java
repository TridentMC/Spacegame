package com.tridevmc.spacegame.render.shader;

import org.lwjgl.opengl.GL33;

import java.io.File;
import java.io.IOException;

public class FragmentShader extends Shader {
    public FragmentShader(File file) throws IOException {
        super(GL33.GL_FRAGMENT_SHADER, file);
    }
}
