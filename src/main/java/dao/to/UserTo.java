package dao.to;

import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.List;

public class UserTo {

    private volatile List<ChannelHandlerContext> ctxs = new ArrayList<>();
    private volatile ChannelHandlerContext senderChannel;
    private volatile String name;

    private volatile String password;

    private volatile RoomTo roomTo;
    private volatile boolean newJoiner;

    public UserTo(ChannelHandlerContext ctx, String name, String password) {
        this.ctxs.add(ctx);
        this.name = name;
        this.password = password;
        this.senderChannel = ctx;
    }

    public void addCtx(ChannelHandlerContext ctx) {
        ctxs.add(ctx);
    }

    public void setSenderContext(ChannelHandlerContext senderChannel) {
        this.senderChannel = senderChannel;
    }

    public void send(String msg) {
        for (ChannelHandlerContext ctx : ctxs)
            ctx.writeAndFlush(msg);
    }

    public void sendToSameUser(String msg) {
        for (ChannelHandlerContext ctx : ctxs)
            if (ctx != senderChannel)
                ctx.writeAndFlush(msg);
    }

    public void sendMeOnly(String msg) {
        for (ChannelHandlerContext ctx : ctxs)
            if (ctx == senderChannel)
                ctx.writeAndFlush(msg);
    }

    public boolean isNewJoiner() {
        return newJoiner;
    }

    public void setNewJoiner(boolean newJoiner) {
        this.newJoiner = newJoiner;
    }

    public void terminate() {
        ctxs.remove(senderChannel);
        senderChannel.close();
    }

    public boolean stillActive() {
        return ctxs.size() > 0;
    }

    public String getName() {
        return name;
    }

    public RoomTo getRoom() {
        return roomTo;
    }

    public String getPassword() {
        return password;
    }

    public void setRoom(RoomTo roomTo) {
        this.roomTo = roomTo;
    }

    public Boolean hasRoom() {
        return roomTo != null;
    }

    @Override
    public String toString() {
        return "User [Name=" + name + ", Room Name=" + roomTo.getName() + "]";
    }
}
