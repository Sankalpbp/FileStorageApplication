package ai.typeface.filestorageservice.repository;

import ai.typeface.filestorageservice.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    public int deleteByFilename ( String filename );
}
