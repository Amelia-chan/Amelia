package pw.mihou.amelia.io.cache.entities;

public interface CacheEntity<T> {

    T get();
    long getExpiry();
    long getCreation();
    boolean isValid();

}
