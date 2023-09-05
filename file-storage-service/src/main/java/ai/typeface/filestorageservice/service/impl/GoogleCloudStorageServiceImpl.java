package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.exception.GoogleCloudStorageException;
import ai.typeface.filestorageservice.service.GoogleCloudStorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public String updateFilename ( String existingFilename, String newFilename, String contentType ) {

        LOGGER.debug ( "Started File name update process" );

        existingFilename = gcsDirName + "/" + existingFilename;
        final BlobId blobId = BlobId.of ( gcsBucketId, existingFilename );
        final Blob blob = getStorage ().get ( blobId );

        if ( blob == null ) {
            LOGGER.error ( "An error occurred while updating the file. File not found for provided filename: " + existingFilename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, "File not found for provided filename: " + existingFilename );
        }

        newFilename = gcsDirName + "/" + newFilename;
        final byte [] fileData = blob.getContent ();
        boolean isDeleted = storage.delete ( blobId );

        if ( !isDeleted ) {
            LOGGER.error ( "An error occurred while deleting the file." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the file." );
        }

        BlobInfo updatedBlobInfo = BlobInfo.newBuilder ( gcsBucketId, newFilename )
                .setContentType ( contentType )
                .build ();
        Blob updatedBlob = storage.create ( updatedBlobInfo, fileData );

        if ( updatedBlob == null ) {
            LOGGER.error ( "File update operation failed on Google Cloud Storage. An error occurred while saving the file" );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "File update operation failed on Google Cloud Storage." );
        }

        LOGGER.info ( "File updated successfully to Google Cloud Storage." );
        return updatedBlob.getMediaLink ();
    }

    @Override
    public String updateFile ( MultipartFile file, String filename, String contentType ) {
        LOGGER.debug ( "Started file updating process on Google cloud storage." );

        byte [] fileData;

        try {
            fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );
        } catch ( IOException e ) {
            LOGGER.debug ( "An error occurred while reading provided file. Error: {}", e.getMessage () );
            throw new RuntimeException ( "An error occurred while reading provided file. Error: " + e.getMessage () );
        }

        filename = gcsDirName + "/" + filename;

        final BlobId blobId = BlobId.of ( gcsBucketId, filename );
        final Blob blob = getStorage ().get ( blobId );

        if ( blob == null ) {
            LOGGER.error ( "An error occurred while updating the file. File not found for provided filename: " + filename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, "File not found for provided filename: " + filename );
        }

        boolean isDeleted = storage.delete ( blobId );
        if ( !isDeleted ) {
            LOGGER.error ( "An error occurred while deleting the file." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the file." );
        }

        BlobInfo updatedBlobInfo = BlobInfo.newBuilder ( gcsBucketId, filename )
                .setContentType ( contentType )
                .build ();
        Blob updatedBlob = storage.create ( updatedBlobInfo, fileData );
        if ( updatedBlob == null ) {
            LOGGER.error ( "File update operation failed on Google Cloud Storage. An error occurred while saving the file" );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "File update operation failed on Google Cloud Storage." );
        }

        LOGGER.info ( "File updated successfully to Google Cloud Storage." );
        return updatedBlob.getMediaLink ();
    }

    @Override
    public String deleteFile ( String filename ) {

        LOGGER.debug ( "Started file deletion process on Google Cloud Storage." );

        filename = gcsDirName + "/" + filename;

        final BlobId blobId = BlobId.of ( gcsBucketId, filename );
        final Blob blob = getStorage ().get ( blobId );

        if ( blob == null ) {
            LOGGER.error ( "An error occurred while deleting data. No file found for provided filename: " + filename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, "An error occurred while deleting data. No file found for provided filename: " + filename );
        }

        boolean isDeleted = storage.delete ( blobId );
        if ( !isDeleted ) {
            LOGGER.error ( "An error occurred while deleting the file." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while deleting the file." );
        }
        return "File Successfully deleted from Google Cloud Storage!";
    }

    public List<String> getAllFiles ( ) {
        List<String> filenames = new ArrayList<>();
        Iterable<Blob> blobs = getStorage ().list ( gcsBucketId, Storage.BlobListOption.prefix ( gcsDirName ) ).getValues ();
        if ( blobs == null ) {
            LOGGER.error ( "Files couldn't be fetched from Google Cloud Storage." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "Files couldn't be fetched from Google Cloud Storage." );
        }

        LOGGER.info ( "Files fetched successfully" );
        blobs.forEach ( blob -> filenames.add ( blob.getName () ) );

        return filenames;
    }

    @Override
    public String uploadFile ( MultipartFile file, String filename, String contentType ) {

        LOGGER.debug ( "Started file uploading process on Google Cloud Storage." );

        byte [] fileData;

        try {
            fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );
        } catch ( IOException e ) {
            LOGGER.debug ( "An error occurred while reading provided file. Error: {}", e.getMessage () );
            throw new RuntimeException ( "An error occurred while reading provided file. Error: " + e.getMessage () );
        }
        final Bucket bucket = getStorage ().get ( gcsBucketId, Storage.BucketGetOption.fields () );

        filename = gcsDirName + "/" + filename;
        final Blob blob = bucket.create ( filename, fileData, contentType );

        if ( blob == null ) {
            LOGGER.error ( "File upload failed on Google Cloud Storage." );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "File upload failed on Google Cloud Storage." );
        }

        LOGGER.info ( "File uploaded successfully to Google Cloud Storage." );
        return blob.getMediaLink ();
    }

    @Override
    public Blob downloadFile ( String filename ) {
        BlobId blobId = BlobId.of ( gcsBucketId, gcsDirName + "/" + filename );
        Blob blob = getStorage().get(blobId);
        if ( blob == null ) {
            LOGGER.error ( "File not found for provided filename: " + filename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, "File not found for provided filename: " + filename );
        }

        LOGGER.info ( "Successfully fetched file from Google Cloud Storage" );
        return blob;
    }

    private File convertFile ( MultipartFile file ) {
        File convertedFile = new File ( Objects.requireNonNull ( file.getOriginalFilename () ) );
        try ( FileOutputStream outputStream = new FileOutputStream ( convertedFile ) ){
            outputStream.write ( file.getBytes () );
            LOGGER.debug ( "Converting Multipart file : {}", convertedFile.getName () );
        } catch ( IOException e ) {
            LOGGER.error ( "An error occurred while writing data to the file. Error: " + e.getMessage() );
            throw new RuntimeException ( "An error occurred while writing data to the file. Error: " + e.getMessage() );
        }
        return convertedFile;
    }

    private Storage getStorage ( ) {
        try {
            if ( storage == null ) {
                final InputStream inputStream = new ClassPathResource ( gcsConfigurationsFile ).getInputStream ();
                final StorageOptions options = StorageOptions.newBuilder()
                        .setProjectId ( gcsProjectId )
                        .setCredentials ( GoogleCredentials.fromStream ( inputStream ) )
                        .build ();
                storage = options.getService();
            }
        } catch ( IOException e ) {
            LOGGER.error ( "An error occurred while reading the Google Cloud Storage Configurations file. Please check if the file exists and is accessible. Error: " + e.getMessage() );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while reading the Google Cloud Storage Configurations file. Please check if the file exists and is accessible. Error: " + e.getMessage() );
        }
        return storage;
    }

}
