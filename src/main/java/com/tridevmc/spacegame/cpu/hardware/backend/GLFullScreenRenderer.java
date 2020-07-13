package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.camera.ViewProj;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.gl.shader.AttributeType;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import org.joml.Matrix4f;
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

    public GLFullScreenRenderer() {
        super("default", VERTS);
    }


    @Override
    public void init(ShaderProgram s, I2DScreen screen) {
        super.init(s, screen);
        s.use();

        s.setupAttribute(AttributeType.VERTEX);
        s.setupAttribute(AttributeType.TEXCOORD);
    }

    @Override
    public void render(ShaderProgram s, ViewProj proj, I2DScreen screen) {
        super.render(s, proj, screen);

        s.use();

        GL33.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

}
