package server.session;

import dao.impl.UserDao;
import dao.to.UserTo;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class InMemoryRepository implements SessionRepository {

    private UserDao users = new UserDao();
    public InMemoryRepository(){}

    public UserTo get(String userName) {
        return users.get(userName);
    }

    public void add(UserTo user) {
        users.save(user);
    }

    public void remove(String userName) {
        users.delete(users.get(userName));
    }

    public synchronized Boolean contains(String userName) {
        return users.contains(userName);
    }

    public static UserTo buildUser(ChannelHandlerContext ctx, String userName, String password) {
        return new UserTo(ctx, userName, password);
    }
}
