package serap.core.pointer;

public class SimplePointer<T> implements MutablePointer<T> {

    protected T value;

    @Override
    public T get() {
        return this.value;
    }

    @Override
    public void pointTo(T value) {
        this.value = value;
    }
}
