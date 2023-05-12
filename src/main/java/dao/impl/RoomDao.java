package dao.impl;

import dao.Dao;
import dao.to.RoomTo;
import dao.to.UserTo;
import dao.to.impl.RoomToImpl;
import dao.to.impl.UserToImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RoomDao implements Dao<RoomTo> {

    private Map<Long, RoomTo> rooms = new HashMap<>();

    @Override
    public RoomTo get(Long id) {
        return rooms.get(id);
    }

    public RoomTo get(String name) {
        for (Map.Entry<Long, RoomTo> entry : rooms.entrySet())
            if (((RoomToImpl) entry.getValue()).getName().equals(name))
                return  entry.getValue();
        return null;
    }

    @Override
    public List<RoomTo> get() {
        return new ArrayList<>(rooms.values());
    }

    @Override
    public void save(RoomTo roomTo) {
        rooms.put(roomTo.getRoomId(), roomTo);
    }

    @Override
    public void update(RoomTo roomTo) {
        delete(roomTo);
        save(roomTo);
    }

    @Override
    public void delete(RoomTo roomTo) {
        rooms.remove(roomTo.getRoomId());
    }
}
