import java.util.*;
import java.util.concurrent.*;
import java.io.IOException;
import java.net.*;

class Server {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    private Map<Integer,String> ticket;

    private static int nextTicket = 1;

    private Map<Integer,Player> currentPlayers;

    private Map<Integer, Game> currentGames;

    public Server() throws IOException {
        ticket = new HashMap<>();
        currentPlayers = new HashMap<>();
        currentGames = new HashMap<>();

        serverSocket = new ServerSocket(13337);
        pool = Executors.newFixedThreadPool(50);

        while(true) {
            try {
                pool.execute(new Player(serverSocket.accept()));
            } catch(IOException e) {
            }
        }
    }

    public synchronized int getTicket() {
        return nextTicket ++;
    }
}

