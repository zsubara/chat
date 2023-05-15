# chat - simple text based chat based on Netty’s framework

Chat is a channel based communication tool, supports multiple channels for users communications. 
There is one restriction though: a user can only join one channel at a time, when they join another they leave their current channel. 
Moreover, the same user can auth twice from different devices, and on both them they should be able to
receive messages.

ChatServer should handle the following commands:

● /login <name> <password>
If the user doesn’t exists, create profile else login, after login join to last connected
channel (use join logic, if client’s limit exceeded, keep connected, but without active
channel).
● /join <channel>
Try to join a channel (max 10 active clients per channel is needed). If client's limit
exceeded - send error, otherwise join channel and send last N messages of activity. ●
/leave
Leave current channel.
● /disconnect
Close connection to server.
● /list
Send list of channels.
● /users
Send list of unique users in current channel.
● <text message terminated with CR>
Sends message
