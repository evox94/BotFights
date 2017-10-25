package rs.etf.stud.botfights.core;

import java.util.Collection;
import java.util.List;

public interface Game extends GameActions, GameView {
    void setPlayers(List<Player> players);
    List<Player> getPlayers();
    void setContext(Context context);
    Context getContext();

    String getName();
    int getMinPlayers();
    int getMaxPlayers();

    GameState getGameStateForPlayer(Player player);
    GameFlow getFlow();

    Runner getRunner(String fileUrl);

    Collection<Template> getTemplates();

    String getDescription();

    Collection<FileExtension> getRecognizedFileExtensions();
}
