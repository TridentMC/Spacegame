package com.tridevmc.spacegame.gl;

import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.IntBuffer;

public class Shader {
    private int _shader;
    public boolean good = false;
    public Shader(int type, File file) throws IOException {

        _shader = GL33.glCreateShader(type);

        InputStream stream = getClass().getClassLoader().getResourceAsStream(file.getPath());
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF8"));
        StringBuilder text = new StringBuilder();
        while(reader.ready()) {
            text.append(reader.readLine()).append('\n');
        }
        GL33.glShaderSource(_shader, text.toString());
        GL33.glCompileShader(_shader);

        MemoryStack stack = null;
        try {
            stack = MemoryStack.stackPush();
            IntBuffer buff = stack.mallocInt(1);
            GL33.glGetShaderiv(_shader, GL33.GL_COMPILE_STATUS, buff);

            if (buff.get(0) != GL33.GL_TRUE)
                throw new IllegalStateException(GL33.glGetShaderInfoLog(_shader, 512));
            good = true;
        } finally {
            stack.pop();
        }
    }

    public int getShader() {
        return _shader;
    }
}
