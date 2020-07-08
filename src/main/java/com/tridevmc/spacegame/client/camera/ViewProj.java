package com.tridevmc.spacegame.client.camera;

import org.joml.Matrix4f;

public class ViewProj {
    public Matrix4f view;
    public Matrix4f proj;

    public ViewProj(Matrix4f view, Matrix4f proj) {
        this.view = view;
        this.proj = proj;
    }
}
