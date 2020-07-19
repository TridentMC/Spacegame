package com.tridevmc.spacegame.render;

import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import org.lwjgl.opengl.GL33;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class VertexBuffer {
    private final int _vao;
    private final int _vbo;
    private int _ebo;
    private int _elementCount;
    private int _vertexCount;
    private boolean _vertexesConfigured = false;
    private final boolean _useEbo;

    public VertexBuffer() {
        this(true);
    }

    public VertexBuffer(boolean useEbo) {
        _vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(_vao);
        _vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        _useEbo = useEbo;
        if(_useEbo) {
            _ebo = GL33.glGenBuffers();
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);
        }
    }

    public void setupAttributes(ShaderProgram s, AttributeType ...attributes) {
        if(_vertexesConfigured)
            return;

        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        if(_useEbo)
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);

        s.use();

        for(AttributeType t : attributes) {
            s.setupAttribute(t);
        }
        _vertexesConfigured = true;
    }

    public boolean isConfigured() {
        return _vertexesConfigured;
    }

    public void bind(FloatBuffer vertexBuffer, IntBuffer elementBuffer, int elementCount) {
        if(!_useEbo) {
            throw new RuntimeException("Attempted to EBO bind on a non-EBO VAO!");
        }
        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL33.GL_STATIC_DRAW);
        _elementCount = elementCount;
    }

    public void bind(float[] vertexBuffer, int vertexCount) {
        if(_useEbo) {
            throw new RuntimeException("Attempted to plain bind on an EBO VAO!");
        }
        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, vertexBuffer, GL33.GL_STATIC_DRAW);
        _vertexCount = vertexCount;
    }

    public void render(ShaderProgram shader) {
        GL33.glBindVertexArray(_vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, _vbo);
        if(_useEbo) {
            GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, _ebo);
            GL33.glDrawElements(GL33.GL_TRIANGLES, _elementCount, GL33.GL_UNSIGNED_INT, 0);
        } else {
            GL33.glDrawArrays(GL33.GL_TRIANGLES, 0, _vertexCount);
        }
    }

}
