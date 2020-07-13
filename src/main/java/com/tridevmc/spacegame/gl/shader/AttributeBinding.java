package com.tridevmc.spacegame.gl.shader;

import org.lwjgl.opengl.GL33;

public class AttributeBinding {
    public int id;
    public int size;
    public int type;
    public int offset;

    public AttributeBinding(int id, int size, int type, int offset) {
        this.id = id;
        this.size = size;
        this.type = type;
        this.offset = offset;
    }
}
