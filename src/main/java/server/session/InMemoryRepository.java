package server.session;

import dao.impl.UserDao;
import dao.to.UserTo;
import io.netty.channel.ChannelHandlerContext;

public class InMemoryRepository implements SessionRepository {

    private UserDao users = new UserDao();
    public InMemoryRepository(){}

    public UserTo get(String userName) {
        return users.get(userName);
    }

    public void add(ChannelHandlerContext ctx, String userName, String password) {
        if (!users.contains(userName)) {
            users.save(new UserTo(ctx, userName, password));
            return;
        }
        users.get(userName).addCtx(ctx);
    }

    public void remove(String userName) {
        users.delete(users.get(userName));
    }

    public synchronized Boolean contains(String userName) {
        return users.contains(userName);
    }
}
