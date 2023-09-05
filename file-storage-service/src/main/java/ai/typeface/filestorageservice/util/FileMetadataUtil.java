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

        validateField(ValidationErrorMessages.SIZE, existingMetadata.getSize().toString (), metadata.getSize().toString (), ValidationErrorMessages.SIZE);
        validateField(ValidationErrorMessages.FILE_URL, existingMetadata.getFileURL(), metadata.getFileURL(), ValidationErrorMessages.FILE_URL);
        validateField(ValidationErrorMessages.FILE_TYPE, existingMetadata.getFileType(), metadata.getFileType(), ValidationErrorMessages.FILE_TYPE);

        if ( metadata.getFilename () != null ) {
            String fileType = metadata.getFilename ().split ( Symbols.BACKSLASH + Symbols.PERIOD ) [ 1 ];
            validateField(ValidationErrorMessages.FILE_TYPE, existingMetadata.getFileType(), fileType, ValidationErrorMessages.FILE_TYPE);
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

    private static void validateField(String fieldName, String existingValue, String newValue, String errorMessage) {
        if (newValue != null && !existingValue.equals(newValue)) {
            LOGGER.error(getLoggerMessage(errorMessage), existingValue, newValue);
            throw new FileMetadataValidationException(HttpStatus.BAD_REQUEST,
                    getExceptionMessage(errorMessage, existingValue, newValue));
        }
    }

}
