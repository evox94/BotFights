package rs.etf.stud.botfights.core;

import java.util.concurrent.TimeUnit;

public class Player {
    private static int lastId = 0;

    private int id;
    private String name;
    private String algFilePath;
    private Game game;
    RunnerProxy runnerProxy;

    public Player(String name, String algFilePath, Game game) {
        this.name = name;
        this.algFilePath = algFilePath;
        this.game = game;
        this.id = ++lastId;
    }

    public void setRunnerProxy(RunnerProxy runnerProxy){
        this.runnerProxy = runnerProxy;
    }

    public void queryMove(){
        runnerProxy.queryMove(game.getGameStateForPlayer(this));
    }

    public Move getMove(int timeout, TimeUnit timeUnit){
        return runnerProxy.getNextMove(timeout, timeUnit);
    }

    public String getName() {
        return name;
    }

    public String getAlgFilePath() {
        return algFilePath;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        return id == player.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
