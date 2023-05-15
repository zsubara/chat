package server;

import dao.to.UserTo;
import io.netty.channel.ChannelHandlerContext;
import org.junit.Assert;
import org.junit.Test;
import server.chat.Chat;
import server.command.ChatCommand;
import server.session.SessionRepository;
import util.Constants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChatCommandTest {

    @Test
    public void OnChatUserJoinedUserGetsSubscribedToRoom() {
        Chat chat = mock(Chat.class);
        SessionRepository repo = mock(SessionRepository.class);
        ChatCommand cmd = new ChatCommand(chat, repo);

        ChannelHandlerContext ctx = mock(ChannelHandlerContext.class);
        UserTo user = new UserTo(ctx, "fooUser", "fooPasword");

        Map<String, BiConsumer<UserTo, String[]>> handlers = cmd.handlers();
        String[] args = {"room1"};
        handlers.get(Constants.COMMAND_JOIN).accept(user, args);

        verify(chat, times(1)).subscribe(anyString(), anyString());
        verify(ctx, times(1)).writeAndFlush(anyString());
        Assert.assertTrue(user.getRoom().getName().equals("topic"));
    }

    @Test
    public void OnChatUserLeaveUserGetsUnSubscribedAndDisconnects() {
        Chat chat = mock(Chat.class);
        SessionRepository repo = mock(SessionRepository.class);
        ChatCommand cmd = new ChatCommand(chat, repo);

        UserTo user = mock(UserTo.class);
        when(user.hasRoom()).thenReturn(true);

        Map<String, BiConsumer<UserTo, String[]>> handlers = cmd.handlers();
        String[] args = {"room1"};
        handlers.get(Constants.COMMAND_JOIN).accept(user, args);

        String[] leaveArgs = {};
        handlers.get(Constants.COMMAND_LEAVE).accept(user, leaveArgs);

        verify(chat, times(1)).unsubscribe(anyString(), anyString());
        verify(user, times(2)).send(anyString()); //Joined and Leaved
        verify(user, times(1)).terminate();
    }

    @Test
    public void UsersCommandReturnsRoomUsers() {
        Chat chat = mock(Chat.class);
        Collection<String> users = new ArrayList<>();
        users.add("foo");

        when(chat.getSubscribers(anyString())).thenReturn(users);

        SessionRepository repo = mock(SessionRepository.class);
        ChatCommand cmd = new ChatCommand(chat, repo);

        UserTo user = mock(UserTo.class);
        when(user.hasRoom()).thenReturn(true);
        when(user.getName()).thenReturn("foo");
        when(user.getRoom().getName()).thenReturn("room1");

        Map<String, BiConsumer<UserTo, String[]>> handlers = cmd.handlers();
        String[] args = {"room1"};
        handlers.get(Constants.COMMAND_JOIN).accept(user, args);
        verify(user, times(1)).send("Joined room");

        handlers.get(Constants.COMMAND_USERS).accept(user, args);

        verify(user, times(1)).send("Users in channel room1");
        verify(user, times(1)).send(" -foo");
    }

    @Test
    public void OnPublishCommandSendMessageToAllRoomUsers() {
        Chat chat = mock(Chat.class);
        Collection<String> users = new ArrayList<>();
        users.add("foo");
        users.add("bar");
        when(chat.getSubscribers(anyString())).thenReturn(users);

        UserTo user1 = mock(UserTo.class);
        when(user1.hasRoom()).thenReturn(true);
        when(user1.getName()).thenReturn("foo");
        when(user1.getRoom().getName()).thenReturn("topic");

        UserTo user2 = mock(UserTo.class);
        when(user2.hasRoom()).thenReturn(true);
        when(user2.getName()).thenReturn("bar");
        when(user2.getRoom().getName()).thenReturn("topic");

        SessionRepository repo = mock(SessionRepository.class);
        when(repo.get("foo")).thenReturn(user1);
        when(repo.get("bar")).thenReturn(user2);

        ChatCommand cmd = new ChatCommand(chat, repo);

        Map<String, BiConsumer<UserTo, String[]>> handlers = cmd.handlers();
        String[] args = {"room1"};
        handlers.get(Constants.COMMAND_JOIN).accept(user1, args);
        verify(user1, times(1)).send("Joined room");

        String[] pubArgs = {"hi"};
        handlers.get(Constants.COMMAND_PUBLISH).accept(user1, pubArgs);

        verify(user2, times(1)).send("hi");
    }
}
