package rs.etf.stud.botfights.core;

public class FileExtension {
    private String fileType;
    private String fileExtension;

    public FileExtension(String fileType, String fileExtension) {
        this.fileType = fileType;
        this.fileExtension = fileExtension;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFileExtension() {
        return fileExtension;
    }
}
