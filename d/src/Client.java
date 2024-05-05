package OS;

import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    private Socket sock;
    private Scanner in;
    private PrintStream out;
    private Scanner kb;

    public Client(String ip, int port) throws Exception {
        this.sock = new Socket(ip, port);
        this.in = new Scanner(sock.getInputStream(), "UTF-8");
        this.out = new PrintStream(sock.getOutputStream(), true, "UTF-8");
        this.kb = new Scanner(System.in);
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java Client <IP address> <port>");
            return;
        }

        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            System.out.println("CLIENT CONNECTED TO SERVER \n");
            client.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        while (true) {
            System.out.println("\n1. Have existing ticket");
            System.out.println("2. Get new ticket");
            System.out.println("3. Exit");
            String choice = kb.next();
            if (choice.equals("1")) {
                System.out.print("Enter ticket number: ");
                int ticket = kb.nextInt();
                out.println("ID " + ticket);
                String response = in.nextLine();
                if (response.startsWith("FAIL")) {
                    System.out.println("Invalid ticket number");
                } else {
                    System.out.println("Login successful!");
                    System.out.println(response + "\n");
                    break;
                }
            } else if (choice.equals("2")) {
                System.out.print("Enter nickname: ");
                String name = kb.next();
                out.println("NAME " + name);
                String response = in.nextLine();
                System.out.println("Login successful!");
                System.out.println(response + "\n");
                break;
            } else if (choice.equals("3")) {
                System.out.println("Exiting...");
                out.close();
                in.close();
                sock.close();
                System.exit(0);
            } else {
                System.out.println("Invalid choice");
            }
        }

        mainLoop();
    }

    private void mainLoop() throws Exception {
        while (true) {
            System.out.println("\n\n1. Get list of games");
            System.out.println("2. Get list of players");
            System.out.println("3. Join a game");
            System.out.println("4. Create a new game");
            System.out.println("5. Logout");
            System.out.println("6. Exit");

            String choice = kb.next();

            switch (choice) {
                case "1":
                case "2":
                    out.println(choice.equals("1") ? "GAMES" : "PLAYERS");
                    printServerResponse();
                    break;
                case "3":
                    joinGame();
                    break;
                case "4":
                    createGame();
                    break;
                case "5":
                    logout();
                    break;
                case "6":
                    System.out.println("Exiting...");
                    out.close();
                    in.close();
                    sock.close();
                    System.exit(0);
                    break;
                default:
                    System.out.println("Invalid option");
            }
        }
    }

    private void joinGame() throws Exception {
        System.out.print("Enter game number to join: ");
        int game = kb.nextInt();
        out.println("JOIN " + game);
        handleGameResponse();
    }

    private void createGame() throws Exception {
        System.out.print("Enter the game name: ");
        String name = kb.next();
        out.println("CREATE " + name);
        handleGameResponse();
    }

    private void handleGameResponse() throws Exception {
        String response = in.nextLine();
        if (response.startsWith("FAIL")) {
            System.out.println(response);
        } else {
            System.out.println(response);
            printServerResponse();
            System.out.println("Waiting for game to start...\n");
            playGame();
        }
    }

    private void printServerResponse() throws Exception {
        String line;
        while (true) {
            line = in.nextLine();
            System.out.println(line);
            if (line.length() == 0)
                break;
        }
    }

    private void logout() throws Exception {
        out.println("LEAVE");
        sock.close();
        System.exit(0);
    }

    private void playGame() throws Exception {
        while (true) {
            String action = in.nextLine();
            if (action.startsWith("GAME")) { // GAME-OVER
                System.out.println("GAME OVER");
                return;
            }
            if (action.startsWith("ROUND")) {
                System.out.print(action + " - Enter your number: ");
                int num = kb.nextInt();
                out.println("PLAY " + num);
            } else {
                System.out.println(action);
            }
        }
    }
}


