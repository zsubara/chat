package server;

import com.sun.xml.internal.bind.v2.runtime.reflect.opt.Const;
import dao.to.RoomTo;
import org.junit.Assert;
import org.junit.Test;
import server.chat.Entry;
import server.chat.InMemoryChat;
import util.Constants;

import java.util.Collection;
import java.util.List;

public class ChatTest {

    @Test
    public void SubscribeToTopicIncludesUserAsSubscriber(){
        InMemoryChat chat = new InMemoryChat(10, 10);
        String roomName = "room1";
        chat.subscribe(roomName, "foo");
        chat.subscribe(roomName, "bar");

        Collection<String> subscribers =  chat.getSubscribers(roomName);

        Assert.assertEquals(2, subscribers.size());
        Assert.assertTrue(subscribers.contains("foo"));
        Assert.assertTrue(subscribers.contains("bar"));
    }

    @Test
    public void UnSubscribeToTopicRemovesUserAsSubscriber(){
        InMemoryChat chat = new InMemoryChat(10, 10);
        String roomName = "room1";
        chat.subscribe(roomName, "foo");

        Collection<String> subscribers =  chat.getSubscribers(roomName);
        Assert.assertEquals(1, subscribers.size());
        chat.unsubscribe(roomName, "foo");

        subscribers =  chat.getSubscribers(roomName);

        Assert.assertFalse(subscribers.contains("foo"));
        Assert.assertEquals(0, subscribers.size());
    }

    @Test
    public void OnUserAdditionIfAlreadyExistsTrowException(){
        InMemoryChat chat = new InMemoryChat(10, 10);
        String roomName = "room1";
        chat.subscribe(roomName, "foo");

        try {
            chat.subscribe(roomName, "foo");
            Assert.fail();
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage().equals(Constants.USER_ALREADY_JOINED));
        }
    }

    @Test
    public void RoomHistoryEntryListGetsCappedOnMaxEntriesAchieved(){
        int maxHistorySize = 3;
        InMemoryChat chat = new InMemoryChat(10, maxHistorySize);
        String userName = "user1";
        String roomName = "room1";
        chat.addToHistory(userName, roomName, "msg1");
        chat.addToHistory(userName, roomName, "msg2");
        chat.addToHistory(userName, roomName, "msg3");

        List<Entry> entries = chat.getHistory(roomName);
        Assert.assertEquals(3, entries.size());

        chat.addToHistory(userName, roomName, "msg4");

        entries = chat.getHistory(roomName);
        Assert.assertEquals(maxHistorySize, entries.size());

        Entry entry = entries.get(0);
        Assert.assertTrue(entry.getMessage().equals("msg2"));
    }
}