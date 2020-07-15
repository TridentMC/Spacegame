package com.tridevmc.spacegame.util;

import org.tinylog.Logger;

/**
 * {@code FloatList} is a dynamically resizable array of floats, in lieu of using {@link Object}s to accomplish the same
 * thing in a comparable generic {@link java.util.List}.
 *
 * @author CalmBit
 */
public class FloatList {

    private static final int DEFAULT_CAPACITY = 1024;

    /**
     * The current maximum capacity of the list.
     *
     * This is the size that the array is current allocated to, but doesn't represent the actual number of entries to
     * the list.
     */
    private int _capacity;

    /**
     * The current maximum entry in the list, from {@code 0} to {@link #_capacity}.
     *
     * This value causes a resize if greater than or equal to {@link #_capacity} at any point.
     */
    private int _length = 0;

    /**
     * The internal store for the list.
     *
     * Only modified through the functions below, in order to keep the external state accurate.
     */
    private float[] _store;

    /**
     * Constructs a {@code FloatList} with the default initial capacity of {@value DEFAULT_CAPACITY}.
     */
    public FloatList() {
       this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a {@code FloatList} with a variable initial capacity of {@code size}.
     *
     * @param size the capacity of the new list
     */
    public FloatList(int size) {
        _capacity = size;
        _store = new float[size];
    }

    /**
     * Arbitrarily resize the list with a new value for {@link #_capacity}.
     *
     * If {@code truncate} is true, values can be smaller than {@link #_capacity}, but will erase data in the process.
     * Only {@code truncate} if the entries at {@code newCapacity} and above can be erased!
     *
     * @param newCapacity the new capacity for the underlying store
     * @param truncate whether or not the new capacity can be lower than the current one,
     *                 erasing entries at or above {@code newCapacity}
     */
    public void resize(int newCapacity, boolean truncate) {
        if(!truncate && this._length > newCapacity) {
            Logger.error("Unable to resize FloatList - new capacity '" + newCapacity + "' is below current length '" + _length + "'!");
        }
        float[] n = new float[newCapacity];
        for(int i = 0;i < newCapacity;i++) {
            n[i] = _store[i];
            if(i == _capacity-1)
                break;
        }
        _store = n;
        _capacity = newCapacity;
        if(_length > _capacity) _length = _capacity;
    }

    /**
     * Arbitrarily resize the list without truncation (by default).
     *
     * @param capacity the new capacity for the underlying store
     */
    public void resize(int capacity) {
        resize(capacity, false);
    }

    /**
     * Appends a new value to the list, increasing {@link #_length} and possibly resizing the store if equal to or
     * greater than {@link #_capacity}.
     *
     * @param value the new value to append
     */
    public void append(float value)  {
        if(_length + 1 > _capacity) {
            resize(_capacity * 2);
        }
        _store[_length++] = value;
    }

    /**
     * Appends a set of values to the list, increasing {@link #_length} and possibly resizing the store multiple times
     * while the new {@link #_length} is under capacity.
     *
     * @param values the values to append to the list
     */
    public void appendAll(float ...values) {
        while(_length + values.length > _capacity) {
            resize(_capacity * 2);
        }
        for(float v : values) {
            _store[_length++] = v;
        }
    }

    /**
     * Clears the entirety of the list, erasing all values and setting {@link #_length} to zero.
     *
     * Note that this doesn't resize the array. If an array has been resized, it will stay that way until manually
     * resized.
     */
    public void clear() {
        for(int i =0;i < _length;i++) {
            _store[i] = 0.0f;
        }
        _length = 0;
    }

    /**
     * Replaces an element at the position {@code element} with {@code value}.
     *
     * @param element the position of the element being replaced
     * @param value the new value for the element
     */
    public void set(int element, float value) {
        if(element > _capacity-1) {
            resize(element+1);
        }
        _store[element] = value;
    }

    /**
     * Gets an element at the position {@code element}.
     *
     * @param element the position of the element to retrieve
     * @return the value of the element
     */
    public float get(int element) {
        if(element > _capacity-1) {
            return -1;
        }
        return _store[element];
    }

    /**
     * Gets the underlying store of the {@code FloatList}. Useful for operations that require an array type.
     *
     * Note that the underlying store is sized to {@link #_capacity}, but only has valid data up to {@link #_length}.
     * A function that consumes this underlying store should always check the bounds of the array and only use elements
     * up to {@link #_length}.
     *
     * @return the underlying store of the list
     */
    public float[] getStore() {
        return _store;
    }

    /**
     * Gets the current number of elements in the list.
     *
     * @return the number of elements in the list
     */
    public int getLength() {
        return _length;
    }

    /**
     * Gets the current maximum capacity of the list
     *
     * @return the capacity of the list
     */
    public int getCapacity() {
        return _capacity;
    }

}
