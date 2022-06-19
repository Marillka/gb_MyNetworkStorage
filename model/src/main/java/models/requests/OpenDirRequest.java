package models.requests;

import lombok.Getter;

import java.nio.file.Path;

public class OpenDirRequest implements BasicRequest {

    @Getter
    private String pathStr;

    public OpenDirRequest(Path path) {
        this.pathStr = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "OpenDirRequest";
    }
}
