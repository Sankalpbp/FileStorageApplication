package ai.typeface.filestorageservice.service;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;

import java.util.List;
import java.util.UUID;

public interface FileMetadataService {

    public FileMetadataDTO save ( FileMetadataDTO metadata );

    public FileMetadataDTO findByUniqueIdentifier ( UUID uniqueIdentifier );

    public List<FileMetadataDTO> getAll ( );

}
