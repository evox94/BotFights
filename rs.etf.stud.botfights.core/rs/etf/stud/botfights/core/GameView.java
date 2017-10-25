package rs.etf.stud.botfights.core;

import javafx.application.Platform;
import javafx.scene.Scene;

public interface GameView {
    Scene initScene();
    void onGameStart(ViewContext viewContext);
    void onTurnStart(ViewContext viewContext );
    void beforePlayerTurn(ViewContext viewContext, Player player);
    void afterPlayerTurn(ViewContext viewContext, Player player, Move move);
    void onTurnEnd(ViewContext viewContext);
    void onGameEnd(ViewContext viewContext);
}
