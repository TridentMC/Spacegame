package com.tridevmc.spacegame.gl;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.ReadableObj;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class ObjHelper {
    public static FloatBuffer getVerticiesAndNormals(ReadableObj obj) {
        FloatBuffer e = BufferUtils.createFloatBuffer(obj.getNumVertices() * 3 + obj.getNumNormals() * 3);
        for(int i =0;i < obj.getNumVertices();i++) {
            FloatTuple t = obj.getVertex(i);
            e.put(t.getX());
            e.put(t.getY());
            e.put(t.getZ());
            FloatTuple n = obj.getNormal(i);
            e.put(n.getX());
            e.put(n.getY());
            e.put(n.getZ());
        }
        e.position(0);
        return e;
    }
}
