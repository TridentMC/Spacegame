package com.tridevmc.spacegame.cpu.hardware;

import com.tridevmc.spacegame.cpu.DCPU;

public interface IHardware {
    void connect(DCPU origin);
    void interrupt();
    int id();
    char version();
    int manufacturer();

    void update();
}
