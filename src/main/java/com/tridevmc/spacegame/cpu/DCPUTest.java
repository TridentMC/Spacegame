package com.tridevmc.spacegame.cpu;

import com.tridevmc.spacegame.client.Window;
import com.tridevmc.spacegame.cpu.hardware.LEM1802;
import com.tridevmc.spacegame.cpu.hardware.backend.GLFullScreenRenderer;
import com.tridevmc.spacegame.cpu.hardware.backend.IScreenRenderer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL33;
import org.tinylog.Logger;

public class DCPUTest {

    private static Window _window;
    private static DCPU _cpu;
    private static LEM1802 _lem;
    private static IScreenRenderer _lemRender;

    private static int loadRom(DCPU cpu, int ...args) {
        int index = 0x0000;
        for(int c : args) {
            cpu.ram[index++] = (char)c;
        }
        return index;
    }

    public static void main(String[] args) {
        try {
            _window = new Window();
            run();
        } catch(Exception e) {
            Logger.error("Caught exception while running :(");
            Logger.error(e);
        }
        _window.destroy();
    }

    private static void run() {
        GLFW.glfwSetInputMode(_window.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        GL.createCapabilities();

        _cpu = new DCPU();
        _lem = new LEM1802();
        _cpu.connect(_lem);

        _lemRender = new GLFullScreenRenderer();
        _lemRender.init(_lem);

        int max = loadRom(_cpu, 0x8401, 0x7c21, 0x8000, 0x7e40, 0x0000, 0x9001, 0x9421, 0x7e40, 0x0000, 0x7c01, 0xdead, 0x7c21, 0xbaba, 0x7cc1, 0x0100, 0x1afe, 0x8000, 0x1825, 0x7c2a, 0x0ff0, 0x7422, 0x882f, 0x8bad, 0x0024, 0x8432, 0x1821, 0x7cf3, 0x0180, 0x7f81, 0x000f, 0x84e1, 0x84d3, 0x7f81, 0x000f, 0x7f81, 0x0022);

        GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        while(!GLFW.glfwWindowShouldClose(_window.window)) {
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            update(max);

            render();

            GLFW.glfwSwapBuffers(_window.window); // swap the color buffers
        }
    }

    private static void update(int max) {
        GLFW.glfwPollEvents();

        if(_cpu.pc != max) {
            _cpu.eval(10000/60);
        }

        _cpu.hardwareList.updateAll();
    }

    private static void render() {
        _lemRender.render(_lem);
    }
}
