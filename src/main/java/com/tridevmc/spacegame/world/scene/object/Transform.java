package com.tridevmc.spacegame.world.scene.object;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Transform {
    public Matrix4f mat;
    private float _scale;
    private Quaternionf _rotation;
    private Vector3f _pos;

    public Transform(float scale, Quaternionf rotation, Vector3f pos) {
        _scale = scale;
        _rotation = rotation;
        _pos = pos;
        recalculateModelMatrix();
    }

    private void recalculateModelMatrix() {
        mat = new Matrix4f().scale(_scale).translate(_pos).rotate(_rotation);
    }

    public void rotate(float x, float y, float z, float angle) {
        _rotation.mul(x*angle, y*angle, z*angle, 1.0f).normalize();
        recalculateModelMatrix();
    }
}
