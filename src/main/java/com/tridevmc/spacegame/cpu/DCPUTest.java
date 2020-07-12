package com.tridevmc.spacegame.cpu;

import com.tridevmc.spacegame.client.Window;
import com.tridevmc.spacegame.cpu.hardware.EchoDevice;
import com.tridevmc.spacegame.cpu.hardware.IHardware;
import com.tridevmc.spacegame.cpu.hardware.LEM1802;
import com.tridevmc.spacegame.gl.ObjHelper;
import com.tridevmc.spacegame.gl.shader.FragmentShader;
import com.tridevmc.spacegame.gl.shader.ShaderProgram;
import com.tridevmc.spacegame.gl.shader.VertexShader;
import de.javagl.obj.ObjData;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.tinylog.Logger;

import java.io.File;
import java.io.IOException;

public class DCPUTest {

    public static Window w;
    public static DCPU cpu;
    public static LEM1802 lem;
    private static int loadRom(DCPU cpu, int ...args) {
        int index = 0x0000;
        for(int c : args) {
            cpu.ram[index++] = (char)c;
        }
        return index;
    }
    public static void main(String[] args) {
        try {
            w = new Window();
            run();
        } catch(Exception e) {
            Logger.error("Caught exception while running :(");
            Logger.error(e);
        }
        w.destroy();
        /*cpu.connect(dmhw);
        //int max = loadRom(cpu, 0x8401,0x8821,0xac41,0x8452,0x7f81,0x000d,0x0061,0x0462,0x0401,0x0c21,0x8843,0x7f81,0x0003);
        int max = loadRom(cpu, 0x7c21,0xcafe,0x7d40,0x000c,0x8801,0x7e40,0x0000,0x8401,0x7e40,0x0000,0x7c01,0xdead,0x0041,0x1d60);
        while(cpu.pc < max) {
            cpu.eval(cpu.ram[cpu.pc]);
        }
        System.out.println("A: " + String.format("0x%04X", (int)cpu.a)
                + "\nB: " + String.format("0x%04X", (int)cpu.b)
                + "\nC: " + String.format("0x%04X", (int)cpu.c)
                + "\nX: " + String.format("0x%04X", (int)cpu.x)
                + "\nY: " + String.format("0x%04X", (int)cpu.y)
                + "\nZ: " + String.format("0x%04X", (int)cpu.z)
                + "\nI: " + String.format("0x%04X", (int)cpu.i)
                + "\nJ: " + String.format("0x%04X", (int)cpu.j)
                + "\nSP: " + String.format("0x%04X", (int)cpu.sp)
                + "\nPC: " + String.format("0x%04X", (int)cpu.pc)
                + "\nEX: " + String.format("0x%04X", (int)cpu.ex)
                + "\nIA: " + String.format("0x%04X", (int)cpu.ia));
        System.out.println("Cycles elapsed: " + cpu.cycles);
        System.out.println("RAM[0x0000]: " + String.format("0x%04X", (int)cpu.ram[0x0000]));
        System.out.println("RAM[0xFFFF]: " + String.format("0x%04X", (int)cpu.ram[0xFFFF]));
        w.destroy();*/
    }

    public static void run() throws IOException {
        GLFW.glfwSetInputMode(w.window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        GL.createCapabilities();
        cpu = new DCPU();
        lem = new LEM1802();
        cpu.connect(lem);
        float[] verts = {
                -1.0f, -1.0f, 0.0f, 1.0f,
                -1.0f, 1.0f, 0.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 0.0f,
                1.0f, 1.0f, 1.0f, 0.0f,
                1.0f, -1.0f, 1.0f, 1.0f,
                -1.0f, -1.0f, 0.0f, 1.0f
        };

        int max = loadRom(cpu, 0x8401, 0x7c21, 0x8000, 0x7e40, 0x0000, 0x0301, 0x7c01, 0xdead, 0x7c21, 0xcadd, 0x06fe, 0x8000, 0x1825, 0x7c2a, 0x0ff0, 0x7422, 0x882f, 0x8bad, 0x0024, 0x8432, 0x1821, 0x7cd3, 0x0180, 0x7f81, 0x000a, 0x7f81, 0x0019);

        int vao = GL33.glGenVertexArrays();
        GL33.glBindVertexArray(vao);
        int vbo = GL33.glGenBuffers();
        GL33.glBindBuffer(GL33.GL_ARRAY_BUFFER, vbo);
        GL33.glBufferData(GL33.GL_ARRAY_BUFFER, verts, GL33.GL_STATIC_DRAW);

        VertexShader v = new VertexShader(new File("shaders", "default.vert"));
        FragmentShader f = new FragmentShader(new File("shaders", "default.frag"));
        ShaderProgram s = new ShaderProgram(v, f);

        s.use();

        int posAttrib = GL33.glGetAttribLocation(s.getProgram(), "position");
        GL33.glEnableVertexAttribArray(posAttrib);
        GL33.glVertexAttribPointer(posAttrib, 2, GL33.GL_FLOAT, false, 4*4, 0L);

        int texAttrib = GL33.glGetAttribLocation(s.getProgram(), "texCoord");
        GL33.glEnableVertexAttribArray(texAttrib);
        GL33.glVertexAttribPointer(texAttrib, 2, GL33.GL_FLOAT, false, 4*4, 2*4);

        GL33.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        while(!GLFW.glfwWindowShouldClose(w.window)) {
            GL33.glClear(GL33.GL_COLOR_BUFFER_BIT | GL33.GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            GLFW.glfwPollEvents();

            if(cpu.pc != max) {
                cpu.eval(cpu.ram[cpu.pc]);
            }

            lem.render();

            GL33.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

            GLFW.glfwSwapBuffers(w.window); // swap the color buffers
        }
    }
}
