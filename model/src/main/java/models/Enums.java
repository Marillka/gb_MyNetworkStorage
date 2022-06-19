package models;

import models.requests.BasicRequest;

public enum Enums implements BasicRequest {
    LOGIN_BAD_RESPONSE,
    LOGIN_OK_RESPONSE,
    LEVEL_UP;

    @Override
    public String getType() {
        return "ENUM";
    }

}
