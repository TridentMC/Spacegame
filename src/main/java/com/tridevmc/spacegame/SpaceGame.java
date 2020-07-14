package com.tridevmc.spacegame;

import com.tridevmc.spacegame.client.Window;
import com.tridevmc.spacegame.client.Camera;
import com.tridevmc.spacegame.client.ViewProj;
import com.tridevmc.spacegame.cpu.DCPU;
import com.tridevmc.spacegame.cpu.hardware.EchoDevice;
import com.tridevmc.spacegame.cpu.hardware.LEM1802;
import com.tridevmc.spacegame.cpu.hardware.backend.GLWorldScreenRenderer;
import com.tridevmc.spacegame.cpu.hardware.backend.IScreenRenderer;
import com.tridevmc.spacegame.world.scene.Mesh;
import com.tridevmc.spacegame.gl.shader.*;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.StaticObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public final class SpaceGame {
    private static Window w;

    private static StaticObject object;
    private static StaticObject deux;
    private static StaticObject trois;

    private static DCPU cpu;
    private static LEM1802 mon;
    private static EchoDevice echo;
    private static IScreenRenderer monren;

    public static final Camera c = new Camera();

    private static ShaderProgram world;
    private static ShaderProgram screen;
    private static ShaderProgram def;

    public static void main(String[] args) {
        try {
            w = new Window();
            loadContent();
            run();
        } catch(Exception e) {
            Logger.error("Caught exception while running :(");
            Logger.error(e);
        }
        w.destroy();
    }

    public static void loadContent() throws IOException {
        world = new ShaderProgram(new ResourceLocation("spacegame", "world"));
        world.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        world.registerAttribute(AttributeType.NORMAL, 3, GL33.GL_FLOAT, "normal");
        world.registerUniform(UniformType.MODEL, "model");
        world.registerUniform(UniformType.VIEW, "view");
        world.registerUniform(UniformType.PROJ, "proj");
        world.registerUniform(UniformType.LIGHT_POS, "lightPos");
        world.registerUniform(UniformType.LIGHT_COL, "lightCol");
        screen = new ShaderProgram(new ResourceLocation("spacegame", "world_screen"));
        screen.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        screen.registerAttribute(AttributeType.TEXCOORD, 2, GL33.GL_FLOAT, "texCoord");
        screen.registerUniform(UniformType.MODEL, "model");
        screen.registerUniform(UniformType.VIEW, "view");
        screen.registerUniform(UniformType.PROJ, "proj");
        def = new ShaderProgram(new ResourceLocation("spacegame", "default"));
        def.registerAttribute(AttributeType.VERTEX, 2, GL33.GL_FLOAT, "position");
        def.registerAttribute(AttributeType.TEXCOORD, 2, GL33.GL_FLOAT, "texCoord");

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

        cpu = new DCPU();
        mon = new LEM1802();
        echo = new EchoDevice();

        cpu.connect(mon);
        cpu.connect(echo);

        monren = new GLWorldScreenRenderer();
        monren.init(screen, mon);

        int max = loadRom(cpu, new File("/fonttest.bin"));

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glEnable(GL33.GL_TEXTURE_2D);

        object = new StaticObject(new ResourceLocation("spacegame", "testmesh"), 2.0f, new Quaternionf(), new Vector3f(0.0f, 0.0f, 0.0f));
        deux = new StaticObject(new ResourceLocation("spacegame", "cube"), 1.0f, new Quaternionf(), new Vector3f(0.0f, 0.0f, -0.001f));
        trois = new StaticObject(new ResourceLocation("spacegame", "testmesh"), 2.0f, new Quaternionf(), new Vector3f(-20.0f, 0.0f, 0.0f));
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while ( !GLFW.glfwWindowShouldClose(w.window) ) {
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GLFW.glfwPollEvents();

            c.updatePosition(1/60.0, Window.KEY_STATES);

            update(max);
            render();

            GLFW.glfwSwapBuffers(w.window); // swap the color buffers

        }
    }

    private static void update(int max) {
        if(cpu.pc != max) {
            cpu.eval(10000/60);
        }

        cpu.hardwareList.updateAll();

        trois.getTransform().rotate(0.0f, 1.0f, 0.0f, (float)(0.1f*Math.PI/180.0f));
    }

    private static void render() {
        ViewProj proj = c.generateViewProj(128.0f);

        world.use();

        MemoryStack stack = null;
        try {
            stack = MemoryStack.stackPush();
            world.setUniform(UniformType.VIEW, proj.view.get(stack.mallocFloat(16)));
            world.setUniform(UniformType.PROJ, proj.proj.get(stack.mallocFloat(16)));
            world.setUniform(UniformType.LIGHT_POS, new Vector3f(0.0f, 5.0f, 0.0f).get(stack.mallocFloat(3)));
            world.setUniform(UniformType.LIGHT_COL, mon.getAverage().get(stack.mallocFloat(3)));
        } finally {
            assert stack != null;
            stack.pop();
        }

        object.render(world);
        deux.render(world);
        trois.render(world);

        monren.render(screen, proj, mon);
    }
}
