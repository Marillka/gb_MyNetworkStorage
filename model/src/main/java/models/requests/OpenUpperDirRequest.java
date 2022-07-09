//package models.requests;
//
//import lombok.Getter;
//import lombok.Setter;
//
//import java.nio.file.Paths;
//
//@Getter
//@Setter
//public class OpenUpperDirRequest implements BasicRequest {
//
//    AuthRequest authRequest;
//    private String pathStr;
//    private String getParentPathStr;
//
//    public OpenUpperDirRequest(AuthRequest authRequest, String pathStr) {
//        this.authRequest = authRequest;
//        this.pathStr = pathStr;
//        getParentPathStr = Paths.get(pathStr).getParent().toString();
//    }
//
//    @Override
//    public String getType() {
//        return null;
//    }
//}
