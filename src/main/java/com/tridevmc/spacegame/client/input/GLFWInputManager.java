package com.tridevmc.spacegame.client.input;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class GLFWInputManager implements IInputManager {
    private final boolean[] _keyStates = new boolean[GLFW.GLFW_KEY_LAST+1];
    private final List<CursorPosCallback> _cursorPosCallbacks = new ArrayList<>();

    @Override
    public void setKeyState(int key, boolean state) {
        _keyStates[key] = state;
    }

    @Override
    public boolean isKeyDown(int key) {
        return _keyStates[key];
    }

    @Override
    public void callCursorPosCallbacks(double x, double y) {
        for(CursorPosCallback c : _cursorPosCallbacks) {
            c.move(x, y);
        }
    }

    @Override
    public void registerCursorPosCallback(CursorPosCallback callback) {
        _cursorPosCallbacks.add(callback);
    }
}
