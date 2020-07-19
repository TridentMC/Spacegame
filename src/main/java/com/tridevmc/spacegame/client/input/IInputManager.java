package com.tridevmc.spacegame.client.input;

public interface IInputManager {
    void setKeyState(int key, boolean state);
    boolean isKeyDown(int key);
    void callCursorPosCallbacks(double x, double y);
    void registerCursorPosCallback(CursorPosCallback callback);
}
