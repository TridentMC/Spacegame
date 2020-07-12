package com.tridevmc.spacegame;

import com.tridevmc.spacegame.client.Window;
import com.tridevmc.spacegame.client.camera.Camera;
import com.tridevmc.spacegame.client.camera.ViewProj;
import com.tridevmc.spacegame.cpu.DCPU;
import com.tridevmc.spacegame.cpu.hardware.EchoDevice;
import com.tridevmc.spacegame.cpu.hardware.LEM1802;
import com.tridevmc.spacegame.cpu.hardware.backend.GLWorldScreenRenderer;
import com.tridevmc.spacegame.cpu.hardware.backend.IScreenRenderer;
import com.tridevmc.spacegame.gl.ObjHelper;
import com.tridevmc.spacegame.gl.shader.FragmentShader;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.gl.shader.VertexShader;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjData;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.lwjgl.system.MemoryStack;
import org.tinylog.Logger;
import sun.jvm.hotspot.memory.Space;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SpaceGame {
    private static Window w;

    private static int vao;
    private static int vbo;
    private static int ebo;
    private static Obj obj;
    private static ShaderProgram s;

    private static int _uniProj;
    private static int _uniView;
    private static int _uniTrans;
    private static int _uniLight;


    private static DCPU cpu;
    private static LEM1802 mon;
    private static EchoDevice echo;
    private static IScreenRenderer monren;

    public static final Camera c = new Camera();

    public static void main(String[] args) {
        try {
            w = new Window();
            run();
        } catch(Exception e) {
            Logger.error("Caught exception while running :(");
            Logger.error(e);
        }
        w.destroy();
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

    public static void run() throws IOException {
        GL.createCapabilities();

        cpu = new DCPU();
        mon = new LEM1802();
        echo = new EchoDevice();

        cpu.connect(mon);
        cpu.connect(echo);

        monren = new GLWorldScreenRenderer();
        monren.init(mon);

        int max = loadRom(cpu, new File("/diag.bin"));
        GL33.glEnable(GL33.GL_DEPTH_TEST);
        GL33.glEnable(GL33.GL_CULL_FACE);
        GL33.glEnable(GL33.GL_TEXTURE_2D);

        obj = ObjReader.read(SpaceGame.class.getResourceAsStream("/testmesh.obj"));
        obj = ObjUtils.convertToRenderable(obj);

        vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao);
        vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, ObjHelper.getVerticiesAndNormals(obj), GL33.GL_STATIC_DRAW);
        ebo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo);
        GL33.glBufferData(GL33.GL_ELEMENT_ARRAY_BUFFER, ObjData.getFaceVertexIndices(obj), GL33.GL_STATIC_DRAW);

        VertexShader v = new VertexShader(new File("shaders", "world.vert"));
        FragmentShader f = new FragmentShader(new File("shaders", "world.frag"));
        s = new ShaderProgram(v, f);

        s.use();

        _uniTrans = GL33.glGetUniformLocation(s.getProgram(), "model");
        _uniView = GL33.glGetUniformLocation(s.getProgram(), "view");
        _uniProj = GL33.glGetUniformLocation(s.getProgram(), "proj");
        _uniLight = GL33.glGetUniformLocation(s.getProgram(), "lightPos");

        int posAttrib = GL33.glGetAttribLocation(s.getProgram(), "position");
        GL33.glEnableVertexAttribArray(posAttrib);
        GL33.glVertexAttribPointer(posAttrib, 3, GL33.GL_FLOAT, false, 6*4, 0L);

        int normAttrib = GL33.glGetAttribLocation(s.getProgram(), "normal");
        GL33.glEnableVertexAttribArray(normAttrib);
        GL33.glVertexAttribPointer(normAttrib, 3, GL33.GL_FLOAT, false, 6*4, 3*4);

        GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);

        while ( !GLFW.glfwWindowShouldClose(w.window) ) {
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GLFW.glfwPollEvents();

            c.updatePosition(1/60.0, Window.keyStates);

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
    }

    private static void render() {
        ViewProj proj = c.generateViewProj(128.0f);

        GL33.glBindVertexArray(vao);
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBindBuffer(GL33.GL_ELEMENT_ARRAY_BUFFER, ebo);

        s.use();

        MemoryStack stack = null;
        try {
            stack = MemoryStack.stackPush();
            GL33.glUniformMatrix4fv(_uniTrans, false, new Matrix4f().scale(8.0f).translate(0.0f, 0.0f, 0.0f).get(stack.mallocFloat(16)));
            GL33.glUniformMatrix4fv(_uniView, false, proj.view.get(stack.mallocFloat(16)));
            GL33.glUniformMatrix4fv(_uniProj, false, proj.proj.get(stack.mallocFloat(16)));
            GL33.glUniform3fv(_uniLight, c.getPos().get(stack.mallocFloat(3)));
        } finally {
            assert stack != null;
            stack.pop();
        }

        GL33.glDrawElements(GL33.GL_TRIANGLES, ObjData.getTotalNumFaceVertices(obj), GL33.GL_UNSIGNED_INT, 0);

        //GL33.glDisable(GL33.GL_DEPTH_TEST);
        monren.render(proj.proj, proj.view, mon);
        //GL33.glEnable(GL33.GL_DEPTH_TEST);

    }
}
