package rs.etf.stud.botfights.core;

public class Template {
    String templateFileName;
    String language;

    public Template(String templateFileName, String language) {
        this.templateFileName = templateFileName;
        this.language = language;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public String getLanguage() {
        return language;
    }
}
