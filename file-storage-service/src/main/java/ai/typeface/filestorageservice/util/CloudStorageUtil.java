package ai.typeface.filestorageservice.util;

import ai.typeface.filestorageservice.constants.FailureMessages;
import ai.typeface.filestorageservice.constants.Symbols;
import ai.typeface.filestorageservice.constants.ValidationErrorMessages;
import ai.typeface.filestorageservice.exception.FileMetadataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class CloudStorageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger ( CloudStorageUtil.class );

    public static String checkAndReturnOriginalFileName ( MultipartFile file ) {
        String originalFileName = file.getOriginalFilename();
        if ( originalFileName == null ) {
            LOGGER.error (ValidationErrorMessages.ERROR_READING_FILE);
            throw new RuntimeException ( ValidationErrorMessages.ERROR_READING_FILE );
        }

        String [] partsOfFileName = originalFileName.split (Symbols.BACKSLASH + Symbols.PERIOD);
        int numberOfStrings = ( int ) Arrays.stream ( partsOfFileName )
                                            .filter ( s -> !s.isBlank () )
                                            .count ();

        if ( numberOfStrings < 2 || ( partsOfFileName [ 0 ].isBlank () || partsOfFileName [ partsOfFileName.length - 1 ].isBlank() ) ) {
            LOGGER.error ( ValidationErrorMessages.MALFORMED_FILENAME );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST, ValidationErrorMessages.MALFORMED_FILENAME );
        }

        return originalFileName;
    }

    public static String getContentType ( String originalFileName ) {
        try {
            Path path = new File( originalFileName ).toPath ();
            return Files.probeContentType ( path );
        } catch ( IOException e ) {
            LOGGER.error ( FailureMessages.ERROR_READING_CONTENT_TYPE );
            throw new RuntimeException ( FailureMessages.ERROR_READING_CONTENT_TYPE );
        }
    }
}
