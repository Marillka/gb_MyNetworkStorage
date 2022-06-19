package models.requests;

import lombok.Data;


@Data
public class AuthRequest implements BasicRequest {

    private String login;
    private String password;
    private Boolean result;

    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getType() {
        return "AuthRequest";
    }
}
