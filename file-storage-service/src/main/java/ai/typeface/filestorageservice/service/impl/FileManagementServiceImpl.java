package ai.typeface.filestorageservice.service.impl;

import ai.typeface.filestorageservice.dtos.FileMetadataDTO;
import ai.typeface.filestorageservice.entity.FileMetadata;
import ai.typeface.filestorageservice.service.CloudStorageService;
import ai.typeface.filestorageservice.service.FileManagementService;
import ai.typeface.filestorageservice.service.FileMetadataService;
import ai.typeface.filestorageservice.util.CloudStorageUtil;
import ai.typeface.filestorageservice.util.FileMetadataUtil;
import com.google.cloud.storage.Blob;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

@Service
public class FileManagementServiceImpl implements FileManagementService {

    private static final Logger LOGGER = LoggerFactory.getLogger ( FileManagementServiceImpl.class );

    private final CloudStorageService cloudStorageService;

    private final FileMetadataService fileMetadataService;

    public FileManagementServiceImpl ( CloudStorageService cloudStorageService,
                                       FileMetadataService fileMetadataService ) {
        this.cloudStorageService = cloudStorageService;
        this.fileMetadataService = fileMetadataService;
    }

    @Override
    public FileMetadataDTO updateMetadata ( FileMetadataDTO metadataDTO, UUID fileIdentifier ) {

        /* TODO: first update the name of the file in the GCS, then, call the following update method */
        FileMetadataDTO existingMetadata = fileMetadataService.findByUniqueIdentifier ( fileIdentifier );
        if ( existingMetadata == null ) {
            LOGGER.error ( "No file found for the provided fileIdentifier: {}", fileIdentifier );
            return null;
        }

        /* TODO: Move this repeated logic to a method */
        String contentType;
        try {
            contentType = CloudStorageUtil.getContentType ( existingMetadata.getFilename () );
        } catch (IOException e) {
            /* TODO: Throw a Custom exception for GCS here */
            throw new RuntimeException(e);
        }

        List<String> errors = FileMetadataUtil.validateUpdateMetadataWithExistingMetadata ( metadataDTO, existingMetadata, fileIdentifier );
        if ( !errors.isEmpty () ) {
            errors.forEach ( LOGGER::error );
            return null;
        }

        String existingFilename = existingMetadata.getFilename();
        String newFilename = metadataDTO.getFilename();
        String fileURL = existingMetadata.getFileURL();
        if ( !existingFilename.equals ( newFilename ) ) {
            fileURL = cloudStorageService.updateFilename ( existingFilename, newFilename, contentType );
        }

        metadataDTO.setFileURL ( fileURL );

        return fileMetadataService.updateFileMetadata ( metadataDTO, fileIdentifier );
    }

    @Override
    public FileMetadataDTO updateFileData ( MultipartFile file, UUID fileIdentifier ) {
        String originalFileName = CloudStorageUtil.checkAndReturnOriginalFileName ( file );
        /* TODO: remove these checks and throw an exception instead rom the util method */
        if ( originalFileName.isEmpty() || originalFileName.isBlank () ) {
            return null;
        }

        FileMetadataDTO metadata = fileMetadataService.findByUniqueIdentifier ( fileIdentifier );
        if ( metadata == null ) {
            /* TODO: throw a custom exception */
            LOGGER.error ( "Invalid Unique Identifier: {}", fileIdentifier );
            return null;
        }

        String contentType;

        try {
            contentType = CloudStorageUtil.getContentType ( originalFileName );
        } catch (IOException e) {
            /* TODO: Throw a Custom exception for GCS here */
            throw new RuntimeException(e);
        }

        String fileURL = cloudStorageService.updateFile ( file, metadata.getFilename (), contentType );
        metadata.setFileURL ( fileURL );

        try {
            return fileMetadataService.updateFileData ( FileMetadataUtil.createUpdatedFileMetadataDTO ( metadata.getFilename (),
                                                                                                        fileIdentifier,
                                                                                                        file.getBytes (),
                                                                                                        fileURL,
                                                                                                        metadata.getCreatedAt() ) );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public FileMetadataDTO updateMetadata ( FileMetadataDTO metadata ) {
        return null;
    }

    @Override
    @Transactional
    public String delete ( UUID fileIdentifier ) {
        String filename = getFilenameFromMetadata ( fileIdentifier );
        LOGGER.info ( fileMetadataService.deleteByFilename ( filename ) );
        return cloudStorageService.deleteFile ( filename );
    }

    @Override
    public List<FileMetadataDTO> getAllFiles ( ) {
        return fileMetadataService.getAll ();
    }

    @Override
    public Blob download ( UUID fileIdentifier ) {
        return cloudStorageService.downloadFile ( getFilenameFromMetadata ( fileIdentifier ) );
    }

    /* TODO: This method should potentially be refactored */
    /* TODO: remove metadata, it doesn't seem to be required */
    @Override
    public UUID upload ( MultipartFile file ) {

        LOGGER.debug ( "Started file upload operation." );

        // Storing file in Google Cloud Storage
        String originalFileName = CloudStorageUtil.checkAndReturnOriginalFileName ( file );
        /* TODO: remove these checks and throw an exception instead rom the util method */
        if ( originalFileName.isEmpty() || originalFileName.isBlank () ) {
            return null;
        }

        final UUID uniqueIdentifier = UUID.nameUUIDFromBytes ( originalFileName.getBytes () );
        String fileURL;

        try {
            fileURL = cloudStorageService.uploadFile( file, originalFileName, CloudStorageUtil.getContentType ( originalFileName ) );

            if ( fileURL != null ) {
                /* TODO: update the log message to show the file name as well */
                LOGGER.info ( "File uploaded successfully, file name: {} and url: {}", originalFileName, fileURL );
            }
        } catch (IOException e) {
            /* TODO: Throw a Custom exception for GCS here */
            throw new RuntimeException(e);
        }

        // Storing metadata in MySQL Database
        FileMetadataDTO savedMetadata = null;
        try {
            savedMetadata = fileMetadataService.save ( FileMetadataUtil.createFileMetadataDTO ( originalFileName,
                                                                                                uniqueIdentifier,
                                                                                                file.getBytes(),
                                                                                                fileURL) );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return savedMetadata.getUniqueIdentifier ();
    }

    private String getFilenameFromMetadata ( UUID fileIdentifier ) {
        FileMetadataDTO metadata = fileMetadataService.findByUniqueIdentifier ( fileIdentifier );
        /* TODO: Throw a relevant exception here */
        if ( metadata == null ) {
            LOGGER.error ( "metadata corresponding to provided fileIdentifier: {} wasn't found.", fileIdentifier );
            return null;
        }
        return metadata.getFilename();
    }

}
