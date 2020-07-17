package com.tridevmc.spacegame.world.scene.light;

import org.joml.Vector3f;


public abstract class StaticLight implements ILight {
    Vector3f _position;
    Vector3f _color;

    StaticLight(Vector3f pos, Vector3f color) {
        _position = pos;
        _color = color;
    }

    public void setPosition(Vector3f pos) {
        _position = pos;
    }

    public void setColor(Vector3f color) {
        _color = color;
    }


    @Override
    public Vector3f position() {
        return _position;
    }

    @Override
    public Vector3f color() {
        return _color;
    }
}
