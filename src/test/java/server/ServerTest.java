package server;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

public class ServerTest {

    private static ChatServer server = new ChatServer("localhost", 8080);

    @BeforeClass
    public static void setup() {
        server.run();
    }

    @Test
    public void EndToEndTest() {

        try {
            Thread.sleep(500L);
            // Client A Open Session
            Client clientA = new Client();
            clientA.write("/login foo pass\n");
            String resA = clientA.readLine();
            Assert.assertTrue(resA.equals("[SERVER] - Welcome foo"));

            // Client A Joins channel
            clientA.write("/join channel1\n");
            resA = clientA.readLine();
            Assert.assertTrue(resA.equals("[SERVER] - Joined room channel1"));

            // Client B Open Session
            Client clientB = new Client();
            clientB.write("/login bar pass\n");
            String resB = clientB.readLine();
            Assert.assertTrue(resB.equals("[SERVER] - Welcome bar"));

            // Client B Joins channel
            clientB.write("/join channel1\n");
            resB = clientB.readLine();
            Assert.assertTrue(resB.equals("[SERVER] - Joined room channel1"));

            // Client A publish in room
            clientA.write("Hi There\n");

            // Client B must receive published message
            String pub = clientB.readLine();
            Assert.assertTrue(pub.contains("Hi There"));

            // Check users in room
            clientB.write("/users\n");
            resB = clientB.readLine();
            Assert.assertTrue(resB.contains("Users in channel channel1"));

            // As Order Result is not guaranteed (Iterates over users Collection), we store it in a list
            List<String> users = new ArrayList<>();
            resB = clientB.readLine();
            users.add(resB.trim());

            resB = clientB.readLine();
            users.add(resB.trim());

            // Assert foo & bar exists as users
            Assert.assertTrue(users.contains("foo"));
            Assert.assertTrue(users.contains("bar"));

            // Client A leaves room
            clientA.write("/leave\n");

            // Users show just ClientB user
            clientB.write("/users\n");
            clientB.readLine();

            users = new ArrayList<>();
            resB = clientB.readLine();
            resB = clientB.readLine();
            users.add(resB.trim());

            Assert.assertFalse(users.contains("foo"));
            Assert.assertTrue(users.contains("bar"));

            // disconnect
            clientA.close();
            clientB.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Test
    public void EndToEndTestWithMultipleDevicesOnSameUser() {

        try {
            Thread.sleep(500L);
            // Client A Open Session
            Client clientA = new Client();
            clientA.write("/login foo pass\n");
            String resA = clientA.readLine();
            Assert.assertTrue(resA.equals("[SERVER] - Welcome foo"));

            // Client A Joins channel
            clientA.write("/join channel1\n");
            resA = clientA.readLine();
            Assert.assertTrue(resA.equals("[SERVER] - Joined room channel1"));

            // Client B Open Session
            Client clientB = new Client();
            clientB.write("/login bar pass\n");
            String resB = clientB.readLine();
            Assert.assertTrue(resB.equals("[SERVER] - Welcome bar"));

            // Client B Joins channel
            clientB.write("/join channel1\n");
            resB = clientB.readLine();
            resA = clientA.readLine();
            Assert.assertTrue(resB.equals("[SERVER] - Joined room channel1"));
            Assert.assertTrue(resA.equals("[SERVER] - bar joined your channel"));

            // Client C Open Session with Client A credentials
            Client clientC = new Client();
            clientC.write("/login foo pass\n");
            String resC = clientC.readLine();
            Assert.assertTrue(resC.equals("[SERVER] - Welcome foo"));
            resC = clientC.readLine();
            Assert.assertTrue(resC.equals("[SERVER] - Joined room channel1")); // because user from first session is already in this channel

            // Client B publish in room
            clientB.write("Hi There\n");

            // Client A and C must receive published message
            String pub = clientA.readLine();
            Assert.assertTrue(pub.contains("Hi There"));
            pub = clientC.readLine();
            Assert.assertTrue(pub.contains("Hi There"));

            // disconnect
            clientA.close();
            clientB.close();
            clientC.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void EndToEndTestJoinChannelAndDisconnect() {

        try {
            Thread.sleep(500L);

            Client client = new Client();
            client.write("/login foo pass\n");
            String res = client.readLine();
            Assert.assertTrue(res.equals("[SERVER] - Welcome foo"));

            // Joins channel
            client.write("/join channel1\n");
            res = client.readLine();
            Assert.assertTrue(res.equals("[SERVER] - Joined room channel1"));

            // disconnect and login again, should be in the same room as before
            client.write("/disconnect\n");
            Client client1 = new Client();
            client1.write("/login foo pass\n");
            res = client1.readLine(); // welcome
            res = client1.readLine();
            Assert.assertTrue(res.equals("[SERVER] - Joined room channel1"));

            // disconnect
            client.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @AfterClass
    public static void tearDown() {
        server.terminate();
    }

    class Client {

        private BufferedWriter bufferedWriter;
        private BufferedReader bufferedReader;

        Client() throws IOException {
            InetAddress inteAddress = InetAddress.getByName("localhost");
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, 8080);

            // create a socket
            Socket socket = new Socket();

            // this method will block no more than timeout ms.
            int timeoutInMs = 10 * 1000;   // 10 seconds
            socket.connect(socketAddress, timeoutInMs);

            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        }

        public void write(String msg) throws IOException {
            bufferedWriter.write(msg);
            bufferedWriter.flush();

        }

        public String readLine() throws IOException {
            return bufferedReader.readLine();
        }

        public void close() throws IOException{
            bufferedReader.close();
        }
    }
}
