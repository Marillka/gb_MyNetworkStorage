package models.requests;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class OpenDirRequest implements BasicRequest {

    private AuthRequest authRequest;
    private String serverPathToOpen;

    public OpenDirRequest(AuthRequest authRequest, Path path) {
        this.authRequest = authRequest;
        this.serverPathToOpen = path.normalize().toString();
    }

    @Override
    public String getType() {
        return "OpenDirRequest";
    }
}
