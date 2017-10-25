package rs.etf.stud.botfights.core;

public interface GameFlow {
    void init(GameActions actions, ViewControl view, PlayerManager playerManager, Context context);
    void flow(GameActions actions, ViewControl view, PlayerManager playerManager, Context context);
    GameOutcome end(GameActions actions, ViewControl view, PlayerManager playerManager, Context context);
}
