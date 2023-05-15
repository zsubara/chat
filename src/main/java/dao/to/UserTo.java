package dao.to;

import io.netty.channel.ChannelHandlerContext;

public class UserTo {

    private ChannelHandlerContext ctx;
    private volatile String name;

    private volatile String password;

    private volatile RoomTo roomTo;

    public UserTo(ChannelHandlerContext ctx, String name, String password) {
        this.ctx = ctx;
        this.name = name;
        this.password = password;
    }

    public void send(String msg) {
        ctx.writeAndFlush(msg);
    }

    public void terminate() {
        ctx.close();
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
