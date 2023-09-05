package ai.typeface.filestorageservice.exception;

import org.springframework.http.HttpStatus;

public class FileMetadataValidationException extends CustomFileStorageException {

    public FileMetadataValidationException ( HttpStatus status,
                                             String message ) {
        super ( status, message );
    }
}
