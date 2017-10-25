package rs.etf.stud.botfights.extend;

import rs.etf.stud.botfights.core.GameState;
import rs.etf.stud.botfights.core.Move;
import rs.etf.stud.botfights.core.Player;

import java.util.Collection;


public interface TCPStringProtocol {
    String getHelloMessage();
    boolean processHelloResponse(String response);
    String getPlayerDataMessage(Collection<Player> playerList);
    boolean processPlayerDataResponse(String response);
    String getMoveRequestMessage(GameState gameState, Player player);
    Move processMoveResponse(String move, Collection<Player> playerList);
    String getGoodbyeMessage();
}
