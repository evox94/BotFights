package rs.etf.stud.botfights.extend.java;

import rs.etf.stud.botfights.core.GameState;
import rs.etf.stud.botfights.core.Move;
import rs.etf.stud.botfights.core.Player;
import rs.etf.stud.botfights.extend.TCPStringProtocol;

import java.util.Collection;

public class JavaOnlyTCPSimpleProtocol implements TCPStringProtocol {
    Class<? extends JavaGameStateDeserializer> deserializerClass;

    public JavaOnlyTCPSimpleProtocol(Class<? extends JavaGameStateDeserializer> deserializerClass) {
        this.deserializerClass = deserializerClass;
    }

    @Override
    public String getHelloMessage() {
        return String.join(",","H", deserializerClass.getName());
    }

    @Override
    public boolean processHelloResponse(String response) {
        return "HB".equals(response);
    }

    @Override
    public String getPlayerDataMessage(Collection<Player> playerList) {
        return String.join(",", playerList.stream().map(p -> p.getId()+":"+p.getAlgFilePath()).toArray(String[]::new));
    }

    @Override
    public boolean processPlayerDataResponse(String response) {
        return "START".equals(response);
    }

    @Override
    public String getMoveRequestMessage(GameState gameState, Player player) {
        Object o = gameState.serialize();
        if(!(o instanceof String))throw new RuntimeException("Game state has to be serialized as a string for java only games");
        String gameStateString = (String)o;
        return String.join(":", Integer.toString(player.getId()), gameStateString);
    }

    @Override
    public Move processMoveResponse(final String move, Collection<Player> playerList) {
        String[] parts = move.split(":",2);
        final String id = parts[0];
        final String command = parts[1];
        return new Move(playerList.stream()
                .filter(player -> Integer.toString(player.getId()).equals(id))
                .findFirst()
                .orElseThrow(()-> new RuntimeException("Player with the given id doesn't exist")), command);
    }

    @Override
    public String getGoodbyeMessage() {
        return "END";
    }
}
