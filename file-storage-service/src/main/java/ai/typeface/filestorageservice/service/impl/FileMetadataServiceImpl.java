package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.constants.ApiConstants;
import ai.typeface.filestorageservice.constants.FailureMessages;
import ai.typeface.filestorageservice.constants.InfoMessages;
import ai.typeface.filestorageservice.constants.Symbols;
import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.exception.ResourceNotFoundException;
import ai.typeface.filestorageservice.repository.FileMetadataRepository;
import ai.typeface.filestorageservice.service.FileMetadataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                                          .orElseThrow ( () -> new ResourceNotFoundException ( ApiConstants.FILE_METADATA,
                                                                                               ApiConstants.UNIQUE_IDENTIFIER,
                                                                                               uniqueIdentifier.toString () )
                                          );
        return entityToDTO ( metadata );
    }

    @Override
    public FileMetadataPageResponse getAll (int pageNumber, int pageSize, String sortBy, String sortDir ) {
        Sort sort = sortDir.equalsIgnoreCase ( Sort.Direction.ASC.name () )
                            ? Sort.by ( sortBy ).ascending ()
                            : Sort.by ( sortBy ).descending ();

        Pageable pageable = PageRequest.of ( pageNumber, pageSize, sort );
        Page<FileMetadata> pageOfFileMetadata = repository.findAll ( pageable );

        List<FileMetadata> fileMetatadaList = pageOfFileMetadata.getContent ();

        List<FileMetadataDTO> content =  fileMetatadaList.stream ()
                                                         .map ( this::entityToDTO )
                                                         .toList ();

        return FileMetadataPageResponse.builder ()
                                       .content ( content )
                                       .pageNumber ( pageNumber )
                                       .pageSize( pageSize )
                                       .totalElements ( pageOfFileMetadata.getTotalElements() )
                                       .totalPages ( pageOfFileMetadata.getTotalPages() )
                                       .last ( pageOfFileMetadata.isLast () )
                                       .build ();
    }

    @Override
    public String deleteByFilename ( String filename ) {
        int numberOfRowsDeleted = repository.deleteByFilename ( filename );
        if ( numberOfRowsDeleted > 0 ) {
            return InfoMessages.METADATA_DELETION_SUCCESS;
        } else {
            LOGGER.error (FailureMessages.METADATA_DELETION_FAILED );
            throw new RuntimeException (FailureMessages.METADATA_DELETION_FAILED );
        }
    }

    @Override
    public FileMetadataDTO updateFileData ( FileMetadataDTO metadata ) {
        FileMetadata updatedMetadata = dtoToEntity ( metadata );
        return entityToDTO ( repository.save ( updatedMetadata ) );
    }

    public FileMetadataDTO updateFileMetadata ( FileMetadataDTO metadata, UUID fileIdentifier ) {
        FileMetadata existingMetadata = repository.findById ( fileIdentifier )
                                                  .orElseThrow ( () -> new ResourceNotFoundException ( ApiConstants.FILE_METADATA,
                                                                                                       ApiConstants.UNIQUE_IDENTIFIER,
                                                                                                       fileIdentifier.toString () ) );

        if ( metadata.getFilename () != null ) {
            existingMetadata.setFilename ( metadata.getFilename ().split ( Symbols.BACKSLASH + Symbols.PERIOD ) [ 0 ] + Symbols.PERIOD + existingMetadata.getFileType() );
        }
        if ( metadata.getFileURL () != null ) {
            existingMetadata.setFileURL ( metadata.getFileURL () );
        }

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
