package dao.impl;

import dao.Dao;
import dao.to.RoomTo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RoomDao implements Dao<RoomTo> {

    private volatile Map<String, RoomTo> rooms = new ConcurrentHashMap<>();

    @Override
    public RoomTo get(String name) {
        return rooms.get(name);
    }

    @Override
    public List<RoomTo> get() {
        return new ArrayList<>(rooms.values());
    }

    @Override
    public void save(RoomTo roomTo) {
        rooms.put(roomTo.getName(), roomTo);
    }

    @Override
    public void update(RoomTo roomTo) {
        delete(roomTo);
        save(roomTo);
    }

    @Override
    public void delete(RoomTo roomTo) {
        rooms.remove(roomTo.getName());
    }

    public boolean contains(String name) {
        return rooms.containsKey(name);
    }
}
