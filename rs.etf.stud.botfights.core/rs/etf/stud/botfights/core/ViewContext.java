package rs.etf.stud.botfights.core;

import javafx.scene.Scene;

public interface ViewContext {
    public Scene getScene();
    public void pause(int ms, boolean sticky);
}
