package models.responses;

import lombok.Getter;
import lombok.Setter;

@Getter
public class FilePartResponse implements BasicResponse {

    private String fileName;
    private long fileLength;
    private byte[] partBytes;
    private int partBytesLength;

    @Setter
    private String pathToStr;

    public FilePartResponse(String fileName, long fileLength, byte[] partBytes, int partBytesLength) {
        this.fileName = fileName;
        this.fileLength = fileLength;
        this.partBytes = partBytes;
        this.partBytesLength = partBytesLength;
    }

    @Override
    public String getType() {
        return "FilePartResponse";
    }
}


