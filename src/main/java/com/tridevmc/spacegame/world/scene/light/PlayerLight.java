package com.tridevmc.spacegame.world.scene.light;

import com.tridevmc.spacegame.SpaceGame;
import org.joml.Vector3f;

public class PlayerLight extends PointLight {
    public PlayerLight() {
        super(SpaceGame._camera.pos(), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 0.07f, 0.014f));
    }

    @Override
    public void update() {
        setPosition(SpaceGame._camera.pos());
    }
}
