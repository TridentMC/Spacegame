package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.gl.shader.FragmentShader;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.gl.shader.VertexShader;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;

public class GLWorldScreenRenderer implements IScreenRenderer {

    private int _vao;
    private int _vbo;
    private int _screenTex;
    private ShaderProgram _shader;
    private int _uniTrans;
    private int _uniView;
    private int _uniProj;


    @Override
    public void init(I2DScreen screen) {
        float[] verts = {
                -10.0f, 10.0f, 10.0f, 0.0f, 0.0f,
                -10.0f, -10.0f, 10.0f, 0.0f, 1.0f,
                10.0f, -10.0f, 10.0f, 1.0f, 1.0f,
                10.0f, -10.0f, 10.0f, 1.0f, 1.0f,
                10.0f, 10.0f, 10.0f, 1.0f, 0.0f,
                -10.0f, 10.0f, 10.0f, 0.0f, 0.0f,
        };

        _vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(_vao);
        _vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verts, GL33.GL_STATIC_DRAW);

        VertexShader v;
        FragmentShader f;
        try {
            v = new VertexShader(new File("shaders", "world_screen.vert"));
            f = new FragmentShader(new File("shaders", "world_screen.frag"));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        _shader = new ShaderProgram(v, f);

        _shader.use();

        _uniTrans = GL33.glGetUniformLocation(_shader.getProgram(), "model");
        _uniView = GL33.glGetUniformLocation(_shader.getProgram(), "view");
        _uniProj = GL33.glGetUniformLocation(_shader.getProgram(), "proj");

        int posAttrib = GL33.glGetAttribLocation(_shader.getProgram(), "position");
        GL33.glEnableVertexAttribArray(posAttrib);
        GL33.glVertexAttribPointer(posAttrib, 3, GL33.GL_FLOAT, false, 5*4, 0L);

        int texAttrib = GL33.glGetAttribLocation(_shader.getProgram(), "texCoord");
        GL33.glEnableVertexAttribArray(texAttrib);
        GL33.glVertexAttribPointer(texAttrib, 2, GL33.GL_FLOAT, false, 5*4, 3*4);

        _screenTex = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, screen.getWidth(), screen.getHeight(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, screen.getScreenBuffer());
    }

    @Override
    public void bind(I2DScreen screen) {
        screen.beginRender();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
        GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, 0, 0, screen.getWidth(), screen.getHeight(), GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, screen.getScreenBuffer());
        screen.endRender();
    }

    @Override
    public void render(Matrix4f proj, Matrix4f view, I2DScreen screen) {
        bind(screen);

        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);

        _shader.use();

        MemoryStack stack = null;
        try {
            stack = MemoryStack.stackPush();
            GL33.glUniformMatrix4fv(_uniTrans, false, new Matrix4f().get(stack.mallocFloat(16)));
            GL33.glUniformMatrix4fv(_uniView, false, view.get(stack.mallocFloat(16)));
            GL33.glUniformMatrix4fv(_uniProj, false, proj.get(stack.mallocFloat(16)));
        } finally {
            assert stack != null;
            stack.pop();
        }

        GL33.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

}
