package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;

public class GLFullScreenRenderer extends GLScreenRenderer {
    private static final float[] VERTS = {
            -1.0f, 1.0f, 0.0f, 0.0f,
            -1.0f, -1.0f, 0.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f,
            1.0f, -1.0f, 1.0f, 1.0f,
            1.0f, 1.0f, 1.0f, 0.0f,
            -1.0f, 1.0f, 0.0f, 0.0f,
    };

    public GLFullScreenRenderer(I2DScreen screen) {
        super(screen, VERTS);
    }


    @Override
    public void setup(ShaderProgram s) {
        _vao.setupAttributes(s, AttributeType.VERTEX, AttributeType.TEXCOORD);
    }

    @Override
    public void pre(ShaderProgram s, ViewProjection proj, I2DScreen screen) {

    }
}
