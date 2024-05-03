import java.util.*;
import java.io.*;
import java.net.*;

public class Client {
    private Socket sock;
    private Scanner in;
    private PrintStream out;
    private Scanner kb;

    public Client(String ip, int port) throws Exception {
        this.sock = new Socket(ip,port);
        this.in = new Scanner(sock.getInputStream(), "UTF-8");
        this.out = new PrintStream(sock.getOutputStream(), true, "UTF-8");
        this.kb = new Scanner(System.in);
    }

    public static void main(String [] args) {
        if(args.length < 2) {
            System.out.println("Usage: java Client <IP address> <port>");
            return;
        }

        try {
            Client client = new Client(args[0], Integer.parseInt(args[1]));
            System.out.println("CONNECTED TO SERVER\n");

            client.start();

        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void start() throws Exception {
        while(true) {
            System.out.println("\n1. Have existing ticket");
            System.out.println("2. Get new ticket");
            System.out.print("? ");
            String s = kb.next();
            if(s.equals("1")) {
                System.out.print("Enter ticket number: ");
                int tick = kb.nextInt();
                out.println("ID " + tick);
                String ans = in.nextLine();
                if(ans.startsWith("FAIL")) {
                    System.out.println("Invalid ticket number");
                } else {
                    System.out.println("Login successful!");
                    System.out.println(ans + "\n");
                    break;
                }
            } else if(s.equals("2")) {
                System.out.print("Enter nickname: ");
                String name = kb.next();
                out.println("NAME " + name);
                String ans = in.nextLine();
                System.out.println("Login successful!");
                System.out.println(ans + "\n");
                break;
            } else {
                System.out.println("Invalid choice");
            }
        }

        // logged in now, fetch all the info from server and print it
        int blankLines = 0;
        while(blankLines < 3) {
            String s = in.nextLine();
            System.out.println(s);
            if(s.length() == 0)
                blankLines ++;
        }

        while(true) {
            System.out.println("\n\n1. Get list of games");
            System.out.println("2. Get list of players");
            System.out.println("3. Join a game");
            System.out.println("4. Create a new game");
            System.out.println("5. Logout");
            System.out.print("? ");

            String s = kb.next();

            if(s.equals("1") || s.equals("2")) {
                if(s.equals("1")) {
                    out.println("GAMES");
                } else {
                    out.println("PLAYERS");
                }

                while(true) {
                    String l = in.nextLine();
                    System.out.println(l);
                    if(l.length() == 0)
                        break;
                }
            } else if(s.equals("3") || s.equals ("4")) {
                if(s.equals("3")) {
                    System.out.print("Enter game number to join: ");
                    int game = kb.nextInt();
                    out.println("JOIN " + game);
                } else {
                    System.out.print("Enter the game name: ");
                    String n = kb.next();
                    out.println("CREATE " + n);
                }
                String ans = in.nextLine();
                if(ans.startsWith("FAIL")) {
                    if(s.equals("3")) {
                        System.out.println("FAILED TO JOIN GAME");
                    } else {
                        System.out.println("FAILED TO CREATE GAME");
                    }
                } else {
                    if(s.equals("3")) {
                        System.out.println("Joined the game\n");
                    } else {
                        System.out.println("Created the game\n");
                    }
                    System.out.println(ans);
                    while(true) {
                        String l = in.nextLine();
                        System.out.println(l);
                        if(l.length() == 0)
                            break;
                    }
                    System.out.println("Waiting for game to start...\n");
                    playGame();
                }
            } else if(s.equals("4")) {
                System.out.print("Enter the game name: ");
                String n = kb.next();
                out.println("CREATE " + n);
                String ans = in.nextLine();
            } else if(s.equals("5")) {
                out.println("LEAVE");
                sock.close();
                System.exit(0);
            } else {
                System.out.println("Invalid option");
            }
        }
    }

    private void playGame() throws Exception {
        boolean started = false;
        while(true) {
            String a = in.nextLine();
            if(a.startsWith("GAME")) { // GAME-OVER
                try {
                    Thread.sleep(30*1000);
                } catch(InterruptedException e) {
                }
                System.out.println("GAME OVER");
                return;
            }
            if(a.startsWith("ROUND")) {
                System.out.print(a + " - Enter your number: ");
                int num = kb.nextInt();
                out.println("PLAY " + num);
            } else {
                System.out.println(a);
            }
        }
    }
}
