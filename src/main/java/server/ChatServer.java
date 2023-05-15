package server;

import com.sun.corba.se.impl.activation.CommandHandler;
import command.CommandExecutor;
import command.Commander;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.LineEncoder;
import io.netty.handler.codec.string.LineSeparator;
import io.netty.util.CharsetUtil;
import server.auth.Authenticator;
import server.chat.InMemoryChat;
import executor.Executor;
import executor.Scheduler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import server.command.ChatCommand;
import server.encoder.CommandDecoder;
import server.handler.AuthHandler;
import server.session.InMemoryRepository;
import server.session.SessionRepository;
import util.Constants;

import java.net.InetSocketAddress;

public class ChatServer {

    private final String host;
    private final int port;
    public static String EXECUTOR_WORKER_NAME = "serverExecutor";
    public static Integer EXECUTOR_WORKER_THREADS = 4;
    public static Integer ACCEPTOR_THREADS = 2;
    public static Integer HANDLER_THREADS = 4;

    private EventLoopGroup acceptorGroup; // handles I/O operation
    private EventLoopGroup handlerGroup; // handles the traffic of the accepted connection
    private ChannelFuture channelFuture;
    private Router router;
    private SessionRepository repository;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;

        InMemoryChat broker = new InMemoryChat(Constants.MAX_USERS_BY_ROOM, Constants.HISTORY_SIZE);
        repository = new InMemoryRepository();
        ChatCommand cmds = new ChatCommand(broker, repository);
        Commander commandExecutor = new CommandExecutor();
        commandExecutor.register(cmds);

        Executor executor = new Scheduler(EXECUTOR_WORKER_NAME, EXECUTOR_WORKER_THREADS);
        router = new Router(broker, commandExecutor, executor, repository);
    }

    public void run() {
        acceptorGroup = new NioEventLoopGroup(ACCEPTOR_THREADS);
        handlerGroup = new NioEventLoopGroup(HANDLER_THREADS);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(acceptorGroup, handlerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress("localhost", port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel socketChannel) {

                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast("frameDecoder", new DelimiterBasedFrameDecoder(Integer.MAX_VALUE, Delimiters.lineDelimiter()));
                            pipeline.addLast("stringDecoder", new CommandDecoder(CharsetUtil.UTF_8));
                            pipeline.addLast("lineEncoder", new LineEncoder(LineSeparator.UNIX, CharsetUtil.UTF_8));
                            pipeline.addLast("authHandler", new AuthHandler(new Authenticator(), repository, router));
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true); // keep their connections open with keepalive packets.

            channelFuture = serverBootstrap.bind().sync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void terminate() {
        try {
            acceptorGroup.shutdownGracefully().sync();
            handlerGroup.shutdownGracefully().sync();
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            // log
        }
    }
}