package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.repository.FileMetadataRepository;
import ai.typeface.filestorageservice.service.FileMetadataService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class FileMetadataServiceImpl implements FileMetadataService {

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
        return (numberOfRowsDeleted > 0) ? "File Metadata Deleted Successfully!"
                         : "File Delete operation failed!";
    }

    private FileMetadata dtoToEntity ( FileMetadataDTO dto ) {
        return mapper.map ( dto, FileMetadata.class );
    }

    private FileMetadataDTO entityToDTO ( FileMetadata metadata ) {
        return mapper.map ( metadata, FileMetadataDTO.class );
    }

}
