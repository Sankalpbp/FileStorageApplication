package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.constants.FailureMessages;
import ai.typeface.filestorageservice.constants.InfoMessages;
import ai.typeface.filestorageservice.constants.Symbols;
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

        LOGGER.debug ( InfoMessages.FILE_UPDATE_STARTED );

        existingFilename = gcsDirName + Symbols.SLASH + existingFilename;
        final BlobId blobId = BlobId.of ( gcsBucketId, existingFilename );
        final Blob blob = getStorage ().get ( blobId );

        if ( blob == null ) {
            LOGGER.error ( FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + existingFilename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + existingFilename );
        }

        newFilename = gcsDirName + Symbols.SLASH + newFilename;
        final byte [] fileData = blob.getContent ();
        boolean isDeleted = storage.delete ( blobId );

        if ( !isDeleted ) {
            LOGGER.error (FailureMessages.GCS_DELETION_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_DELETION_FAILED );
        }

        BlobInfo updatedBlobInfo = BlobInfo.newBuilder ( gcsBucketId, newFilename )
                .setContentType ( contentType )
                .build ();
        Blob updatedBlob = storage.create ( updatedBlobInfo, fileData );

        if ( updatedBlob == null ) {
            LOGGER.error (FailureMessages.GCS_UPDATE_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_UPDATE_FAILED );
        }

        LOGGER.info (InfoMessages.GCS_UPDATE_SUCCESS );
        return updatedBlob.getMediaLink ();
    }

    @Override
    public String updateFile ( MultipartFile file, String filename, String contentType ) {
        LOGGER.debug ( InfoMessages.FILE_UPDATE_STARTED );

        byte [] fileData;

        try {
            fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );
        } catch ( IOException e ) {
            LOGGER.debug ( FailureMessages.ERROR_READING_PROVIDED_FILE + Symbols.OPENING_CURLY_BRACE + Symbols.CLOSING_CURLY_BRACE, e.getMessage () );
            throw new RuntimeException ( FailureMessages.ERROR_READING_PROVIDED_FILE + e.getMessage () );
        }

        filename = gcsDirName + "/" + filename;

        final BlobId blobId = BlobId.of ( gcsBucketId, filename );
        final Blob blob = getStorage ().get ( blobId );

        if ( blob == null ) {
            LOGGER.error ( FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + filename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + filename );
        }

        boolean isDeleted = storage.delete ( blobId );
        if ( !isDeleted ) {
            LOGGER.error (FailureMessages.GCS_DELETION_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_DELETION_FAILED );
        }

        BlobInfo updatedBlobInfo = BlobInfo.newBuilder ( gcsBucketId, filename )
                .setContentType ( contentType )
                .build ();
        Blob updatedBlob = storage.create ( updatedBlobInfo, fileData );
        if ( updatedBlob == null ) {
            LOGGER.error (FailureMessages.GCS_UPDATE_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_UPDATE_FAILED );
        }

        LOGGER.info (InfoMessages.GCS_UPDATE_SUCCESS );
        return updatedBlob.getMediaLink ();
    }

    @Override
    public String deleteFile ( String filename ) {

        LOGGER.debug ( InfoMessages.FILE_DELETION_STARTED );

        filename = gcsDirName + Symbols.SLASH + filename;

        final BlobId blobId = BlobId.of ( gcsBucketId, filename );
        final Blob blob = getStorage ().get ( blobId );

        if ( blob == null ) {
            LOGGER.error ( FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + filename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + filename );
        }

        boolean isDeleted = storage.delete ( blobId );
        if ( !isDeleted ) {
            LOGGER.error (FailureMessages.GCS_DELETION_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_DELETION_FAILED );
        }
        return InfoMessages.GCS_DELETION_SUCCESS;
    }

    public List<String> getAllFiles ( ) {
        List<String> filenames = new ArrayList<>();
        Iterable<Blob> blobs = getStorage ().list ( gcsBucketId, Storage.BlobListOption.prefix ( gcsDirName ) ).getValues ();
        if ( blobs == null ) {
            LOGGER.error ( FailureMessages.GCS_FETCH_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_FETCH_FAILED );
        }

        LOGGER.info ( InfoMessages.GCS_FETCH_SUCCESS );
        blobs.forEach ( blob -> filenames.add ( blob.getName () ) );

        return filenames;
    }

    @Override
    public String uploadFile ( MultipartFile file, String filename, String contentType ) {

        LOGGER.debug ( InfoMessages.FILE_UPLOAD_STARTED );

        byte [] fileData;

        try {
            fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );
        } catch ( IOException e ) {
            LOGGER.debug ( FailureMessages.ERROR_READING_PROVIDED_FILE + Symbols.OPENING_CURLY_BRACE + Symbols.CLOSING_CURLY_BRACE, e.getMessage () );
            throw new RuntimeException ( FailureMessages.ERROR_READING_PROVIDED_FILE + e.getMessage () );
        }
        final Bucket bucket = getStorage ().get ( gcsBucketId, Storage.BucketGetOption.fields () );

        filename = gcsDirName + "/" + filename;
        final Blob blob = bucket.create ( filename, fileData, contentType );

        if ( blob == null ) {
            LOGGER.error ( FailureMessages.GCS_UPLOAD_FAILED );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.GCS_UPLOAD_FAILED );
        }

        LOGGER.info ( InfoMessages.GCS_UPLOAD_SUCCESS );
        return blob.getMediaLink ();
    }

    @Override
    public Blob downloadFile ( String filename ) {

        LOGGER.debug ( InfoMessages.FILE_DOWNLOAD_STARTED );

        BlobId blobId = BlobId.of ( gcsBucketId, gcsDirName + "/" + filename );
        Blob blob = getStorage().get(blobId);
        if ( blob == null ) {
            LOGGER.error ( FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + filename );
            throw new GoogleCloudStorageException ( HttpStatus.BAD_REQUEST, FailureMessages.NO_FILENAME_FOR_GIVEN_FILE + filename );
        }

        LOGGER.info ( InfoMessages.GCS_FETCH_SUCCESS );
        return blob;
    }

    private File convertFile ( MultipartFile file ) {
        File convertedFile = new File ( Objects.requireNonNull ( file.getOriginalFilename () ) );
        try ( FileOutputStream outputStream = new FileOutputStream ( convertedFile ) ){
            outputStream.write ( file.getBytes () );
            LOGGER.debug (InfoMessages.CONVERTING_MULTIPART_FILE, convertedFile.getName () );
        } catch ( IOException e ) {
            LOGGER.error ( FailureMessages.ERROR_WRITING_TO_FILE + e.getMessage() );
            throw new RuntimeException ( FailureMessages.ERROR_WRITING_TO_FILE + e.getMessage() );
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
            LOGGER.error ( FailureMessages.ERROR_READING_GCS_CONFIGURATION_FILE + e.getMessage() );
            throw new GoogleCloudStorageException ( HttpStatus.INTERNAL_SERVER_ERROR, FailureMessages.ERROR_READING_GCS_CONFIGURATION_FILE + e.getMessage() );
        }
        return storage;
    }

}
