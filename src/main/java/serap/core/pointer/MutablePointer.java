package serap.core.pointer;

public interface MutablePointer<T> extends Pointer<T> {
    void pointTo(T neu);

    static <T> MutablePointer<T> newMutablePointer() {
        return new SimplePointer<>();
    }
}
