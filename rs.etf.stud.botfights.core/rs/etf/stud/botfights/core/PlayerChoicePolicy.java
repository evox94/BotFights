package rs.etf.stud.botfights.core;

import java.util.List;

public interface PlayerChoicePolicy {
    public Player choose(List<Player> players);
    public void reset();
}
