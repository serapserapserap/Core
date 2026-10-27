package serap.core.pointer;

public interface Pointer<T> {
    T get();

    static <T> T unwrap(Pointer<T> pointer) {
        return pointer.get();
    }
}

