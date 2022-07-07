package models.requests;

import lombok.Getter;

@Getter
public class GetFirstFileListRequest implements BasicRequest {

    private AuthRequest authRequest;

    public GetFirstFileListRequest(AuthRequest authRequest) {
        this.authRequest = authRequest;
    }

    @Override
    public String getType() {
        return "GetFirstFileListRequest";
    }
}
