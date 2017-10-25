package rs.etf.stud.botfights.extend.java;

import rs.etf.stud.botfights.core.FileExtension;
import rs.etf.stud.botfights.core.LoggerUtil;
import rs.etf.stud.botfights.core.Runner;
import rs.etf.stud.botfights.core.Template;
import rs.etf.stud.botfights.extend.BaseGame;

import java.util.Collection;
import java.util.List;

public abstract class JavaOnlyGame extends BaseGame{

    JavaOnlyRunner runner = null;

    public JavaOnlyGame(){

    }

    public abstract Class<? extends JavaGameStateDeserializer> getDeserializerClass();

    @Override
    public Runner getRunner(String fileUrl)
    {
        if(fileUrl.matches("^.*\\.java$")){
            if(runner == null || runner.isStopped()){
                runner = new JavaOnlyRunner(getDeserializerClass());
            }
            return runner;
        }
        return null;
    }

    @Override
    public Collection<Template> getTemplates() {
        return List.of(new Template(getTemplateFileName(), "Java"));
    }

    protected abstract String getTemplateFileName();

    @Override
    public Collection<FileExtension> getRecognizedFileExtensions() {
        return List.of(new FileExtension("Java source", "*.java"));
    }
}
