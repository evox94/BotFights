package rs.etf.stud.botfights.extend;

import rs.etf.stud.botfights.core.*;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class TCPProcRunner extends Runner {
    private Map<Player, RunnerProxyImpl> runnerProxies;
    private TCPServer tcpServer;
    private TCPStringProtocol protocol;
    private boolean started;
    private boolean established;
    private boolean stopped;

    public TCPProcRunner(TCPStringProtocol protocol) {
        runnerProxies = new HashMap<>();
        this.protocol = protocol;
        started = false;
        established = false;
        stopped = false;
    }

    protected abstract void startProcess(int port);

    protected abstract void stopProcess();

    @Override
    public void start() {
        if (started) return;
        try {
            tcpServer = new TCPServer();
            startProcess(tcpServer.getPort());
            tcpServer.start();
            synchronized (tcpServer) {
                if (!established) try {
                    tcpServer.wait(6000);
                } catch (InterruptedException e) {/**/}
            }
            if(!established) {
                stopped = true;
                tcpServer.kill();
                stopProcess();
                throw new RuntimeException("Connection with the process couldn't be established");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }finally {
            started = true;
        }
    }

    @Override
    public void stop() {
        if (stopped || !started) return;
        stopped = true;
        tcpServer.kill();
        stopProcess();
    }

    @Override
    protected RunnerProxy createProxyForPlayer(Player player) {
        if (started) throw new IllegalStateException();
        RunnerProxyImpl runnerProxy = new RunnerProxyImpl(this, player);
        runnerProxies.put(player, runnerProxy);
        return runnerProxy;
    }

    public boolean isStopped() {
        return stopped;
    }

    private class RunnerProxyImpl extends RunnerProxy {
        BlockingQueue<Move> moves;

        public RunnerProxyImpl(Runner runner, Player player) {
            super(runner, player);
            moves = new ArrayBlockingQueue<Move>(20);
        }

        @Override
        public synchronized void queryMove(GameState state) {
            tcpServer.write(protocol.getMoveRequestMessage(state, player));
        }

        @Override
        public Move getNextMove(int timeout, TimeUnit timeUnit) {
            Move m = null;
            try {
                m = moves.poll(timeout, timeUnit);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return m;
        }

        private void addMove(Move m) {
            moves.offer(m);
        }
    }

    private class TCPServer extends Thread {
        private Socket procSocket = null;
        ServerSocket serverSocket;//debug
        private PrintWriter output;
        private BufferedReader input;
        private boolean end;

        public TCPServer() throws IOException {
            serverSocket = new ServerSocket(0, 50, InetAddress.getByName(null));
        }

        public int getPort() {
            return serverSocket.getLocalPort();
        }

        public String read() throws IOException {
            if(established){
                return input.readLine();
            }
            throw new IOException("Connection is not established");
        }

        public void write(String data) {
            if(established){
                output.println(data);
            }
        }

        public void kill() {
            end = true;
            try {
                write(protocol.getGoodbyeMessage());
                sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
            closeResources();
            interrupt();
        }

        @Override
        public void run() {
            try {
                procSocket = serverSocket.accept();
                LoggerUtil.getLogger().log("TCPServer: Got process connection!");
                output = new PrintWriter(procSocket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(procSocket.getInputStream()));
                LoggerUtil.getLogger().log("TCPServer: Writing hello");
                output.println(protocol.getHelloMessage());
                if (!protocol.processHelloResponse(input.readLine())) {
                    throw new IOException("Invalid Hello Response!");
                }
                LoggerUtil.getLogger().log("TCPServer: Writing player data");
                output.println(protocol.getPlayerDataMessage(runnerProxies.keySet()));
                if (!protocol.processPlayerDataResponse(input.readLine())) {
                    throw new IOException("Invalid Player Data Response!");
                }

                LoggerUtil.getLogger().log("TCPServer: All ok so far");
                if (end) {
                    return;
                }

                synchronized (this) {
                    established = true;
                    this.notifyAll();
                }

                LoggerUtil.getLogger().log("TCPServer: Established connection with the process!");
                while (!end) {
                    String data = read();
                    if (data != null) {
                        Move m = protocol.processMoveResponse(data, runnerProxies.keySet());
                        if (m != null) {
                            runnerProxies.get(m.getPlayer()).addMove(m);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                closeResources();
            }

            if (!end) {
                throw new IllegalStateException("Client closed connection abruptly!");
            }
        }

        private void closeResources() {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (output != null) {
                output.close();
            }
            if (procSocket != null) {
                try {
                    procSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(serverSocket != null){
                try{
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
