package rs.etf.stud.botfights.core;

import java.util.List;

public class PlayerManager {
    private PlayerChoicePolicy playerChoicePolicy;
    List<Player> players;
    int turn = 1;

    public PlayerManager(List<Player> players){
        this.players = players;
    }

    public void setPlayerChoicePolicy(PlayerChoicePolicy playerChoicePolicy){
        this.playerChoicePolicy = playerChoicePolicy;
    }

    public Player nextPlayer(){
        return playerChoicePolicy.choose(players);
    }

    public void nextTurn(){
        turn++;
        playerChoicePolicy.reset();
    }

    public void disablePlayer(Player p){
        //todo
    }
}
