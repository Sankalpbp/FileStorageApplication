package ai.typeface.filestorageservice.util;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.exception.FileMetadataValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class FileMetadataUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileMetadataUtil.class );

    /* TODO: Create Builder for FileMetadataDTO */
    public static FileMetadataDTO createFileMetadataDTO ( String filename,
                                                          UUID uniqueIdentifier,
                                                          int fileBytes,
                                                          String fileURL ) {
        FileMetadataDTO dto = new FileMetadataDTO ();

        dto.setUniqueIdentifier ( uniqueIdentifier );
        dto.setFileType ( filename.split ("\\." )[ 1 ] );
        dto.setFilename ( filename );
        dto.setCreatedAt ( new Date () );
        dto.setSize ( new BigInteger( String.valueOf ( fileBytes ) ) );
        dto.setFileURL ( fileURL );

        return dto;
    }

    public static FileMetadataDTO createUpdatedFileMetadataDTO ( String filename,
                                                                 UUID uniqueIdentifier,
                                                                 int fileBytes,
                                                                 String fileURL,
                                                                 Date createdAt ) {
        FileMetadataDTO dto = new FileMetadataDTO ();

        dto.setUniqueIdentifier ( uniqueIdentifier );
        dto.setFileType ( filename.split ("\\." )[ 1 ] );
        dto.setFilename ( filename );
        dto.setLastModifiedAt ( new Date () );
        dto.setCreatedAt ( createdAt );
        dto.setSize ( new BigInteger( String.valueOf ( fileBytes ) ) );
        dto.setFileURL ( fileURL );

        return dto;
    }

    /* TODO: This needs some serious refactoring */
    public static void validateUpdateMetadataWithExistingMetadata ( FileMetadataDTO metadata,
                                                                            FileMetadataDTO existingMetadata
                                                                            ) {

        List<String> errors = new ArrayList<>();

        if ( metadata.getSize () != null && !existingMetadata.getSize ().equals ( metadata.getSize () ) ) {
            LOGGER.error ( "File Size cannot be updated without updating the file data. Existing size: {}, New size: {}",
                    existingMetadata.getSize (), metadata.getSize () );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST, "File size cannot be updated without updating the file data. Existing size: " + existingMetadata.getSize ()
                                                        + ", New size: " + metadata.getSize () );
        }

        if ( metadata.getFileURL () != null && !existingMetadata.getFileURL().equals ( metadata.getFileURL () ) ) {
            LOGGER.error ( "File URL cannot be updated without updating the file data. Existing fileURL: {}, New fileURL: {}",
                    existingMetadata.getSize (), metadata.getSize () );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST, "File URL cannot be updated without updating the file data. Existing fileURL: " + existingMetadata.getFileURL ()
                                                        + ", New filetype: " + metadata.getFileURL () );
        }

        if ( metadata.getFileType () != null && !existingMetadata.getFileType().equals ( metadata.getFileType() ) ) {
            LOGGER.error ( "File type cannot be updated without updating the file data. Existing fileType: {}, New fileType: {}",
                    existingMetadata.getSize (), metadata.getSize () );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST, "File type cannot be updated without updating the file data. Existing filetype: " + existingMetadata.getFileType ()
                                                        + ", New filetype: " + metadata.getFileType () );
        }

        if ( metadata.getFilename () != null ) {
            String fileType = metadata.getFilename ().split ( "\\." ) [ 1 ];
            if ( !existingMetadata.getFileType ().equals ( fileType ) ) {
                LOGGER.error ( "File type cannot be updated without updating the file data. Existing fileType: {}, New fileType: {}",
                        existingMetadata.getSize (), metadata.getSize () );
                throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST, "File type cannot be updated without updating the file data. Existing filetype: " + existingMetadata.getFileType ()
                                                        + ", New filetype: " + fileType );
            }
        }

    }

}
