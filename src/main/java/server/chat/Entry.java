package server.chat;

import java.time.LocalTime;

public class Entry {

    private String userName;
    private String message;
    private LocalTime time;

    public Entry(String userName, String message) {
        this.userName = userName;
        this.message = message;
        this.time = LocalTime.now();
    }

    public LocalTime getTime() {
        return time;
    }

    public String getUserName() {
        return userName;
    }

    public String getMessage() {
        return message;
    }
}
