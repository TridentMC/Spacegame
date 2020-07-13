package com.tridevmc.spacegame.world.scene;

import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.IObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class StaticObject implements IObject {
    private final Mesh _mesh;
    private final Transform _trans;

    public StaticObject(ResourceLocation name, float scale, Quaternionf rotation, Vector3f pos) {
        _mesh = Mesh.getMesh(name);
        _trans = new Transform(scale, rotation, pos);
    }

    @Override
    public void update() {

    }

    @Override
    public void render(ShaderProgram s) {
       _mesh.render(_trans.mat, s);
    }

    public Transform getTransform() {
        return _trans;
    }

}
