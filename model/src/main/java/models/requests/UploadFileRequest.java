package models.requests;

import lombok.Getter;

import java.nio.file.Path;

@Getter
public class UploadFileRequest implements BasicRequest {

    private AuthRequest authRequest;
    private String pathToServerDirStr;
    private String pathToFileOnClientStr;
    private long fileLength;

    public UploadFileRequest(AuthRequest authRequest, Path pathToRemote, Path pathToLocalFileStr, long fileLength) {
        this.authRequest = authRequest;
        this.pathToServerDirStr = String.valueOf(pathToRemote);
        this.pathToFileOnClientStr = String.valueOf(pathToLocalFileStr);
        this.fileLength = fileLength;
    }

    @Override
    public String getType() {
        return "UploadFileRequest";
    }
}
