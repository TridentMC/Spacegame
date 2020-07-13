package com.tridevmc.spacegame.util;

public class CharQueue {
    private final char[] _store;
    private final int _cap;
    private int _head = 0;
    private int _tail = 0;
    private int _len = 0;

    public CharQueue() {
        this(256);
    }

    public CharQueue(int cap) {
        _cap = cap;
        _store = new char[cap];
    }

    public void enqueue(char value) throws OverCapacityException {
        if(++_len > _cap) {
            throw new OverCapacityException("Tried to enqueue more than "+_cap+" entries!!!");
        }
        _store[_tail++] = value;
        if(_tail >= _cap) _tail = 0;
    }

    public char dequeue() {
        if(_len == 0) {
            throw new RuntimeException("Tried to dequeue with no entries!!!");
        }
        char value = _store[_head++];
        --_len;
        if(_head >= _cap) _head = 0;
        return value;
    }

    public int size() {
        return _len;
    }
}
