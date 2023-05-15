package server.chat;

import dao.impl.RoomDao;
import dao.to.RoomTo;
import util.Constants;

import java.util.Collection;
import java.util.List;

public class InMemoryChat implements Chat {

    private RoomDao rooms = new RoomDao();
    private Integer maxUsersByRoom;
    private Integer historySize;

    public InMemoryChat(Integer maxUsersByRoom, Integer historySize) {
        this.maxUsersByRoom = maxUsersByRoom;
        this.historySize = historySize;
        createRooms();
    }

    private void createRooms() {
        if (rooms.get().isEmpty()) {
            rooms.save(new RoomTo(Constants.CHANNEL_1, maxUsersByRoom, historySize));
            rooms.save(new RoomTo(Constants.CHANNEL_2, maxUsersByRoom, historySize));
            rooms.save(new RoomTo(Constants.CHANNEL_3, maxUsersByRoom, historySize));
        }
    }

    public synchronized void subscribe(String name, String userName) {

        RoomTo room = rooms.get(name);
        if (room == null)
            throw new RuntimeException(Constants.NO_ROOM);
        if (room.hasUser(userName)) {
            throw new RuntimeException(Constants.USER_ALREADY_JOINED);
        }

        room.addUser(userName);
    }

    public synchronized void unsubscribe(String name, String userName) {
        rooms.get(name).removeUser(userName);
    }

    public synchronized Collection<String> getSubscribers(String name) {
        return rooms.get(name).subscribers();
    }

    public void addToHistory(String userName, String name, String msg) {
        rooms.get(name).addToHistory(userName, msg);
    }

    public List<Entry> getHistory(String name) {
        return rooms.get(name).getHistory();
    }

    public RoomTo getRoom(String name) {
        return rooms.get(name);
    }

    public List<RoomTo> getRooms() {
        return rooms.get();
    }
}
