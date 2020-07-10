package com.tridevmc.spacegame.client;

import com.tridevmc.spacegame.SpaceGame;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    public final long window;
    public static final boolean[] keyStates = new boolean[GLFW.GLFW_KEY_LAST+1];
    private int _fWidth;
    private int _fHeight;
    private boolean _focused = false;
    private int _width;
    private int _height;
    private final boolean _vsync = false;

    public Window() {
        GLFWErrorCallback.createPrint(System.err).set();
        if(!GLFW.glfwInit()) {
            throw new RuntimeException("Unable to initialize GLFW!");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(800, 600, "SpaceGame", NULL, NULL);
        if(window == NULL) {
            throw new RuntimeException("Unable to create window!");
        }
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(window, pWidth, pHeight);

            _width = pWidth.get();
            _height = pHeight.get();

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

            // Center the window
            GLFW.glfwSetWindowPos(
                   window,
                    (vidmode.width() - _width) / 2,
                    (vidmode.height() - _height) / 2
            );
        }

        GLFW.glfwMakeContextCurrent(window);
        if(_vsync) {
            GLFW.glfwSwapInterval(1);
        }

        GLFW.glfwSetKeyCallback(window, (long window, int key, int scancode, int action, int mods) -> {
            if (key > 0)
                keyStates[key] = (action != GLFW.GLFW_RELEASE);
        });

        GLFW.glfwSetWindowFocusCallback(window, (long window, boolean focused) -> {
            if(_focused && !focused) {
                _focused = false;
            }
        });

        GLFW.glfwSetMouseButtonCallback(window, (long window, int button, int action, int mods) -> {
            /*if (!_focused) {
                GuiRenderer.mouseClick(button, action);
            }*/
        });

        GLFW.glfwSetCursorPosCallback(window, (long window, double x, double y) -> {
            SpaceGame.c.updateCameraRotation(x, y);
            if(_focused) {

            } /*else {
                GuiRenderer.updateScreenMouse((float)x, (float)y);
            }*/
        });

        GLFW.glfwSetWindowSizeCallback(window, (long window, int width, int height) -> {
            _width = width;
            _height = height;
        });

        GLFW.glfwSetFramebufferSizeCallback(window, (long window, int width, int height) -> {
            _fWidth = width;
            _fHeight = height;
        });

        GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);

        GLFW.glfwShowWindow(window);
    }

    public void destroy() {
        Callbacks.glfwFreeCallbacks(window);
        GLFW.glfwDestroyWindow(window);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null);
    }
}
