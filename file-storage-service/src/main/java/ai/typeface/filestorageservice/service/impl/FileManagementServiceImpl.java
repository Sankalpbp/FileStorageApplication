package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.service.CloudStorageService;
import ai.typeface.filestorageservice.service.FileManagementService;
import com.google.cloud.storage.Blob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class FileManagementServiceImpl implements FileManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementServiceImpl.class );

    private final CloudStorageService cloudStorageService;

    public FileManagementServiceImpl ( CloudStorageService cloudStorageService ) {
        this.cloudStorageService = cloudStorageService;
    }

    @Override
    public String update ( MultipartFile file, String filename ) {
        String originalFileName = file.getOriginalFilename();
        if ( originalFileName == null ) {
            /* TODO: Throw a relevant exception */
            return "file name is not present";
        }

        Path path = new File( originalFileName ).toPath ();
        String contentType = "";
        try {
            contentType = Files.probeContentType ( path );
        } catch (IOException e) {
            /* Throw a Custom exception for GCS here */
            throw new RuntimeException(e);
        }
        return cloudStorageService.updateFile( file, filename, contentType );
    }

    @Override
    public String delete ( String filename ) {
        return cloudStorageService.deleteFile ( filename );
    }

    @Override
    public Blob download (String filename ) {
        return cloudStorageService.downloadFile ( filename );
    }

    @Override
    public String upload ( MultipartFile file ) {

        LOGGER.debug ( "Started file upload operation." );

        String originalFileName = file.getOriginalFilename();
        if ( originalFileName == null ) {
            /* TODO: Throw a relevant exception */
            return "file name is not present";
        }

        Path path = new File( originalFileName ).toPath ();
        String fileIdentifier = "";

        try {
            String contentType = Files.probeContentType ( path );
            fileIdentifier = cloudStorageService.uploadFile( file, originalFileName, contentType );

            if ( fileIdentifier != null ) {
                /* TODO: update the log message to show the file name as well */
                LOGGER.info ( "File uploaded successfully, file name: 'dummy-name' and url: {}", fileIdentifier );
            }
        } catch (IOException e) {
            /* Throw a Custom exception for GCS here */
            throw new RuntimeException(e);
        }

        return fileIdentifier;
    }

    private String getHashOfFileName ( String filename ) throws NoSuchAlgorithmException {
        final MessageDigest digest = MessageDigest.getInstance ( "SHA-256" );
        byte [ ] inputBytes = filename.getBytes ( StandardCharsets.UTF_8 );
        byte [ ] hashedBytes = digest.digest ( inputBytes );

        final StringBuilder hexString = new StringBuilder ();

        for ( final byte hashedByte : hashedBytes ) {
            final String hex = Integer.toHexString ( 0xff & hashedByte );
            if ( hex.length () == 1 ) {
                hexString.append ( '0' );
            }
            hexString.append ( hex );
        }

        return hexString.toString ();
    }

}
