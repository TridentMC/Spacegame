package com.tridevmc.spacegame.world.scene;

import com.tridevmc.spacegame.SpaceGame;
import com.tridevmc.spacegame.gl.ObjHelper;
import com.tridevmc.spacegame.gl.VertexArrayObject;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.gl.shader.UniformType;
import com.tridevmc.spacegame.util.ResourceLocation;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Mesh {
    private static final Map<ResourceLocation, Mesh> _meshCache = new HashMap<>();
    private final VertexArrayObject _vao;
    private final ResourceLocation _name;

    public static void registerMesh(ResourceLocation name) throws IOException {
        Mesh _mesh = new Mesh(name, "/"+name.getName()+".obj");
        _meshCache.put(name, _mesh);
    }

    public static Mesh getMesh(ResourceLocation name) {
        if(!_meshCache.containsKey(name)) {
            try {
                registerMesh(name);
            } catch(IOException e) {
                Logger.error("Tried to getMesh but encountered IOException!");
                Logger.error(e);
            }
        }
        return _meshCache.get(name);
    }

    private Mesh(ResourceLocation name, String location) throws IOException {
        _name = name;
        _vao = new VertexArrayObject();

        Obj _obj = ObjReader.read(SpaceGame.class.getResourceAsStream(location));
        _obj = ObjUtils.convertToRenderable(_obj);

        _vao.bind(ObjHelper.getVerticiesAndNormals(_obj), ObjData.getFaceVertexIndices(_obj), ObjData.getTotalNumFaceVertices(_obj));
    }

    public void render(Matrix4f _trans, ShaderProgram shader) {
        shader.use();
        MemoryStack stack = null;
        try {
            stack = MemoryStack.stackPush();
            shader.setUniform(UniformType.MODEL, _trans.get(stack.mallocFloat(16)));
        } finally {
            assert stack != null;
            stack.pop();
        }
        _vao.render(shader);
    }
}
