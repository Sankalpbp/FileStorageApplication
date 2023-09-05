package ai.typeface.filestorageservice.exception;

import org.springframework.http.HttpStatus;

abstract public class CustomFileStorageException extends RuntimeException {

    private final HttpStatus status;

    private final String message;

    public CustomFileStorageException ( HttpStatus status,
                                        String message ) {
        super ( message );
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }

}
