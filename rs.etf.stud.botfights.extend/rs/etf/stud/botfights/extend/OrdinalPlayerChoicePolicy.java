package rs.etf.stud.botfights.extend;

import rs.etf.stud.botfights.core.Player;
import rs.etf.stud.botfights.core.PlayerChoicePolicy;

import java.util.List;

public class OrdinalPlayerChoicePolicy implements PlayerChoicePolicy {
    int currentIndex = 0;

    @Override
    public Player choose(List<Player> players) {
        if(currentIndex < players.size()){
            return players.get(currentIndex++);
        }
        return null;
    }

    @Override
    public void reset() {
        currentIndex = 0;
    }
}
