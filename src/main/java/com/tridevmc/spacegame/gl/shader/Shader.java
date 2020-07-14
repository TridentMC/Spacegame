package com.tridevmc.spacegame.gl.shader;

import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

import java.io.*;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;

public class Shader {

    private final int _shader;
    public boolean good;

    public Shader(int type, File file) throws IOException {

        _shader = GL33.glCreateShader(type);

        InputStream stream = getClass().getClassLoader().getResourceAsStream(file.getPath());
        if(stream == null) {
            throw new IOException("Unable to read '" + file.getPath() + "' from resources!");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
        StringBuilder text = new StringBuilder();
        while(reader.ready()) {
            text.append(reader.readLine()).append('\n');
        }
        GL33.glShaderSource(_shader, text.toString());
        GL33.glCompileShader(_shader);

        MemoryStack stack = null;
        good = false;
        try {
            stack = MemoryStack.stackPush();
            IntBuffer buff = stack.mallocInt(1);
            GL33.glGetShaderiv(_shader, GL33.GL_COMPILE_STATUS, buff);

            if (buff.get(0) != GL33.GL_TRUE)
                throw new IllegalStateException(GL33.glGetShaderInfoLog(_shader, 512));
            good = true;
        } finally {
            assert stack != null;
            stack.pop();
        }
    }

    public int getShader() {
        return _shader;
    }
}
