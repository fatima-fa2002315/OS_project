import java.util.*;
import java.util.concurrent.*;
import java.net.*;
import java.io.*;

class Server {
    private final ServerSocket serverSocket;
    private final ExecutorService pool;

    // user info
    private Map<Integer,String> ticket;
    private Map<Integer,Integer> score;

    private static int nextTicket = 1;

    private Map<Integer,Player> currentPlayers;

    private Map<Integer, Game> currentGames;

    public Server() throws IOException {
        ticket = new HashMap<>();
        currentPlayers = new HashMap<>();
        currentGames = new HashMap<>();
        score = new HashMap<>();
        serverSocket = new ServerSocket(13337);
        pool = Executors.newFixedThreadPool(50);

        while(true) {
            try {
                pool.execute(new Player(this,serverSocket.accept()));
            } catch(IOException e) {
            }
        }
    }

    public int getTicket(String name) {
        synchronized(ticket) {
            ticket.put(nextTicket,name);
            return nextTicket ++;
        }
    }

    public boolean addPlayer(int tickNum, Player p) {
        synchronized(ticket) {
            if(ticket.get(tickNum) == null)
                return false;
            System.out.println("PLAYER " + ticket.get(tickNum) + " JOINED");
        }
        synchronized(currentPlayers) {
            currentPlayers.put(tickNum,p);
            return true;
        }
    }

    public String getLeaderboardInfo() {
        String s = "LEADER BOARD\n";
        List<Map.Entry<Integer,Integer> > list = new ArrayList<>();
        synchronized(score) {
            list.addAll(score.entrySet());
        }

        Collections.sort(list, new Comparator<Map.Entry<Integer,Integer> >() {
            public int compare(Map.Entry<Integer,Integer> e1, Map.Entry<Integer,Integer> e2) {
                // sort in descending order of score
                return (-1) * e1.getValue().compareTo(e2.getValue());
            }
        });

        int i=0;
        synchronized (ticket) {
            for(Map.Entry<Integer,Integer> me : list) {
                s += (i+1) + ". " + ticket.get(me.getKey()) + " - " + me.getValue() + "\n";
                i++;
                if(i == 5)
                    break;
            }
        }
        for(; i<5; i++) {
            s += (i+1) + ". -\n";
        }
        return s;
    }

    public String getPlayerInfo() {
        String s = "CURRENT PLAYERS\n";
        synchronized(currentPlayers) {
            for(Integer tid : currentPlayers.keySet()) {
                s += "  " + ticket.get(tid) + "\n";
            }
        }
        return s;
    }

    public String getGamesInfo() {
        String s = "GAME LIST\n";
        synchronized(currentGames) {
            if(currentGames.size() == 0) {
                Game def = new Game(this, "default");
                currentGames.put(def.getId(),def);
            }
            for(Map.Entry<Integer,Game> ge : currentGames.entrySet()) {
                Game g = ge.getValue();
                if(! g.isActivated()) {
                    s += ge.getKey() + " " + g.getName() + "\n";
                }
            }
        }
        return s;
    }

    public void endGame(int gameId, Set<Integer> winners) {
        synchronized(currentGames) {
            System.out.println("GAME " + currentGames.get(gameId).getName() + " ENDED");
            currentGames.remove(gameId);
        }
        synchronized(score) {
            for(Integer w : winners) {
                Integer prev = score.get(w);
                if(prev == null) {
                    score.put(w,1);
                } else {
                    score.put(w,prev+1);
                }
            }
        }
    }

    public void removePlayer(int tid) {
        synchronized(ticket) {
            System.out.println("PLAYER " + ticket.get(tid) + " LEFT");
        }
        synchronized(currentPlayers) {
            currentPlayers.remove(tid);
        }
    }

    public Game joinGame(int gid, Player p) {
        Game g = null;
        synchronized(currentGames) {
            g = currentGames.get(gid);
        }
        if(g == null) {
            p.send("FAILED");
            return null;
        }
        if(g.addPlayer(p)) {
            return g;
        }
        return null;
    }

    public String getPlayerName(int tid) {
        synchronized(ticket) {
            return ticket.get(tid);
        }
    }

    public int addGame(String n) {
        Game g = new Game(this, n);
        synchronized(currentGames) {
            currentGames.put(g.getId(),g);
        }
        System.out.println("NEW GAME " + n);
        return g.getId();
    }

    public static void main(String [] args) {
        try {
            new Server();
        } catch(IOException e) {
            System.out.println("Server failed");
        }
    }
}

