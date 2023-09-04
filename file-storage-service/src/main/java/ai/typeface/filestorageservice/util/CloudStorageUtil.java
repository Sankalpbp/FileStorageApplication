package ai.typeface.filestorageservice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class CloudStorageUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger ( CloudStorageUtil.class );

    public static String checkAndReturnOriginalFileName ( MultipartFile file ) {
        String originalFileName = file.getOriginalFilename();
        if ( originalFileName == null ) {
            /* TODO: Throw a relevant exception */
            LOGGER.error ( "filename is empty" );
            return "";
        }
        return originalFileName;
    }

    /* TODO: Throw a custom exception from here */
    public static String getContentType ( String originalFileName ) throws IOException {
        Path path = new File( originalFileName ).toPath ();
        return Files.probeContentType ( path );
    }
}
