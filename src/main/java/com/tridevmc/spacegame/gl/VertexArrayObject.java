package com.tridevmc.spacegame.gl;

import com.tridevmc.spacegame.gl.shader.AttributeType;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VertexArrayObject {
    private final int _vao;
    private final int _vbo;
    private final int _ebo;
    private int _elementCount;

    public VertexArrayObject(ShaderProgram s) {
        _vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(_vao);
        _vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        _ebo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);

        s.use();

        s.setupAttribute(AttributeType.VERTEX);
        s.setupAttribute(AttributeType.NORMAL);
    }

    public void bind(FloatBuffer vertexBuffer, IntBuffer elementBuffer, int elementCount) {
        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL33.GL_STATIC_DRAW);
        _elementCount = elementCount;
    }

    public void render() {
        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);

        GL33.glDrawElements(GL33.GL_TRIANGLES, _elementCount, GL33.GL_UNSIGNED_INT, 0);
    }

}
