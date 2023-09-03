package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.service.FileManagementService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class FileManagementServiceImpl implements FileManagementService {

    @Override
    public String upload ( MultipartFile file,
                           String metadata ) {

        String filePath = metadata + file.getOriginalFilename();

        try {
            Path fileToSave = Path.of ( filePath );
            Files.createDirectories( fileToSave.getParent () );
            Files.write ( fileToSave, file.getBytes () );
            return getHashOfFileName ( filePath );
        } catch ( IOException | NoSuchAlgorithmException e ) {
            e.printStackTrace();
        }

        return "File couldn't be saved";
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
