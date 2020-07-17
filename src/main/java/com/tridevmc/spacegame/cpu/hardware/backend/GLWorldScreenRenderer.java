package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.gl.shader.AttributeType;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.gl.shader.UniformType;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

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

    public GLWorldScreenRenderer() {
        super("world_screen", VERTS);
    }


    @Override
    public void init(ShaderProgram s, I2DScreen screen) {
        super.init(s, screen);
        s.use();

        s.setupAttribute(AttributeType.VERTEX);
        s.setupAttribute(AttributeType.TEXCOORD);
    }

    @Override
    public void render(ShaderProgram s, ViewProjection proj, I2DScreen screen) {
        super.render(s, proj, screen);

        s.use();

        s.setupViewProjection(proj);
        s.setupModel(_trans);

        GL33.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

}
