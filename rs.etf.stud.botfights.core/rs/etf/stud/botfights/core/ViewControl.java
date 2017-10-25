package rs.etf.stud.botfights.core;

import javafx.scene.Scene;

public interface ViewControl{
    void onGameStart();
    void onTurnStart();
    void beforePlayerTurn(Player player);
    void afterPlayerTurn(Player player, Move move);
    void customManipulation(SceneManipulation SceneManipulation);
    void onTurnEnd();
    void onGameEnd();
}
