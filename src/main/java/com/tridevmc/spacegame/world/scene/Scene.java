package com.tridevmc.spacegame.world.scene;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.light.ILight;
import com.tridevmc.spacegame.world.scene.light.LightType;
import com.tridevmc.spacegame.world.scene.light.PlayerLight;
import com.tridevmc.spacegame.world.scene.light.PointLight;
import com.tridevmc.spacegame.world.scene.object.IObject;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL33;

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
        addLight(new PointLight(new Vector3f(-1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(1.0f, 0.14f, 0.07f)));
        addLight(new PointLight(new Vector3f(1.0f, 0.0f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f), new Vector3f(1.0f, 0.14f, 0.07f)));
        addLight(new PointLight(new Vector3f(0.0f, 0.0f, 2.0f), new Vector3f(0.0f, 0.0f, 1.0f), new Vector3f(1.0f, 0.14f, 0.07f)));
        addLight(new PlayerLight());

        _lightingShaders.put(LightType.POINT, ShaderProgram.getShader(new ResourceLocation("spacegame", "point_pass")));
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

    public void geometryPass(ViewProjection proj) {
        _world.use();

        _world.setupViewProjection(proj);

        for(IObject o : _objects) {
            o.render();
        }
    }

    public void lightingPass(ViewProjection proj) {
        GL33.glCullFace(GL33.GL_FRONT);



        for(LightType t : _lights.keySet()) {
            _currentPass = _lightingShaders.get(t);
            _currentPass.use();
            _currentPass.setupViewProjection(proj);

            for(ILight l : _lights.get(t)) {
                l.render(proj);
            }
        }
        GL33.glCullFace(GL33.GL_BACK);
    }

    public void addObject(IObject object) {
        _objects.add(object);
    }
}
