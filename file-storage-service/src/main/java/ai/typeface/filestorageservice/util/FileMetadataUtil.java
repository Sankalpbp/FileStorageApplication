package ai.typeface.filestorageservice.util;

import ai.typeface.filestorageservice.constants.Symbols;
import ai.typeface.filestorageservice.constants.ValidationErrorMessages;
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
        dto.setFileType ( filename.split (Symbols.BACKSLASH + Symbols.PERIOD )[ 1 ] );
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
        dto.setFileType ( filename.split (Symbols.BACKSLASH + Symbols.PERIOD )[ 1 ] );
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

        if ( metadata.getSize () != null && !existingMetadata.getSize ().equals ( metadata.getSize () ) ) {
            LOGGER.error ( getLoggerMessage ( ValidationErrorMessages.SIZE ), existingMetadata.getSize (), metadata.getSize () );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST,
                                                        getExceptionMessage ( ValidationErrorMessages.SIZE,
                                                                                existingMetadata.getSize ().toString (),
                                                                                metadata.getSize ().toString () ) );
        }

        if ( metadata.getFileURL () != null && !existingMetadata.getFileURL().equals ( metadata.getFileURL () ) ) {
            LOGGER.error ( getLoggerMessage ( ValidationErrorMessages.FILE_URL ), existingMetadata.getFileURL (), metadata.getFileURL () );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST,
                                                        getExceptionMessage ( ValidationErrorMessages.FILE_URL,
                                                                                existingMetadata.getFileURL(),
                                                                                metadata.getFileURL() ) );
        }

        if ( metadata.getFileType () != null && !existingMetadata.getFileType().equals ( metadata.getFileType() ) ) {
            LOGGER.error ( getLoggerMessage ( ValidationErrorMessages.FILE_TYPE ), existingMetadata.getFileType (), metadata.getFileType () );
            throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST,
                                                        getExceptionMessage ( ValidationErrorMessages.FILE_URL,
                                                                                existingMetadata.getFileType(),
                                                                                metadata.getFileType() ) );
        }

        if ( metadata.getFilename () != null ) {
            String fileType = metadata.getFilename ().split ( Symbols.BACKSLASH + Symbols.PERIOD ) [ 1 ];
            if ( !existingMetadata.getFileType ().equals ( fileType ) ) {
                LOGGER.error ( getLoggerMessage ( ValidationErrorMessages.FILE_TYPE ), existingMetadata.getFileType(), metadata.getFileType() );
                throw new FileMetadataValidationException ( HttpStatus.BAD_REQUEST,
                                                            getExceptionMessage ( ValidationErrorMessages.FILE_TYPE,
                                                                                    existingMetadata.getFileType(),
                                                                                    metadata.getFileType() ) );
            }
        }

    }

    private static String getExceptionMessage ( String attributeName, String existingAttributeValue, String newAttributeValue ) {
        return String.format ( "%s %s %s %s: %s, %s %s: %s", attributeName,
                                                             ValidationErrorMessages.UPDATE_ERROR_MESSAGE,
                                                             ValidationErrorMessages.EXISTING,
                                                             attributeName,
                                                             existingAttributeValue,
                                                             ValidationErrorMessages.NEW,
                                                             attributeName,
                                                             newAttributeValue );
    }

    private static String getLoggerMessage ( String attribute ) {
        return String.format ( "%s %s %s %s: {}%s %s %s: {}", attribute,
                                                              ValidationErrorMessages.UPDATE_ERROR_MESSAGE,
                                                              ValidationErrorMessages.EXISTING,
                                                              attribute,
                                                              Symbols.COMMA,
                                                              ValidationErrorMessages.NEW,
                                                              attribute );
    }

}
