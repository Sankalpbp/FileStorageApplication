package ai.typeface.filestorageservice.service;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;

import java.util.List;
import java.util.UUID;

public interface FileMetadataService {

    public FileMetadataDTO save ( FileMetadataDTO metadata );

    public FileMetadataDTO findByUniqueIdentifier ( UUID uniqueIdentifier );

    public FileMetadataPageResponse getAll (int pageNumber, int pageSize, String sortBy, String sortDir );

    public String deleteByFilename ( String filename );

    public FileMetadataDTO updateFileData ( FileMetadataDTO metadata );

    public FileMetadataDTO updateFileMetadata ( FileMetadataDTO metadata, UUID fileIdentifier );

}
