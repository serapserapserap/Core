package serap.core.registry;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class StringHashRegistryIdentifier implements RegistryIdentifier {
    private final String idAsString;
    private final int stringHash;

    public StringHashRegistryIdentifier(@NotNull String id) {
        this.idAsString = id;
        this.stringHash = id.hashCode();
    }

    @Contract(pure = true)
    private boolean hashMatches(@NotNull StringHashRegistryIdentifier other) {
        return this.stringHash == other.stringHash;
    }

    @Override
    public boolean is(RegistryIdentifier other) {
        return other instanceof StringHashRegistryIdentifier candidate && this.hashMatches(candidate);
    }

    @Override
    public String toString() {
        return idAsString;
    }
}
