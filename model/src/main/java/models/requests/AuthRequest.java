package models.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthRequest implements BasicRequest {

    private String login;
    private String password;


    public AuthRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getType() {
        return "AuthRequest";
    }
}
