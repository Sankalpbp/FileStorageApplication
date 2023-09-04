package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.service.GoogleCloudStorageService;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.*;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class GoogleCloudStorageServiceImpl implements GoogleCloudStorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( GoogleCloudStorageServiceImpl.class );

    @Value ( "${gcs.config.file}" )
    private String gcsConfigurationsFile;

    @Value ( "${gcs.project.id}" )
    private String gcsProjectId;

    @Value ( "${gcs.bucket.id}" )
    private String gcsBucketId;

    @Value ( "${gcs.dir.name}" )
    private String gcsDirName;

    @Override
    public String uploadFile ( MultipartFile file, String fileName, String contentType ) {

        LOGGER.debug ( "Started file uploading process on Google Cloud Storage." );

        try {
            byte [ ] fileData = FileUtils.readFileToByteArray ( convertFile ( file ) );

            final Bucket bucket = getStorage ().get ( gcsBucketId, Storage.BucketGetOption.fields () );
            final RandomString id = new RandomString ( 6, ThreadLocalRandom.current () );

            /* TODO: Replace the String literals with constant variables defined in an interface */
            final Blob blob = bucket.create ( gcsDirName + "/" +
                                                fileName + "_" + id.nextString () +
                                                "." + fileName.split ("\\.")[1],
                                                fileData, contentType );

            if ( blob != null ) {
                /* TODO: Replace the messages with the constant variables defined in the interfaces */
                LOGGER.info ( "File uploaded successfully to Google Cloud Storage." );
                return blob.getMediaLink ();
            }

        } catch ( Exception e ) {
            LOGGER.debug ( "An error occurred while uploading data. Exception: " + e );
            /* TODO: Throw a custom exception */
        }

        return "";
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
        final InputStream inputStream = new ClassPathResource ( gcsConfigurationsFile ).getInputStream ();
        final StorageOptions options = StorageOptions.newBuilder()
                .setProjectId ( gcsProjectId )
                .setCredentials ( GoogleCredentials.fromStream ( inputStream ) )
                .build ();
        return options.getService ();
    }

}
