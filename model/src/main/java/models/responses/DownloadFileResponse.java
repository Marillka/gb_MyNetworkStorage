package models.responses;

import lombok.Getter;

@Getter
public class DownloadFileResponse implements BasicResponse {

    private String pathToFileOnServer;
    private String pathToClientDirStr;
    private boolean needToDeleteFile;
    private boolean noFreeStorage;

    public DownloadFileResponse(String pathToServerFileStr, String pathToClientDirStr, boolean needToDeleteFile, boolean noFreeStorage) {
        this.pathToFileOnServer = pathToServerFileStr;
        this.pathToClientDirStr = pathToClientDirStr;
        this.needToDeleteFile = needToDeleteFile;
        this.noFreeStorage = noFreeStorage;
    }

    @Override
    public String getType() {
        return "DownloadFileResponse";
    }
}
