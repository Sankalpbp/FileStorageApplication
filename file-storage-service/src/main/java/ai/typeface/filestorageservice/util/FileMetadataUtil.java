package ai.typeface.filestorageservice.util;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public final class FileMetadataUtil {

    /* TODO: Create Builder for FileMetadataDTO */
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

    public static FileMetadataDTO createUpdatedFileMetadataDTO ( String filename,
                                                                 UUID uniqueIdentifier,
                                                                 byte [] fileBytes,
                                                                 String fileURL,
                                                                 Date createdAt ) {
        FileMetadataDTO dto = new FileMetadataDTO ();

        dto.setUniqueIdentifier ( uniqueIdentifier );
        dto.setFileType ( filename.split ("\\." )[ 1 ] );
        dto.setFilename ( filename );
        dto.setLastModifiedAt ( new Date () );
        dto.setCreatedAt ( createdAt );
        dto.setSize ( new BigInteger( String.valueOf ( fileBytes.length ) ) );
        dto.setFileURL ( fileURL );

        return dto;
    }

    public static List<String> validateUpdateMetadataWithExistingMetadata ( FileMetadataDTO metadata,
                                                                            FileMetadataDTO existingMetadata,
                                                                            UUID fileIdentifier ) {

        List<String> errors = new ArrayList<>();

        /* TODO: Refactor these validations */

        if ( existingMetadata == null ) {
            /* TODO: throw a custom exception */
            errors.add ( "Invalid Unique Identifier: " + fileIdentifier );
            return errors;
        }

        if ( metadata.getSize () != null && !existingMetadata.getSize ().equals ( metadata.getSize () ) ) {
            /* TODO: throw a custom exception */
            errors.add ( "File Size cannot be updated without updating the file data. Existing size: " + existingMetadata.getSize () +
                            ", New size: " + metadata.getSize () );
        }

        if ( metadata.getFileURL () != null && !existingMetadata.getFileURL().equals ( metadata.getFileURL () ) ) {
            /* TODO: throw a custom exception */
            errors.add ( "File URL cannot be updated without updating the file data. Existing fileURL: " + existingMetadata.getFileURL ()
                            + ", New filetype: " + metadata.getFileURL () );
        }

        if ( metadata.getFileType () != null && !existingMetadata.getFileType().equals ( metadata.getFileType() ) ) {
            /* TODO: throw a custom exception */
            errors.add ( "File type cannot be updated without updating the file data. Existing filetype: " + existingMetadata.getFileType () +
                            ", New filetype: " + metadata.getFileType () );
        }

        if ( metadata.getFilename () != null ) {
            String fileType = metadata.getFilename ().split ( "\\." ) [ 1 ];
            if ( !existingMetadata.getFileType ().equals ( fileType ) ) {
                /* TODO: throw a custom exception */
                errors.add ( "File type cannot be updated without updating the file data. Existing filetype: " + existingMetadata.getFileType ()
                                + ", New filetype: " + fileType );
            }
        }
        /* TODO: Handle these scenarios where file type is extracted out of the file */
        return errors;
    }

}
