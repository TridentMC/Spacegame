package com.tridevmc.spacegame.util;

public class ResourceLocation {
    private String _namespace;
    private String _name;

    public ResourceLocation(String namespace, String name) {
        _namespace = namespace;
        _name = name;
    }

    @Override
    public String toString() {
        return _namespace+":"+_name;
    }

    @Override
    public boolean equals(Object obj) {
        return toString().equals(obj.toString());
    }

    public int hashCode() {
        int result = _namespace.hashCode();
        result = 31 * result + _name.hashCode();
        return result;
    }

    public String getNamespace() {
        return _namespace;
    }

    public String getName() {
        return _name;
    }
}
