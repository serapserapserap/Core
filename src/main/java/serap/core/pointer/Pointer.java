package serap.core.pointer;

public interface Pointer<T> {
    T get();

    static <T> Pointer<T> newPointer() {
        return null;
    }

    static <T> T unwrap(Pointer<T> pointer) {
        return pointer.get();
    }
}

