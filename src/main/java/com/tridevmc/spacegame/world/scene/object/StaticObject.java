package com.tridevmc.spacegame.world.scene.object;

import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.Scene;
import com.tridevmc.spacegame.world.scene.object.component.Mesh;
import com.tridevmc.spacegame.world.scene.object.component.Transform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class StaticObject implements IObject {
    private final Mesh _mesh;
    private final Transform _trans;
    private static final ShaderProgram _world = ShaderProgram.getShader(new ResourceLocation("spacegame", "world"));


    public StaticObject(ResourceLocation location, float scale, Quaternionf rotation, Vector3f pos) {
        _mesh = Mesh.getMesh(location);
        _trans = new Transform(scale, rotation, pos);
    }

    @Override
    public void update() {

    }

    @Override
    public void render() {
       _mesh.render(_trans.mat, _world);
    }

    @Override
    public void onAdd(Scene scene) {

    }

    @Override
    public void onRemove(Scene scene) {

    }

    public Transform getTransform() {
        return _trans;
    }

}
