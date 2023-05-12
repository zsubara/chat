package server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ChatServer {

    private final String host;
    private final int port;

    public ChatServer(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        new ChatServer("localhost", 8080).run();
    }

    public void run() throws Exception {
        // accepts an incoming connection
        EventLoopGroup group = new NioEventLoopGroup(); // handles I/O operation
        EventLoopGroup workerGroup = new NioEventLoopGroup(); // handles the traffic of the accepted connection

        try {
            // server configuration
            ServerBootstrap bootstrap = new ServerBootstrap()
                .group(group, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChatServerInitializer());

            Channel channel = bootstrap.bind(host, port).sync().channel();
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

            while (true) {
                channel.write(in.readLine() + "\r\n");
            }
        } finally {
            group.shutdownGracefully();
            workerGroup.shutdownGracefully();

            group.terminationFuture().sync();
            workerGroup.terminationFuture().sync();
        }
    }
}