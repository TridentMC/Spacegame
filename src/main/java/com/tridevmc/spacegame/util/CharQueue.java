package com.tridevmc.spacegame.util;

/**
 * CharQueue is a primitive-typed alternative to {@link java.util.Queue} for {@code char} primitives.
 *
 * We use this to avoid some of the overhead of instantiating {@link Object}s with generics.
 *
 * @author CalmBit
 */
public class CharQueue {

    private static final int DEFAULT_CAPACITY = 256;

    /**
     * The underlying store of the queue.
     *
     * Only modified through the functions below, in order to keep the external state accurate.
     */
    private final char[] _store;

    /**
     * The capacity of the queue.
     *
     * Entries cannot be added past this limit.
     */
    private final int _capacity;

    /**
     * The position in the queue from which entries will be removed.
     */
    private int _head = 0;

    /**
     * The position in the queue where entries will be added.
     */
    private int _tail = 0;

    /**
     * The current number of items in the queue.
     */
    private int _length = 0;


    /**
     * Constructs a {@code CharQueue} with the default initial capacity of {@value DEFAULT_CAPACITY}.
     */
    public CharQueue() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a {@code CharQueue} with a variable initial capacity {@code cap}.
     *
     * @param cap the capacity of the new queue
     */
    public CharQueue(int cap) {
        _capacity = cap;
        _store = new char[cap];
    }

    /**
     * Attempts to force-add a value to the {@code CharQueue}.
     *
     * @param value the value to add to the tail of the queue
     * @throws IllegalStateException if the value cannot be added because the queue
     *                               is at capacity.
     */
    public void add(char value) {
        if(_length >= _capacity) {
            throw new IllegalStateException("Tried to add more than "+ _capacity +" entries");
        }
        _store[_tail++] = value;
        if(_tail >= _capacity) _tail = 0;
    }

    /**
     * Attempts to force-remove a value from the {@code CharQueue}.
     *
     * @return the value removed from the head of the queue
     * @throws IllegalStateException if a value cannot be removed from the queue because
     *                               the queue is empty.
     */
    public char remove() {
        if(_length == 0) {
            throw new IllegalStateException("Tried to dequeue with no entries");
        }
        char value = _store[_head++];
        --_length;
        if(_head >= _capacity) _head = 0;
        return value;
    }

    /**
     * Gets the current size of the queue.
     *
     * @return the number of entries in the queue
     */
    public int size() {
        return _length;
    }

    /**
     * Gets the current capacity of the queue.
     *
     * @return the maximum number of entries able to be added to the queue.
     */
    public int capacity() {
        return _capacity;
    }
}
