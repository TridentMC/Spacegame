package com.tridevmc.spacegame.cpu.hardware.backend;

import com.tridevmc.spacegame.client.ViewProj;
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
    public void render(ShaderProgram s, ViewProj proj, I2DScreen screen) {
        super.render(s, proj, screen);

        s.use();

        MemoryStack stack = null;
        try {
            stack = MemoryStack.stackPush();
            s.setUniform(UniformType.MODEL, new Matrix4f().get(stack.mallocFloat(16)));
            s.setUniform(UniformType.VIEW, proj.view.get(stack.mallocFloat(16)));
            s.setUniform(UniformType.PROJ, proj.proj.get(stack.mallocFloat(16)));
        } finally {
            assert stack != null;
            stack.pop();
        }

        GL33.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

}
