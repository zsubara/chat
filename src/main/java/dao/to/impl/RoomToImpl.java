package dao.to.impl;

import dao.to.RoomTo;
import io.netty.channel.group.ChannelGroup;
import util.Constants;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

public class RoomToImpl implements RoomTo {
    private Long id;
    private ChannelGroup channelGroup;
    private String name;
    private ConcurrentLinkedDeque<String> roomHistory;
    private int activeUsersCounter;

    private static AtomicLong ID_GENERATOR = new AtomicLong(1000);

    public RoomToImpl(ChannelGroup channelGroup, String name) {
        this.id = ID_GENERATOR.getAndIncrement();
        this.channelGroup = channelGroup;
        this.name = name;
        this.roomHistory = new ConcurrentLinkedDeque<>();
    }

    public void incrementActiveUsersCounter() {
        this.activeUsersCounter++;
    }

    public void decrementActiveUsersCounter() {
        this.activeUsersCounter--;
    }

    public void saveMessage(String finalMessage) {
        if (roomHistory.size() < Constants.MESSAGE_LIMIT) {
            roomHistory.add(finalMessage);
        } else if (roomHistory.size() >= Constants.MESSAGE_LIMIT) {
            roomHistory.removeFirst();
            roomHistory.add(finalMessage);
        }
    }

    @Override
    public Long getRoomId() {
        return this.id;
    }

    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

    public String getName() {
        return name;
    }

    public ConcurrentLinkedDeque<String> getRoomHistory() {
        return roomHistory;
    }

    public int getActiveUsersCounter() {
        return activeUsersCounter;
    }

    public void setChannelGroup(ChannelGroup channelGroup) {
        this.channelGroup = channelGroup;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoomHistory(ConcurrentLinkedDeque<String> roomHistory) {
        this.roomHistory = roomHistory;
    }

    public void setActiveUsersCounter(int activeUsersCounter) {
        this.activeUsersCounter = activeUsersCounter;
    }

    @Override
    public String toString() {
        return "Channel [Name=" + name + "]";
    }
}
