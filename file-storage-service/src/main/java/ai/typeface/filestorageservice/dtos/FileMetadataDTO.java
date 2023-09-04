package ai.typeface.filestorageservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileMetadataDTO {

    private UUID uniqueIdentifier;

    private String filename;

    private Date createdAt;

    private Date lastModifiedAt;

    private BigInteger size;

    private String fileType;

    private String fileURL;

    public List<String> validate ( ) {
        /* TODO: Create a ValidationError class - a generic validation errors for all types of validations */
        List<String> errors = new ArrayList<>();
        if ( filename.isEmpty () || filename.isBlank () ) {
            errors.add ( "Filename is empty in the provided metadata" );
        }

        if ( fileType.isEmpty () || fileType.isBlank () ) {
            errors.add ( "File type is empty in the provided metadata" );
        }

        return errors;
    }

}
