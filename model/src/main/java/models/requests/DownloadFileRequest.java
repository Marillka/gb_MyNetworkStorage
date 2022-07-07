package models.requests;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class DownloadFileRequest implements BasicRequest {

    private AuthRequest authRequest;
    private String pathToFileOnServer;

    public DownloadFileRequest(AuthRequest authRequest, Path pathToFileOnServer) {
        this.authRequest = authRequest;
        this.pathToFileOnServer = pathToFileOnServer.normalize().toString();
    }

    @Override
    public String getType() {
        return "DownloadFileRequest";
    }


}
