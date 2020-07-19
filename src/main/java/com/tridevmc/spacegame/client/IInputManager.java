package com.tridevmc.spacegame.client;

public interface IInputManager {
    void setKeyState(int key, boolean state);
    boolean isKeyDown(int key);
}
