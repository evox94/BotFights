package rs.etf.stud.botfights.javaprocrunner;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class JavaProcRunner {
    public interface IOHandle {
        public void write(String data) throws Exception;

        public String read() throws Exception;
    }

    private interface State {
        public String process(String input) throws Exception;
    }

    private class ActionController {
        public void initGameStateDeserializer(String className) throws Exception {
            deserializer = DeserializerWrapper.fromClassName(className);
        }

        public void createNewPlayerProxy(String playerId, String playerAlgPath) {
            playerProxies.put(playerId, new PlayerProxy(playerId, playerAlgPath));
        }

        public void preparePlayerAlgorithms() throws Exception {
            JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
            if (javac == null) throw new Exception("Couldn't locate java compiler");

            for (PlayerProxy playerProxy : playerProxies.values()) {
                try {
                    playerProxy.loadAlgorithm(javac);
                } catch (Exception e) {
                    throw new Exception("Algorithm preparing failed for playerId:" + playerProxy.getId(), e);
                }
            }
        }

        public void startPlayerProxies() {
            playerProxies.values().forEach(PlayerProxy::start);
        }

        public void queryMove(String player, String gameState) throws Exception {
            PlayerProxy playerProxy = playerProxies.get(player);
            if (playerProxy == null) throw new Exception("Player with playerId:" + player + " doesn't exist");
            playerProxy.queryCall(deserializer.deserialize(gameState));
        }

        public void end() {
            end = true;
        }
    }

    private IOHandle io;
    private DeserializerWrapper deserializer;
    private Map<String, PlayerProxy> playerProxies;
    private ActionController actions;
    private State currentState;
    private boolean end;

    public JavaProcRunner(IOHandle io) {
        actions = new ActionController();
        currentState = new StartState();
        this.io = io;
        playerProxies = new HashMap<>();
        end = false;
    }

    public void launch() {
        try {
            while (!end) {
                String data = io.read();
                if (data != null) {
                    String response = currentState.process(data);
                    if (response != null) {
                        io.write(response);
                    }
                } else {
                    end = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        playerProxies.values().forEach(PlayerProxy::stop);
    }

    public static void main(String args[]) {
        int port = 0;
        try{
            port = Integer.parseInt(args[0]);
        }catch (Exception ex){
            ex.printStackTrace();
            System.err.println("Arguments are invalid");
            System.exit(1);
        }

        try (final Socket server = new Socket(InetAddress.getByName(null), port);
             final PrintWriter writer = new PrintWriter(server.getOutputStream(), true);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(server.getInputStream()))) {


            JavaProcRunner jpr = new JavaProcRunner(new IOHandle() {
                @Override
                public void write(String data) {
                    writer.println(data);
                }

                @Override
                public String read() throws Exception {
                    return reader.readLine();
                }
            });
            System.setOut(new PlayerInterceptor(System.out, writer));
            jpr.launch();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.err.println("Java runtime process exit");
    }

    private class StartState implements State {
        @Override
        public String process(String input) throws Exception {
            String parts[] = input.split(",", 2);
            if (parts.length != 2) throw new Exception("Invalid Hello message format");
            if (!parts[0].equals("H")) throw new Exception("Invalid Hello message header");
            actions.initGameStateDeserializer(parts[1]);

            currentState = new PlayerDataState();
            return "HB";
        }
    }

    private class PlayerDataState implements State {
        @Override
        public String process(String input) throws Exception {
            String[] playerEntries = input.split(",");
            for (String playerString : playerEntries) {
                String[] playerEntry = playerString.split(":", 2);
                if (playerEntry.length != 2) throw new Exception("Invalid Player Data message format");
                actions.createNewPlayerProxy(playerEntry[0], playerEntry[1]);
            }
            actions.preparePlayerAlgorithms();
            actions.startPlayerProxies();

            currentState = new MoveListenerState();
            return "START";
        }
    }

    private class MoveListenerState implements State {
        @Override
        public String process(String input) throws Exception {
            if (input.equals("END")) {
                actions.end();
            } else {
                String playerParts[] = input.split(":", 2);
                if (playerParts.length != 2) throw new Exception("Invalid Player Query Request message format");
                actions.queryMove(playerParts[0], playerParts[1]);
            }
            return null;
        }
    }
}
