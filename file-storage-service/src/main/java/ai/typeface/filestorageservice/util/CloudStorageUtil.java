package ai.typeface.filestorageservice.util;

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
            LOGGER.error ( "An error occurred while reading the file name from the provided file to upload / update: " );
            throw new RuntimeException ( "An error occurred while reading the file name from the provided file to upload / update." );
        }

        String [] partsOfFileName = originalFileName.split ( "\\." );
        int numberOfStrings = ( int ) Arrays.stream ( partsOfFileName )
                                            .filter ( s -> !s.isBlank () )
                                            .count ();

        if ( numberOfStrings < 2 || ( partsOfFileName [ 0 ].isBlank () || partsOfFileName [ partsOfFileName.length - 1 ].isBlank() ) ) {
            LOGGER.error ( "An error occurred while reading the file. Malformed filename. Should be of type: name.type" );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST,
                                    "An error occurred while reading the file. Malformed filename" );
        }

        return partsOfFileName [ partsOfFileName.length - 1 ];
    }

    /* TODO: Throw a custom exception from here */
    public static String getContentType ( String originalFileName ) {
        try {
            Path path = new File( originalFileName ).toPath ();
            return Files.probeContentType ( path );
        } catch ( IOException e ) {
            LOGGER.error ( "An error occurred while reading the content type of the provided file to upload / update." );
            throw new RuntimeException ( "An error occurred while reading the content type of the provided file to upload / update." );
        }
    }
}
