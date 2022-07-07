package models.requests;

import lombok.Getter;

@Getter
public class RegistrationRequest implements BasicRequest {
    private String login;
    private String password;

    public RegistrationRequest(String login, String password) {
        this.login = login;
        this.password = password;
    }

    @Override
    public String getType() {
        return "RegistrationRequest";
    }
}
