package models.requests;

import lombok.Getter;

import java.nio.file.Paths;

public class OpenUpperDirRequest implements BasicRequest {

    @Getter
    private String pathStr;
    @Getter
    private String getParentPathStr;

    public OpenUpperDirRequest(String pathStr) {
        this.pathStr = pathStr;
        getParentPathStr = Paths.get(pathStr).getParent().toString();
    }

    @Override
    public String getType() {
        return null;
    }
}
