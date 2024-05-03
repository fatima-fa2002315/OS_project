import java.util.*;

public class Game {
    private final Server server;
    private Map<Integer, Player> players;
    private Map<Integer, Integer> points;

    private Map<Integer, Integer> played;

    private int gameId;
    private String name;
    private static int nextGame = 1;

    private boolean activated;
    private boolean gameOver;

    public Game(Server s, String n) {
        this.server = s;
        this.gameId = nextGame ++;
        this.players = new HashMap<>();
        this.points = new HashMap<> ();
        this.played = new HashMap<> ();
        this.name = n;
        this.activated = false;
        this.gameOver = false;
    }

    public void activate() {
        // start timer for starting the game
        try {
            Thread.sleep(30*1000);
        } catch(InterruptedException e) {
        }
        synchronized(this) {
            activated = true;
        }

        boolean gameFinished = false;
        int round = 1;
        Set<Integer> winners = new HashSet<>();

        while(! gameFinished) {
            synchronized(played) {
                played.clear();
            }
            // send query to players still in game
            for(Map.Entry<Integer,Player> ip: players.entrySet()) {
                if(points.get(ip.getKey()) > 0) {
                    ip.getValue().send("ROUND " + round);
                }
            }
            // 30 seconds for one round
            try {
                Thread.sleep(30*1000);
            } catch(InterruptedException e) {
            }

            Map<Integer,Double> playedValues = new HashMap<>();
            double sum =0; int count = 0;
            int first = -1;
            boolean allsame = true; // whether all players played the same number
            int ingamecount = 0; // to keep track of how many players are in the game
            String playerStr = "", playStr = "", remStr = "", resStr="";
            synchronized(played) {
                for(Map.Entry<Integer,Player> ip: players.entrySet()) {
                    if(points.get(ip.getKey()) > 0) {
                        ingamecount ++;
                        if(playerStr.length() > 0)
                            playerStr += ",";
                        playerStr += server.getPlayerName(ip.getKey());
                        try {
                            int v = played.get(ip.getKey());
                            if(first == -1)
                                first = v;
                            else if(v != first)
                                allsame = false;
                            if(v >= 0 && v <= 100) {
                                sum += v;
                                count ++;
                                playedValues.put(ip.getKey(), (double)v);
                                if(playStr.length() > 0)
                                    playStr += ",";
                                playStr += v;
                            }
                        } catch(Exception e) {
                            if(playStr.length() > 0)
                                playStr += ",";
                            playStr += "timeout";
                        }
                    }
                }
            }

            // less than or equal to one remains - he is the winner
            if(playedValues.size() <= 1) {
                for(Map.Entry<Integer,Player> ip: players.entrySet()) {
                    if(points.get(ip.getKey()) > 0) {
                        if(resStr.length() > 0) {
                            resStr += ",";
                            remStr += ",";
                        }
                        if(playedValues.containsKey(ip.getKey())) {
                            resStr += "win";
                            remStr += points.get(ip.getKey());
                            winners.add(ip.getKey());
                        }
                        else {
                            resStr += "lose";
                            remStr += (points.get(ip.getKey()) - 1);
                        }
                    }
                }
                gameFinished = true;
            } else {
                sum = (sum/count) * 2.0/3.0;
                double min = 200;
                for(Map.Entry<Integer,Double> pv : playedValues.entrySet()) {
                    double v = pv.getValue();
                    if(ingamecount == 2 && v == 0)
                        v = 1000; // this marks an entry 0 in a 2-player game
                    else
                        v = Math.abs(sum-v);
                    pv.setValue(v);
                    if(v < min) min = v;
                }
                for(Map.Entry<Integer,Player> ip: players.entrySet()) {
                    if(points.get(ip.getKey()) > 0) {
                        if(resStr.length() > 0) {
                            resStr += ",";
                            remStr += ",";
                        }
                        if(ingamecount > 1 && count > 1 && allsame) {
                            resStr += "lose";
                            remStr += (points.get(ip.getKey()) - 1);
                            points.put(ip.getKey(), points.get(ip.getKey())-1);
                        } else if(playedValues.containsKey(ip.getKey())
                                && playedValues.get(ip.getKey()) == min) {
                            resStr += "win";
                            remStr += points.get(ip.getKey());
                        } else {
                            resStr += "lose";
                            remStr += (points.get(ip.getKey()) - 1);
                            points.put(ip.getKey(), points.get(ip.getKey())-1);
                        }
                    }
                }
                int remaining = 0;
                for(Map.Entry<Integer,Player> ip: players.entrySet()) {
                    if(points.get(ip.getKey()) > 0) {
                        remaining ++;
                    }
                }
                if(remaining <= 1) {
                    gameFinished = true;
                    for(Map.Entry<Integer,Player> ip:players.entrySet()) {
                        if(points.get(ip.getKey()) > 0) {
                            winners.add(ip.getKey());
                        }
                    }
                }
            }

            // send the round results
            String toSendStr = "game round " + round + " " + playerStr + " " + playStr + " " + remStr + " " +resStr;
            for(Map.Entry<Integer,Player> ip: players.entrySet()) {
                ip.getValue().send(toSendStr);
            }

            round ++;
        }
        for(Map.Entry<Integer,Player> ip: players.entrySet()) {
            ip.getValue().send("GAME-OVER");
            ip.getValue().endGame();
        }
        server.endGame(gameId,winners);
    }

    public boolean addPlayer(Player p) {
        synchronized (this) {
            if(activated || players.size() == 6) {
                p.send("FAILED");
                return false;
            }
            String msg = "NEW PLAYER JOINED: " + server.getPlayerName(p.getTicket());
            String msg2 = "PLAYERS IN GAME:\n";
            for(Map.Entry<Integer,Player> me: players.entrySet()) {
                msg2 += server.getPlayerName(me.getKey()) + "\n";
                me.getValue().send(msg);
            }
            players.put(p.getTicket(),p);
            points.put(p.getTicket(), 5);
            p.send(msg2);
            if(players.size() == 2) {
                // Set a timer of 60 seconds to start the game
                (new Thread(()->activate())).start();
            }
            return true;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isActivated() {
        return activated;
    }

    public void sendMessage(int ticket, String msg) {
        if(msg == null)
            return;

        String [] tok = msg.split(" ");
        if(tok.length < 2 || !tok[0].equals("PLAY"))
            return;
        int val = 0;
        try {
            val = Integer.parseInt(tok[1]);
            if(val >= 0 && val <= 100) {
                synchronized(played) {
                    played.put(ticket,val);
                }
            }
        } catch(Exception e) {
        }
    }

    public int getId() {
        return gameId;
    }
}
