package com.tridevmc.spacegame.world.scene.object;

import com.tridevmc.spacegame.client.ViewProjection;

/**
 * An object that implements an optional extended feature: rendering a forward pass component.
 */
public interface IObjectWFP extends IObject {
    void renderForward(ViewProjection proj);
}
