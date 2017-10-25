package rs.etf.stud.botfights.main.utility;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import rs.etf.stud.botfights.core.LoggerUtil;

import java.io.PrintStream;

public class Console implements LoggerUtil.Logger{
    private static TextArea textArea;
    private static PrintStream out;

    public Console(TextArea textArea, PrintStream out) {
        Console.textArea = textArea;
        Console.out = out;
    }

    @Override
    public synchronized void log(String msg) {
        Platform.runLater(() -> {
            textArea.appendText(msg+"\n");
            out.println(msg);
        });
    }
}
