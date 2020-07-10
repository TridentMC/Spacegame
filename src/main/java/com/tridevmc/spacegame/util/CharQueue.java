package com.tridevmc.spacegame.util;

public class CharQueue {
    private char[] _store;
    private int _cap = 0;
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

    public void enqueue(char value) {
        if(++_len > _cap) {
            throw new RuntimeException("Tried to enqueue more than "+_cap+" entries!!!");
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
}
