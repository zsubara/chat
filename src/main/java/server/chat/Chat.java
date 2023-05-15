package server.chat;

import dao.to.RoomTo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Chat {

    void subscribe(String name, String userName);

    void unsubscribe(String name, String userName);

    Collection<String> getSubscribers(String name);

    void addToHistory(String userName, String name, String msg);

    List<Entry> getHistory(String name);
    RoomTo getRoom(String name);
    List<RoomTo> getRooms();
}
