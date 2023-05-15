package server.handler;

import command.CommandRequest;
import dao.impl.UserDao;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import server.Router;
import server.auth.Authenticator;
import server.session.SessionRepository;
import util.Constants;

public class AuthHandler extends ChannelInboundHandlerAdapter {

    public static String NOT_AUTH_ERROR = "Not authorized, you must log in";
    public static String INVALID_REQUEST = "Invalid Request";
    public static String USER_ALREADY_EXISTS = "User already exists";
    public static String INVALID_CREDENTIALS = "Invalid Credentials";

    private Authenticator authenticator;
    private Boolean authorized = false;
    private SessionRepository repository;
    private Router router;

    public AuthHandler(Authenticator authenticator, SessionRepository repository, Router router) {
        this.authenticator = authenticator;
        this.repository = repository;
        this.router = router;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        CommandRequest request = (CommandRequest) msg;

        try {
            if (authorized) {
                ctx.fireChannelRead(msg);
                return;
            }

            if (!request.getCmd().equals(Constants.COMMAND_LOGIN)) {
                ctx.writeAndFlush("[SERVER] - " + NOT_AUTH_ERROR + "\r");
                return;
            }

            if (request.getArguments().length != 2) {
                ctx.writeAndFlush("[SERVER] - " + INVALID_REQUEST + "\r");
                return;
            }

            String userName = request.getArguments()[0];
            String password = request.getArguments()[1];

            if (repository.contains(userName)) {
                ctx.writeAndFlush("[SERVER] - " + USER_ALREADY_EXISTS + "\r");
                ctx.close();

                return;
            }

            if (!authenticator.validateCredentials(repository.get(userName), password)) {
                ctx.writeAndFlush("[SERVER] - " + INVALID_CREDENTIALS + "\r");

                return;
            }

            authorized = true;
            ctx.pipeline().addLast(new ClientHandler(userName, password, router));
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