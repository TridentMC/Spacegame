package com.tridevmc.spacegame.world.scene.light;

import com.tridevmc.spacegame.render.shader.AttributeType;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.object.component.Mesh;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

public class LightVolume extends Mesh {

    protected LightVolume(ResourceLocation location, File file) throws IOException {
        super(location, file);
    }

    public static void registerVolume(ResourceLocation location) throws IOException {
        LightVolume _volume = new LightVolume(location, new File("model", location.name()+".obj"));
        _meshCache.put(location, _volume);
    }

    public static LightVolume getVolume(ResourceLocation location) {
        if(!_meshCache.containsKey(location)) {
            try {
                registerVolume(location);
            } catch(IOException e) {
                Logger.error("Tried to getMesh but encountered IOException!");
                Logger.error(e);
            }
        }
        return (LightVolume)_meshCache.get(location);
    }

    @Override
    public void bind(Obj obj) {
        _vao.bind(ObjData.getVertices(obj), ObjData.getFaceVertexIndices(obj), ObjData.getTotalNumFaceVertices(obj));
    }

    @Override
    public void setupAttributes(ShaderProgram shader) {
        _vao.setupAttributes(shader, AttributeType.VERTEX);
    }
}
