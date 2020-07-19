package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.render.VertexBuffer;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import org.lwjgl.opengl.GL33;

public abstract class GLScreenRenderer implements IScreenRenderer {

    protected int _screenTex;
    protected VertexBuffer _vao;

    public GLScreenRenderer(I2DScreen screen, float[] verts) {
        _vao = new VertexBuffer(false);
        _vao.bind(verts, 6);

        // TODO: Perhaps object-ify this?
        _screenTex = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_S, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_WRAP_T, GL33.GL_REPEAT);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, GL33.GL_RGBA, screen.getWidth(), screen.getHeight(), 0, GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, screen.getScreenBuffer());
    }

    @Override
    public abstract void setup(ShaderProgram s);

    @Override
    public void bind(I2DScreen screen) {
        screen.beginRender();
        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
        GL33.glTexSubImage2D(GL33.GL_TEXTURE_2D, 0, 0, 0, screen.getWidth(), screen.getHeight(), GL33.GL_RGBA, GL33.GL_UNSIGNED_INT_8_8_8_8_REV, screen.getScreenBuffer());
        screen.endRender();
    }

    @Override
    public abstract void pre(ShaderProgram s, ViewProjection proj, I2DScreen screen);

    @Override
    public void render(ShaderProgram s, ViewProjection proj, I2DScreen screen) {
        bind(screen);

        s.use();

        if(!_vao.isConfigured()) {
            setup(s);
        }
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, _screenTex);
        pre(s, proj, screen);
        _vao.render(s);
    }
}
