package models.responses;

import lombok.Getter;

@Getter
public class UploadFileResponse implements BasicResponse {
    private String pathToClientFileStr;
    private String pathToServerDirStr;
    private boolean needToDeleteFile;
    private boolean noFreeStorage;

    public UploadFileResponse(String pathToClientFileStr, String pathToServerFileSer, boolean needToDeleteFile, boolean noFreeStorage) {
        this.pathToClientFileStr = pathToClientFileStr;
        this.pathToServerDirStr = pathToServerFileSer;
        this.needToDeleteFile = needToDeleteFile;
        this.noFreeStorage = noFreeStorage;
    }

    @Override
    public String getType() {
        return "UploadFileResponse";
    }
}
