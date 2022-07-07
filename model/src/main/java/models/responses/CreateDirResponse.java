package models.responses;

import lombok.Getter;

@Getter
public class CreateDirResponse implements BasicResponse {
    private GetFileListResponse getFileListResponse;
    private boolean isCreateDirOk;

    public CreateDirResponse(GetFileListResponse getFileListResponse, boolean isCreateDirOk) {
        this.getFileListResponse = getFileListResponse;
        this.isCreateDirOk = isCreateDirOk;
    }

    @Override
    public String getType() {
        return "CreateDirResponse";
    }
}
