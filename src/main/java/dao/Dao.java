package dao;

import java.util.List;

public interface Dao<T> {

    T get(String name);

    List<T> get();

    void save(T t);

    void update(T t);

    void delete(T t);
}
