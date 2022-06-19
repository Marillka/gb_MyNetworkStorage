package cloud;

import lombok.Data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Data
public class FileInfo {

    public enum FileType {
        FILE("file"),
        DIRECTORY("dir");

        private String name;

        FileType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    private String fileName;
    private FileType type;
    private long size;
    private LocalDateTime lastModifiedTime;

    public FileInfo(Path path) {
        try {
            this.fileName = path.getFileName().toString();
            this.type = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;
            if (this.type == FileType.DIRECTORY) {
                this.size = -1L;
            }
            this.size = Files.size(path);
            this.lastModifiedTime = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneOffset.ofHours(3));
        } catch (IOException e) {
            throw new RuntimeException("Something wrong with file: " + path);
        }
    }

}
