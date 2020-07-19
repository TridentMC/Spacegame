package com.tridevmc.spacegame.client;

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
