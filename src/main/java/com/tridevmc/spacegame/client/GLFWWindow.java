package com.tridevmc.spacegame.client;

import com.tridevmc.spacegame.client.input.GLFWInputManager;
import com.tridevmc.spacegame.client.input.IInputManager;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.system.MemoryUtil.NULL;

public class GLFWWindow implements IWindow {
    private final long _window;
    private int _fWidth;
    private int _fHeight;
    private boolean _focused = false;
    private int _width;
    private int _height;
    private final boolean _vsync = true;
    private final GLFWInputManager _input = new GLFWInputManager();

    public GLFWWindow() {
        GLFWErrorCallback.createPrint(System.err).set();
        boolean init = GLFW.glfwInit();
        if(!init) {
            throw new RuntimeException("Unable to initialize GLFW!");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);

        _window = GLFW.glfwCreateWindow(800, 600, "SpaceGame", NULL, NULL);
        if(_window == NULL) {
            throw new RuntimeException("Unable to create window!");
        }
        try ( MemoryStack stack = MemoryStack.stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pFWidth = stack.mallocInt(1);
            IntBuffer pFHeight = stack.mallocInt(1);

            GLFW.glfwGetWindowSize(_window, pWidth, pHeight);
            GLFW.glfwGetFramebufferSize(_window, pFWidth, pFHeight);

            _width = pWidth.get();
            _height = pHeight.get();
            _fWidth = pFWidth.get();
            _fHeight = pFHeight.get();

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

            if(vidmode == null) {
                throw new RuntimeException("Couldn't get vidmode!");
            }

            // Center the window
            GLFW.glfwSetWindowPos(
                    _window,
                    (vidmode.width() - _width) / 2,
                    (vidmode.height() - _height) / 2
            );
        }

        GLFW.glfwMakeContextCurrent(_window);
        if(_vsync) {
            GLFW.glfwSwapInterval(1);
        }

        GLFW.glfwSetKeyCallback(_window, (long window, int key, int scancode, int action, int mods) -> {
            if (key > 0)
                _input.setKeyState(key, action != GLFW.GLFW_RELEASE);
        });

        GLFW.glfwSetWindowFocusCallback(_window, (long window, boolean focused) -> {
            if(_focused && !focused) {
                _focused = false;
            }
        });

        GLFW.glfwSetMouseButtonCallback(_window, (long window, int button, int action, int mods) -> {
            /*if (!_focused) {
                GuiRenderer.mouseClick(button, action);
            }*/
        });

        GLFW.glfwSetCursorPosCallback(_window, (long window, double x, double y) -> {
            _input.callCursorPosCallbacks(x, y);
        });

        GLFW.glfwSetWindowSizeCallback(_window, (long window, int width, int height) -> {
            _width = width;
            _height = height;
        });

        GLFW.glfwSetFramebufferSizeCallback(_window, (long window, int width, int height) -> {
            _fWidth = width;
            _fHeight = height;
        });

        GLFW.glfwShowWindow(_window);

        GL.createCapabilities();
    }

    @Override
    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(_window);
    }

    @Override
    public void eventPoll() {
        GLFW.glfwPollEvents();
    }

    @Override
    public void swapBuffers() {
        GLFW.glfwSwapBuffers(_window);
    }

    @Override
    public int width() {
        return _width;
    }

    @Override
    public int height() {
        return _height;
    }

    @Override
    public int fWidth() {
        return _fWidth;
    }

    @Override
    public int fHeight() {
        return _fHeight;
    }

    @Override
    public void destroy() {
        Callbacks.glfwFreeCallbacks(_window);
        GLFW.glfwDestroyWindow(_window);

        GLFW.glfwTerminate();
        GLFW.glfwSetErrorCallback(null);
    }

    @Override
    public IInputManager getInputManager() {
        return _input;
    }

    @Override
    public void lockCursor() {
        GLFW.glfwSetInputMode(_window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }

    @Override
    public void unlockCursor() {
        GLFW.glfwSetInputMode(_window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
    }
}
