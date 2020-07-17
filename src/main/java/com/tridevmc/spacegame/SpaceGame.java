package com.tridevmc.spacegame;

import com.tridevmc.spacegame.client.Window;
import com.tridevmc.spacegame.client.Camera;
import com.tridevmc.spacegame.client.ViewProjection;
import com.tridevmc.spacegame.cpu.DCPU;
import com.tridevmc.spacegame.cpu.hardware.EchoDevice;
import com.tridevmc.spacegame.cpu.hardware.LEM1802;
import com.tridevmc.spacegame.cpu.hardware.backend.GLWorldScreenRenderer;
import com.tridevmc.spacegame.cpu.hardware.backend.IScreenRenderer;
import com.tridevmc.spacegame.world.scene.object.Mesh;
import com.tridevmc.spacegame.gl.shader.*;
import com.tridevmc.spacegame.util.ResourceLocation;
import com.tridevmc.spacegame.world.scene.Scene;
import com.tridevmc.spacegame.world.scene.object.StaticObject;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryUtil;
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

    private static int gBuffer;
    private static int gPosition;
    private static int gNormal;
    private static int gAlbedo;
    private static int gDepth;

    private static int screenVao;
    private static int screenVbo;

    private static Scene scene;

    private static final float[] VERTS = {
            -1.0f, 1.0f, 0.0f, 1.0f,
            -1.0f, -1.0f, 0.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, -1.0f, 1.0f, 0.0f,
            1.0f, 1.0f, 1.0f, 1.0f,
            -1.0f, 1.0f, 0.0f, 1.0f,
    };

    public static void main(String[] args) {
        try {
            w = new Window();
            loadContent();
            run();
        } catch(Exception e) {
            Logger.error("Caught exception while running :(");
            Logger.error(e);
        }
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);
        GL33.glDeleteFramebuffers(gBuffer);
        w.destroy();
    }

    public static void loadContent() throws IOException {
        world = new ShaderProgram(new ResourceLocation("spacegame", "world"));
        world.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        world.registerAttribute(AttributeType.NORMAL, 3, GL33.GL_FLOAT, "normal");
        world.registerUniform(UniformType.MODEL, "model");
        world.registerUniform(UniformType.VIEW, "view");
        world.registerUniform(UniformType.PROJ, "proj");

        screen = new ShaderProgram(new ResourceLocation("spacegame", "world_screen"));
        screen.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        screen.registerAttribute(AttributeType.TEXCOORD, 2, GL33.GL_FLOAT, "texCoord");
        screen.registerUniform(UniformType.MODEL, "model");
        screen.registerUniform(UniformType.VIEW, "view");
        screen.registerUniform(UniformType.PROJ, "proj");

        ShaderProgram def = new ShaderProgram(new ResourceLocation("spacegame", "default"));
        def.registerAttribute(AttributeType.VERTEX, 2, GL33.GL_FLOAT, "position");
        def.registerAttribute(AttributeType.TEXCOORD, 2, GL33.GL_FLOAT, "texCoord");

        ShaderProgram point = new ShaderProgram(new ResourceLocation("spacegame", "point_pass"));
        point.registerAttribute(AttributeType.VERTEX, 3, GL33.GL_FLOAT, "position");
        point.registerUniform(UniformType.MODEL, "model");
        point.registerUniform(UniformType.VIEW, "view");
        point.registerUniform(UniformType.PROJ, "proj");
        point.registerUniform(UniformType.SAMPLER0, "gPosition");
        point.registerUniform(UniformType.SAMPLER1, "gNormal");
        point.registerUniform(UniformType.SAMPLER2, "gAlbedo");
        point.registerUniform(UniformType.LIGHT_POS, "lightPos");
        point.registerUniform(UniformType.LIGHT_COL, "lightCol");
        point.registerUniform(UniformType.LIGHT_ATTENUATION, "lightAtten");
        point.registerUniform(UniformType.SCREEN_SIZE, "gScreenSize");
        point.setUniform(UniformType.SAMPLER0, 0);
        point.setUniform(UniformType.SAMPLER1, 1);
        point.setUniform(UniformType.SAMPLER2, 2);
        point.setUniform(UniformType.SCREEN_SIZE, new Vector3f(w.fWidth(), w.fHeight(), 0.0f));

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

    private static int genFrameBufferTexture(int format, int type, int attachment) {
        int t = GL33.glGenTextures();
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, t);
        GL33.glTexImage2D(GL33.GL_TEXTURE_2D, 0, format, w.fWidth(), w.fHeight(),
                0, GL33.GL_RGBA, type, MemoryUtil.NULL);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MIN_FILTER, GL33.GL_NEAREST);
        GL33.glTexParameteri(GL33.GL_TEXTURE_2D, GL33.GL_TEXTURE_MAG_FILTER, GL33.GL_NEAREST);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, 0);
        GL33.glFramebufferTexture2D(GL33.GL_FRAMEBUFFER, attachment, GL33.GL_TEXTURE_2D, t, 0);
        return t;
    }

    public static void run() {
        gBuffer = GL33.glGenFramebuffers();
        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, gBuffer);

        gPosition = genFrameBufferTexture(GL33.GL_RGBA16F, GL33.GL_FLOAT, GL33.GL_COLOR_ATTACHMENT0);
        gNormal = genFrameBufferTexture(GL33.GL_RGBA16F, GL33.GL_FLOAT, GL33.GL_COLOR_ATTACHMENT1);
        gAlbedo = genFrameBufferTexture(GL33.GL_RGBA, GL33.GL_UNSIGNED_BYTE, GL33.GL_COLOR_ATTACHMENT2);
        GL33.glDrawBuffers(new int[]{GL33.GL_COLOR_ATTACHMENT0, GL33.GL_COLOR_ATTACHMENT1, GL33.GL_COLOR_ATTACHMENT2});

        gDepth = GL33.glGenRenderbuffers();
        GL33.glBindRenderbuffer(GL33.GL_RENDERBUFFER, gDepth);
        GL33.glRenderbufferStorage(GL33.GL_RENDERBUFFER, GL33.GL_DEPTH_COMPONENT24, w.fWidth(), w.fHeight());
        GL33.glFramebufferRenderbuffer(GL33.GL_FRAMEBUFFER, GL33.GL_DEPTH_ATTACHMENT, GL33.GL_RENDERBUFFER, gDepth);
        if(GL33.glCheckFramebufferStatus(GL33.GL_FRAMEBUFFER) != GL33.GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Framebuffer didn't initialize properly!");
        }

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);

        scene = new Scene();

        cpu = new DCPU();
        mon = new LEM1802();
        echo = new EchoDevice();

        cpu.connect(mon);
        cpu.connect(echo);

        monren = new GLWorldScreenRenderer();
        monren.init(screen, mon);

        int max = loadRom(cpu, new File("/bin/fonttest.bin"));

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glEnable(GL33.GL_TEXTURE_2D);
        GL33.glEnable(GL33.GL_BLEND);

        object = new StaticObject(new ResourceLocation("spacegame", "testmesh"), 2.0f, new Quaternionf(), new Vector3f(0.0f, 0.0f, 0.0f));
        deux = new StaticObject(new ResourceLocation("spacegame", "cube"), 1.0f, new Quaternionf(), new Vector3f(0.0f, 0.0f, -0.001f));
        trois = new StaticObject(new ResourceLocation("spacegame", "testmesh"), 2.0f, new Quaternionf(), new Vector3f(-20.0f, 0.0f, 0.0f));

        scene.addObject(object);
        scene.addObject(deux);
        GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while ( !GLFW.glfwWindowShouldClose(w.window) ) {
            GLFW.glfwPollEvents();

            c.updatePosition(1/60.0, Window.KEY_STATES);

            update(max);
            render();

            GLFW.glfwSwapBuffers(w.window); // swap the color buffers

        }
    }

    private static void update(int max) {
        if(cpu.pc != max) {
            cpu.eval(1000/60);
        }

        cpu.hardwareList.updateAll();

        scene.update();
    }

    private static void render() {
        ViewProjection proj = c.generateViewProjection(128.0f);

        // Geometry Pass

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 1);
        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        GL33.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_BLEND);

        scene.geometryPass(proj);

        // Lighting Pass

        GL33.glDisable(GL33.GL_DEPTH_TEST);

        GL33.glEnable(GL33.GL_BLEND);
        GL33.glBlendEquation(GL33.GL_FUNC_ADD);
        GL33.glBlendFunc(GL33.GL_ONE, GL33.GL_ONE);

        GL33.glBindFramebuffer(GL33.GL_FRAMEBUFFER, 0);

        GL33.glActiveTexture(GL33.GL_TEXTURE0);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, gPosition);
        GL33.glActiveTexture(GL33.GL_TEXTURE1);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, gNormal);
        GL33.glActiveTexture(GL33.GL_TEXTURE2);
        GL33.glBindTexture(GL33.GL_TEXTURE_2D, gAlbedo);

        GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT);

        scene.lightingPass(proj);

        // Forward Pass

        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glDisable(GL33.GL_BLEND);
        monren.render(screen, proj, mon);
    }
}
