package sk.zemco.messengerstats;

@FunctionalInterface
public interface SupplierWithException<T> {

    T get() throws Exception;

}
