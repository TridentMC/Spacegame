package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProj;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import org.lwjgl.opengl.GL33;

public abstract class GLScreenRenderer implements IScreenRenderer {

    protected int _vao;
    protected int _vbo;
    protected int _screenTex;
    protected final String _shaderName;
    protected float[] _verts;

    public GLScreenRenderer(String shaderName, float[] verts) {
        _shaderName = shaderName;
        _verts = verts;
    }
    @Override
    public void init(ShaderProgram s, I2DScreen screen) {
        _vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(_vao);
        _vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, _verts, GL33.GL_STATIC_DRAW);

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
    public void render(ShaderProgram program, ViewProj proj, I2DScreen screen) {
        bind(screen);

        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
    }
}
