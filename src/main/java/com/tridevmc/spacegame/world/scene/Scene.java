package com.tridevmc.spacegame.world.scene;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.light.DirectionalLight;
import com.tridevmc.spacegame.world.scene.light.ILight;
import com.tridevmc.spacegame.world.scene.light.LightType;
import com.tridevmc.spacegame.world.scene.object.IObject;
import com.tridevmc.spacegame.world.scene.object.IObjectWFP;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scene {

    private final List<IObject> _objects = new ArrayList<>();
    private final Map<LightType, List<ILight>> _lights = new HashMap<>();
    private final Map<LightType, ShaderProgram> _lightingShaders = new HashMap<>();
    private static final ShaderProgram _world = ShaderProgram.getShader(new ResourceLocation("spacegame", "world"));
    private ShaderProgram _currentPass;

    public Scene() {
        addLight(new DirectionalLight(new Vector3f(-0.2f, -1.0f, -0.3f), new Vector3f(0.01f, 0.01f, 0.01f)));
        //addLight(new PointLight(new Vector3f(-1.0f, -2.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.22f, 0.2f)));
        //addLight(new PointLight(new Vector3f(1.0f, -2.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 0.22f, 0.2f)));
        //addLight(new PointLight(new Vector3f(0.0f, -2.0f, 2.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(1.0f, 0.22f, 0.2f)));
        //addLight(new PlayerLight());

        _lightingShaders.put(LightType.POINT, ShaderProgram.getShader(new ResourceLocation("spacegame", "point_pass")));
        _lightingShaders.put(LightType.DIRECTIONAL, ShaderProgram.getShader(new ResourceLocation("spacegame", "directional_pass")));
    }

    public void addLight(ILight l) {
        LightType type = l.type();
        if(!_lights.containsKey(type)) {
            _lights.put(type, new ArrayList<>());
        }
        _lights.get(type).add(l);
    }

    public void update() {
        for(IObject o : _objects) {
            o.update();
        }

        for(LightType t : _lights.keySet()) {
            for(ILight l : _lights.get(t)) {
                l.update();
            }
        }

    }

    public void geometryStage(ViewProjection proj) {
        _world.use();

        _world.setupViewProjection(proj);

        for(IObject o : _objects) {
            o.render();
        }
    }

    public void lightingStage(ViewProjection proj) {
        for(LightType t : _lights.keySet()) {
            _currentPass = _lightingShaders.get(t);
            _currentPass.use();
            _currentPass.setupViewProjection(proj);

            for(ILight l : _lights.get(t)) {
                l.render(proj);
            }
        }
    }

    public void forwardStage(ViewProjection proj) {
        for(IObject o : _objects) {
            if(o instanceof IObjectWFP) {
                ((IObjectWFP)o).renderForward(proj);
            }
        }
    }

    public void addObject(IObject object) {
        _objects.add(object);
        object.onAdd(this);
    }


}
