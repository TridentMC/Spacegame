package com.tridevmc.spacegame.world.scene.object;

import com.tridevmc.spacegame.world.scene.Scene;

public interface IObject {
    void onAdd(Scene scene);
    void onRemove(Scene scene);
    void update();
    void render();
}
