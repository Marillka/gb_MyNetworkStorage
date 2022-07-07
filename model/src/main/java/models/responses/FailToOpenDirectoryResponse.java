package models.responses;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class FailToOpenDirectoryResponse implements BasicResponse {

    String pathToOpenStr;

    public FailToOpenDirectoryResponse(Path pathToOpen) {
        this.pathToOpenStr = pathToOpen.toString();
    }

    @Override
    public String getType() {
        return "FailToOpenDirectoryResponse";
    }
}
