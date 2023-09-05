package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.repository.FileMetadataRepository;
import ai.typeface.filestorageservice.service.FileMetadataService;
import ai.typeface.filestorageservice.util.FileMetadataUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FileMetadataServiceImpl implements FileMetadataService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementServiceImpl.class );

    private final FileMetadataRepository repository;

    private final org.modelmapper.ModelMapper mapper;

    public FileMetadataServiceImpl ( FileMetadataRepository repository,
                                     org.modelmapper.ModelMapper mapper ) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public FileMetadataDTO save ( FileMetadataDTO dto ) {
        FileMetadata metadata = dtoToEntity ( dto );
        return entityToDTO ( repository.save ( metadata ) );
    }

    @Override
    public FileMetadataDTO findByUniqueIdentifier ( UUID uniqueIdentifier ) {
        FileMetadata metadata = repository.findById ( uniqueIdentifier )
                                          .orElse ( null );
        return ( metadata == null ) ? null : entityToDTO ( metadata );
    }

    @Override
    public List<FileMetadataDTO> getAll ( ) {
        List<FileMetadata> metadataList = repository.findAll ();
        return metadataList.stream ()
                           .map ( this::entityToDTO )
                           .toList ();
    }

    @Override
    public String deleteByFilename ( String filename ) {
        int numberOfRowsDeleted = repository.deleteByFilename ( filename );
        return ( numberOfRowsDeleted > 0 ) ? "File Metadata Deleted Successfully!"
                                           : "File Delete operation failed!";
    }

    @Override
    public FileMetadataDTO updateFileData ( FileMetadataDTO metadata ) {
        FileMetadata updatedMetadata = dtoToEntity ( metadata );
        return entityToDTO ( repository.save ( updatedMetadata ) );
    }

    public FileMetadataDTO updateFileMetadata ( FileMetadataDTO metadata, UUID fileIdentifier ) {
        FileMetadata existingMetadata = repository.findById ( fileIdentifier )
                                                  .orElse ( null );

        /* TODO: To suppress NPE warnings - think about removing it */
        if ( existingMetadata == null ) {
            return null;
        }

        if ( metadata.getFilename () != null ) {
            existingMetadata.setFilename ( metadata.getFilename ().split ( "\\." ) [ 0 ] + "." + existingMetadata.getFileType() );
        }
        if ( metadata.getFileURL () != null ) {
            existingMetadata.setFileURL ( metadata.getFileURL () );
        }
        /* TODO: Handle these scenarios where file type is extracted out of the file */

        existingMetadata.setLastModifiedAt ( new Date () );

        return entityToDTO ( repository.save ( existingMetadata ) );
    }

    private FileMetadata dtoToEntity ( FileMetadataDTO dto ) {
        return mapper.map ( dto, FileMetadata.class );
    }

    private FileMetadataDTO entityToDTO ( FileMetadata metadata ) {
        return mapper.map ( metadata, FileMetadataDTO.class );
    }

}
