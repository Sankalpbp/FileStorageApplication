package ai.typeface.filestorageservice.exception;

import org.springframework.http.HttpStatus;

public class GoogleCloudStorageException extends CustomFileStorageException {

    public GoogleCloudStorageException ( HttpStatus status,
                                         String message ) {
        super ( status, message );
    }

}
