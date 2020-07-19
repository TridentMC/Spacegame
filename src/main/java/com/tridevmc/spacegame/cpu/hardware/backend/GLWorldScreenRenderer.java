package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import org.joml.Matrix4f;

public class GLWorldScreenRenderer extends GLScreenRenderer {
    private static final float[] VERTS = {
            -1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
            -1.0f, -1.0f, 1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 1.0f, 0.0f, 0.0f,
    };

    private static final Matrix4f _trans = new Matrix4f();

    public GLWorldScreenRenderer(I2DScreen screen) {
        super(screen, VERTS);
    }

    @Override
    public void setup(ShaderProgram s) {
        _vao.setupAttributes(s, AttributeType.VERTEX, AttributeType.TEXCOORD);
    }

    @Override
    public void pre(ShaderProgram s, ViewProjection proj, I2DScreen screen) {
        s.setupViewProjection(proj);
        s.setupModel(_trans);
    }
}
