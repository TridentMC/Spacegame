package com.tridevmc.spacegame.util;

/**
 * {@code ResourceLocation} is a class encapsulating a namespaced resource identity.
 *
 * This serves as a way to separate resources into distinct classes while still being able to detect duplicates.
 */
public class ResourceLocation {

    /**
     * The namespace of the identity.
     */
    private String _namespace;

    /**
     * The name of the identity.
     *
     * Within the namespace {@link #_namespace}, and assuming the resources being compared are of the same type,
     * this should be unique.
     */
    private String _name;

    /**
     * Constructs a {@code ResourceLocation} with the specified {@code namespace} and {@code name}.
     *
     * @param namespace the namespace component of the identity
     * @param name the name component of the identity
     */
    public ResourceLocation(String namespace, String name) {
        _namespace = namespace;
        _name = name;
    }

    /**
     * Gets the namespace of the {@code ResourceLocation}.
     *
     * @return The namespace of the identity
     */
    public String namespace() {
        return _namespace;
    }

    /**
     * Gets the name of the {@code ResourceLocation}.
     *
     * @return The name of the identity
     */
    public String name() {
        return _name;
    }

    @Override
    public String toString() {
        return _namespace+":"+_name;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ResourceLocation) {
            return toString().equals(obj.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = _namespace.hashCode();
        result = 31 * result + _name.hashCode();
        return result;
    }
}
