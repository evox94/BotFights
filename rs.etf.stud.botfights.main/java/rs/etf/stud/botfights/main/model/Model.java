package rs.etf.stud.botfights.main.model;

import rs.etf.stud.botfights.core.Game;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

public class Model {
    private List<Game> games;
    private Game currentGame;

    private static Model instance;

    private Model(){
        ServiceLoader<Game> loader = ServiceLoader.load(Game.class);
        games = new ArrayList<>();
        for(Game g: loader){games.add(g);}
        games.sort(Comparator.comparing(Game::getName));
    }

    public static Model getInstance(){
        if(instance == null){
            instance = new Model();
        }
        return instance;
    }

    public List<Game> getGames() {
        return games;
    }

    public void selectGame(Game game){
        currentGame = game;
    }

    public Game getCurrentGame() {
        return currentGame;
    }
}
