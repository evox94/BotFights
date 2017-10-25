package rs.etf.stud.botfights.core;

public class LoggerUtil {

    private static Logger logger;

    public interface Logger{
        void log(String msg);
    }
    public static void setInstance(Logger logger){
        LoggerUtil.logger = logger;
    }

    public static Logger getLogger(){
        return logger;
    }
}
