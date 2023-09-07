package ai.typeface.filestorageservice.controller;

import ai.typeface.filestorageservice.constants.ApiConstants;
import ai.typeface.filestorageservice.constants.InfoMessages;
import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.dtos.FileMetadataPageResponse;
import ai.typeface.filestorageservice.service.FileManagementService;
import com.google.cloud.storage.Blob;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileManagementControllerTest {

    private FileManagementController controller;

    @Mock
    private FileManagementService service;

    @BeforeEach
    public void setUp () {
        MockitoAnnotations.openMocks( this );
        controller = new FileManagementController( service );

        String mockedMaxAllowedFileSize = "50MB"; // Replace with your desired mock value
        ReflectionTestUtils.setField( controller, "maxAllowedFileSize", mockedMaxAllowedFileSize );
    }

    @Test
    public void testDeleteSuccess () {
        Mockito.when ( service.delete (ArgumentMatchers.any ( UUID.class )) ).thenReturn (InfoMessages.GCS_DELETION_SUCCESS );
        ResponseEntity<String > response = controller.deleteFile( UUID.randomUUID() );
        Assertions.assertNotNull ( response.getBody() );
        Assertions.assertEquals ( HttpStatus.OK, response.getStatusCode() );
    }

    @Test
    public void testUpdateFileDataSuccess () {
        FileMetadataDTO mockedMetadata = Mockito.mock ( FileMetadataDTO.class );
        Mockito.when ( service.updateFileData ( ArgumentMatchers.any ( MultipartFile.class ), ArgumentMatchers.any ( UUID.class ) )).thenReturn ( mockedMetadata );

        ResponseEntity<FileMetadataDTO> response = controller.updateFile(createTestFile(), UUID.randomUUID());
        Assertions.assertNotNull( response.getBody() );
        Assertions.assertEquals ( HttpStatus.OK, response.getStatusCode() );
    }

    @Test
    public void testUploadFileSuccess () {
        MultipartFile mockedFile = createTestFile();
        Mockito.when ( service.upload(ArgumentMatchers.any(MultipartFile.class))).thenReturn ( UUID.randomUUID());

        ResponseEntity<UUID> response = controller.uploadFile(mockedFile);
        Assertions.assertNotNull( response.getBody() );
        Assertions.assertEquals ( HttpStatus.CREATED, response.getStatusCode() );
    }

    @Test
    public void testUpdateFileMetadataSuccess () {
        FileMetadataDTO mockedMetadataDTO = Mockito.mock( FileMetadataDTO.class);
        Mockito.when ( service.updateMetadata (  ArgumentMatchers.any ( FileMetadataDTO.class ), ArgumentMatchers.any ( UUID.class ) )).thenReturn ( mockedMetadataDTO );

        ResponseEntity<FileMetadataDTO> response = controller.updateFile(UUID.randomUUID(), mockedMetadataDTO);
        Assertions.assertNotNull( response.getBody() );
        Assertions.assertEquals ( HttpStatus.OK, response.getStatusCode() );
    }

    @Test
    public void testGetAllFilesSuccess () {
        FileMetadataPageResponse response = Mockito.mock ( FileMetadataPageResponse.class );
        Mockito.when ( service.getAllFiles ( ArgumentMatchers.anyInt (), ArgumentMatchers.anyInt (),
                ArgumentMatchers.anyString(), ArgumentMatchers.anyString()) ).thenReturn ( response );
        ResponseEntity<FileMetadataPageResponse> responseObject = controller.getAllFiles( 1, 2, ApiConstants.DEFAULT_SORT_BY_FIELD, ApiConstants.DEFAULT_SORT_DIRECTION );
        Assertions.assertNotNull ( responseObject );
        Assertions.assertEquals ( HttpStatus.NO_CONTENT, responseObject.getStatusCode() );
    }

    @Test
    public void testDownloadSuccess () {
        Blob mockedBlob = Mockito.mock ( Blob.class );
        HttpHeaders mockedHttpHeaders = Mockito.mock ( HttpHeaders.class );
        Mockito.when ( service.download ( ArgumentMatchers.any ( UUID.class ) ) ).thenReturn( mockedBlob );
        Mockito.when ( service.getHttpHeaders ( ArgumentMatchers.anyString (), ArgumentMatchers.anyString())).thenReturn ( mockedHttpHeaders );
        Mockito.when ( mockedBlob.getContent() ).thenReturn ( new byte [] { 12, 23, 34, 45 } );
        Mockito.when ( mockedBlob.getContentType() ).thenReturn ( "multipart/form-data" );

        ResponseEntity<Resource> response = controller.downloadFile ( UUID.randomUUID() );
        Assertions.assertNotNull ( response.getBody() );
        Assertions.assertEquals ( HttpStatus.OK, response.getStatusCode ());
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
