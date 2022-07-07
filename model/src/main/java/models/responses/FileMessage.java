package models.responses;

import lombok.Getter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Getter
public class FileMessage implements BasicResponse {

    private final String name;
    private String directory;
    private final long size;
    private final byte[] data;

    public FileMessage(Path path, String directory) throws IOException {
        this.name = path.getFileName().toString();
        this.directory = directory;
        this.size = Files.size(path);
        this.data = Files.readAllBytes(path);
    }

    public FileMessage(Path path) throws IOException {
        this.name = path.getFileName().toString();
        this.size = Files.size(path);
        this.data = Files.readAllBytes(path);
    }



    @Override
    public String getType() {
        return "FileMessage";
    }
}
