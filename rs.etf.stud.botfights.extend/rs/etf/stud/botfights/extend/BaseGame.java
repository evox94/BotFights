package rs.etf.stud.botfights.extend;

import rs.etf.stud.botfights.core.Context;
import rs.etf.stud.botfights.core.Game;
import rs.etf.stud.botfights.core.Player;

import java.util.List;

public abstract class BaseGame implements Game {
    protected List<Player> players;
    protected Context context;

    @Override
    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    @Override
    public List<Player> getPlayers() {
        return players;
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public Context getContext() {
        return context;
    }
}
