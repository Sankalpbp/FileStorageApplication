package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.constants.ApiConstants;
import ai.typeface.filestorageservice.constants.InfoMessages;
import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.repository.FileMetadataRepository;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;

import java.util.*;

public class FileMetadataServiceImplTest {

    private FileMetadataServiceImpl metadataService;

    @Mock
    private FileMetadataDTO metadataDTO;

    @Mock
    private FileMetadataRepository metadataRepository;

    @Mock
    private FileMetadata metadata;

    @Mock
    ModelMapper mapper;

    @BeforeEach
    public void setUp () {
        MockitoAnnotations.openMocks(this);
        metadataService = new FileMetadataServiceImpl ( metadataRepository, mapper );
        Mockito.when ( metadataRepository.save ( metadata ) ).thenReturn ( metadata );
        Mockito.when ( mapper.map ( metadataDTO, FileMetadata.class ) ).thenReturn ( metadata );
        Mockito.when ( mapper.map ( metadata, FileMetadataDTO.class ) ).thenReturn ( metadataDTO );
        Mockito.when ( metadataRepository.findById ( ArgumentMatchers.any ( UUID.class ))).thenReturn (Optional.of(metadata));
    }

    @Test
    public void testSaveSuccessSuccess () {
        Assertions.assertNotNull ( metadataService.save ( metadataDTO ) );
    }

    @Test
    public void testDeleteByFilename () {
        Mockito.when ( metadataRepository.deleteByFilename( ArgumentMatchers.anyString () )).thenReturn ( 1 );
        Assertions.assertEquals ( metadataService.deleteByFilename( "filename" ), InfoMessages.METADATA_DELETION_SUCCESS );
    }

    @Test
    public void testUpdateFileDataSuccess () {
        Assertions.assertNotNull ( metadataService.updateFileData(metadataDTO));
    }

    @Test
    public void testFindByUniqueIdentifierSuccess () {
        UUID uuid = UUID.randomUUID();
        FileMetadataDTO metadata = metadataService.findByUniqueIdentifier ( uuid );
        Assertions.assertNotNull( metadata );
    }

    @Test
    public void testUpdateFileMetadataSuccess () {
        UUID uuid = UUID.randomUUID();
        Assertions.assertNotNull ( metadataService.updateFileMetadata( metadataDTO, uuid ) );
    }

    @Test
    public void testGetAllSuccess () {
        List<FileMetadata> mockedMetadata = Arrays.asList (
                Mockito.mock ( FileMetadata.class ),
                Mockito.mock ( FileMetadata.class )
        );
        Page<FileMetadata> mockedPage = Mockito.mock ( Page.class );
        Mockito.when ( metadataRepository.findAll ( ArgumentMatchers.any ( Pageable.class ) ) ).thenReturn ( mockedPage);
        Mockito.when ( mockedPage.getContent() ).thenReturn( mockedMetadata );

        Assertions.assertEquals ( 2, metadataService.getAll ( 1, 2,
                ApiConstants.UNIQUE_IDENTIFIER, ApiConstants.DEFAULT_SORT_DIRECTION ).getContent().size () );
    }

}
