package server;

import dao.impl.RoomDao;
import dao.impl.UserDao;
import dao.to.RoomTo;
import dao.to.UserTo;
import dao.to.impl.RoomToImpl;
import dao.to.impl.UserToImpl;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.internal.StringUtil;
import util.Constants;

public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    private static UserDao chatUsers = new UserDao();
    private static RoomDao chatRooms = new RoomDao();

    public ChatServerHandler() {
        super();
        // create default rooms
        if (chatRooms.get().isEmpty()) {
            chatRooms.save(new RoomToImpl(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
                    Constants.NO_ACTIVE_CHANNEL));
            chatRooms.save(new RoomToImpl(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
                    Constants.CHANNEL_1));
            chatRooms.save(new RoomToImpl(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
                    Constants.CHANNEL_2));
            chatRooms.save(new RoomToImpl(new DefaultChannelGroup(GlobalEventExecutor.INSTANCE),
                    Constants.CHANNEL_3));
        }
    }

    /**
     * Keep track of our active channels.
     *
     * This method will be called when a new client connects to the server.
     *
     * @param ctx context
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {

    }

    /**
     * Keep track of when a client disconnect from the server.
     *
     * This method will be called when a new client disconnect from the server
     *
     * @param ctx context
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        UserToImpl incoming = (UserToImpl) chatUsers.get(ctx.channel());
        if (incoming != null) {
            RoomToImpl incomingChannelGroup = incoming.getRoomTo();

            if (!incomingChannelGroup.getName().equals(Constants.NO_ACTIVE_CHANNEL)) {
                for (Channel channel : incomingChannelGroup.getChannelGroup()) {
                    channel.writeAndFlush("[SERVER] - " + incoming.getName() + " has left the server!\r\n");
                }
                incomingChannelGroup.decrementActiveUsersCounter();
            }

            incomingChannelGroup.getChannelGroup().remove(incoming.getChannel());
            chatUsers.get();
        }
    }

    /**
     * This method is called with the received message, whenever new data is
     * received from a client.
     *
     * @param ctx context
     * @param msg input
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {

        UserToImpl incoming = (UserToImpl) chatUsers.get(ctx.channel());
        if (incoming == null) {
            incoming = new UserToImpl(
                    ctx.channel(),
                    null,
                    null,
                    chatRooms.get(Constants.NO_ACTIVE_CHANNEL));
        }
        RoomToImpl incomingChannelGroup = incoming.getRoomTo();
        msg = msg.trim();
        if (!StringUtil.isNullOrEmpty(msg)) {
            String command = messageHasCommand(msg);
            switch (command != null ? command : msg) {
                case Constants.COMMAND_DISCONNECT:
                    incoming.getChannel().close();
                    break;

                case Constants.COMMAND_LOGIN:
                    String[] data = msg.replaceFirst(command, "").trim().split(" ");

                    if (data.length == 2) {
                        String name = data[0];
                        String password = data[1];
                        UserToImpl user = (UserToImpl) chatUsers.findUserByName(name);
                        if (user != null) {
                            if (user.getPassword().equals(password)) {
                                if (!user.getRoomTo().getName().equals(Constants.NO_ACTIVE_CHANNEL)) {
                                    user.setChannel(ctx.channel());
                                    String theLatestRoomName = user.getRoomTo().getName();
                                    user.setRoomTo(chatRooms.get(Constants.NO_ACTIVE_CHANNEL));
                                    channelRead0(ctx, Constants.COMMAND_JOIN + " " + theLatestRoomName);
                                }
                            }
                            else {
                                incoming.getChannel().writeAndFlush(
                                        "[SERVER] - Incorrect password.\r\n");
                            }
                        }
                        else {
                            incoming.setName(name);
                            incoming.setPassword(password);
                            chatUsers.save(incoming);

                            incoming.getChannel().writeAndFlush(
                                    "[SERVER] - New user successfully created <" + name + ">.\r\n");
                        }
                    } else {
                        incoming.getChannel().writeAndFlush(
                                "[SERVER] - ERROR, empty or invalid parameters, the command format is '/login <namse> <password>'.\r\n");
                    }
                    break;

                case Constants.COMMAND_LIST:
                    incoming.getChannel().writeAndFlush("[SERVER] - List of active channels ('*' is where you are):\r\n");
                    for (RoomTo roomTo : chatRooms.get()) {
                        if (!(((RoomToImpl) roomTo).getName().equals(Constants.NO_ACTIVE_CHANNEL)))
                            incoming.getChannel().writeAndFlush(((RoomToImpl) roomTo).getName()
                                + (incoming.getRoomTo().getName().equals(((RoomToImpl) roomTo).getName()) ? " *" : "") + "\r\n");
                    }
                    break;

                case Constants.COMMAND_USERS:
                    incoming.getChannel().writeAndFlush("[SERVER] - List of active users:\r\n");
                    for (UserTo userTo : chatUsers.get()) {
                        incoming.getChannel().writeAndFlush("- " + ((UserToImpl) userTo).getName() + "\r\n");
                    }
                    break;

                case Constants.COMMAND_JOIN:
                    String roomName = msg.replaceFirst(command, "").trim();
                    //joinChannel(incoming, roomName);
                    if (!StringUtil.isNullOrEmpty(roomName) && !incomingChannelGroup.getName().equals(roomName)) {
                        if (isUserLogged(ctx.channel(), incoming.getName())) {
                            RoomToImpl channel = (RoomToImpl) chatRooms.get(roomName);
                            if (channel != null) {
                                if (channel.getActiveUsersCounter() < Constants.CLIENT_LIMIT) {
                                    joinGroupHandler(incomingChannelGroup, incoming, roomName);

                                    // Shows history if there's any
                                    if (!incoming.getRoomTo().getRoomHistory().isEmpty()) {
                                        for (String messageHistory : incoming.getRoomTo().getRoomHistory()) {
                                            incoming.getChannel().writeAndFlush(messageHistory);
                                        }
                                    }
                                }
                                else {
                                    incoming.getChannel().writeAndFlush(
                                            "[SERVER] - ERROR, channel full.\r\n");
                                }
                            } else {
                                incoming.getChannel().writeAndFlush(
                                        "[SERVER] - ERROR, channel does not exist, use '/list to get all channels'.\r\n");
                            }
                        }
                    } else {
                        if (StringUtil.isNullOrEmpty(roomName)) {
                            incoming.getChannel().writeAndFlush(
                                    "[SERVER] - ERROR, empty channel name, the command format is '/join <channel>'.\r\n");
                        }

                        if (incomingChannelGroup.getName().equals(roomName)) {
                            incoming.getChannel().writeAndFlush("[SERVER] - You are already in this room.\r\n");
                        }
                    }
                    break;

                case Constants.COMMAND_INVALID:
                    incoming.getChannel().writeAndFlush("[SERVER] - Invalid command.\r\n");
                    break;

                default:
                    if (isUserLogged(ctx.channel(), incoming.getName())) {
                        if (!incomingChannelGroup.getName().equals(Constants.NO_ACTIVE_CHANNEL)) {
                            String finalMessage = "[" + incoming.getName() + "]: " + msg + "\r\n";
                            for (Channel channel : incomingChannelGroup.getChannelGroup()) {
                                if (channel != incoming.getChannel()) {
                                    channel.writeAndFlush(finalMessage);
                                }
                            }
                            incomingChannelGroup.saveMessage(finalMessage);
                        }
                        else {
                            ctx.channel().writeAndFlush("[SERVER] - You are not in any channel. Use /join <channel>.\r\n");
                        }
                    }
                    break;
            }
        }
    }

    private boolean isUserLogged(Channel channel, String userName) {
        if (userName == null) {
            channel.writeAndFlush("[SERVER] - You are not logged. Use /login <name> <password>.\r\n");
            return false;
        }
        return true;
    }
    // Operations to join new group
    private void joinGroupHandler(RoomToImpl incomingChannelGroup, UserToImpl incomingUser, String roomName) {
        if (!incomingChannelGroup.getName().equals(Constants.NO_ACTIVE_CHANNEL)) {
            // delete incoming channel from the channelgroup that is leaving
            incomingChannelGroup.getChannelGroup().remove(incomingUser.getChannel());
            incomingChannelGroup.decrementActiveUsersCounter();

            // Leaving message for other users
            for (Channel channel : incomingChannelGroup.getChannelGroup()) {
                channel.writeAndFlush("[SERVER] - " + incomingUser.getName() + " has left the channel!\r\n");
            }
        }

        // Assign the new room to the user
        incomingUser.setRoomTo((chatRooms.get(roomName)));
        // Add the user channel to the new room channelgroup
        incomingUser.getRoomTo().getChannelGroup().add(incomingUser.getChannel());
        incomingUser.getRoomTo().incrementActiveUsersCounter();
        incomingUser.getChannel().writeAndFlush("[SERVER] - Welcome to <" + incomingUser.getRoomTo().getName()
                + ">, there are " + incomingUser.getRoomTo().getActiveUsersCounter() + " users connected.\r\n");
    }

    private String messageHasCommand(String msg) {
        String command = null;

        if (msg.startsWith(Constants.COMMAND_LOGIN)) {
            command = Constants.COMMAND_LOGIN;
        }
        else if (msg.startsWith(Constants.COMMAND_JOIN)) {
            command = Constants.COMMAND_JOIN;
        }
        else if (msg.startsWith(Constants.COMMAND_LEAVE)) {
            command = Constants.COMMAND_LEAVE;
        }
        else if (msg.startsWith(Constants.COMMAND_DISCONNECT)) {
            command = Constants.COMMAND_DISCONNECT;
        }
        else if (msg.startsWith(Constants.COMMAND_USERS)) {
            command = Constants.COMMAND_USERS;
        }
        else if (msg.startsWith(Constants.COMMAND_LIST)) {
            command = Constants.COMMAND_LIST;
        }
        else if (msg.startsWith(Constants.COMMAND_INVALID)) {
            command = Constants.COMMAND_INVALID;
        }

        return command;
    }

    /**
     * Method is called with a Throwable when an exception was raised by Netty due
     * to an I/O error or by a handler implementation due to the exception thrown
     * while processing events.
     *
     * @param ctx context
     * @param cause reason
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}