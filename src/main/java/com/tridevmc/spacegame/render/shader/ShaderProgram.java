package com.tridevmc.spacegame.render.shader;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.util.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class ShaderProgram {
    private final int _program;
    private final Map<UniformType, Integer> _uniforms = new HashMap<>();
    private int _totalAttributeSize = 0;
    private final Map<AttributeType, AttributeBinding> _attributes = new HashMap<>();
    private static final Map<ResourceLocation, ShaderProgram> _shaderMap = new HashMap<>();
    private static final float[] threefvBuffer = new float[3];
    private static MemoryStack _stack = null;

    public ShaderProgram(ResourceLocation location) throws IOException {
        this(location,
                new VertexShader(new File("shader", location.name()+".vert")),
                new FragmentShader(new File("shader", location.name()+".frag")));
    }

    public ShaderProgram(ResourceLocation location, VertexShader vertexShader, FragmentShader fragmentShader) {
        _program = GL33.glCreateProgram();

        GL33.glAttachShader(_program, vertexShader.getShader());
        GL33.glAttachShader(_program, fragmentShader.getShader());

        GL33.glBindFragDataLocation(_program, 0, "outColor");
        GL33.glLinkProgram(_program);

        _shaderMap.put(location, this);

        use();
    }

    public void registerUniform(UniformType type, String name) {
        if(_uniforms.containsKey(type))
            return; // silently ignore
        int uniform = GL33.glGetUniformLocation(this._program, name);
        _uniforms.put(type, uniform);
    }

    public void registerAttribute(AttributeType attr, int size, int type, String location) {
        if(_attributes.containsKey(attr))
            return; // silently ignore
        int attrib = GL33.glGetAttribLocation(this._program, location);
        _attributes.put(attr, new AttributeBinding(attrib, size, type, _totalAttributeSize));

        if(type == GL33.GL_FLOAT) {
            _totalAttributeSize += size * 4;
        }
    }

    public void setUniform(UniformType type, FloatBuffer buffer) {
        if(!_uniforms.containsKey(type)) {
            throw new RuntimeException("Shader doesn't have the '" + type.toString() + "' uniform location!");
        }
        if(buffer.limit() == 16) {
            GL33.glUniformMatrix4fv(_uniforms.get(type), false, buffer);
        } else if(buffer.limit() == 3) {
            GL33.glUniform3fv(_uniforms.get(type), buffer);
        }
    }

    public void setUniform(UniformType type, Vector3f v) {
        if(!_uniforms.containsKey(type)) {
            throw new RuntimeException("Shader doesn't have the '" + type.toString() + "' uniform location!");
        }
        threefvBuffer[0] = v.x;
        threefvBuffer[1] = v.y;
        threefvBuffer[2] = v.z;
        GL33.glUniform3fv(_uniforms.get(type), threefvBuffer);
    }

    public void setUniform(UniformType type, float val) {
        if(!_uniforms.containsKey(type)) {
            throw new RuntimeException("Shader doesn't have the '" + type.toString() + "' uniform location!");
        }
        GL33.glUniform1f(_uniforms.get(type), val);
    }

    public void setUniform(UniformType type, int val) {
        if(!_uniforms.containsKey(type)) {
            throw new RuntimeException("Shader doesn't have the '" + type.toString() + "' uniform location!");
        }
        GL33.glUniform1i(_uniforms.get(type), val);
    }

    public void setupAttribute(AttributeType type) {
        if(!_attributes.containsKey(type)) {
            throw new RuntimeException("Shader doesn't have the '" + type.toString() + "' attribute location!");
        }
        AttributeBinding binding = _attributes.get(type);
        GL33.glEnableVertexAttribArray(binding.id);
        int size = _attributes.size() == 1 ? 0 : _totalAttributeSize;
        GL33.glVertexAttribPointer(binding.id, binding.size, GL33.GL_FLOAT, false, size , binding.offset);
    }

    public void setupViewProjection(ViewProjection proj) {
        try {
            _stack = MemoryStack.stackPush();
            this.setUniform(UniformType.VIEW, proj.view.get(_stack.mallocFloat(16)));
            this.setUniform(UniformType.PROJ, proj.proj.get(_stack.mallocFloat(16)));
        } finally {
            assert _stack != null;
            _stack.pop();
        }
        _stack = null;
    }

    public void setupModel(Matrix4f model) {
        try {
            _stack = MemoryStack.stackPush();
            this.setUniform(UniformType.MODEL, model.get(_stack.mallocFloat(16)));
        } finally {
            assert _stack != null;
            _stack.pop();
        }
        _stack = null;
    }


    public void use() {
        GL33.glUseProgram(_program);
    }

    public int getProgram() {
        return _program;
    }

    public static ShaderProgram getShader(ResourceLocation location) {
        return _shaderMap.get(location);
    }
}
