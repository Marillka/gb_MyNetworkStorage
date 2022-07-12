package models.responses;

public class ChangeLoginResponse implements BasicResponse {

    private boolean isChangeLoginOk;

    public ChangeLoginResponse(boolean isChangeLoginOk) {
        this.isChangeLoginOk = isChangeLoginOk;
    }

    @Override
    public String getType() {
        return "ChangeLoginResponse";
    }
}
