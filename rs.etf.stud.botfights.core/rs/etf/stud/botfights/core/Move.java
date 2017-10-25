package rs.etf.stud.botfights.core;


public class Move {
    private Player player;
    private String command;

    public Move(Player player, String command) {
        this.player = player;
        this.command = command;
    }

    public Player getPlayer() {
        return player;
    }

    public String getCommand() {
        return command;
    }
}
