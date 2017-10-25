package rs.etf.stud.botfights.main.components;

import javafx.scene.control.Label;
import rs.etf.stud.botfights.core.Game;

public class GameLabel extends Label {
    private Game game;

    public GameLabel(String text, Game game) {
        super(text);
        this.game = game;
    }

    public Game getGame() {
        return game;
    }
}
