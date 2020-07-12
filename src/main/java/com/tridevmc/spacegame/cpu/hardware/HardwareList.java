package com.tridevmc.spacegame.cpu.hardware;

import com.tridevmc.spacegame.cpu.DCPU;

public class HardwareList {
    private int _len = 0;
    private IHardware[] _list = new IHardware[16];

    private void resize(int size) {
        if(size < _list.length) {
            return;
        }
        if(size >= 0x10000) {
            size = 0xFFFF;
        }
        IHardware[] n = new IHardware[size];
        System.arraycopy(_list, 0, n, 0, _list.length);
        _list = n;
    }

    public void connect(DCPU cpu, IHardware hw) {
        if(_len >= _list.length) {
            if(_list.length == 0xFFFF) {
                throw new RuntimeException("too many devices connected!!");
            }
            resize(_list.length*2);
        }
        hw.connect(cpu);
        _list[_len++] = hw;
    }

    public void interrupt(int hwid) {
        if(hwid >= _len) return;
        _list[hwid].interrupt();
    }

    public IHardware get(int hwid) {
        if(hwid >= _len) return null;
        return _list[hwid];
    }

    public char size() {
        return (char)_len;
    }
}
