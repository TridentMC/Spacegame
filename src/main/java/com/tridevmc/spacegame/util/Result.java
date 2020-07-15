package com.tridevmc.spacegame.util;

public class Result<T> {

    private final T _value;
    private final String _err;

    private Result(T val) {
        _value = val;
        _err = null;
    }

    private Result(String err) {
        _value = null;
        _err = err;
    }

    public static <T> Result<T> ok(T val) {
        return new Result<>(val);
    }

    public static <T> Result<T> error(String err) {
        return new Result<>(err);
    }

    public boolean isError() {
        return _err != null;
    }

    public boolean isOk() {
        return _value != null;
    }

    public T get() {
        return _value;
    }

    public String getError() {
        return _err;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Result) {
            return this._value == (((Result) obj)._value);
        }
        return false;
    }
}
