package com.tridevmc.spacegame.client;

import org.joml.Matrix4f;

public class ViewProjection {
    public final Matrix4f view;
    public final Matrix4f proj;

    public ViewProjection(Matrix4f view, Matrix4f proj) {
        this.view = view;
        this.proj = proj;
    }
}
