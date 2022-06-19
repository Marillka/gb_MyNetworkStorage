package models.responses;

import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;


public class GetFileListResponse implements BasicResponse {

    @Getter
    List<File> itemsList;

    @Getter
    String pathOfList;

    public GetFileListResponse(Path pathOfList) {
        try {
            itemsList = Files.list(pathOfList)
                    .map(Path::toFile)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.pathOfList = pathOfList.normalize().toString();
    }


    @Override
    public String getType() {
        return "GetFileListResponse";
    }
}
