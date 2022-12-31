import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class ChatServer implements Runnable {
    private static ArrayList<Connection> connections;
    private boolean done;
    private Map<String, LocalTime> time;
    private ExecutorService pool;
    private static int count;
    protected static List<String> activePingu;
    private static List<String> str;
    private ServerSocket server;
    private int limit;

    public ChatServer() {
        str = new ArrayList<>();
        connections = new ArrayList<>();
        done = false;
        time = new HashMap<>();
        limit = 0;
        activePingu = new ArrayList<>();
        count = 0;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(3000);
            pool = Executors.newCachedThreadPool();
            while (!done) {
                limit++;
                if (limit > 50)
                    break;
                Socket client = server.accept();
                Connection connection = new Connection(client);
                connections.add(connection);
                pool.execute(connection);
            }
        } catch (IOException e) {
            e.getMessage();
        }
    }


    public void broadCast(String message) {
        connections.stream().filter(i -> i.name != null & activePingu.contains(i.name)).forEach(i -> i.sendMessage(message));
    }

    class Connection implements Runnable {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String name;

        private ArrayList<String> names;
        private String[] facts;

        public Connection(Socket client) {
            this.client = client;
            this.names = new ArrayList<>();
            facts = new String[]{"Penguins don’t have teeth", "There are 18 species of penguin"
                  , "The largest living penguin is the emperor penguin", "A group of penguins in the water is called a raft, and on land, that group is called a waddle",
                    " Penguins cannot fly", "Penguin populations are declining"};
        }

        @Override
        public void run() {
            try {
                System.out.println(LocalTime.now() + ": Server is waiting on port 3000");
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("please enter a nickname");
                name = in.readLine();
                time.put(name, LocalTime.now());
                out.println(LocalTime.now() + ": Connection accepted localhost/127.0.0.1:3000\n");
                out.println(name + " wish us all the best");
                String message;
                message = in.readLine();

                if (!message.equals("LOGOUT") && message != null) {
                    broadCast(LocalTime.now() + " " + name + ": " + message);
                }

                out.println("\n" + "Hello.! Welcome to the chatroom.\nInstruction \n. Simply type the message to send broadcast to all active clients"
                        + "\n. Type '@username<space>your message' without quotes to send message to desired client"
                        + "\n. Type 'WHOIS' without quotes to see list of active clients" +
                        "\n. Type 'LOGOUT' without quotes to log off from server"
                        + "\n. Type 'PENGU' without quotes to request a random penguin fact");
                System.out.println(LocalTime.now() + "  *** " + name + " has joined the chat room. ***");
                broadCast(LocalTime.now() + "  *** " + name + " has joined the chat room. ***");

                names.add(name);
                activePingu.add(name);
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("@")) {
                        String sub = message.substring(1);
                        String messages[] = sub.split(" ");
                        if (connections.contains(messages[0]))
                        connections.stream().filter(i -> i.name.equals(messages[0])).forEach(i -> i.sendMessage(LocalTime.now() + " " + this.name + ": @" + sub));
                        else
                            sendMessage("wrong person");

                    } else if (message.equals("WHOIS")) {
                        sendMessage("Lists of the users connected at " + LocalTime.now() + "\n");
                        time.forEach((i, e) -> {
                            count++;
                            str.add(count + ") " + i + " since " + e);
                        });
                        str.forEach(this::sendMessage);
                        str = new ArrayList<>();
                        count = 0;

                    } else if (message.equals("LOGOUT")) {
                        names.remove(name);
                        time.remove(name);
                        broadCast(name + " has left the chatroom.");
                        System.out.println(name + " has left the chatroom.");
                        shutDown();
                    } else if (message.equals("PINGU")) {
                        Random random = new Random();
                        broadCast(facts[random.nextInt(facts.length)]);
                    } else {
                        broadCast(LocalTime.now() + " " + name + ": " + message);
                        System.out.println(LocalTime.now() + " " + name + ": " + message);
                    }
                }
            } catch (IOException e) {
                e.getMessage();
            }
        }

        public void sendMessage(String message) {
            out.println(message);
        }

        public void shutDown() throws IOException {
            in.close();
            out.close();
            if (!client.isClosed()) {
                client.close();
            }
        }
    }

    public static void main(String[] args) {
        ChatServer main = new ChatServer();
        main.run();
    }
}