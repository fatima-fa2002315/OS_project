import java.net.*;
import java.util.*;
import java.io.*;

public class Player implements Runnable {
    private final Socket sock;
    private Scanner in;
    private PrintStream out;

    private Game game;

    public Player(Socket sock) throws IOException {
        this.sock = sock;
        this.in = new Scanner(sock.getInputStream(), "UTF-8");
        this.out = new PrintStream(sock.getOutputStream(), true, "UTF-8");
        this.game = null;
    }

    public void run() {
    }
}
