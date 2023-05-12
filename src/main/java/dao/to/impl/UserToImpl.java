package dao.to.impl;

import dao.to.RoomTo;
import dao.to.UserTo;

import io.netty.channel.Channel;

import java.util.concurrent.atomic.AtomicLong;

public class UserToImpl implements UserTo {

    private Long id;
    private Channel channel;
    private String name;

    private String password;

    private RoomTo roomTo;

    private static AtomicLong ID_GENERATOR = new AtomicLong(1000);

    public UserToImpl(Channel channel, String name, String password, RoomTo roomTo) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.channel = channel;
        this.name = name;
        this.password = password;
        this.roomTo = roomTo;
    }

    @Override
    public Long getUserId() {
        return this.id;
    }

    public Channel getChannel() {
        return channel;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public RoomToImpl getRoomTo() {
        return (RoomToImpl) roomTo;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRoomTo(RoomTo roomTo) {
        this.roomTo = roomTo;
    }

    @Override
    public String toString() {
        return "User [Name=" + name + ", Room Name=" + ((RoomToImpl) roomTo).getName() + "]";
    }
}
