package models.requests;

import lombok.Getter;

@Getter
public class ChangeLoginRequest implements BasicRequest {

    private AuthRequest authRequest;
    private String newLogin;

    public ChangeLoginRequest(AuthRequest authRequest, String newLogin) {
        this.authRequest = authRequest;
        this.newLogin = newLogin;
    }

    @Override
    public String getType() {
        return "ChangeLoginRequest";
    }
}
