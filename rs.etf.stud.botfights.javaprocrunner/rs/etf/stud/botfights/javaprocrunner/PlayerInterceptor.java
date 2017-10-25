package rs.etf.stud.botfights.javaprocrunner;

import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

public class PlayerInterceptor extends PrintStream {
    private OutputStream org;
    PrintWriter writer;

    public PlayerInterceptor(OutputStream out, PrintWriter writer) {
        super(out, true);
        this.org = out;
        this.writer = writer;
    }

    @Override
    public synchronized void print(String s) {
        writer.println(addPlayerId(s));
    }

    @Override
    public synchronized void println(String s) {
        writer.println(addPlayerId(s));
    }

    private String addPlayerId(String s){
        return String.join(":", PlayerProxy.getCurrentPlayerId(), s);
    }

    public OutputStream getOrg() {
        return org;
    }
}
