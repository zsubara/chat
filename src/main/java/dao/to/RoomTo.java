package dao.to;

import server.chat.Entry;
import util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class RoomTo {
    private Integer maxUsers;
    private Integer historySize;
    private String name;
    private volatile List<Entry> entries = Collections.synchronizedList(new ArrayList<>());
    private Collection<String> users = Collections.synchronizedList(new ArrayList<>());

    public RoomTo(String name, Integer maxUsers, Integer historySize) {
        this.name = name;
        this.maxUsers = maxUsers;
        this.historySize = historySize;
    }

    public String getName() {
        return name;
    }

    public synchronized void addToHistory(String userName, String msg) {
        entries.add(new Entry(userName, msg));

        if (entries.size() > historySize) {
            entries.remove(0);
        }
    }

    public synchronized List<Entry> getHistory() {
        return entries;
    }

    public synchronized void addUser(String userName) {

        if (users.size() >= maxUsers) {
            throw new RuntimeException(Constants.MAX_USERS_ERROR);
        }

        users.add(userName);
    }

    public void removeUser(String userName) {
        users.remove(userName);
    }

    public Boolean hasUser(String userName) {
        return users.contains(userName);
    }

    public Collection<String> subscribers() {
        return users;
    }

    @Override
    public String toString() {
        return "Channel [Name=" + name + "]";
    }
}
