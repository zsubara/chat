package dao.impl;

import dao.Dao;
import dao.to.RoomTo;
import dao.to.UserTo;
import dao.to.impl.RoomToImpl;
import dao.to.impl.UserToImpl;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDao implements Dao<UserTo> {

    private Map<Long, UserTo> users = new HashMap<>();

    public UserTo get(Channel channel) {
        for (Map.Entry<Long, UserTo> entry : users.entrySet())
            if (((UserToImpl) entry.getValue()).getChannel().equals(channel))
                return  entry.getValue();
        return null;
    }

    public UserTo findUserByName(String name) {
        for (Map.Entry<Long, UserTo> entry : users.entrySet())
            if (((UserToImpl) entry.getValue()).getName().equals(name))
                return  entry.getValue();
        return null;
    }

    @Override
    public UserTo get(Long id) {
        return users.get(id);
    }
    @Override
    public List<UserTo> get() {
        return new ArrayList<>(users.values());
    }

    @Override
    public void save(UserTo user) {
        users.put(user.getUserId(), user);
    }

    @Override
    public void update(UserTo user) {
        delete(user);
        save(user);
    }

    @Override
    public void delete(UserTo user) {
        users.remove(user.getUserId());
    }
}
