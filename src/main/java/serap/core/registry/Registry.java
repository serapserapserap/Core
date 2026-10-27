package serap.core.registry;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import serap.core.pointer.MutablePointer;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

/**
 * a helper class that allows for the storage and management of program resources
 * @param <T> Stored internal class of the registry
 */
public class Registry<T> {

    /**
     * Creates a new mutable registry
     * @param tag The tag for the registry to be identified under
     * @param pointerCreatFunc the supplier that the registry fetches from when registering a new entry
     * @return the resultant registry
     * @param <T> the stored type that the registry will hold
     * @param <P> the pointer type that the registry will use to hold the stored type {@code <T>}
     */
    @NotNull
    public static <T, P extends MutablePointer<T>> Registry.Mutable<T, P> newMutableRegistry(String tag, Supplier<P> pointerCreatFunc) {
        return new Mutable<>(tag) {
            @Override
            public P register(RegistryIdentifier identifier) {
                P pointer;
                this.register(identifier, pointer = pointerCreatFunc.get());
                return pointer;
            }
        };
    }

    /**
     * Creates a new regular registry
     * @param tag the tag for the registry to be identified under
     * @return the resultant registry
     * @param <T> the stored type that the registry will hold
     */
    @NotNull
    public static <T> Registry<T> newRegistry(String tag) {
        return new Registry<>(tag);
    }

    private Registry(String tag) {
        this.tag = tag;
    }

    private boolean writable = true;

    public void lock() {
        this.writable = false;
    }

    public boolean isLocked() {
        return this.writable;
    }

    @Getter
    private final String tag;
    protected final ObjectArrayList<Registrant<T>> registrants = new ObjectArrayList<>();

    /**
     * Attempts to access a registered object
     * @param identifier the identifier of the registered object that you're trying to access
     * @return the object registered under the supplied identifier, or null if no object is registered under the supplied identifier
     */
    @Nullable
    public T get(RegistryIdentifier identifier) {
        return findFirstWhere((objectIdentifier, obj) -> objectIdentifier.is(identifier));
    }

    /**
     * Attempts to access the object registered under the supplied identifier.
     * If none is found it registers an object under it using the given supplier
     * @param identifier the identifier to try access from and store to if access fails
     * @param supplier the supplier that will provide the value to be associated with the identifier
     *                 if there is not already an associated value
     * @return the value registered under the identifier (either pre-existing or newly computed)
     */
    @NotNull
    public T getOrComputeIfAbsent(RegistryIdentifier identifier, Supplier<T> supplier) {
        T getResult = this.get(identifier);
        if(getResult == null) {
            getResult = register(identifier, supplier.get());
        }
        return getResult;
    }

    /**
     * finds all registered objects that pass the supplied predicate
     * @param predicate the predicate to test the registrants against
     * @return an {@link ObjectArrayList} of all the registered objects that pass the supplied {@link BiPredicate}
     */
    @NotNull
    public ObjectArrayList<T> findAllWhere(BiPredicate<RegistryIdentifier, T> predicate) {
        ObjectArrayList<T> resultant = new ObjectArrayList<>();

        for(Registrant<T> registrant : this.registrants) {
            if(predicate.test(registrant.identifier, registrant.registrant)) {
                resultant.add(registrant.registrant);
            }
        }

        return resultant;
    }

    /**
     * returns the first object that passes the supplied predicate
     * @param predicate the predicate to test registry registrants against
     * @return the first found registrant that passes the {@link BiPredicate} supplied, or null if no registrants passed
     */
    @Nullable
    public T findFirstWhere(BiPredicate<RegistryIdentifier, T> predicate) {
        for(Registrant<T> registrant : this.registrants) {
            if(predicate.test(registrant.identifier, registrant.registrant)) {
                return registrant.registrant;
            }
        }
        return null;
    }

    /**
     * returns if the registry contains the supplied identifier
     * @param identifier the identifier to scan for
     * @return if the registry has a value associated with the supplied identifier
     */
    protected boolean containsIdentifier(RegistryIdentifier identifier) {
        return this.get(identifier) != null;
    }

    /**
     * registers an object and associates it with an identifier
     * @param identifier the identifier to be associated with the registrant
     * @param registrant the registrant being registered into the registry
     * @throws IllegalStateException if the identifier supplied already has an associated value
     * @throws IllegalStateException if the registry is locked
     */
    public T register(RegistryIdentifier identifier, T registrant) {
        if(this.isLocked()) {
            throw new IllegalStateException("Registry is locked");
        }

        if(this.containsIdentifier(identifier)) {
            throw new IllegalStateException("There is already a value associated with the identifier " + identifier.toString());
        }

        this.registrants.add(new Registrant<>(identifier, registrant));
        return registrant;
    }

    protected static class Registrant<T> {
        protected RegistryIdentifier identifier;
        protected T registrant;

        public Registrant(RegistryIdentifier id, T registrant) {
            this.identifier = id;
            this.registrant = registrant;
        }
    }

    public static abstract class Mutable<T, P extends MutablePointer<T>> extends Registry<P> {
        public abstract P register(RegistryIdentifier identifier);

        /**
         * attempts to get the pointer stored at the supplied identifier, if there is no
         * {@link MutablePointer} stored under it, it simply registers a {@link MutablePointer} under
         * it and returns that pointer
         * @param identifier the identifier to get from or register to if absent
         * @return the found pointer (either newly registered or already existing)
         */
        @NotNull
        public P getOrRegisterIfAbsent(@NotNull RegistryIdentifier identifier) {
            P ptr = this.get(identifier);
            return ptr != null ? ptr : this.register(identifier);
        }

        public Mutable(String tag) {
            super(tag);
        }

        /**
         * puts the supplied object in the registry under the supplied identifier.
         * If there is no {@link MutablePointer} registered to the identifier, it will
         * automatically register one to it and place the object in that
         * @param identifier the identifier to that the object should be associated with
         * @param object the object to store in the registry under the supplied identifier
         */
        public void deposit(RegistryIdentifier identifier, T object) {
            this.getOrRegisterIfAbsent(identifier).pointTo(object);
        }
    }
}
