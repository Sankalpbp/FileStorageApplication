package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.constants.InfoMessages;
import com.google.api.gax.paging.Page;
import com.google.cloud.storage.*;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class GoogleCloudStorageServiceImplTest {

    @Mock
    private Storage storage;

    private GoogleCloudStorageServiceImpl storageService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        storageService = new GoogleCloudStorageServiceImpl();

        Storage mockedStorage = Mockito.mock ( Storage.class );
        storage = mockedStorage;
        ReflectionTestUtils.setField(storageService, "storage", mockedStorage);

        String mockGcsBucketId = "gcs-bucket-id"; // Replace with your desired mock value
        ReflectionTestUtils.setField(storageService, "gcsBucketId", mockGcsBucketId);

        String mockGcsDirName = "gcs-dir-name"; // Replace with your desired mock value
        ReflectionTestUtils.setField(storageService, "gcsDirName", mockGcsDirName);

        String mockGcsProjectId = "gcs-project-id"; // Replace with your desired mock value
        ReflectionTestUtils.setField(storageService, "gcsProjectId", mockGcsDirName);

        String mockGcsConfigurationFile = "gcs-configuration-file"; // Replace with your desired mock value
        ReflectionTestUtils.setField(storageService, "gcsConfigurationsFile", mockGcsConfigurationFile );

        Blob mockedBlob = Mockito.mock ( Blob.class );
        Blob updatedMockedBlob = Mockito.mock ( Blob.class );
        byte [] content = { 21, 2, 52, 51 };

        Mockito.when ( mockedBlob.getContent ( )).thenReturn( content );
        Mockito.when ( storage.get ( Mockito.any ( BlobId.class ) ) ).thenReturn ( mockedBlob );
        Mockito.when ( storage.delete ( Mockito.any ( BlobId.class ) ) ).thenReturn ( true );
        Mockito.when ( storage.create ( ArgumentMatchers.any ( BlobInfo.class ), ArgumentMatchers.eq ( content ) )).thenReturn ( updatedMockedBlob );
        Mockito.when ( updatedMockedBlob.getMediaLink() ).thenReturn ( "fileUrl" );

        Bucket mockedBucket = Mockito.mock ( Bucket.class );
        Mockito.when ( storage.get ( ArgumentMatchers.anyString (), ArgumentMatchers.any () ) ).thenReturn( mockedBucket );
        Mockito.when ( mockedBucket.create (ArgumentMatchers.anyString(), ArgumentMatchers.eq ( new byte [] { 21, 2, 52, 51 } ), ArgumentMatchers.eq ("text" ) ) ).thenReturn ( updatedMockedBlob );
    }

    @Test
    public void testUpdateFilenameSuccess ( ) {
        String result = storageService.updateFilename("existingFilename", "newFilename", "contentType");

        Assertions.assertEquals ( "fileUrl", result );
    }

    @Test
    public void testUpdateFileSuccess () {
        String result = storageService.updateFile(createTestFile(), "mock.txt", "text" );
        Assertions.assertEquals ( "fileUrl", result );
    }

    @Test
    public void testUploadFileSuccess () {
        String result = storageService.uploadFile ( createTestFile(), "mock.txt", "text" );
        Assertions.assertEquals ( "fileUrl", result );
    }

    @Test
    public void testDeleteFileSuccess () {
        String result = storageService.deleteFile ( "mock.txt" );
        Assertions.assertEquals (InfoMessages.GCS_DELETION_SUCCESS, result );
    }

    @Test
    public void testDownloadFileSuccess () {
        Assertions.assertNotNull ( storageService.downloadFile( "mock.txt" ) );
    }

    @Test
    public void testGetAllFiles () {
        Iterable<Blob> mockedBlobs = Collections.singletonList(
                Mockito.mock ( Blob.class )
        );

        Page<Blob> mockedPage = Mockito.mock ( Page.class );
        Mockito.when ( storage.list ( ArgumentMatchers.any(), ArgumentMatchers.any () ) ).thenReturn ( mockedPage );
        Mockito.when ( mockedPage.getValues() ).thenReturn( mockedBlobs );

        List<String> filenames = storageService.getAllFiles();

        Assertions.assertEquals(1, filenames.size());
    }

    private MultipartFile createTestFile() {
        try {
            File testFile = File.createTempFile("mock.txt", "" );
            FileUtils.writeByteArrayToFile(testFile, new byte [] { 21, 2, 52, 51 });
            return new MockMultipartFile( "file", testFile.getName (), "text/plain", FileUtils.readFileToByteArray(testFile) );
        } catch ( IOException e ) {
            throw new RuntimeException( "Mock file couldn't be created " );
        }
    }
}
