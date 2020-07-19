package com.tridevmc.spacegame.client;

import org.lwjgl.glfw.GLFW;

public class GLFWInputManager implements IInputManager {
    private final boolean[] _keyStates = new boolean[GLFW.GLFW_KEY_LAST+1];

    @Override
    public void setKeyState(int key, boolean state) {
        _keyStates[key] = state;
    }

    @Override
    public boolean isKeyDown(int key) {
        return _keyStates[key];
    }
}
