package com.tridevmc.spacegame;

import com.tridevmc.spacegame.client.Camera;
import com.tridevmc.spacegame.client.GLFWWindow;
import com.tridevmc.spacegame.client.IWindow;
import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.DCPU;
import com.tridevmc.spacegame.cpu.hardware.EchoDevice;
import com.tridevmc.spacegame.cpu.hardware.LEM1802;
import com.tridevmc.spacegame.render.GLRenderer;
import com.tridevmc.spacegame.render.IRenderer;
import com.tridevmc.spacegame.render.RenderStage;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.Scene;
import com.tridevmc.spacegame.world.scene.object.Monitor;
import com.tridevmc.spacegame.world.scene.object.StaticObject;
import com.tridevmc.spacegame.world.scene.object.component.Mesh;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class SpaceGame {
    private static IWindow _window;

    private static DCPU cpu;
    private static LEM1802 mon;
    private static EchoDevice echo;

    private static StaticObject object;
    private static Monitor monitor;

    public static final Camera _camera = new Camera();

    private static Scene _scene;

    private static IRenderer _renderer = new GLRenderer();

    public static void main(String[] args) {
        try {
            _window = new GLFWWindow();
            _renderer.init(_window.fWidth(), _window.fHeight());
            loadContent();
            run();
        } catch(Exception e) {
            Logger.error("Caught exception while running :(");
            Logger.error(e);
        }
        _renderer.destroy();
        _window.destroy();
    }

    public static void loadContent() throws IOException {
        _renderer.loadContent();

        Mesh.registerMesh(new ResourceLocation("spacegame", "testmesh"));
        Mesh.registerMesh(new ResourceLocation("spacegame", "cube"));
    }

    private static int loadRom(DCPU cpu, int ...args) {
        int index = 0x0000;
        for(int c : args) {
            cpu.ram[index++] = (char)c;
        }
        return index;
    }

    private static int loadRom(DCPU cpu, File file) {
        InputStream s = SpaceGame.class.getResourceAsStream(file.getPath());

        int a = 0x0000;
        while(true) {
            try {
                if (!(s.available() > 0)) break;
                cpu.ram[a++] = (char)((s.read() << 8) + s.read());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return a;
    }



    public static void run() {
        _window.lockCursor();

        _window.getInputManager().registerCursorPosCallback(_camera::updateCameraRotation);

        _scene = new Scene();

        cpu = new DCPU();
        mon = new LEM1802();
        echo = new EchoDevice();

        cpu.connect(mon);
        cpu.connect(echo);

        monitor = new Monitor(mon);

        int max = loadRom(cpu, new File("/bin/fonttest.bin"));

        object = new StaticObject(new ResourceLocation("spacegame", "testmesh"), 4.0f, new Quaternionf(), new Vector3f(0.0f, 0.0f, 0.0f));

        _scene.addObject(object);
        _scene.addObject(monitor);

        while ( !_window.shouldClose() ) {
            _window.eventPoll();

            _camera.updatePosition(1/60.0, _window.getInputManager());

            update(max);
            render();

            _window.swapBuffers();
        }
    }

    private static void update(int max) {
        if(cpu.pc != max) {
            cpu.eval(1000/60);
        }

        cpu.hardwareList.updateAll();

        _scene.update();
    }

    private static void render() {
        ViewProjection proj = _camera.generateViewProjection(128.0f);

        _renderer.pre(RenderStage.GEOMETRY);
        _scene.geometryStage(proj);
        _renderer.post(RenderStage.GEOMETRY);

        _renderer.pre(RenderStage.LIGHTING);
        _scene.lightingStage(proj);
        _renderer.post(RenderStage.LIGHTING);

        _renderer.pre(RenderStage.FORWARD);
        _scene.forwardStage(proj);
        _renderer.post(RenderStage.FORWARD);
    }
}
