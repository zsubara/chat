package server.handler;

import command.CommandRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import server.Router;
import server.session.InMemoryRepository;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private Router router;
    private String userName;
    private String password;

    public ClientHandler(String userName, String password, Router router) {
        this.userName = userName;
        this.password = password;
        this.router = router;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        try {
            router.accept(InMemoryRepository.buildUser(ctx, userName, password));
            ctx.writeAndFlush(String.format("[SERVER] - Welcome %s\r", userName));
            router.joinLastChannel(userName);
        } catch (Exception e) {
            ctx.writeAndFlush(e.getMessage());
            ctx.close();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        router.close(userName);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            CommandRequest req = (CommandRequest) msg;
            router.receiveMessage(userName, req);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}