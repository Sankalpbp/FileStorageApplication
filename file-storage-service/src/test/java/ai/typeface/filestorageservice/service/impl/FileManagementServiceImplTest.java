package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.constants.ApiConstants;
import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.service.CloudStorageService;
import ai.typeface.filestorageservice.service.FileMetadataService;
import com.google.cloud.storage.Blob;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileManagementServiceImplTest {

    private FileManagementServiceImpl fileManagementService;

    @Mock
    private CloudStorageService cloudStorageService;

    @Mock
    private FileMetadataService fileMetadataService;

    @Mock
    private FileMetadata metadata;

    @Mock
    private FileMetadataDTO metadataDTO;

    @BeforeEach
    public void setUp () {
        MockitoAnnotations.openMocks(this);
        fileManagementService = new FileManagementServiceImpl ( cloudStorageService, fileMetadataService );
        Mockito.when ( fileMetadataService.findByUniqueIdentifier(ArgumentMatchers.any ( UUID.class ))).thenReturn ( metadataDTO );
        Mockito.when ( metadataDTO.getFilename () ).thenReturn ( "filename.txt" );
        Mockito.when ( metadata.getFilename () ).thenReturn ( "filename.txt" );
        Mockito.when ( metadataDTO.getFileType () ).thenReturn ( "txt" );
        Mockito.when ( metadataDTO.getFileURL () ).thenReturn ( "fileURL" );
        Mockito.when ( metadataDTO.getUniqueIdentifier () ).thenReturn ( UUID.randomUUID() );
        Mockito.when ( fileMetadataService.updateFileMetadata(ArgumentMatchers.any ( FileMetadataDTO.class ), ArgumentMatchers.any ( UUID.class ))).thenReturn ( metadataDTO );
    }

    @Test
    public void testUploadFileSuccess ( ) {
        MultipartFile file = createTestFile();
        Mockito.when ( cloudStorageService.uploadFile( ArgumentMatchers.eq ( file ), ArgumentMatchers.anyString(), ArgumentMatchers.anyString() ) ).thenReturn ( "fileURL" );
        Mockito.when ( fileMetadataService.save ( ArgumentMatchers.any ( FileMetadataDTO.class ) ) ).thenReturn( metadataDTO );

        fileManagementService.upload( file );
    }

    @Test
    public void testUpdateFileSuccess () {
        MultipartFile file = createTestFile();
        Mockito.when ( cloudStorageService.updateFile( ArgumentMatchers.eq ( file ), ArgumentMatchers.anyString(), ArgumentMatchers.anyString() ) ).thenReturn ( "fileURL" );
        Mockito.when ( fileMetadataService.updateFileData ( ArgumentMatchers.any ( FileMetadataDTO.class ) ) ).thenReturn( metadataDTO );

        fileManagementService.updateFileData ( file, UUID.randomUUID() );
    }

    @Test
    public void testFileMetadataSuccess () {
        Assertions.assertNotNull ( fileManagementService.updateMetadata( metadataDTO, UUID.randomUUID() ));
    }

    @Test
    public void testDeleteSuccess () {
        Mockito.when ( cloudStorageService.deleteFile( ArgumentMatchers.anyString() )).thenReturn ( "File deleted");
        UUID uuid = UUID.randomUUID();
        Assertions.assertEquals ( fileManagementService.delete( uuid ), "File deleted" );
    }

    @Test
    public void testDownloadSuccess () {
        Blob mockedBlob = Mockito.mock ( Blob.class );
        UUID uuid = UUID.randomUUID();
        Mockito.when ( cloudStorageService.downloadFile(ArgumentMatchers.anyString())).thenReturn ( mockedBlob );

        Assertions.assertNotNull ( fileManagementService.download( uuid ));
    }

    @Test
    public void testGetAllFilesSuccess () {
        FileMetadataPageResponse response = Mockito.mock ( FileMetadataPageResponse.class );
        Mockito.when ( fileMetadataService.getAll ( ArgumentMatchers.anyInt (), ArgumentMatchers.anyInt (),
                 ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) ).thenReturn ( response );
        Assertions.assertNotNull ( fileManagementService.getAllFiles( 1, 2, ApiConstants.DEFAULT_SORT_BY_FIELD, ApiConstants.DEFAULT_SORT_DIRECTION ));
    }

    private MultipartFile createTestFile() {
        try {
            File testFile = File.createTempFile("mock", ".txt" );
            FileUtils.writeByteArrayToFile(testFile, new byte [] { 21, 2, 52, 51 });
            return new MockMultipartFile( "file", testFile.getName (), "text/plain", FileUtils.readFileToByteArray(testFile) );
        } catch ( IOException e ) {
            throw new RuntimeException( "Mock file couldn't be created " );
        }
    }
}
