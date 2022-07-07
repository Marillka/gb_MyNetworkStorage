package models.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
public class RegistrationResponse implements BasicResponse {

    @Setter
    boolean regOk;
    @Setter
    private int maxFolderDepth;

    public RegistrationResponse(boolean regOk) {
        this.regOk = regOk;
    }

    @Override
    public String getType() {
        return "RegistrationResponse";
    }
}
