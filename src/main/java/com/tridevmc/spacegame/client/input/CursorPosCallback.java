package com.tridevmc.spacegame.client.input;

@FunctionalInterface
public interface CursorPosCallback {
    void move(double x, double y);
}
