package ai.typeface.filestorageservice.exception;

import ai.typeface.filestorageservice.constants.ValidationErrorMessages;
import ai.typeface.filestorageservice.dtos.ErrorDetailsDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Date;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler( Exception.class )
    public ResponseEntity<ErrorDetailsDTO> handleGenericException (Exception exception,
                                                                   WebRequest request ) {
        ErrorDetailsDTO errorDetails = new ErrorDetailsDTO( new Date(),
                                                            exception.getMessage (),
                                                            request.getDescription ( false ) );

        return new ResponseEntity<> ( errorDetails, HttpStatus.INTERNAL_SERVER_ERROR );
    }

    @ExceptionHandler ( MaxUploadSizeExceededException.class )
    public ResponseEntity<ErrorDetailsDTO> handleMaxUploadSizeExceededException ( Exception exception,
                                                                                  WebRequest request ) {
        ErrorDetailsDTO errorDetails = new ErrorDetailsDTO ( new Date (),
                                                             ValidationErrorMessages.MAX_ALLOWED_FILE_SIZE_EXCEEDED,
                                                             request.getDescription( false ) );

        return new ResponseEntity<> ( errorDetails, HttpStatus.BAD_REQUEST );
    }

    @ExceptionHandler ( ResourceNotFoundException.class )
    public ResponseEntity<ErrorDetailsDTO> handleResourceNotFoundException ( ResourceNotFoundException exception,
                                                                             WebRequest request ) {
        ErrorDetailsDTO errorDetails = new ErrorDetailsDTO ( new Date(),
                                                             exception.getMessage (),
                                                             request.getDescription ( false ) );

        return new ResponseEntity<> ( errorDetails, HttpStatus.NOT_FOUND );
    }

    @ExceptionHandler ( CustomFileStorageException.class )
    public ResponseEntity<ErrorDetailsDTO> handleCustomFileStorageException ( GoogleCloudStorageException exception,
                                                                              WebRequest request ) {
        ErrorDetailsDTO errorDetails = new ErrorDetailsDTO ( new Date (),
                                                             exception.getMessage (),
                                                             request.getDescription ( false ) );

        return new ResponseEntity<> ( errorDetails, exception.getStatus () );
    }

}
