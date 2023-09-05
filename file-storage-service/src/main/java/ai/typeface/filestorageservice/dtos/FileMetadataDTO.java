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

}
