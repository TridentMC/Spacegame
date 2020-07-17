package com.tridevmc.spacegame.world.scene.light;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.gl.shader.UniformType;
import com.tridevmc.spacegame.util.ResourceLocation;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PointLight extends StaticLight {
    private static final LightVolume _volume = LightVolume.getVolume(new ResourceLocation("spacegame", "point_volume"));
    private static final ShaderProgram _lighting = ShaderProgram.getShader(new ResourceLocation("spacegame", "point_pass"));

    private Vector3f _attenuation;
    private float _radius;
    private Matrix4f _trans;

    public PointLight(Vector3f pos, Vector3f color, Vector3f attenuation) {
        super(pos, color);
        _attenuation = attenuation;
        recalculateRadius();
    }

    @Override
    public LightType type() {
        return LightType.POINT;
    }

    @Override
    public void update() {

    }

    @Override
    public void setPosition(Vector3f pos) {
        super.setPosition(pos);
        recalculateTransform();
    }

    @Override
    public void render(ViewProjection proj) {
        _lighting.use();

        _lighting.setUniform(UniformType.LIGHT_POS, _position);
        _lighting.setUniform(UniformType.LIGHT_COL, _color);
        _lighting.setUniform(UniformType.LIGHT_ATTENUATION, _attenuation);

        _volume.render(_trans, _lighting);
    }

    private void recalculateRadius() {
        _radius = (float)(-_attenuation.y + Math.sqrt(_attenuation.y * _attenuation.y - 4 * _attenuation.z * (_attenuation.x - (256.0 / 5.0) * 1.0)))/ (2 * _attenuation.z);
        recalculateTransform();
    }

    private void recalculateTransform() {
        _trans = new Matrix4f().translate(_position).scale(_radius);
    }

    public void setAttenuation(Vector3f atten) {
        _attenuation = atten;
        recalculateRadius();
    }
}
