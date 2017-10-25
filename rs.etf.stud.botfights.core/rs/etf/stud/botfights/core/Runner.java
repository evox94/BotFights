package rs.etf.stud.botfights.core;

public abstract class Runner {
    abstract public void start();
    public void command(RunnerCommand command){
        command.execute();
    }
    abstract public void stop();
    abstract protected RunnerProxy createProxyForPlayer(Player player);
    public void subscribePlayer(Player player){
        player.setRunnerProxy(createProxyForPlayer(player));
    }
}
