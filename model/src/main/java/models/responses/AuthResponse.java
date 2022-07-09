package models.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse implements BasicResponse {

    private boolean authOK;
    private int maxFolderDepth;

    private  String rootDirectoryOnClientStr;
    private  String rootDirectoryOnServerStr;

    public AuthResponse(boolean authOK) {
        this.authOK = authOK;
    }

    @Override
    public String getType() {
        return "AuthResponse";
    }
}
