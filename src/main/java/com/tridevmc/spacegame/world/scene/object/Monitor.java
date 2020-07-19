package com.tridevmc.spacegame.world.scene.object;

import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.hardware.I2DScreen;
import com.tridevmc.spacegame.cpu.hardware.backend.GLWorldScreenRenderer;
import com.tridevmc.spacegame.cpu.hardware.backend.IScreenRenderer;
import com.tridevmc.spacegame.render.shader.ShaderProgram;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.Scene;
import com.tridevmc.spacegame.world.scene.light.PointLight;
import com.tridevmc.spacegame.world.scene.object.component.Mesh;
import com.tridevmc.spacegame.world.scene.object.component.Transform;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Monitor implements IObjectWFP {
    private static final Mesh _mesh = Mesh.getMesh(new ResourceLocation("spacegame", "cube"));
    private final Transform _trans;
    private static final ShaderProgram _world = ShaderProgram.getShader(new ResourceLocation("spacegame", "world"));
    private static final ShaderProgram _screen = ShaderProgram.getShader(new ResourceLocation("spacegame", "world_screen"));
    private final IScreenRenderer _screenRender;
    private final I2DScreen _scr;
    private final PointLight _light;


    public Monitor(I2DScreen screen) {
        _trans = new Transform(1.0f, new Quaternionf(), new Vector3f(0.0f, 0.0f, -0.001f));
        _scr = screen;
        _screenRender = new GLWorldScreenRenderer(screen);
        _light = new PointLight(new Vector3f(0.0f, 0.0f, 1f), new Vector3f(1.0f, 1.0f, 1.0f), new Vector3f(1.0f, 0.07f, 0.014f));

    }

    @Override
    public void renderForward(ViewProjection proj) {
        _screenRender.render(_screen, proj, _scr);
    }

    @Override
    public void update() {
        _light.setColor(_scr.getAverage());
    }

    @Override
    public void onAdd(Scene scene) {
        scene.addLight(_light);
    }

    @Override
    public void onRemove(Scene scene) {
    }


    @Override
    public void render() {
        _mesh.render(_trans.mat, _world);
    }
}
