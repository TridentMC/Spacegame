package com.tridevmc.spacegame.world.scene.light;

import com.tridevmc.spacegame.client.ViewProjection;
import org.joml.Vector3f;

public interface ILight {
    Vector3f position();
    Vector3f color();
    LightType type();
    void update();
    void render(ViewProjection proj);
}
