# Chat - simple text based chat based on Netty’s framework

## Specification

Chat is a channel based communication tool, supports multiple channels for users communications. 
There is one restriction though: a user can only join one channel at a time, when they join another they leave their current channel. 
Moreover, the same user can auth twice from different devices, and on both them they should be able to
receive messages.

ChatServer should handle the following commands:

- **/login** <name> <password> 
  If the user doesn’t exists, create profile else login, after login join to last connected channel (use join logic, if client’s limit exceeded, keep connected, but without active
channel),
- **/join channel**
  Try to join a channel (max 10 active clients per channel is needed). If client's limit exceeded - send error, otherwise join channel and send last N messages of activity,
- **/leave**
  Leave current channel,
- **/disconnect**
  Close connection to server,
- **/list**
  Send list of channels,
- **/users**
  Send list of unique users in current channel,
- **text message terminated with CR**
  Sends message.

## Remarks
- Data is stored in memory, no persistence to database needed, but extendable to it, 
- there are three rooms created on start (channel1, channel2, channel3),
- check the server via a simple text based telnet command.
  
## Compilation and execution
- The project code is an Intellij maven project.
- Import the project to Intellij and execute a maven update on the project once imported.
- To execute it, the Main.java class must be started (Run/Debug). Once this is done, we can connect using telnet, for example: "$ telnet localhost 8080".
  
## Examples
Client A
```
 telnet localhost 8080

 /login foo pass

 /join channel1

 hello
```
  
Client B
```
 telnet localhost 8080

 /login bar pass

 /join channel1

 hi
```
