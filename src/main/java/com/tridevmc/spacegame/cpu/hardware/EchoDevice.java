package com.tridevmc.spacegame.cpu.hardware;

import com.tridevmc.spacegame.cpu.DCPU;

public class EchoDevice implements IHardware {
    private DCPU _origin;
    private char _msg = 0;
    @Override
    public void connect(DCPU origin) {
        _origin = origin;
    }

    @Override
    public void interrupt() {
        switch (_origin.a) {
            case 0x00:
                if(_msg != 0) {
                    _origin.interrupt(_msg);
                }
                break;
            case 0x01:
                _msg = _origin.b;
                break;
        }
    }

    @Override
    public int id() {
        return 0x211b0428 ;
    }

    @Override
    public char version() {
        return 1;
    }

    @Override
    public int manufacturer() {
        return 0xFFFFFFFF;
    }

    @Override
    public void update() {

    }
}
