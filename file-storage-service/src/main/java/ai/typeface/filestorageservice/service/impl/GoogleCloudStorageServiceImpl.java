package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.service.GoogleCloudStorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class GoogleCloudStorageServiceImpl implements GoogleCloudStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( GoogleCloudStorageServiceImpl.class );

    private Storage storage;

    @Value ( "${gcs.config.file}" )
    private String gcsConfigurationsFile;

    @Value ( "${gcs.project.id}" )
    private String gcsProjectId;

    @Value ( "${gcs.bucket.id}" )
    private String gcsBucketId;

    @Value ( "${gcs.dir.name}" )
    private String gcsDirName;

    @Override
    public String updateFile (MultipartFile file, String filename, String contentType ) {
        /* TODO: Many of these strings contain repeating literals - put them in the Labels interface */
        LOGGER.debug ( "Started file updating process on Google cloud storage." );

        try {
            byte [] fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );

            filename = gcsDirName + "/" + filename;

            /* TODO: update the name of the file: my-bucket/documents/example.txt */
            final BlobId blobId = BlobId.of ( gcsBucketId, filename );
            final Blob blob = getStorage ().get ( blobId );

            if ( blob != null ) {
                boolean deleted = storage.delete ( blobId );

                BlobInfo updatedBlobInfo = BlobInfo.newBuilder ( gcsBucketId, filename )
                                                   .setContentType ( contentType )
                                                   .build ();
                Blob updatedBlob = storage.create ( updatedBlobInfo, fileData );

                if ( updatedBlob != null ) {
                    /* TODO: Replace the messages with the constant variables defined in the interfaces */
                    LOGGER.info ( "File updated successfully to Google Cloud Storage." );
                    return updatedBlob.getMediaLink ();
                }
            }

        } catch ( Exception e ) {
            LOGGER.debug ( "An error occurred while updating data. Exception: " + e );
            /* TODO: Throw a custom exception */
        }
        return "File Update failed";
    }

    @Override
    public String deleteFile ( String filename ) {

        LOGGER.debug ( "Started file deletion process on Google Cloud Storage." );

        filename = gcsDirName + "/" + filename;

        try {
            final BlobId blobId = BlobId.of ( gcsBucketId, filename );
            final Blob blob = getStorage ().get ( blobId );

            if ( blob != null ) {
                boolean isDeleted = storage.delete ( blobId );
                if ( isDeleted ) {
                    return "File Successfully deleted from Google Cloud Storage!";
                }
            }
        } catch (IOException e) {
            LOGGER.debug ( "An error occurred while deleting data. Exception: " + e );
            /* TODO: Throw a custom exception */
        }

        return "Delete operation failed.";
    }

    public List<String> getAllFiles ( ) {
        List<String> filenames = new ArrayList<>();
        try {
            Iterable<Blob> blobs = getStorage ().list ( gcsBucketId, Storage.BlobListOption.prefix ( gcsDirName )).getValues ();
            blobs.forEach ( blob -> filenames.add ( blob.getName () ) );
        } catch (IOException e) {
            LOGGER.debug ( "An error occurred while deleting data. Exception: " + e );
            /* TODO: Throw a custom exception */
        }

        return filenames;
    }

    @Override
    public String uploadFile ( MultipartFile file, String fileName, String contentType ) {

        LOGGER.debug ( "Started file uploading process on Google Cloud Storage." );

        try {
            byte [ ] fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );

            final Bucket bucket = getStorage ().get ( gcsBucketId, Storage.BucketGetOption.fields () );

            /* TODO: Replace the String literals with constant variables defined in an interface */
            /* TODO: Get the stored file name out of the method and create it outside for more readability */
            final Blob blob = bucket.create ( gcsDirName + "/" + fileName, fileData, contentType );

            if ( blob != null ) {
                /* TODO: Replace the messages with the constant variables defined in the interfaces */
                LOGGER.info ( "File uploaded successfully to Google Cloud Storage." );
                return blob.getMediaLink ();
            }

        } catch ( Exception e ) {
            LOGGER.debug ( "An error occurred while uploading data. Exception: " + e );
            /* TODO: Throw a custom exception */
        }

        return "File Upload failed";
    }

    @Override
    public Blob downloadFile ( String filename ) {

        BlobId blobId = BlobId.of(gcsBucketId, gcsDirName + "/" + filename);
        try {
            return getStorage().get(blobId);
        } catch ( Exception e ) {
            LOGGER.debug ( "An error occurred while downloading data. Exception: " + e );
            /* TODO: Throw a custom exception */
        }
        return null;
    }

    private File convertFile ( MultipartFile file ) {
        File convertedFile = new File (Objects.requireNonNull(file.getOriginalFilename()));
        try ( FileOutputStream outputStream = new FileOutputStream ( convertedFile ) ){
            outputStream.write ( file.getBytes () );
            LOGGER.debug ( "Converting Multipart file : {}", convertedFile );
        } catch ( IOException e ) {
            /* TODO: Throw a custom exception here */
            e.printStackTrace();
        }
        return convertedFile;
    }

    private Storage getStorage ( ) throws IOException {
        if ( storage == null ) {
            final InputStream inputStream = new ClassPathResource ( gcsConfigurationsFile ).getInputStream ();
            final StorageOptions options = StorageOptions.newBuilder()
                    .setProjectId ( gcsProjectId )
                    .setCredentials ( GoogleCredentials.fromStream ( inputStream ) )
                    .build ();
            storage = options.getService();
        }
        return storage;
    }

}
