package models.responses;

import lombok.Getter;
import models.FileInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class GetFileListResponse implements BasicResponse {

    List<FileInfo> fileListResponse;
    List<FileInfo> parentFileListResponse;
    String pathOfFileInListStr;
    String parentPathOfFileInListStr;

    public GetFileListResponse(Path path) {
        try (Stream<Path> list = Files.list(path)) {
            this.pathOfFileInListStr = path.toAbsolutePath().normalize().toString();
            this.fileListResponse = list
                    .map(FileInfo::new)
                    .collect(Collectors.toList());
            this.parentPathOfFileInListStr = path.getParent().toAbsolutePath().normalize().toString();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось создать GetFileListResponse");
        }

        try(Stream<Path> list = Files.list(Paths.get(parentPathOfFileInListStr))) {
            this.parentFileListResponse = list
                    .map(FileInfo::new)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Не удалось создать GetFileListResponse");

        }
    }

    @Override
    public String getType() {
        return "GetFileListResponse";
    }
}
