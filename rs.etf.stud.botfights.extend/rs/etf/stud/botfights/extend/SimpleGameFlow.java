package rs.etf.stud.botfights.extend;

import rs.etf.stud.botfights.core.*;

import java.util.concurrent.TimeUnit;

public class SimpleGameFlow implements GameFlow {
    private int timeout;
    private GameOutcome gameOutcome;

    public SimpleGameFlow (int timeoutMillies){
        this.timeout = timeoutMillies;
    }

    @Override
    public void init(GameActions actions, ViewControl view, PlayerManager playerManager, Context context) {
        playerManager.setPlayerChoicePolicy(new OrdinalPlayerChoicePolicy());
        view.onGameStart();
    }

    @Override
    public void flow(GameActions actions, ViewControl view, PlayerManager playerManager, Context context) {
        view.onTurnStart();
        while(!actions.endCondition()){
            Player player;
            while((player = playerManager.nextPlayer()) != null && !actions.endCondition()){
                view.beforePlayerTurn(player);
                player.queryMove();
                Move move = player.getMove(timeout, TimeUnit.MILLISECONDS);
                if(move == null){
                    //todo locale
                    gameOutcome = new GameOutcome("Player "+player.getName() +" took too long to respond!", GameOutcome.OutcomeType.RULE_BREACH);
                    return;
                }
                System.out.println(player.getName()+": "+ move.getCommand());
                actions.onPlayerMove(move, player);
                view.afterPlayerTurn(player, move);
            }
            view.onTurnEnd();
            playerManager.nextTurn();
        }
    }

    @Override
    public GameOutcome end(GameActions actions, ViewControl view, PlayerManager playerManager, Context context) {
        view.onGameEnd();
        if(gameOutcome == null){
            gameOutcome = actions.getGameOutcome();
            if(gameOutcome == null){
                gameOutcome = new GameOutcome("Game did not return a valid game outcome!", GameOutcome.OutcomeType.ERROR);
            }
        }
        return gameOutcome;
    }
}
