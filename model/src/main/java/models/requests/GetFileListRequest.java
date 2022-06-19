package models.requests;

import lombok.Data;

@Data
public class GetFileListRequest implements BasicRequest {

    @Override
    public String getType() {
        return "GetFileListRequest";
    }
}
