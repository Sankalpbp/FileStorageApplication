package ai.typeface.filestorageservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;

import java.math.BigInteger;
import java.util.UUID;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table ( name = "metadata" )
public class FileMetadata {

    @Id
    private UUID uniqueIdentifier;

    @Column (
            name = "filename",
            nullable = false
    )
    private String filename;

    @Column (
            name = "createdAt",
            nullable = false
    )
    private Date createdAt;

    @Column (
            name = "lastModifiedAt"
    )
    private Date lastModifiedAt;

    @Column (
            name = "size",
            nullable = false
    )
    private BigInteger size;

    @Column (
            name = "fileType",
            nullable = false
    )
    private String fileType;

    @Column (
            name = "fileURL",
            nullable = false
    )
    private String fileURL;

}
