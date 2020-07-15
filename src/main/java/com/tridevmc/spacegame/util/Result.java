package com.tridevmc.spacegame.util;

import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * {@code Result} represents a value that can either be valid (of type {@code T}) or invalid (of type {@code String}).
 *
 * This differs semantically from {@link java.util.Optional}, as {@link java.util.Optional} represents a return value
 * where an absence of a value is an acceptable outcome for a function, while {@code Result} represents a return value
 * where an absence of a value should be contextualized with some form of error.
 *
 * @param <T> the type to return if valid
 *
 * @author CalmBit
 */
public class Result<T> {

    private final T _value;
    private final String _err;

    /**
     * Constructs a valid {@code Result} with the value provided.
     *
     * @param val the value to encapsulate
     */
    private Result(T val) {
        _value = val;
        _err = null;
    }

    /**
     * Constructs an invalid {@code Result} with the error provided.
     *
     * @param err the error to encapsulate
     */
    private Result(String err) {
        _value = null;
        _err = err;
    }

    /**
     * Constructs a new, valid {@code Result} from {@code val}.
     *
     * @param val the value to fill the result with.
     * @param <T> the type of the value
     * @return the constructed {@code Result}
     */
    public static <T> Result<T> ok(T val) {
        return new Result<>(val);
    }

    /**
     * Constructs a new, invalid {@code Result} from an error string {@code err}.
     *
     * @param err the error to fill the result with.
     * @param <T> the type of the non-existent value
     * @return the constructed {@code Result}
     */
    public static <T> Result<T> error(String err) {
        return new Result<>(err);
    }

    /**
     * Determines if the {@code Result} is invalid.
     *
     * @return {@code true} if an error is present, or {@code false} otherwise.
     */
    public boolean isError() {
        return _err != null;
    }

    /**
     * Determines if the {@code Result} is valid.
     *
     * @return {@code true} if a value is present, or {@code false} otherwise.
     */
    public boolean isOk() {
        return _value != null;
    }

    /**
     * Returns the encapsulated value if the value is present, but throws a {@link NoSuchElementException} if it isn't.
     *
     * @return {@link #_value} if not {@code null}
     * @throws NoSuchElementException if {@link #_value} is {@code null}.
     */
    public T get() {
        if(_value == null) {
            throw new NoSuchElementException("Value is not present - '" + _err + "'");
        }
        return _value;
    }

    /**
     * Returns the encapsulated error if the error is present, but throws a {@link NoSuchElementException} if it isn't.
     *
     * @return {@link #_err} if not {@code null}
     * @throws NoSuchElementException if {@link #_err} is {@code null}.
     */
    public String getError() {
        if(_err == null) {
            throw new NoSuchElementException("No error to get!'");
        }
        return _err;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Result) {
            if(this.isError()) {
                return Objects.equals(this._err, ((Result) obj)._err);
            }
            return Objects.equals(this._value, ((Result) obj)._value);
        }
        return false;
    }
}
