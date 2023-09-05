package ai.typeface.filestorageservice.dtos;

import java.util.Date;

public class ErrorDetailsDTO {
    private final Date timestamp;
    private final String message;
    private final String details;

    public ErrorDetailsDTO ( Date timestamp, String message, String details ) {
        this.timestamp = timestamp;
        this.message = message;
        this.details = details;
    }

    public Date getTimestamp () {
        return this.timestamp;
    }

    public String getMessage () {
        return this.message;
    }

    public String getDetails ( ) {
        return this.details;
    }
}
