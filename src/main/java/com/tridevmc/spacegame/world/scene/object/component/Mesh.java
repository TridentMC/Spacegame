package com.tridevmc.spacegame.world.scene.object.component;

import com.tridevmc.spacegame.render.ObjHelper;
import com.tridevmc.spacegame.render.VertexBuffer;
import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.joml.Matrix4f;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

// TODO: Refactor this class to not be used as a component.
//       Most of this logic should probably be abstracted through a Renderer component on objects instead,
//       which holds a version of this class, preferably API agnostic.
public class Mesh {
    protected static final Map<ResourceLocation, Mesh> _meshCache = new HashMap<>();
    protected final VertexBuffer _vao;
    protected final ResourceLocation _location;

    public static void registerMesh(ResourceLocation location) throws IOException {
        Mesh _mesh = new Mesh(location, new File("model", location.name()+".obj"));
        _meshCache.put(location, _mesh);
    }

    public static Mesh getMesh(ResourceLocation location) {
        if(!_meshCache.containsKey(location)) {
            try {
                registerMesh(location);
            } catch(IOException e) {
                Logger.error("Tried to getMesh but encountered IOException!");
                Logger.error(e);
            }
        }
        return _meshCache.get(location);
    }

    protected Mesh(ResourceLocation location, File file) throws IOException {
        _location = location;
        _vao = new VertexBuffer();

        InputStream stream = getClass().getResourceAsStream("/"+file.getPath());
        Obj _obj = ObjReader.read(stream);
        _obj = ObjUtils.convertToRenderable(_obj);

        bind(_obj);
    }

    public void bind(Obj obj) {
        _vao.bind(ObjHelper.getVerticiesAndNormals(obj), ObjData.getFaceVertexIndices(obj), ObjData.getTotalNumFaceVertices(obj));
    }

    public void setupAttributes(ShaderProgram shader) {
        _vao.setupAttributes(shader, AttributeType.VERTEX, AttributeType.NORMAL);
    }

    public void render(Matrix4f _trans, ShaderProgram shader) {
        shader.use();
        if(!_vao.isConfigured())
            setupAttributes(shader);
        shader.setupModel(_trans);
        _vao.render(shader);
    }
}
