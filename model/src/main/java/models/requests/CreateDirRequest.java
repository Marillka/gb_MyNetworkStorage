package models.requests;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class CreateDirRequest implements BasicRequest {

    private AuthRequest authRequest;
    private String pathToTheNewDir;

    public CreateDirRequest(AuthRequest authRequest, Path path) {
        this.authRequest = authRequest;
        this.pathToTheNewDir = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "CreateDirRequest";
    }
}
