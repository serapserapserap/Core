package serap.core.registry;

public interface RegistryIdentifier {
    boolean is(RegistryIdentifier other);
    String toString();

    static RegistryIdentifier simpleIdOf(String id) {
        return new StringHashRegistryIdentifier(id);
    }
}
