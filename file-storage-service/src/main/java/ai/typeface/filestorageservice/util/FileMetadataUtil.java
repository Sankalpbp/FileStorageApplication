package ai.typeface.filestorageservice.util;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

public final class FileMetadataUtil {

    public static FileMetadataDTO createFileMetadataDTO ( String filename,
                                                          UUID uniqueIdentifier,
                                                          byte [] fileBytes,
                                                          String fileURL ) {
        FileMetadataDTO dto = new FileMetadataDTO ();

        dto.setUniqueIdentifier ( uniqueIdentifier );
        dto.setFileType ( filename.split ("\\." )[ 1 ] );
        dto.setFilename ( filename );
        dto.setCreatedAt ( new Date () );
        dto.setSize ( new BigInteger( String.valueOf ( fileBytes.length ) ) );
        dto.setFileURL ( fileURL );

        return dto;
    }

}
