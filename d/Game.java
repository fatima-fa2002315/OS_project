package d;

import java.util.*;

public class Game {
    private Map<Integer, Player> players;
    private Map<Integer, Integer> points;

    private int gameId;
    private static int nextGame = 1;

    private boolean isActivated;

    public Game() {
        this.gameId = nextGame ++;
        players = new HashMap<>();
        points = new HashMap<> ();
        this.isActivated = false;
    }

    public int getPlayerCount() {
		return gameId;
    }

    public void activate() {
    }

    public boolean addPlayer(Player p) {
		return isActivated;
    }
}
