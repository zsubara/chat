package dao.impl;

import dao.Dao;
import dao.to.UserTo;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDao implements Dao<UserTo> {

    private volatile Map<String, UserTo> users = new ConcurrentHashMap<>();

    @Override
    public UserTo get(String name) {
        return users.get(name);
    }

    @Override
    public List<UserTo> get() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void save(UserTo user) {
        users.put(user.getName(), user);
    }

    @Override
    public void update(UserTo user) {
        delete(user);
        save(user);
    }

    public boolean contains(String name) {
        return users.containsKey(name);
    }
    @Override
    public void delete(UserTo user) {
        users.remove(user.getName());
    }
}
