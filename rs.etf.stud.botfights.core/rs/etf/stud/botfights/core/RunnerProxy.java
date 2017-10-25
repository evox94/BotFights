package rs.etf.stud.botfights.core;

import java.util.concurrent.TimeUnit;

public abstract class RunnerProxy {
    protected Runner runner;
    protected Player player;

    public RunnerProxy(Runner runner, Player player) {
        this.runner = runner;
        this.player = player;
    }

    public Runner getRunner() {
        return runner;
    }

    public Player getPlayer() {
        return player;
    }

    public abstract void queryMove(GameState state);

    public abstract Move getNextMove(int timeout, TimeUnit timeUnit);
}
