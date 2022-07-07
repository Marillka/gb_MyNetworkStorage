package models.requests;


import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DeleteFileRequest implements BasicRequest {
    private AuthRequest authRequest;
    private String pathToTheDeleteFileStr;

    public DeleteFileRequest(AuthRequest authRequest, Path path) {
        this.authRequest = authRequest;
        this.pathToTheDeleteFileStr = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "DeleteFileRequest";
    }
}
