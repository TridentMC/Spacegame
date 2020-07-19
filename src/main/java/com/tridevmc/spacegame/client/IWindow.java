package com.tridevmc.spacegame.client;

import com.tridevmc.spacegame.client.input.IInputManager;

public interface IWindow {
    boolean shouldClose();
    void eventPoll();
    void swapBuffers();
    int width();
    int height();
    int fWidth();
    int fHeight();
    void destroy();
    IInputManager getInputManager();
    void lockCursor();
    void unlockCursor();
}
