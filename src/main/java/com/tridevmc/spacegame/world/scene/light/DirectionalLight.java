package com.tridevmc.spacegame.world.scene.light;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.render.VertexBuffer;
import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.render.shader.UniformType;
import com.tridevmc.spacegame.util.ResourceLocation;
import org.joml.Vector3f;

public class DirectionalLight extends StaticLight {
    private static final float[] VERTS = {
            -1.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, -1.0f,
            -1.0f, 1.0f,
    };
    private static final ShaderProgram _lighting = ShaderProgram.getShader(new ResourceLocation("spacegame", "directional_pass"));
    private static VertexBuffer _vao;
    private Vector3f _direction;

    public DirectionalLight(Vector3f direction, Vector3f color) {
        super(new Vector3f(0,0,0), color);
        if(_vao == null) {
            _vao = new VertexBuffer(false);
            _vao.bind(VERTS, 6);
            _vao.setupAttributes(_lighting, AttributeType.VERTEX);
        }
        _direction = direction;
    }

    @Override
    public LightType type() {
        return LightType.DIRECTIONAL;
    }

    @Override
    public void update() {

    }

    @Override
    public void setPosition(Vector3f pos) {
        super.setPosition(pos);
    }

    @Override
    public void render(ViewProjection proj) {
        _lighting.use();

        _lighting.setUniform(UniformType.LIGHT_COL, _color);
        _lighting.setUniform(UniformType.LIGHT_DIRECTION, _direction);

        _vao.render(_lighting);
    }
}
