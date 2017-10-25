package rs.etf.stud.botfights.core;

public interface GameActions {
    public void initGame();
    public void onPlayerMove(Move move, Player player);
    public GameOutcome getGameOutcome();
    public void cleanupGame();
    public boolean endCondition();
}
