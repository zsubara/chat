package server.session;

import dao.to.UserTo;
import io.netty.channel.ChannelHandlerContext;

public interface SessionRepository {

    void add(ChannelHandlerContext ctx, String userName,String password);

    UserTo get(String userName);

    void remove(String userName);

    Boolean contains(String userName);
}
